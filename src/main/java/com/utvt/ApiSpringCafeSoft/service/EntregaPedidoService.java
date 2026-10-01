package com.utvt.ApiSpringCafeSoft.service;

import com.utvt.ApiSpringCafeSoft.dto.ConfirmarEntregaDTO;
import com.utvt.ApiSpringCafeSoft.dto.EntregaPedidoDTO;
import com.utvt.ApiSpringCafeSoft.dto.ReportarIncidenciaEntregaDTO;
import com.utvt.ApiSpringCafeSoft.model.Carga;
import com.utvt.ApiSpringCafeSoft.model.EntregaPedido;
import com.utvt.ApiSpringCafeSoft.model.Usuario;
import com.utvt.ApiSpringCafeSoft.model.Venta;
import com.utvt.ApiSpringCafeSoft.repository.CargaRepository;
import com.utvt.ApiSpringCafeSoft.repository.EntregaPedidoRepository;
import com.utvt.ApiSpringCafeSoft.repository.UsuarioRepository;
import com.utvt.ApiSpringCafeSoft.repository.VentaRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

/**
 * HU-015: lógica para confirmar pedidos entregados por un repartidor.
 */
@Service
public class EntregaPedidoService {

    private static final String ESTADO_CARGA_ACTIVA = "CARGA EN TRÁNSITO";
    private static final String RESULTADO_ENTREGADO = "ENTREGADO";

    @Autowired private EntregaPedidoRepository entregaPedidoRepository;
    @Autowired private VentaRepository ventaRepository;
    @Autowired private UsuarioRepository usuarioRepository;
    @Autowired private CargaRepository cargaRepository;

    @Transactional
    public EntregaPedidoDTO confirmarEntrega(Long ventaId, ConfirmarEntregaDTO dto) {
        Venta venta = obtenerVentaPendiente(ventaId);
        Usuario repartidor = obtenerRepartidor(dto.getRepartidorId());

        if (entregaPedidoRepository.existsByVentaIdAndResultado(ventaId, RESULTADO_ENTREGADO)) {
            throw new RuntimeException("El pedido ya cuenta con una entrega confirmada");
        }

        descontarCarga(
                repartidor.getId(),
                dto.getGarrafonesEntregados()
        );

        EntregaPedido entrega = new EntregaPedido();
        entrega.setVenta(venta);
        entrega.setRepartidor(repartidor);
        entrega.setGarrafonesEntregados(dto.getGarrafonesEntregados());
        entrega.setEnvasesVaciosRecibidos(dto.getEnvasesVaciosRecibidos());
        entrega.setMetodoCobro(dto.getMetodoCobro().toUpperCase());
        entrega.setMontoCobrado(dto.getMontoCobrado());
        entrega.setResultado(RESULTADO_ENTREGADO);
        entrega.setFecha(LocalDateTime.now());
        entrega.setObservaciones(dto.getObservaciones());

        venta.setEstadoPedido(RESULTADO_ENTREGADO);
        ventaRepository.save(venta);

        return convertirDTO(entregaPedidoRepository.save(entrega));
    }

    @Transactional
    public EntregaPedidoDTO reportarIncidencia(Long ventaId, ReportarIncidenciaEntregaDTO dto) {
        Venta venta = obtenerVentaPendiente(ventaId);
        Usuario repartidor = obtenerRepartidor(dto.getRepartidorId());

        EntregaPedido entrega = new EntregaPedido();
        entrega.setVenta(venta);
        entrega.setRepartidor(repartidor);
        entrega.setGarrafonesEntregados(0.0);
        entrega.setEnvasesVaciosRecibidos(0.0);
        entrega.setMontoCobrado(0.0);
        entrega.setMetodoCobro(null);
        entrega.setResultado(dto.getMotivo().toUpperCase());
        entrega.setFecha(LocalDateTime.now());
        entrega.setObservaciones(dto.getObservaciones());

        // La incidencia queda registrada y el pedido deja de aparecer como pendiente.
        venta.setEstadoPedido(dto.getMotivo().toUpperCase());
        ventaRepository.save(venta);

        return convertirDTO(entregaPedidoRepository.save(entrega));
    }

    private Venta obtenerVentaPendiente(Long ventaId) {
        Venta venta = ventaRepository.findById(ventaId)
                .orElseThrow(() -> new RuntimeException("Pedido no encontrado con ID: " + ventaId));

        if (!"PENDIENTE".equalsIgnoreCase(venta.getEstadoPedido())) {
            throw new RuntimeException(
                    "El pedido no puede confirmarse porque su estado actual es: "
                            + venta.getEstadoPedido()
            );
        }

        return venta;
    }

    private Usuario obtenerRepartidor(Long repartidorId) {
        Usuario repartidor = usuarioRepository.findById(repartidorId)
                .orElseThrow(() -> new RuntimeException("Repartidor no encontrado con ID: " + repartidorId));

        if (repartidor.getUserTipo() == null || repartidor.getUserTipo() != 4) {
            throw new RuntimeException("El usuario indicado no tiene el rol de Repartidor");
        }

        if (Boolean.FALSE.equals(repartidor.getActivo())) {
            throw new RuntimeException("El repartidor está inactivo");
        }

        return repartidor;
    }

    /**
     * Descuenta los garrafones entregados de las cargas activas del repartidor.
     * Si hay más de una carga activa, consume primero la más antigua.
     */
    private void descontarCarga(Long repartidorId, Double cantidadEntregada) {
        List<Carga> cargas = cargaRepository
                .findByRepartidorIdAndEstadoOrderByFechaHoraAsc(
                        repartidorId,
                        ESTADO_CARGA_ACTIVA
                );

        double disponibleTotal = cargas.stream()
                .mapToDouble(this::cantidadDisponible)
                .sum();

        if (disponibleTotal + 0.0001 < cantidadEntregada) {
            throw new RuntimeException(
                    "Carga insuficiente. Disponible: " + disponibleTotal
                            + ", solicitado para entregar: " + cantidadEntregada
            );
        }

        double restante = cantidadEntregada;

        for (Carga carga : cargas) {
            if (restante <= 0.0001) break;

            double disponible = cantidadDisponible(carga);
            double descontar = Math.min(disponible, restante);
            double nuevoDisponible = disponible - descontar;

            carga.setCantidadDisponible(Math.max(0.0, nuevoDisponible));

            if (nuevoDisponible <= 0.0001) {
                carga.setEstado("AGOTADA");
            }

            cargaRepository.save(carga);
            restante -= descontar;
        }
    }

    private double cantidadDisponible(Carga carga) {
        return carga.getCantidadDisponible() != null
                ? carga.getCantidadDisponible()
                : (carga.getCantidad() != null ? carga.getCantidad() : 0.0);
    }

    private EntregaPedidoDTO convertirDTO(EntregaPedido entrega) {
        EntregaPedidoDTO dto = new EntregaPedidoDTO();
        dto.setId(entrega.getId());
        dto.setVentaId(entrega.getVenta() != null ? entrega.getVenta().getId() : null);
        dto.setFolio(entrega.getVenta() != null ? entrega.getVenta().getFolio() : null);
        dto.setRepartidorId(entrega.getRepartidor() != null ? entrega.getRepartidor().getId() : null);
        dto.setRepartidorNombre(entrega.getRepartidor() != null ? entrega.getRepartidor().getNombre() : null);
        dto.setGarrafonesEntregados(entrega.getGarrafonesEntregados());
        dto.setEnvasesVaciosRecibidos(entrega.getEnvasesVaciosRecibidos());
        dto.setMetodoCobro(entrega.getMetodoCobro());
        dto.setMontoCobrado(entrega.getMontoCobrado());
        dto.setResultado(entrega.getResultado());
        dto.setFecha(entrega.getFecha());
        dto.setObservaciones(entrega.getObservaciones());
        return dto;
    }
}
