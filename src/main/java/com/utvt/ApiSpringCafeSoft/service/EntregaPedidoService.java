package com.utvt.ApiSpringCafeSoft.service;

import java.time.LocalDate;
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

import com.utvt.ApiSpringCafeSoft.dto.RegistrarVentaRutaDTO;
import com.utvt.ApiSpringCafeSoft.model.Cliente;
import com.utvt.ApiSpringCafeSoft.model.Producto;
import com.utvt.ApiSpringCafeSoft.model.VentaDetalle;
import com.utvt.ApiSpringCafeSoft.repository.ClienteRepository;

import java.util.UUID;

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
    @Autowired
private ClienteRepository clienteRepository;

@Transactional
public EntregaPedidoDTO registrarVentaRuta(
        RegistrarVentaRutaDTO dto) {

    // ============================================
    // CLIENTE
    // ============================================

    Cliente cliente = clienteRepository
            .findById(dto.getClienteId())
            .orElseThrow(() ->
                    new RuntimeException(
                            "Cliente no encontrado con ID: "
                                    + dto.getClienteId()
                    )
            );

    if (Boolean.FALSE.equals(cliente.getActivo())) {
        throw new RuntimeException(
                "El cliente se encuentra inactivo"
        );
    }

    // ============================================
    // REPARTIDOR
    // ============================================

    Usuario repartidor =
            obtenerRepartidor(dto.getRepartidorId());

    // ============================================
    // VALIDAR RUTA
    // ============================================

    if (cliente.getRuta() == null) {
        throw new RuntimeException(
                "El cliente no tiene una ruta asignada"
        );
    }

    if (!Boolean.TRUE.equals(
            cliente.getRuta().getActiva())) {

        throw new RuntimeException(
                "La ruta del cliente no está activa"
        );
    }

    if (cliente.getRuta().getRepartidor() == null) {
        throw new RuntimeException(
                "La ruta no tiene repartidor asignado"
        );
    }

    if (!cliente.getRuta()
            .getRepartidor()
            .getId()
            .equals(repartidor.getId())) {

        throw new RuntimeException(
                "El cliente no pertenece a la ruta de este repartidor"
        );
    }

    // ============================================
    // PRODUCTO / GARRAFÓN
    // ============================================

    Producto producto =
            cliente.getGarrafonPreferencia();

    if (producto == null) {
        throw new RuntimeException(
                "El cliente no tiene un garrafón de preferencia configurado"
        );
    }

    // ============================================
    // CANTIDAD
    // ============================================

    double cantidad =
            dto.getGarrafonesEntregados();

    // Los garrafones deben manejarse como unidades completas.
    if (Math.abs(cantidad - Math.rint(cantidad)) > 0.0001) {
        throw new RuntimeException(
                "La cantidad de garrafones debe ser un número entero"
        );
    }

    int cantidadEntera =
            (int) Math.round(cantidad);

    // ============================================
    // PRECIO Y TOTAL
    // ============================================

    double precioUnitario =
            dto.getPrecioUnitario();

    double total =
            cantidad * precioUnitario;

    // ============================================
    // CREAR VENTA
    // ============================================

    Venta venta = new Venta();

    venta.setFolio(
            "R-" +
            UUID.randomUUID()
                    .toString()
                    .substring(0, 12)
                    .toUpperCase()
    );

    venta.setFecha(LocalDateTime.now());
    venta.setCreatedAt(LocalDateTime.now());

    venta.setUsuario(repartidor);
   venta.setNombreCliente(cliente.getNombre());
venta.setClienteRutaId(cliente.getId());

    venta.setSubtotal(total);
    venta.setImpuestos(0.0);
    venta.setDescuento(0.0);
    venta.setTotal(total);

    String metodoPago =
            dto.getMetodoCobro().toLowerCase();

    venta.setMetodoPago(metodoPago);

    if ("efectivo".equals(metodoPago)) {
        venta.setMontoEfectivo(total);
        venta.setCambio(0.0);
    }

    venta.setEstadoPedido("PENDIENTE");
    venta.setObservaciones(
            dto.getObservaciones()
    );

    // La venta pertenece a la misma sucursal
    // que el repartidor.
    if (repartidor.getSucursal() == null) {
        throw new RuntimeException(
                "El repartidor no tiene una sucursal asignada"
        );
    }

    venta.setSucursal(
            repartidor.getSucursal()
    );

    // ============================================
    // DETALLE DE VENTA
    // ============================================

    VentaDetalle detalle =
            new VentaDetalle();

    detalle.setProducto(producto);
    detalle.setCantidad(cantidadEntera);
    detalle.setPrecioUnitario(
            precioUnitario
    );
    detalle.setSubtotal(total);

    venta.addDetalle(detalle);

    Venta ventaGuardada =
            ventaRepository.save(venta);

    // ============================================
    // CONFIRMAR ENTREGA
    // ============================================

    ConfirmarEntregaDTO entregaDTO =
            new ConfirmarEntregaDTO();

    entregaDTO.setRepartidorId(
            repartidor.getId()
    );

    entregaDTO.setGarrafonesEntregados(
            cantidad
    );

    entregaDTO.setEnvasesVaciosRecibidos(
            dto.getEnvasesVaciosRecibidos()
    );

    entregaDTO.setMetodoCobro(
            dto.getMetodoCobro().toUpperCase()
    );

    entregaDTO.setMontoCobrado(
            total
    );

    entregaDTO.setObservaciones(
            dto.getObservaciones()
    );

    /*
     * Reutilizamos HU-015:
     * - descuenta garrafones de la carga
     * - registra EntregaPedido
     * - registra efectivo/transferencia
     * - cambia la venta a ENTREGADO
     */
    return confirmarEntrega(
            ventaGuardada.getId(),
            entregaDTO
    );
}

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
        dto.setClienteId(
        entrega.getVenta() != null
                ? entrega.getVenta().getClienteRutaId()
                : null
);

dto.setClienteNombre(
        entrega.getVenta() != null
                ? entrega.getVenta().getNombreCliente()
                : null
);
        return dto;
    }

    public List<EntregaPedidoDTO> entregasHoy(
        Long repartidorId) {

    obtenerRepartidor(repartidorId);

    LocalDate hoy =
            LocalDate.now();

    LocalDateTime inicio =
            hoy.atStartOfDay();

    LocalDateTime fin =
            hoy.plusDays(1)
                    .atStartOfDay();

    return entregaPedidoRepository
            .findByRepartidorIdAndFechaBetweenOrderByFechaAsc(
                    repartidorId,
                    inicio,
                    fin
            )
            .stream()
            .map(this::convertirDTO)
            .toList();
}
}