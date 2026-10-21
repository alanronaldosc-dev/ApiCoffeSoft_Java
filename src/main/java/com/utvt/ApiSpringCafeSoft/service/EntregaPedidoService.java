package com.utvt.ApiSpringCafeSoft.service;
import com.utvt.ApiSpringCafeSoft.dto.ConfirmarEntregaDTO;
import com.utvt.ApiSpringCafeSoft.dto.DetalleGarrafonDTO;
import com.utvt.ApiSpringCafeSoft.dto.EntregaPedidoDTO;
import com.utvt.ApiSpringCafeSoft.dto.ItemGarrafonDTO;
import com.utvt.ApiSpringCafeSoft.dto.RegistrarVentaRutaDTO;
import com.utvt.ApiSpringCafeSoft.dto.ReportarIncidenciaEntregaDTO;
import com.utvt.ApiSpringCafeSoft.model.Carga;
import com.utvt.ApiSpringCafeSoft.model.Cliente;
import com.utvt.ApiSpringCafeSoft.model.EntregaGarrafonDetalle;
import com.utvt.ApiSpringCafeSoft.model.EntregaPedido;
import com.utvt.ApiSpringCafeSoft.model.Inventario;
import com.utvt.ApiSpringCafeSoft.model.Producto;
import com.utvt.ApiSpringCafeSoft.model.Ruta;
import com.utvt.ApiSpringCafeSoft.model.Usuario;
import com.utvt.ApiSpringCafeSoft.model.Venta;
import com.utvt.ApiSpringCafeSoft.model.VentaDetalle;
import com.utvt.ApiSpringCafeSoft.model.MovimientoPuntos;
import com.utvt.ApiSpringCafeSoft.repository.MovimientoPuntosRepository;
import com.utvt.ApiSpringCafeSoft.repository.CargaRepository;
import com.utvt.ApiSpringCafeSoft.repository.ClienteRepository;
import com.utvt.ApiSpringCafeSoft.repository.EntregaGarrafonDetalleRepository;
import com.utvt.ApiSpringCafeSoft.repository.EntregaPedidoRepository;
import com.utvt.ApiSpringCafeSoft.repository.InventarioRepository;
import com.utvt.ApiSpringCafeSoft.repository.ProductoRepository;
import com.utvt.ApiSpringCafeSoft.repository.UsuarioRepository;
import com.utvt.ApiSpringCafeSoft.repository.VentaRepository;
import com.utvt.ApiSpringCafeSoft.repository.RutaRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
@Service
public class EntregaPedidoService {
    private static final String ESTADO_CARGA_ACTIVA =
            "CARGA EN TRÁNSITO";
    private static final String RESULTADO_ENTREGADO =
            "ENTREGADO";
    private static final String TIPO_VENDIDO =
            "VENDIDO";
    private static final String TIPO_DEVUELTO =
            "DEVUELTO";
    @Autowired
    private EntregaPedidoRepository entregaPedidoRepository;
    @Autowired
    private EntregaGarrafonDetalleRepository entregaGarrafonDetalleRepository;
    @Autowired
    private VentaRepository ventaRepository;
    @Autowired
    private UsuarioRepository usuarioRepository;
    @Autowired
    private CargaRepository cargaRepository;
    @Autowired
    private InventarioRepository inventarioRepository;
    @Autowired
    private ProductoRepository productoRepository;
    @Autowired
    private ClienteRepository clienteRepository;
    @Autowired
    private MovimientoPuntosRepository movimientoPuntosRepository;
    @Autowired private RutaRepository rutaRepository;
    // =====================================================
    // REGISTRAR VENTA EN RUTA
    // =====================================================
    @Transactional
    public EntregaPedidoDTO registrarVentaRuta(
            RegistrarVentaRutaDTO dto) {
        Cliente cliente =
                clienteRepository
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
        Usuario repartidor =
                obtenerRepartidor(
                        dto.getRepartidorId()
                );
        // =================================================
        // VALIDAR RUTA
        // =================================================
        if (cliente.getRuta() == null) {
            throw new RuntimeException(
                    "El cliente no tiene una ruta asignada"
            );
        }
        Ruta ruta =
                cliente.getRuta();
        if (!Boolean.TRUE.equals(ruta.getActiva())) {
            throw new RuntimeException(
                    "La ruta del cliente no está activa"
            );
        }
        if (ruta.getRepartidor() == null) {
            throw new RuntimeException(
                    "La ruta no tiene repartidor asignado"
            );
        }
        if (!ruta.getRepartidor()
                .getId()
                .equals(repartidor.getId())) {
            throw new RuntimeException(
                    "El cliente no pertenece a la ruta de este repartidor"
            );
        }
        // =================================================
        // GARRAFONES LLENOS
        // =================================================
        List<ItemGarrafonDTO> garrafonesPorTipo =
                dto.getGarrafones() != null
                        ? dto.getGarrafones()
                        : new ArrayList<>();
        if (garrafonesPorTipo.isEmpty()) {
            throw new RuntimeException(
                    "Debe seleccionar al menos un garrafón para entregar"
            );
        }
        double totalGarrafones =
                0.0;
        for (ItemGarrafonDTO linea :
                garrafonesPorTipo) {
            if (linea.getInventarioId() == null
                    || linea.getCantidad() == null
                    || linea.getCantidad() <= 0) {
                throw new RuntimeException(
                        "Cada garrafón debe tener un tipo y una cantidad válida"
                );
            }
            if (Math.abs(
                    linea.getCantidad()
                            - Math.rint(linea.getCantidad())
            ) > 0.0001) {
                throw new RuntimeException(
                        "La cantidad de garrafones debe ser un número entero"
                );
            }
            /*
             * IMPORTANTE:
             * ahora se consulta únicamente
             * la carga asociada a ESTA ruta.
             */
            double disponible =
                    cantidadDisponibleEnCargaRuta(
                            ruta.getId(),
                            linea.getInventarioId()
                    );
            if (disponible + 0.0001
                    < linea.getCantidad()) {
                throw new RuntimeException(
                        "Carga insuficiente de "
                                + nombreInventario(
                                linea.getInventarioId()
                        )
                                + " en esta ruta. Disponible: "
                                + disponible
                                + ", solicitado para entregar: "
                                + linea.getCantidad()
                );
            }
            totalGarrafones +=
                    linea.getCantidad();
        }
        // =================================================
        // ENVASES VACÍOS
        // =================================================
        List<ItemGarrafonDTO> vaciosPorTipo =
                dto.getEnvasesVacios() != null
                        ? dto.getEnvasesVacios()
                        : new ArrayList<>();
        double totalVacios =
                0.0;
        for (ItemGarrafonDTO linea :
                vaciosPorTipo) {
            if (linea.getInventarioId() == null
                    || linea.getCantidad() == null
                    || linea.getCantidad() <= 0) {
                continue;
            }
            if (Math.abs(
                    linea.getCantidad()
                            - Math.rint(linea.getCantidad())
            ) > 0.0001) {
                throw new RuntimeException(
                        "La cantidad de envases vacíos debe ser un número entero"
                );
            }

            Inventario inventarioVacio =
                    inventarioRepository
                            .findById(linea.getInventarioId())
                            .orElseThrow(() ->
                                    new RuntimeException(
                                            "Inventario no encontrado con ID: "
                                                    + linea.getInventarioId()
                                    )
                            );

            if (!esInventarioVacio(inventarioVacio)) {
                throw new RuntimeException(
                        "El inventario seleccionado para envases vacíos no corresponde a un garrafón vacío: "
                                + inventarioVacio.getNombre()
                );
            }

            totalVacios +=
                    linea.getCantidad();
        }
        // =================================================
        // TOTAL
        // =================================================
        double precioUnitario =
                dto.getPrecioUnitario();
        double total =
                totalGarrafones
                        * precioUnitario;
        // =================================================
        // CREAR VENTA
        // =================================================
        Venta venta =
                new Venta();
        venta.setFolio(
                "R-"
                        + UUID.randomUUID()
                        .toString()
                        .substring(0, 12)
                        .toUpperCase()
        );
        venta.setFecha(
                LocalDateTime.now()
        );
        venta.setCreatedAt(
                LocalDateTime.now()
        );
        venta.setUsuario(
                repartidor
        );
        venta.setNombreCliente(
                cliente.getNombre()
        );
        venta.setClienteRutaId(
                cliente.getId()
        );
        venta.setSubtotal(
                total
        );
        venta.setImpuestos(
                0.0
        );
        venta.setDescuento(
                0.0
        );
        venta.setTotal(
                total
        );
        String metodoPago =
                dto.getMetodoCobro()
                        .toLowerCase();
        venta.setMetodoPago(
                metodoPago
        );
        if ("efectivo".equals(metodoPago)) {
            venta.setMontoEfectivo(
                    total
            );
            venta.setCambio(
                    0.0
            );
        }
        venta.setEstadoPedido(
                "PENDIENTE"
        );
        venta.setObservaciones(
                dto.getObservaciones()
        );
        if (repartidor.getSucursal() == null) {
            throw new RuntimeException(
                    "El repartidor no tiene una sucursal asignada"
            );
        }
        venta.setSucursal(
                repartidor.getSucursal()
        );
        // =================================================
        // DETALLE DE VENTA
        // =================================================
        for (ItemGarrafonDTO linea :
                garrafonesPorTipo) {
            Producto producto =
                    productoDelInventario(
                            linea.getInventarioId()
                    );
            if (producto == null) {
                producto =
                        cliente.getGarrafonPreferencia();
            }
            if (producto == null) {
                throw new RuntimeException(
                        "No se encontró el producto del garrafón seleccionado"
                );
            }
            VentaDetalle detalle =
                    new VentaDetalle();
            detalle.setProducto(
                    producto
            );
            detalle.setCantidad(
                    (int) Math.round(
                            linea.getCantidad()
                    )
            );
            detalle.setPrecioUnitario(
                    precioUnitario
            );
            detalle.setSubtotal(
                    linea.getCantidad()
                            * precioUnitario
            );
            venta.addDetalle(
                    detalle
            );
        }
        Venta ventaGuardada =
                ventaRepository.save(
                        venta
                );
        // Una venta genera un solo movimiento: credito por garrafones o debito por canje.
        String metodo = dto.getMetodoCobro() == null ? "" : dto.getMetodoCobro().trim().toUpperCase();
        int puntosMovimiento;
        if ("PUNTOS".equals(metodo)) {
            long costoPuntos = Math.round(total);
            long saldo = movimientoPuntosRepository.saldo(cliente.getId());
            if (saldo < costoPuntos) {
                throw new IllegalArgumentException("Puntos insuficientes. Saldo: " + saldo + ", requeridos: " + costoPuntos);
            }
            puntosMovimiento = -Math.toIntExact(costoPuntos);
        } else {
            puntosMovimiento = Math.toIntExact(Math.round(totalGarrafones));
        }
        if (puntosMovimiento != 0 && movimientoPuntosRepository.findByVentaId(ventaGuardada.getId()).isEmpty()) {
            MovimientoPuntos movimiento = new MovimientoPuntos();
            movimiento.setCliente(cliente);
            movimiento.setVenta(ventaGuardada);
            movimiento.setPuntos(puntosMovimiento);
            movimientoPuntosRepository.save(movimiento);
        }

        // CONFIRMAR ENTREGA
        // =================================================
        // CONFIRMAR ENTREGA
        // =================================================
        ConfirmarEntregaDTO entregaDTO =
                new ConfirmarEntregaDTO();
        entregaDTO.setRepartidorId(
                repartidor.getId()
        );
        entregaDTO.setGarrafonesEntregados(
                totalGarrafones
        );
        entregaDTO.setEnvasesVaciosRecibidos(
                totalVacios
        );
        entregaDTO.setMetodoCobro(
                dto.getMetodoCobro()
                        .toUpperCase()
        );
        entregaDTO.setMontoCobrado(
                total
        );
        entregaDTO.setObservaciones(
                dto.getObservaciones()
        );
        return confirmarEntregaConDetalle(
                ventaGuardada.getId(),
                entregaDTO,
                garrafonesPorTipo,
                vaciosPorTipo,
                ruta
        );
    }
    // =====================================================
    // CONFIRMACIÓN INDIVIDUAL
    // =====================================================
    @Transactional
    public EntregaPedidoDTO confirmarEntrega(
            Long ventaId,
            ConfirmarEntregaDTO dto) {
        return confirmarEntregaConDetalle(
                ventaId,
                dto,
                null,
                null,
                null
        );
    }
    // =====================================================
    // CONFIRMAR ENTREGA CON DETALLE
    // =====================================================
    @Transactional
    public EntregaPedidoDTO confirmarEntregaConDetalle(
            Long ventaId,
            ConfirmarEntregaDTO dto,
            List<ItemGarrafonDTO> garrafonesPorTipo,
            List<ItemGarrafonDTO> vaciosPorTipo,
            Ruta ruta) {
        Venta venta =
                obtenerVentaPendiente(
                        ventaId
                );
        Usuario repartidor =
                obtenerRepartidor(
                        dto.getRepartidorId()
                );
        if (entregaPedidoRepository
                .existsByVentaIdAndResultado(
                        ventaId,
                        RESULTADO_ENTREGADO
                )) {
            throw new RuntimeException(
                    "El pedido ya cuenta con una entrega confirmada"
            );
        }
        // =================================================
        // DESCONTAR CARGA
        // =================================================
        if (garrafonesPorTipo != null
                && !garrafonesPorTipo.isEmpty()) {
            for (ItemGarrafonDTO linea :
                    garrafonesPorTipo) {
                /*
                 * Si conocemos la ruta,
                 * descontamos exclusivamente
                 * de esa ruta.
                 */
                if (ruta != null) {
                    descontarCargaPorTipoRuta(
                            ruta.getId(),
                            linea.getInventarioId(),
                            linea.getCantidad()
                    );
                } else {
                    /*
                     * Compatibilidad con
                     * entregas anteriores.
                     */
                    descontarCargaPorTipo(
                            repartidor.getId(),
                            linea.getInventarioId(),
                            linea.getCantidad()
                    );
                }
            }
        } else {
            if (ruta != null) {
                descontarCargaRuta(
                        ruta.getId(),
                        dto.getGarrafonesEntregados()
                );
            } else {
                descontarCarga(
                        repartidor.getId(),
                        dto.getGarrafonesEntregados()
                );
            }
        }
        // =================================================
        // ENVASES VACÍOS
        // =================================================
        double totalVacios =
                nvl(
                        dto.getEnvasesVaciosRecibidos()
                );
        if (vaciosPorTipo != null) {
            totalVacios =
                    0.0;
            for (ItemGarrafonDTO linea :
                    vaciosPorTipo) {
                if (linea.getInventarioId() == null
                        || linea.getCantidad() == null
                        || linea.getCantidad() <= 0) {
                    continue;
                }
                /*
                 * IMPORTANTE:
                 *
                 * Durante la venta NO se aumenta todavía
                 * el inventario de envases vacíos.
                 *
                 * Solo registramos cuántos envases recibió
                 * el repartidor y el detalle DEVUELTO.
                 *
                 * Los vacíos pasan al inventario de planta
                 * cuando se liquida/cierra la ruta.
                 */
                totalVacios +=
                        linea.getCantidad();
            }
        }
        // =================================================
        // REGISTRAR ENTREGA
        // =================================================
        EntregaPedido entrega =
                new EntregaPedido();
        entrega.setVenta(
                venta
        );
        entrega.setRepartidor(
                repartidor
        );
        /*
         * Guardamos la ruta para poder
         * liquidarla después.
         */
        if (ruta == null) {
            ruta = rutaRepository.findByRepartidorId(repartidor.getId()).stream()
                .filter(r -> "EN_TRANSITO".equalsIgnoreCase(r.getEstado()))
                .findFirst().orElse(null);
        }
        entrega.setRuta(ruta);
        entrega.setGarrafonesEntregados(
                nvl(
                        dto.getGarrafonesEntregados()
                )
        );
        entrega.setEnvasesVaciosRecibidos(
                totalVacios
        );
        entrega.setMetodoCobro(
                dto.getMetodoCobro() != null
                        ? dto.getMetodoCobro()
                        .toUpperCase()
                        : null
        );
        entrega.setMontoCobrado(
                nvl(
                        dto.getMontoCobrado()
                )
        );
        entrega.setResultado(
                RESULTADO_ENTREGADO
        );
        entrega.setFecha(
                LocalDateTime.now()
        );
        entrega.setObservaciones(
                dto.getObservaciones()
        );
        venta.setEstadoPedido(
                RESULTADO_ENTREGADO
        );
        ventaRepository.save(
                venta
        );
        EntregaPedido entregaGuardada =
                entregaPedidoRepository.save(
                        entrega
                );
        // =================================================
        // DETALLES DE GARRAFONES VENDIDOS
        // =================================================
        if (garrafonesPorTipo != null) {
            for (ItemGarrafonDTO linea :
                    garrafonesPorTipo) {
                guardarDetalle(
                        entregaGuardada,
                        linea.getInventarioId(),
                        linea.getCantidad(),
                        TIPO_VENDIDO
                );
            }
        }
        // =================================================
        // DETALLES DE VACÍOS
        // =================================================
        if (vaciosPorTipo != null) {
            for (ItemGarrafonDTO linea :
                    vaciosPorTipo) {
                guardarDetalle(
                        entregaGuardada,
                        linea.getInventarioId(),
                        linea.getCantidad(),
                        TIPO_DEVUELTO
                );
            }
        }
        return convertirDTO(
                entregaGuardada
        );
    }
    // =====================================================
    // REPORTAR INCIDENCIA
    // =====================================================
    @Transactional
    public EntregaPedidoDTO reportarIncidencia(
            Long ventaId,
            ReportarIncidenciaEntregaDTO dto) {
        Venta venta =
                obtenerVentaPendiente(
                        ventaId
                );
        Usuario repartidor =
                obtenerRepartidor(
                        dto.getRepartidorId()
                );
        EntregaPedido entrega =
                new EntregaPedido();
        entrega.setVenta(
                venta
        );
        entrega.setRepartidor(
                repartidor
        );
        entrega.setGarrafonesEntregados(
                0.0
        );
        entrega.setEnvasesVaciosRecibidos(
                0.0
        );
        entrega.setMontoCobrado(
                0.0
        );
        entrega.setMetodoCobro(
                null
        );
        entrega.setResultado(
                dto.getMotivo()
                        .toUpperCase()
        );
        entrega.setFecha(
                LocalDateTime.now()
        );
        entrega.setObservaciones(
                dto.getObservaciones()
        );
        venta.setEstadoPedido(
                dto.getMotivo()
                        .toUpperCase()
        );
        ventaRepository.save(
                venta
        );
        return convertirDTO(
                entregaPedidoRepository.save(
                        entrega
                )
        );
    }
    // =====================================================
    // OBTENER VENTA PENDIENTE
    // =====================================================
    private Venta obtenerVentaPendiente(
            Long ventaId) {
        Venta venta =
                ventaRepository
                        .findById(
                                ventaId
                        )
                        .orElseThrow(() ->
                                new RuntimeException(
                                        "Pedido no encontrado con ID: "
                                                + ventaId
                                )
                        );
        if (!"PENDIENTE"
                .equalsIgnoreCase(
                        venta.getEstadoPedido()
                )) {
            throw new RuntimeException(
                    "El pedido no puede confirmarse porque su estado actual es: "
                            + venta.getEstadoPedido()
            );
        }
        return venta;
    }
    // =====================================================
    // OBTENER REPARTIDOR
    // =====================================================
    private Usuario obtenerRepartidor(
            Long repartidorId) {
        Usuario repartidor =
                usuarioRepository
                        .findById(
                                repartidorId
                        )
                        .orElseThrow(() ->
                                new RuntimeException(
                                        "Repartidor no encontrado con ID: "
                                                + repartidorId
                                )
                        );
        if (repartidor.getUserTipo() == null
                || repartidor.getUserTipo() != 4) {
            throw new RuntimeException(
                    "El usuario indicado no tiene el rol de Repartidor"
            );
        }
        if (Boolean.FALSE.equals(
                repartidor.getActivo()
        )) {
            throw new RuntimeException(
                    "El repartidor está inactivo"
            );
        }
        return repartidor;
    }
    // =====================================================
    // COMPATIBILIDAD: CARGAS POR REPARTIDOR
    // =====================================================
    private void descontarCarga(
            Long repartidorId,
            Double cantidadEntregada) {
        List<Carga> cargas =
                cargaRepository
                        .findByRepartidorIdAndEstadoOrderByFechaHoraAsc(
                                repartidorId,
                                ESTADO_CARGA_ACTIVA
                        );
        double disponibleTotal =
                cargas.stream()
                        .mapToDouble(
                                this::cantidadDisponible
                        )
                        .sum();
        if (disponibleTotal + 0.0001
                < cantidadEntregada) {
            throw new RuntimeException(
                    "Carga insuficiente. Disponible: "
                            + disponibleTotal
                            + ", solicitado para entregar: "
                            + cantidadEntregada
            );
        }
        double restante =
                cantidadEntregada;
        for (Carga carga :
                cargas) {
            if (restante <= 0.0001) {
                break;
            }
            double disponible =
                    cantidadDisponible(
                            carga
                    );
            double descontar =
                    Math.min(
                            disponible,
                            restante
                    );
            double nuevoDisponible =
                    disponible
                            - descontar;
            carga.setCantidadDisponible(
                    Math.max(
                            0.0,
                            nuevoDisponible
                    )
            );
            if (nuevoDisponible <= 0.0001) {
                carga.setEstado(
                        "AGOTADA"
                );
            }
            cargaRepository.save(
                    carga
            );
            restante -=
                    descontar;
        }
    }
    private void descontarCargaPorTipo(
            Long repartidorId,
            Long inventarioId,
            Double cantidadEntregada) {
        List<Carga> cargas =
                cargaRepository
                        .findByRepartidorIdAndEstadoAndInventarioIdOrderByFechaHoraAsc(
                                repartidorId,
                                ESTADO_CARGA_ACTIVA,
                                inventarioId
                        );
        double disponibleTotal =
                cargas.stream()
                        .mapToDouble(
                                this::cantidadDisponible
                        )
                        .sum();
        if (disponibleTotal + 0.0001
                < cantidadEntregada) {
            throw new RuntimeException(
                    "Carga insuficiente de "
                            + nombreInventario(
                            inventarioId
                    )
                            + ". Disponible: "
                            + disponibleTotal
                            + ", solicitado para entregar: "
                            + cantidadEntregada
            );
        }
        double restante =
                cantidadEntregada;
        for (Carga carga :
                cargas) {
            if (restante <= 0.0001) {
                break;
            }
            double disponible =
                    cantidadDisponible(
                            carga
                    );
            double descontar =
                    Math.min(
                            disponible,
                            restante
                    );
            double nuevoDisponible =
                    disponible
                            - descontar;
            carga.setCantidadDisponible(
                    Math.max(
                            0.0,
                            nuevoDisponible
                    )
            );
            if (nuevoDisponible <= 0.0001) {
                carga.setEstado(
                        "AGOTADA"
                );
            }
            cargaRepository.save(
                    carga
            );
            restante -=
                    descontar;
        }
    }
    // =====================================================
    // NUEVO: CARGAS POR RUTA
    // =====================================================
    private double cantidadDisponibleEnCargaRuta(
            Long rutaId,
            Long inventarioId) {
        return cargaRepository
                .findByRutaIdAndEstadoAndInventarioIdOrderByFechaHoraAsc(
                        rutaId,
                        ESTADO_CARGA_ACTIVA,
                        inventarioId
                )
                .stream()
                .mapToDouble(
                        this::cantidadDisponible
                )
                .sum();
    }
    private void descontarCargaRuta(
            Long rutaId,
            Double cantidadEntregada) {
        List<Carga> cargas =
                cargaRepository
                        .findByRutaIdAndEstadoOrderByFechaHoraAsc(
                                rutaId,
                                ESTADO_CARGA_ACTIVA
                        );
        double disponibleTotal =
                cargas.stream()
                        .mapToDouble(
                                this::cantidadDisponible
                        )
                        .sum();
        if (disponibleTotal + 0.0001
                < cantidadEntregada) {
            throw new RuntimeException(
                    "Carga insuficiente en la ruta. Disponible: "
                            + disponibleTotal
                            + ", solicitado: "
                            + cantidadEntregada
            );
        }
        double restante =
                cantidadEntregada;
        for (Carga carga :
                cargas) {
            if (restante <= 0.0001) {
                break;
            }
            double disponible =
                    cantidadDisponible(
                            carga
                    );
            double descontar =
                    Math.min(
                            disponible,
                            restante
                    );
            double nuevoDisponible =
                    disponible
                            - descontar;
            carga.setCantidadDisponible(
                    Math.max(
                            0.0,
                            nuevoDisponible
                    )
            );
            if (nuevoDisponible <= 0.0001) {
                carga.setEstado(
                        "AGOTADA"
                );
            }
            cargaRepository.save(
                    carga
            );
            restante -=
                    descontar;
        }
    }
    private void descontarCargaPorTipoRuta(
            Long rutaId,
            Long inventarioId,
            Double cantidadEntregada) {
        List<Carga> cargas =
                cargaRepository
                        .findByRutaIdAndEstadoAndInventarioIdOrderByFechaHoraAsc(
                                rutaId,
                                ESTADO_CARGA_ACTIVA,
                                inventarioId
                        );
        double disponibleTotal =
                cargas.stream()
                        .mapToDouble(
                                this::cantidadDisponible
                        )
                        .sum();
        if (disponibleTotal + 0.0001
                < cantidadEntregada) {
            throw new RuntimeException(
                    "Carga insuficiente de "
                            + nombreInventario(
                            inventarioId
                    )
                            + " en esta ruta. Disponible: "
                            + disponibleTotal
                            + ", solicitado: "
                            + cantidadEntregada
            );
        }
        double restante =
                cantidadEntregada;
        for (Carga carga :
                cargas) {
            if (restante <= 0.0001) {
                break;
            }
            double disponible =
                    cantidadDisponible(
                            carga
                    );
            double descontar =
                    Math.min(
                            disponible,
                            restante
                    );
            double nuevoDisponible =
                    disponible
                            - descontar;
            carga.setCantidadDisponible(
                    Math.max(
                            0.0,
                            nuevoDisponible
                    )
            );
            if (nuevoDisponible <= 0.0001) {
                carga.setEstado(
                        "AGOTADA"
                );
            }
            cargaRepository.save(
                    carga
            );
            restante -=
                    descontar;
        }
    }
    // =====================================================
    // PRODUCTO
    // =====================================================
    private Producto productoDelInventario(
            Long inventarioId) {
        if (inventarioId == null) {
            return null;
        }
        Inventario inventario =
                inventarioRepository
                        .findById(
                                inventarioId
                        )
                        .orElse(
                                null
                        );
        if (inventario == null
                || inventario.getProductoId() == null) {
            return null;
        }
        return productoRepository
                .findById(
                        inventario.getProductoId()
                )
                .orElse(
                        null
                );
    }
    private boolean esInventarioVacio(
            Inventario inventario) {
        if (inventario == null) {
            return false;
        }

        String nombre = inventario.getNombre() != null
                ? inventario.getNombre().toLowerCase()
                : "";

        String tipo = inventario.getTipo() != null
                ? inventario.getTipo().toLowerCase()
                : "";

        return nombre.contains("vac")
                || tipo.contains("vac")
                || nombre.contains("envase")
                || tipo.contains("envase");
    }

    private String nombreInventario(
            Long inventarioId) {
        if (inventarioId == null) {
            return "garrafón";
        }
        return inventarioRepository
                .findById(
                        inventarioId
                )
                .map(
                        Inventario::getNombre
                )
                .orElse(
                        "garrafón"
                );
    }
    // =====================================================
    // GUARDAR DESGLOSE
    // =====================================================
    private void guardarDetalle(
            EntregaPedido entrega,
            Long inventarioId,
            Double cantidad,
            String tipoMovimiento) {
        if (inventarioId == null
                || cantidad == null
                || cantidad <= 0.0001) {
            return;
        }
        Inventario inventario =
                inventarioRepository
                        .findById(
                                inventarioId
                        )
                        .orElse(
                                null
                        );
        if (inventario == null) {
            return;
        }
        EntregaGarrafonDetalle detalle =
                new EntregaGarrafonDetalle();
        detalle.setEntregaPedido(
                entrega
        );
        detalle.setInventario(
                inventario
        );
        detalle.setTipoMovimiento(
                tipoMovimiento
        );
        detalle.setCantidad(
                cantidad
        );
        entregaGarrafonDetalleRepository.save(
                detalle
        );
    }
    private double cantidadDisponible(
            Carga carga) {
        return carga.getCantidadDisponible() != null
                ? carga.getCantidadDisponible()
                : (
                carga.getCantidad() != null
                        ? carga.getCantidad()
                        : 0.0
        );
    }
    private double nvl(
            Double valor) {
        return valor != null
                ? valor
                : 0.0;
    }
    // =====================================================
    // DTO
    // =====================================================
    private EntregaPedidoDTO convertirDTO(
            EntregaPedido entrega) {
        EntregaPedidoDTO dto =
                new EntregaPedidoDTO();
        dto.setId(
                entrega.getId()
        );
        dto.setVentaId(
                entrega.getVenta() != null
                        ? entrega.getVenta().getId()
                        : null
        );
        dto.setFolio(
                entrega.getVenta() != null
                        ? entrega.getVenta().getFolio()
                        : null
        );
        dto.setRepartidorId(
                entrega.getRepartidor() != null
                        ? entrega.getRepartidor().getId()
                        : null
        );
        dto.setRepartidorNombre(
                entrega.getRepartidor() != null
                        ? entrega.getRepartidor().getNombre()
                        : null
        );
        dto.setGarrafonesEntregados(
                entrega.getGarrafonesEntregados()
        );
        dto.setEnvasesVaciosRecibidos(
                entrega.getEnvasesVaciosRecibidos()
        );
        dto.setMetodoCobro(
                entrega.getMetodoCobro()
        );
        dto.setMontoCobrado(
                entrega.getMontoCobrado()
        );
        dto.setResultado(
                entrega.getResultado()
        );
        dto.setFecha(
                entrega.getFecha()
        );
        dto.setObservaciones(
                entrega.getObservaciones()
        );
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

        dto.setRutaId(
                entrega.getRuta() != null
                        ? entrega.getRuta().getId()
                        : null
        );

        // ========================================
        // HU-015: DESGLOSE POR TIPO DE GARRAFÓN
        // ========================================

        List<EntregaGarrafonDetalle> detalles =
                entregaGarrafonDetalleRepository
                        .findByEntregaPedidoId(
                                entrega.getId()
                        );
        for (EntregaGarrafonDetalle detalle :
                detalles) {
            DetalleGarrafonDTO item =
                    new DetalleGarrafonDTO(
                            detalle.getInventario() != null
                                    ? detalle.getInventario().getId()
                                    : null,
                            detalle.getInventario() != null
                                    ? detalle.getInventario().getNombre()
                                    : null,
                            detalle.getCantidad()
                    );
            if (TIPO_DEVUELTO
                    .equalsIgnoreCase(
                            detalle.getTipoMovimiento()
                    )) {
                dto.getEnvasesVaciosDetalle()
                        .add(item);
            } else {
                dto.getGarrafonesDetalle()
                        .add(item);
            }
        }
        return dto;
    }
    // =====================================================
    // ENTREGAS DE HOY
    // =====================================================
    public List<EntregaPedidoDTO> entregasHoy(
            Long repartidorId) {
        obtenerRepartidor(
                repartidorId
        );
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
                .map(
                        this::convertirDTO
                )
                .toList();
    }
}
