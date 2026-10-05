package com.utvt.ApiSpringCafeSoft.service;

import com.utvt.ApiSpringCafeSoft.dto.LiquidacionDTO;
import com.utvt.ApiSpringCafeSoft.dto.RegistrarLiquidacionDTO;
import com.utvt.ApiSpringCafeSoft.dto.ResumenLiquidacionDTO;
import com.utvt.ApiSpringCafeSoft.model.Carga;
import com.utvt.ApiSpringCafeSoft.model.EntregaPedido;
import com.utvt.ApiSpringCafeSoft.model.Inventario;
import com.utvt.ApiSpringCafeSoft.model.Liquidacion;
import com.utvt.ApiSpringCafeSoft.model.Usuario;
import com.utvt.ApiSpringCafeSoft.repository.CargaRepository;
import com.utvt.ApiSpringCafeSoft.repository.EntregaPedidoRepository;
import com.utvt.ApiSpringCafeSoft.repository.InventarioRepository;
import com.utvt.ApiSpringCafeSoft.repository.LiquidacionRepository;
import com.utvt.ApiSpringCafeSoft.repository.UsuarioRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

/**
 * HU-016: resumen y cierre de turno de repartidores.
 */
@Service
public class LiquidacionService {

    @Autowired private LiquidacionRepository liquidacionRepository;
    @Autowired private UsuarioRepository usuarioRepository;
    @Autowired private CargaRepository cargaRepository;
    @Autowired private EntregaPedidoRepository entregaPedidoRepository;
    @Autowired private InventarioRepository inventarioRepository;

    public ResumenLiquidacionDTO obtenerResumen(Long repartidorId, LocalDate fecha) {
        Usuario repartidor = obtenerRepartidor(repartidorId);
        LocalDate fechaConsulta = fecha != null ? fecha : LocalDate.now();

        LocalDateTime inicio = fechaConsulta.atStartOfDay();
        LocalDateTime fin = fechaConsulta.plusDays(1).atStartOfDay();

        List<Carga> cargas = cargaRepository
                .findByRepartidorIdAndFechaHoraBetweenOrderByFechaHoraAsc(
                        repartidorId,
                        inicio,
                        fin
                );

        List<EntregaPedido> entregas = entregaPedidoRepository
                .findByRepartidorIdAndFechaBetweenOrderByFechaAsc(
                        repartidorId,
                        inicio,
                        fin
                );

        double cargaInicial = cargas.stream()
                .mapToDouble(c -> nvl(c.getCantidad()))
                .sum();

        List<EntregaPedido> entregasCompletadas = entregas.stream()
                .filter(e -> "ENTREGADO".equalsIgnoreCase(e.getResultado()))
                .toList();

        double garrafonesEntregados = entregasCompletadas.stream()
                .mapToDouble(e -> nvl(e.getGarrafonesEntregados()))
                .sum();

        double envasesVacios = entregasCompletadas.stream()
                .mapToDouble(e -> nvl(e.getEnvasesVaciosRecibidos()))
                .sum();

        double efectivo = entregasCompletadas.stream()
                .filter(e -> "EFECTIVO".equalsIgnoreCase(e.getMetodoCobro()))
                .mapToDouble(e -> nvl(e.getMontoCobrado()))
                .sum();

        double transferencias = entregasCompletadas.stream()
                .filter(e -> "TRANSFERENCIA".equalsIgnoreCase(e.getMetodoCobro()))
                .mapToDouble(e -> nvl(e.getMontoCobrado()))
                .sum();

        ResumenLiquidacionDTO dto = new ResumenLiquidacionDTO();
        dto.setRepartidorId(repartidor.getId());
        dto.setRepartidorNombre(repartidor.getNombre());
        dto.setFecha(fechaConsulta);
        dto.setCargaInicial(cargaInicial);
        dto.setGarrafonesEntregados(garrafonesEntregados);
        dto.setGarrafonesPendientes(Math.max(0.0, cargaInicial - garrafonesEntregados));
        dto.setEnvasesVaciosRecibidos(envasesVacios);
        dto.setTotalEfectivo(efectivo);
        dto.setTotalTransferencias(transferencias);
        dto.setTotalCobrado(efectivo + transferencias);
        dto.setEntregasRealizadas(entregasCompletadas.size());
        return dto;
    }

    @Transactional
    public LiquidacionDTO registrarLiquidacion(RegistrarLiquidacionDTO request) {
        LocalDate fecha = request.getFecha() != null
                ? request.getFecha()
                : LocalDate.now();

        Usuario repartidor = obtenerRepartidor(request.getRepartidorId());

        if (liquidacionRepository
                .findByRepartidorIdAndFechaOperacion(repartidor.getId(), fecha)
                .isPresent()) {
            throw new RuntimeException("Ya existe una liquidación registrada para este repartidor en la fecha seleccionada");
        }

        ResumenLiquidacionDTO resumen = obtenerResumen(repartidor.getId(), fecha);

        double devueltos = nvl(request.getGarrafonesNoVendidosDevueltos());
        double efectivoEntregado = nvl(request.getEfectivoEntregado());

        if (devueltos - resumen.getGarrafonesPendientes() > 0.0001) {
            throw new RuntimeException(
                    "Los garrafones devueltos no pueden exceder los pendientes. Pendientes: "
                            + resumen.getGarrafonesPendientes()
            );
        }

        double diferenciaGarrafones =
                resumen.getCargaInicial()
                        - resumen.getGarrafonesEntregados()
                        - devueltos;

        double diferenciaEfectivo =
                resumen.getTotalEfectivo()
                        - efectivoEntregado;

        boolean conforme =
                Math.abs(diferenciaGarrafones) < 0.0001
                        && Math.abs(diferenciaEfectivo) < 0.01;

        Liquidacion liquidacion = new Liquidacion();
        liquidacion.setRepartidor(repartidor);
        liquidacion.setFechaOperacion(fecha);
        liquidacion.setFechaCierre(LocalDateTime.now());
        liquidacion.setCargaInicial(resumen.getCargaInicial());
        liquidacion.setGarrafonesEntregados(resumen.getGarrafonesEntregados());
        liquidacion.setGarrafonesNoVendidosDevueltos(devueltos);
        liquidacion.setTotalEfectivo(resumen.getTotalEfectivo());
        liquidacion.setTotalTransferencias(resumen.getTotalTransferencias());
        liquidacion.setEfectivoEntregado(efectivoEntregado);
        liquidacion.setDiferenciaGarrafones(diferenciaGarrafones);
        liquidacion.setDiferenciaEfectivo(diferenciaEfectivo);
        liquidacion.setEstado(conforme ? "CONFORME" : "DIFERENCIA REGISTRADA");
        liquidacion.setObservaciones(request.getObservaciones());

        regresarGarrafonesAPlanta(repartidor.getId(), fecha, devueltos);

        return convertirDTO(liquidacionRepository.save(liquidacion));
    }

    /**
     * Los garrafones que regresan sin vender vuelven al inventario de planta.
     * Las cargas del turno quedan cerradas como LIQUIDADA.
     */
    private void regresarGarrafonesAPlanta(Long repartidorId, LocalDate fecha, double devueltos) {
        LocalDateTime inicio = fecha.atStartOfDay();
        LocalDateTime fin = fecha.plusDays(1).atStartOfDay();

        List<Carga> cargas = cargaRepository
                .findByRepartidorIdAndFechaHoraBetweenOrderByFechaHoraAsc(
                        repartidorId,
                        inicio,
                        fin
                );

        double porRegresar = devueltos;

        for (Carga carga : cargas) {
            double disponible = carga.getCantidadDisponible() != null
                    ? carga.getCantidadDisponible()
                    : nvl(carga.getCantidad());

            double regresar = Math.min(disponible, Math.max(0.0, porRegresar));

            if (regresar > 0.0001 && carga.getInventario() != null) {
                Inventario inventario = carga.getInventario();
                inventario.setCantidad(nvl(inventario.getCantidad()) + regresar);
                inventarioRepository.save(inventario);
                porRegresar -= regresar;
                carga.setCantidadDisponible(Math.max(0.0, disponible - regresar));
            }

            carga.setEstado("LIQUIDADA");
            cargaRepository.save(carga);
        }
    }

    private Usuario obtenerRepartidor(Long id) {
        Usuario repartidor = usuarioRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Repartidor no encontrado con ID: " + id));

        if (repartidor.getUserTipo() == null || repartidor.getUserTipo() != 4) {
            throw new RuntimeException("El usuario seleccionado no tiene el rol de Repartidor");
        }

        return repartidor;
    }

    private double nvl(Double value) {
        return value != null ? value : 0.0;
    }

    private LiquidacionDTO convertirDTO(Liquidacion l) {
        LiquidacionDTO dto = new LiquidacionDTO();
        dto.setId(l.getId());
        dto.setRepartidorId(l.getRepartidor() != null ? l.getRepartidor().getId() : null);
        dto.setRepartidorNombre(l.getRepartidor() != null ? l.getRepartidor().getNombre() : null);
        dto.setFechaOperacion(l.getFechaOperacion());
        dto.setFechaCierre(l.getFechaCierre());
        dto.setCargaInicial(l.getCargaInicial());
        dto.setGarrafonesEntregados(l.getGarrafonesEntregados());
        dto.setGarrafonesNoVendidosDevueltos(l.getGarrafonesNoVendidosDevueltos());
        dto.setTotalEfectivo(l.getTotalEfectivo());
        dto.setTotalTransferencias(l.getTotalTransferencias());
        dto.setEfectivoEntregado(l.getEfectivoEntregado());
        dto.setDiferenciaGarrafones(l.getDiferenciaGarrafones());
        dto.setDiferenciaEfectivo(l.getDiferenciaEfectivo());
        dto.setEstado(l.getEstado());
        dto.setObservaciones(l.getObservaciones());
        return dto;
    }
}
