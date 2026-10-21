package com.utvt.ApiSpringCafeSoft.service;
/**
     * 
     * (HU-006): agrega confirmacion de carga por repartidor
     * 
     */
import com.utvt.ApiSpringCafeSoft.dto.CargaDTO;
import com.utvt.ApiSpringCafeSoft.dto.ItemGarrafonDTO;
import com.utvt.ApiSpringCafeSoft.model.Carga;
import com.utvt.ApiSpringCafeSoft.model.Inventario;
import com.utvt.ApiSpringCafeSoft.model.Usuario;
import com.utvt.ApiSpringCafeSoft.repository.CargaRepository;
import com.utvt.ApiSpringCafeSoft.repository.InventarioRepository;
import com.utvt.ApiSpringCafeSoft.repository.UsuarioRepository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class CargaService {

    @Autowired
    private CargaRepository cargaRepository;

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private InventarioRepository inventarioRepository;

    /**
     * HU-005
     * Registra la carga inicial de garrafones para un repartidor.
     */
    @Transactional
    public CargaDTO crearCarga(CargaDTO dto) {

        if (dto.getCantidad() == null || dto.getCantidad() <= 0) {
            throw new RuntimeException("La cantidad debe ser mayor a cero");
        }

        /* La carga queda SIN ASIGNAR hasta que se active una ruta */
        Usuario repartidor = null;
        if (dto.getRepartidorId() != null) {
            repartidor = usuarioRepository.findById(dto.getRepartidorId())
                    .orElseThrow(() ->
                            new RuntimeException(
                                    "No se encontró el usuario con ID: "
                                            + dto.getRepartidorId()
                            )
                    );
            if (repartidor.getUserTipo() == null || repartidor.getUserTipo() != 4) {
                throw new RuntimeException("El usuario seleccionado no tiene el rol de Repartidor");
            }
        }

        Inventario inventario = inventarioRepository.findById(dto.getInventarioId())
                .orElseThrow(() ->
                        new RuntimeException(
                                "No se encontró el inventario con ID: "
                                        + dto.getInventarioId()
                        )
                );

        // Validar stock disponible
        if (inventario.getCantidad() == null ||
                inventario.getCantidad() < dto.getCantidad()) {

            throw new RuntimeException(
                    "Stock insuficiente. Disponible: "
                            + inventario.getCantidad()
                            + ", solicitado: "
                            + dto.getCantidad()
            );
        }

        /*
         * Se descuenta la cantidad del inventario porque los garrafones
         * dejan de estar disponibles en planta y pasan a estar bajo
         * responsabilidad del repartidor.
         */
        inventario.setCantidad(
                inventario.getCantidad() - dto.getCantidad()
        );

        inventarioRepository.save(inventario);

        Carga carga = new Carga();

        carga.setRepartidor(repartidor);
        carga.setInventario(inventario);
        carga.setCantidad(dto.getCantidad());
        carga.setCantidadDisponible(dto.getCantidad());
        carga.setFechaHora(LocalDateTime.now());

        // La carga queda sin asignar hasta activar una ruta
        carga.setEstado(repartidor != null ? "PENDIENTE" : "SIN ASIGNAR");

        Carga guardada = cargaRepository.save(carga);

        return convertirDTO(guardada);
    }

    /**
     * HU-015
     * Registra varias cargas (varios tipos de garrafón)
     * para un repartidor en una sola transacción.
     *
     * Permite que un repartidor inicie su ruta con
     * cargas de distintos garrafones, por ejemplo
     * 20 Bonafont y 5 Ciel.
     */
    @Transactional
    public List<CargaDTO> crearCargasMultiples(
            Long repartidorId,
            List<ItemGarrafonDTO> items) {

        if (repartidorId == null) {
            throw new RuntimeException("El repartidor es obligatorio");
        }

        Usuario repartidor = usuarioRepository.findById(repartidorId)
                .orElseThrow(() ->
                        new RuntimeException(
                                "No se encontró el usuario con ID: "
                                        + repartidorId
                        )
                );

        if (repartidor.getUserTipo() == null || repartidor.getUserTipo() != 4) {
            throw new RuntimeException("El usuario seleccionado no tiene el rol de Repartidor");
        }

        if (items == null || items.isEmpty()) {
            throw new RuntimeException(
                    "Debe seleccionar al menos un garrafón"
            );
        }

        List<CargaDTO> creadas = new ArrayList<>();

        for (ItemGarrafonDTO item : items) {

            if (item.getInventarioId() == null
                    || item.getCantidad() == null
                    || item.getCantidad() <= 0) {

                throw new RuntimeException(
                        "Cada garrafón debe tener un inventario y una cantidad válida"
                );
            }

            CargaDTO dto = new CargaDTO();
            dto.setRepartidorId(repartidorId);
            dto.setInventarioId(item.getInventarioId());
            dto.setCantidad(item.getCantidad());

            creadas.add(crearCarga(dto));
        }

        return creadas;
    }

    /**
     * Obtener todas las cargas.
     */
    public List<CargaDTO> obtenerTodas() {

        return cargaRepository.findAll()
                .stream()
                .map(this::convertirDTO)
                .collect(Collectors.toList());
    }

    /**
     * Obtener una carga por ID.
     */
    public CargaDTO obtenerPorId(Long id) {

        Carga carga = cargaRepository.findById(id)
                .orElseThrow(() ->
                        new RuntimeException(
                                "Carga no encontrada con ID: " + id
                        )
                );

        return convertirDTO(carga);
    }

    /**
     * HU-006
     * Obtener las cargas asignadas a un repartidor.
     */
    public List<CargaDTO> obtenerCargasPorRepartidor(Long repartidorId) {

        Usuario repartidor = usuarioRepository.findById(repartidorId)
                .orElseThrow(() ->
                        new RuntimeException(
                                "Repartidor no encontrado con ID: "
                                        + repartidorId
                        )
                );

        if (repartidor.getUserTipo() == null ||
                repartidor.getUserTipo() != 4) {

            throw new RuntimeException(
                    "El usuario indicado no es un Repartidor"
            );
        }

        return cargaRepository
                .findByRepartidorIdOrderByFechaHoraDesc(repartidorId)
                .stream()
                .map(this::convertirDTO)
                .collect(Collectors.toList());
    }

    /**
     * HU-006
     * Obtener únicamente las cargas pendientes del repartidor.
     */
    public List<CargaDTO> obtenerCargasPendientes(Long repartidorId) {

        return cargaRepository
                .findByRepartidorIdAndEstadoOrderByFechaHoraDesc(
                        repartidorId,
                        "PENDIENTE"
                )
                .stream()
                .map(this::convertirDTO)
                .collect(Collectors.toList());
    }

    /**
     * HU-006
     * El repartidor acepta la carga.
     */
    @Transactional
    public CargaDTO aceptarCarga(Long id) {

        Carga carga = cargaRepository.findWithLockById(id)
                .orElseThrow(() ->
                        new RuntimeException(
                                "Carga no encontrada con ID: " + id
                        )
                );

        if (!"PENDIENTE".equalsIgnoreCase(carga.getEstado())) {

            // Idempotente: si ya está en tránsito, devolverla tal cual
            if ("CARGA EN TRÁNSITO".equalsIgnoreCase(carga.getEstado())) {
                if (carga.getCantidadDisponible() == null) {
                    carga.setCantidadDisponible(carga.getCantidad());
                }
                return convertirDTO(carga);
            }

            throw new RuntimeException(
                    "La carga no puede aceptarse porque su estado actual es: "
                            + carga.getEstado()
            );
        }

        if (carga.getRepartidor() == null ||
                carga.getRepartidor().getUserTipo() == null ||
                carga.getRepartidor().getUserTipo() != 4) {

            throw new RuntimeException(
                    "La carga no pertenece a un repartidor válido"
            );
        }

        if (carga.getCantidadDisponible() == null) {
            carga.setCantidadDisponible(carga.getCantidad());
        }

        carga.setEstado("CARGA EN TRÁNSITO");

        return convertirDTO(cargaRepository.save(carga));
    }

    /**
     * Convertir entidad a DTO.
     */
    private CargaDTO convertirDTO(Carga carga) {

        String repartidorNombre = null;
        String inventarioNombre = null;
        String tipoGarrafon = null;
        String unidadMedida = null;

        if (carga.getRepartidor() != null) {
            repartidorNombre = carga.getRepartidor().getNombre();
        }

        if (carga.getInventario() != null) {

            inventarioNombre = carga.getInventario().getNombre();
            tipoGarrafon = carga.getInventario().getTipo();
            unidadMedida = carga.getInventario().getUnidadMedida();
        }

        return new CargaDTO(
                carga.getId(),
                carga.getRepartidor() != null
                        ? carga.getRepartidor().getId()
                        : null,
                carga.getInventario() != null
                        ? carga.getInventario().getId()
                        : null,
                carga.getCantidad(),
                carga.getCantidadDisponible() != null
                        ? carga.getCantidadDisponible()
                        : carga.getCantidad(),
                carga.getFechaHora(),
                carga.getEstado(),
                repartidorNombre,
                inventarioNombre,
                tipoGarrafon,
                unidadMedida
        );
    }
}  