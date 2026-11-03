package com.utvt.ApiSpringCafeSoft.service;

import com.utvt.ApiSpringCafeSoft.dto.PromocionDTO;
import com.utvt.ApiSpringCafeSoft.model.Promocion;
import com.utvt.ApiSpringCafeSoft.model.SegmentoCliente;
import com.utvt.ApiSpringCafeSoft.repository.PromocionRepository;
import com.utvt.ApiSpringCafeSoft.repository.VentaRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Comparator;
import java.util.List;

@Service
public class PromocionService {

    private final PromocionRepository promocionRepository;
    private final VentaRepository ventaRepository;
    private final SegmentacionClienteService segmentacionClienteService;

    public PromocionService(
            PromocionRepository promocionRepository,
            VentaRepository ventaRepository,
            SegmentacionClienteService segmentacionClienteService) {
        this.promocionRepository = promocionRepository;
        this.ventaRepository = ventaRepository;
        this.segmentacionClienteService = segmentacionClienteService;
    }

    @Transactional(readOnly = true)
    public List<PromocionDTO> listar() {
        LocalDateTime ahora = ahora();
        return promocionRepository.findAll().stream()
                .sorted(Comparator.comparing(Promocion::getCreatedAt,
                        Comparator.nullsLast(Comparator.reverseOrder())))
                .map(promocion -> toDto(promocion, ahora))
                .toList();
    }

    @Transactional
    public PromocionDTO crear(PromocionDTO solicitud) {
        validarFechas(solicitud.getFechaInicio(), solicitud.getFechaFin());
        Promocion promocion = new Promocion();
        aplicarCambios(promocion, solicitud);
        promocion.setActiva(true);
        return toDto(promocionRepository.save(promocion), ahora());
    }

    @Transactional
    public PromocionDTO actualizar(Long id, PromocionDTO solicitud) {
        validarFechas(solicitud.getFechaInicio(), solicitud.getFechaFin());
        Promocion promocion = buscarPorId(id);
        aplicarCambios(promocion, solicitud);
        return toDto(promocionRepository.save(promocion), ahora());
    }

    @Transactional
    public void desactivar(Long id) {
        Promocion promocion = buscarPorId(id);
        promocion.setActiva(false);
        promocionRepository.save(promocion);
    }

    @Transactional
    public void eliminar(Long id) {
        Promocion promocion = buscarPorId(id);
        if (ventaRepository.existsByPromocion_Id(id)) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "La promoción ya fue aplicada a ventas; desactívala en lugar de eliminarla.");
        }
        promocionRepository.delete(promocion);
    }

    @Transactional(readOnly = true)
    public List<PromocionDTO> listarAplicables(Long clienteId, Integer cantidadGarrafones) {
        if (cantidadGarrafones == null || cantidadGarrafones < 0) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST, "La cantidad no puede ser negativa.");
        }
        SegmentoCliente segmento = segmentacionClienteService.obtenerSegmento(clienteId);
        LocalDateTime ahora = ahora();
        return promocionRepository.findAll().stream()
                .filter(promocion -> esVigente(promocion, ahora))
                .filter(promocion -> promocion.getMinGarrafones() <= cantidadGarrafones)
                .filter(promocion -> aplicaASegmento(promocion, segmento))
                .sorted(Comparator.comparing(Promocion::getNombre,
                        String.CASE_INSENSITIVE_ORDER))
                .map(promocion -> toDto(promocion, ahora))
                .toList();
    }

    @Transactional(readOnly = true)
    public BigDecimal calcularDescuento(
            Long promocionId,
            Long clienteId,
            int cantidadGarrafones,
            BigDecimal subtotal) {
        if (subtotal == null || subtotal.signum() < 0) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST, "El subtotal no puede ser negativo.");
        }
        if (cantidadGarrafones < 0) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST, "La cantidad no puede ser negativa.");
        }
        Promocion promocion = buscarPorId(promocionId);
        SegmentoCliente segmento = segmentacionClienteService.obtenerSegmento(clienteId);
        validarAplicacion(promocion, segmento, cantidadGarrafones, ahora());

        return subtotal
                .multiply(promocion.getDescuentoPorcentaje())
                .divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP)
                .min(subtotal);
    }

    @Transactional(readOnly = true)
    public Promocion obtenerPorId(Long id) {
        return buscarPorId(id);
    }

    private Promocion buscarPorId(Long id) {
        return promocionRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Promoción no encontrada."));
    }

    private void validarAplicacion(
            Promocion promocion,
            SegmentoCliente segmento,
            int cantidadGarrafones,
            LocalDateTime ahora) {
        if (!esVigente(promocion, ahora)) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT, "La promoción está inactiva o fuera de vigencia.");
        }
        if (promocion.getMinGarrafones() > cantidadGarrafones) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "La compra no cumple la cantidad mínima de la promoción.");
        }
        if (!aplicaASegmento(promocion, segmento)) {
            throw new ResponseStatusException(
                    HttpStatus.FORBIDDEN,
                    "La promoción no corresponde al segmento actual del cliente.");
        }
    }

    private boolean esVigente(Promocion promocion, LocalDateTime ahora) {
        return Boolean.TRUE.equals(promocion.getActiva())
                && promocion.getFechaInicio() != null
                && promocion.getFechaFin() != null
                && !ahora.isBefore(promocion.getFechaInicio())
                && !ahora.isAfter(promocion.getFechaFin());
    }

    private boolean aplicaASegmento(Promocion promocion, SegmentoCliente segmento) {
        return promocion.getSegmentoObjetivo() == SegmentoCliente.TODOS
                || promocion.getSegmentoObjetivo() == segmento;
    }

    private void validarFechas(LocalDateTime inicio, LocalDateTime fin) {
        if (inicio == null || fin == null || fin.isBefore(inicio)) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "La fecha de fin debe ser igual o posterior a la fecha de inicio.");
        }
    }

    private void aplicarCambios(Promocion promocion, PromocionDTO solicitud) {
        promocion.setNombre(solicitud.getNombre().trim());
        promocion.setDescripcion(solicitud.getDescripcion());
        promocion.setDescuentoPorcentaje(solicitud.getDescuentoPorcentaje());
        promocion.setFechaInicio(solicitud.getFechaInicio());
        promocion.setFechaFin(solicitud.getFechaFin());
        promocion.setMinGarrafones(solicitud.getMinGarrafones());
        promocion.setAcumulaPuntos(solicitud.getAcumulaPuntos());
        promocion.setSegmentoObjetivo(solicitud.getSegmentoObjetivo());
    }

    private PromocionDTO toDto(Promocion promocion, LocalDateTime ahora) {
        PromocionDTO dto = new PromocionDTO();
        dto.setId(promocion.getId());
        dto.setNombre(promocion.getNombre());
        dto.setDescripcion(promocion.getDescripcion());
        dto.setDescuentoPorcentaje(promocion.getDescuentoPorcentaje());
        dto.setFechaInicio(promocion.getFechaInicio());
        dto.setFechaFin(promocion.getFechaFin());
        dto.setMinGarrafones(promocion.getMinGarrafones());
        dto.setAcumulaPuntos(promocion.getAcumulaPuntos());
        dto.setSegmentoObjetivo(promocion.getSegmentoObjetivo());
        dto.setActiva(promocion.getActiva());
        dto.setVigente(esVigente(promocion, ahora));
        dto.setSucursalNombre("Todas las sucursales");
        return dto;
    }

    private LocalDateTime ahora() {
        return LocalDateTime.now(ZoneId.systemDefault());
    }
}
