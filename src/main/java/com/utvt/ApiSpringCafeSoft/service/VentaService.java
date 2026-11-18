
package com.utvt.ApiSpringCafeSoft.service;

import com.utvt.ApiSpringCafeSoft.dto.ClienteFrecuenteDTO;
import com.utvt.ApiSpringCafeSoft.dto.VentaDTO;
import com.utvt.ApiSpringCafeSoft.dto.VentaDetalleDTO;
import com.utvt.ApiSpringCafeSoft.dto.ReporteVentasDTO;
import com.utvt.ApiSpringCafeSoft.dto.ProductoMasVendidoDTO;
import com.utvt.ApiSpringCafeSoft.exception.RecursoNoEncontradoException;
import com.utvt.ApiSpringCafeSoft.model.*;
import com.utvt.ApiSpringCafeSoft.repository.*;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.time.format.TextStyle;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class VentaService {

    @Autowired
    private VentaRepository ventaRepository;

    @Autowired
    private ProductoRepository productoRepository;

    @Autowired
    private InventarioRepository inventarioRepository;

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private LoteRepository loteRepository;

    @Autowired
    private BeneficiosService beneficiosService;

    @Autowired
    private NotificacionService notificacionService;

    private static final List<String> ESTADOS_PEDIDO = List.of(
        "PENDIENTE",
        "RECIBIDA",
        "EN_PROCESO",
        "ENTREGADA"
    );

    // =========================================================
    // CONVERTIR VENTA A DTO
    // =========================================================

    private VentaDTO convertToDTO(Venta venta) {

        VentaDTO dto = new VentaDTO();

        dto.setId(venta.getId());
        dto.setFolio(venta.getFolio());
        dto.setFecha(venta.getFecha());
        dto.setSubtotal(venta.getSubtotal());
        dto.setImpuestos(venta.getImpuestos());
        dto.setDescuento(venta.getDescuento());
        dto.setTotal(venta.getTotal());
        dto.setMetodoPago(venta.getMetodoPago());
        dto.setMontoEfectivo(venta.getMontoEfectivo());
        dto.setCambio(venta.getCambio());
        dto.setNombreCliente(venta.getNombreCliente());
        dto.setEstadoPedido(venta.getEstadoPedido());
        dto.setUsuarioId(venta.getUsuario().getId());
        dto.setUsuarioNombre(venta.getUsuario().getNombre());
        dto.setObservaciones(venta.getObservaciones());
        dto.setCreatedAt(venta.getCreatedAt());

        dto.setDetalles(
            venta.getDetalles()
                .stream()
                .map(this::convertDetalleToDTO)
                .collect(Collectors.toList())
        );

        if (venta.getSucursal() != null) {
            dto.setSucursalId(venta.getSucursal().getId());
            dto.setSucursalNombre(venta.getSucursal().getNombre());
        }

        return dto;
    }

    private VentaDetalleDTO convertDetalleToDTO(
            VentaDetalle detalle) {

        VentaDetalleDTO dto = new VentaDetalleDTO();

        dto.setId(detalle.getId());
        dto.setProductoId(detalle.getProducto().getId());
        dto.setProductoNombre(detalle.getProducto().getNombre());
        dto.setCantidad(detalle.getCantidad());
        dto.setPrecioUnitario(detalle.getPrecioUnitario());
        dto.setSubtotal(detalle.getSubtotal());

        return dto;
    }

    // =========================================================
    // GENERAR FOLIO
    // =========================================================

    private String generarFolio() {

        String lastFolio = ventaRepository.findLastFolio();

        if (lastFolio == null) {
            return "V-0001";
        }

        int numero =
            Integer.parseInt(lastFolio.substring(2)) + 1;

        return String.format("V-%04d", numero);
    }

    // =========================================================
    // CREAR VENTA
    // =========================================================

    @Transactional(isolation = Isolation.READ_COMMITTED)
    public VentaDTO crearVenta(VentaDTO ventaDTO) {

        Usuario usuario = usuarioRepository
            .findById(ventaDTO.getUsuarioId())
            .orElseThrow(() -> new RuntimeException(
                "Usuario no encontrado con ID: " +
                ventaDTO.getUsuarioId()
            ));

        // HU-023: validar y consumir puntos canjeados.

        double subtotalCalculado = 0.0;

        for (VentaDetalleDTO detalleDTO :
                ventaDTO.getDetalles()) {

            Producto producto = productoRepository
                .findByIdWithInsumos(
                    detalleDTO.getProductoId()
                );

            if (producto == null) {
                throw new RuntimeException(
                    "Producto no encontrado con ID: " +
                    detalleDTO.getProductoId()
                );
            }

            validarStock(
                producto,
                detalleDTO.getCantidad()
            );

            double precioUnitario =
                detalleDTO.getPrecioUnitario() != null
                    ? detalleDTO.getPrecioUnitario()
                    : producto.getPrecio();

            subtotalCalculado +=
                precioUnitario * detalleDTO.getCantidad();
        }

        double totalCalculado =
            subtotalCalculado * 1.16;

        Integer saldoPuntosRestante = null;

        if (ventaDTO.getPuntosCanjeados() != null
                && ventaDTO.getPuntosCanjeados() > 0) {

            saldoPuntosRestante =
                beneficiosService.consumir(
                    ventaDTO.getClienteId(),
                    ventaDTO.getPuntosCanjeados(),
                    ventaDTO.getDescuento(),
                    totalCalculado
                );

        } else if (
            ventaDTO.getPuntosCanjeados() != null
                && ventaDTO.getPuntosCanjeados() < 0
        ) {

            throw new IllegalArgumentException(
                "Los puntos canjeados no pueden ser negativos"
            );
        }

        Venta venta = new Venta();

        venta.setFolio(generarFolio());
        venta.setFecha(LocalDateTime.now());
        venta.setMetodoPago(ventaDTO.getMetodoPago());
        venta.setUsuario(usuario);
        venta.setObservaciones(ventaDTO.getObservaciones());
        venta.setCreatedAt(LocalDateTime.now());
        venta.setNombreCliente(ventaDTO.getNombreCliente());
        venta.setEstadoPedido("PENDIENTE");

        if (usuario.getSucursal() != null) {
            venta.setSucursal(usuario.getSucursal());
        }

        if ("efectivo".equals(ventaDTO.getMetodoPago())
                && ventaDTO.getMontoEfectivo() != null) {

            venta.setMontoEfectivo(
                ventaDTO.getMontoEfectivo()
            );
        }

        double subtotal = 0.0;

        for (VentaDetalleDTO detalleDTO :
                ventaDTO.getDetalles()) {

            Producto producto = productoRepository
                .findByIdWithInsumos(
                    detalleDTO.getProductoId()
                );

            if (producto == null) {
                throw new RuntimeException(
                    "Producto no encontrado con ID: " +
                    detalleDTO.getProductoId()
                );
            }

            descontarPorLotes(
                producto,
                detalleDTO.getCantidad()
            );

            Double precioUnitario =
                detalleDTO.getPrecioUnitario() != null
                    ? detalleDTO.getPrecioUnitario()
                    : producto.getPrecio();

            Double subtotalDetalle =
                precioUnitario * detalleDTO.getCantidad();

            subtotal += subtotalDetalle;

            VentaDetalle detalle = new VentaDetalle();

            detalle.setProducto(producto);
            detalle.setCantidad(detalleDTO.getCantidad());
            detalle.setPrecioUnitario(precioUnitario);
            detalle.setSubtotal(subtotalDetalle);

            venta.addDetalle(detalle);
        }

        double impuestos = subtotal * 0.16;

        double descuento =
            ventaDTO.getDescuento() != null
                ? ventaDTO.getDescuento()
                : 0.0;

        double total =
            subtotal + impuestos - descuento;

        venta.setSubtotal(subtotal);
        venta.setImpuestos(impuestos);
        venta.setDescuento(descuento);
        venta.setTotal(total);

        if ("efectivo".equals(ventaDTO.getMetodoPago())
                && ventaDTO.getMontoEfectivo() != null) {

            double cambio =
                ventaDTO.getMontoEfectivo() - total;

            if (cambio < 0) {
                throw new RuntimeException(
                    "El monto en efectivo es insuficiente"
                );
            }

            venta.setCambio(cambio);
        }

        Venta ventaGuardada =
            ventaRepository.save(venta);

        notificacionService.enviarCambioEstado(
            ventaGuardada,
            "PENDIENTE"
        );

        VentaDTO respuesta =
            convertToDTO(ventaGuardada);

        respuesta.setClienteId(
            ventaDTO.getClienteId()
        );

        respuesta.setPuntosCanjeados(
            ventaDTO.getPuntosCanjeados()
        );

        respuesta.setSaldoPuntosRestante(
            saldoPuntosRestante
        );

        return respuesta;
    }

    // =========================================================
    // VALIDAR STOCK
    // =========================================================

    private void validarStock(
            Producto producto,
            Integer cantidadVendida) {

        for (ProductoInsumo pi : producto.getInsumos()) {

            Inventario inv = inventarioRepository
                .findByIdForUpdate(
                    pi.getInsumo().getId()
                )
                .orElseThrow(() -> new RuntimeException(
                    "Inventario no encontrado para insumo ID: " +
                    pi.getInsumo().getId()
                ));

            double necesario =
                pi.getCantidad() * cantidadVendida;

            if (inv.getCantidad() < necesario) {

                throw new RuntimeException(
                    "Stock insuficiente para: " +
                    inv.getNombre() +
                    " | Disponible: " +
                    inv.getCantidad() +
                    " | Necesario: " +
                    necesario + " " +
                    inv.getUnidadMedida()
                );
            }
        }
    }

    // =========================================================
    // DESCONTAR INVENTARIO POR LOTES
    // =========================================================

    private void descontarPorLotes(
            Producto producto,
            Integer cantidadVendida) {

        for (ProductoInsumo pi : producto.getInsumos()) {

            Inventario inv = inventarioRepository
                .findByIdForUpdate(
                    pi.getInsumo().getId()
                )
                .orElseThrow(() -> new RuntimeException(
                    "Inventario no encontrado para insumo ID: " +
                    pi.getInsumo().getId()
                ));

            double aDescontar =
                pi.getCantidad() * cantidadVendida;

            if (inv.getCantidad() < aDescontar) {

                throw new RuntimeException(
                    "Stock insuficiente para: " +
                    inv.getNombre() +
                    " | Disponible: " +
                    inv.getCantidad() +
                    " | Necesario: " +
                    aDescontar + " " +
                    inv.getUnidadMedida()
                );
            }

            inv.setCantidad(
                inv.getCantidad() - aDescontar
            );

            inventarioRepository.save(inv);

            List<Lote> lotes = loteRepository
                .findLotesDisponiblesByNombreYUnidadForUpdate(
                    inv.getNombre(),
                    inv.getUnidadMedida()
                );

            double restante = aDescontar;

            for (Lote lote : lotes) {

                if (restante <= 0) {
                    break;
                }

                double disponibleEnLote =
                    lote.getCantidad();

                if (disponibleEnLote >= restante) {

                    lote.setCantidad(
                        disponibleEnLote - restante
                    );

                    restante = 0;

                } else {

                    restante -= disponibleEnLote;
                    lote.setCantidad(0.0);
                }

                loteRepository.save(lote);
            }
        }
    }

    // =========================================================
    // CONSULTAS DE VENTAS
    // =========================================================

    public List<VentaDTO> obtenerTodasLasVentas() {

        return ventaRepository.findAll()
            .stream()
            .map(this::convertToDTO)
            .collect(Collectors.toList());
    }

    public VentaDTO obtenerVentaPorId(Long id) {

        Venta venta = ventaRepository.findById(id)
            .orElseThrow(() -> new RuntimeException(
                "Venta no encontrada con ID: " + id
            ));

        return convertToDTO(venta);
    }

    public VentaDTO obtenerVentaPorFolio(String folio) {

        Venta venta =
            ventaRepository.findByFolio(folio);

        if (venta == null) {
            throw new RuntimeException(
                "Venta no encontrada con folio: " + folio
            );
        }

        return convertToDTO(venta);
    }

    public List<VentaDTO> obtenerVentasPorUsuario(
            Long usuarioId) {

        return ventaRepository.findByUsuarioId(usuarioId)
            .stream()
            .map(this::convertToDTO)
            .collect(Collectors.toList());
    }

    public List<VentaDTO> obtenerVentasPorRangoFechas(
            LocalDateTime inicio,
            LocalDateTime fin) {

        return ventaRepository
            .findByFechaBetween(inicio, fin)
            .stream()
            .map(this::convertToDTO)
            .collect(Collectors.toList());
    }

    public List<VentaDTO> obtenerVentasPorMetodoPago(
            String metodoPago) {

        return ventaRepository
            .findByMetodoPago(metodoPago)
            .stream()
            .map(this::convertToDTO)
            .collect(Collectors.toList());
    }

    // =========================================================
    // PEDIDOS PENDIENTES
    // =========================================================

    public List<VentaDTO> obtenerPedidosPendientes() {

        return ventaRepository
            .findByEstadoPedidoOrderByFechaAsc("PENDIENTE")
            .stream()
            .map(this::convertToDTO)
            .collect(Collectors.toList());
    }

    @Transactional
    public VentaDTO marcarPedidoEntregado(Long id) {

        return actualizarEstadoPedido(id, "ENTREGADA");
    }

    // =========================================================
    // ACTUALIZAR ESTADO DEL PEDIDO
    // =========================================================

    @Transactional
    public VentaDTO actualizarEstadoPedido(
            Long id,
            String estado) {

        String nuevoEstado =
            estado == null
                ? ""
                : estado.trim().toUpperCase(Locale.ROOT);

        if (!ESTADOS_PEDIDO.contains(nuevoEstado)) {

            throw new IllegalArgumentException(
                "Estado no válido. Usa: PENDIENTE, " +
                "RECIBIDA, EN_PROCESO o ENTREGADA."
            );
        }

        Venta venta = ventaRepository.findById(id)
            .orElseThrow(() ->
                new RecursoNoEncontradoException(
                    "Pedido no encontrado con ID: " + id
                )
            );

        if (nuevoEstado.equals(venta.getEstadoPedido())) {
            return convertToDTO(venta);
        }

        venta.setEstadoPedido(nuevoEstado);

        Venta guardada =
            ventaRepository.save(venta);

        notificacionService.enviarCambioEstado(
            guardada,
            nuevoEstado
        );

        return convertToDTO(guardada);
    }

    // =========================================================
    // CANCELAR VENTA
    // =========================================================

    @Transactional
    public void cancelarVenta(Long id) {

        ventaRepository.findById(id)
            .orElseThrow(() -> new RuntimeException(
                "Venta no encontrada con ID: " + id
            ));

        ventaRepository.deleteById(id);
    }

    // =========================================================
    // HU-016: REPORTE DE VENTAS DIARIAS Y MENSUALES
    // =========================================================

    public ReporteVentasDTO obtenerReporteVentas(
            String periodo) {

        Usuario usuario = usuarioConSucursal();

        Long sucursalId =
            usuario.getSucursal().getId();

        String sucursalNombre =
            usuario.getSucursal().getNombre();

        RangoPeriodo rango =
            calcularRangoPeriodo(periodo);

        LocalDateTime desde = rango.desde;
        LocalDateTime hasta = rango.hasta;

        List<Venta> ventas = ventaRepository
            .findByFechaBetweenAndSucursalId(
                desde,
                hasta,
                sucursalId
            );

        long numeroVentas = ventas.size();

        double montoTotal = ventas.stream()
            .mapToDouble(v ->
                v.getTotal() != null
                    ? v.getTotal()
                    : 0.0
            )
            .sum();

        double subtotal = ventas.stream()
            .mapToDouble(v ->
                v.getSubtotal() != null
                    ? v.getSubtotal()
                    : 0.0
            )
            .sum();

        double impuestos = ventas.stream()
            .mapToDouble(v ->
                v.getImpuestos() != null
                    ? v.getImpuestos()
                    : 0.0
            )
            .sum();

        // Desglose por día.

        Map<LocalDate, List<Venta>> porDia =
            ventas.stream()
                .collect(Collectors.groupingBy(
                    v -> v.getFecha().toLocalDate(),
                    LinkedHashMap::new,
                    Collectors.toList()
                ));

        List<ReporteVentasDTO.DesgloseDia> desgloseDia =
            porDia.entrySet()
                .stream()
                .sorted(Map.Entry.comparingByKey())
                .map(e ->
                    new ReporteVentasDTO.DesgloseDia(
                        e.getKey(),
                        e.getValue().size(),
                        e.getValue()
                            .stream()
                            .mapToDouble(v ->
                                v.getTotal() != null
                                    ? v.getTotal()
                                    : 0.0
                            )
                            .sum()
                    )
                )
                .collect(Collectors.toList());

        // Desglose por método de pago.

        Map<String, List<Venta>> porMetodo =
            ventas.stream()
                .collect(Collectors.groupingBy(
                    Venta::getMetodoPago
                ));

        List<ReporteVentasDTO.DesgloseMetodoPago>
            desglosePago =
                porMetodo.entrySet()
                    .stream()
                    .map(e ->
                        new ReporteVentasDTO.DesgloseMetodoPago(
                            e.getKey(),
                            e.getValue().size(),
                            e.getValue()
                                .stream()
                                .mapToDouble(v ->
                                    v.getTotal() != null
                                        ? v.getTotal()
                                        : 0.0
                                )
                                .sum()
                        )
                    )
                    .sorted((a, b) ->
                        Double.compare(
                            b.getMontoTotal(),
                            a.getMontoTotal()
                        )
                    )
                    .collect(Collectors.toList());

        ReporteVentasDTO reporte =
            new ReporteVentasDTO();

        reporte.setPeriodo(rango.periodo);
        reporte.setPeriodoEtiqueta(rango.etiqueta);
        reporte.setDesde(desde);
        reporte.setHasta(hasta);
        reporte.setSucursalId(sucursalId);
        reporte.setSucursalNombre(sucursalNombre);
        reporte.setNumeroVentas(numeroVentas);
        reporte.setMontoTotal(montoTotal);
        reporte.setSubtotal(subtotal);
        reporte.setImpuestos(impuestos);
        reporte.setDesglosePorDia(desgloseDia);
        reporte.setDesglosePorMetodoPago(desglosePago);

        return reporte;
    }

    // =========================================================
    // HU-017: PRODUCTOS MÁS VENDIDOS
    // =========================================================

    public List<ProductoMasVendidoDTO>
            obtenerProductosMasVendidos(String periodo) {

        Usuario usuario = usuarioConSucursal();

        Long sucursalId =
            usuario.getSucursal().getId();

        RangoPeriodo rango =
            calcularRangoPeriodo(periodo);

        List<Venta> ventas = ventaRepository
            .findByFechaBetweenAndSucursalId(
                rango.desde,
                rango.hasta,
                sucursalId
            );

        Map<Long, ProductoMasVendidoDTO> porProducto =
            new LinkedHashMap<>();

        for (Venta venta : ventas) {

            for (VentaDetalle detalle :
                    venta.getDetalles()) {

                Producto producto =
                    detalle.getProducto();

                ProductoMasVendidoDTO fila =
                    porProducto.computeIfAbsent(
                        producto.getId(),
                        k -> new ProductoMasVendidoDTO(
                            producto.getId(),
                            producto.getNombre(),
                            0L,
                            0.0
                        )
                    );

                fila.setUnidadesVendidas(
                    fila.getUnidadesVendidas()
                        + (
                            detalle.getCantidad() != null
                                ? detalle.getCantidad()
                                : 0
                        )
                );

                fila.setMontoTotal(
                    fila.getMontoTotal()
                        + (
                            detalle.getSubtotal() != null
                                ? detalle.getSubtotal()
                                : 0.0
                        )
                );
            }
        }

        Comparator<ProductoMasVendidoDTO> porUnidades =
            Comparator.comparingLong(
                ProductoMasVendidoDTO::getUnidadesVendidas
            ).reversed();

        Comparator<ProductoMasVendidoDTO> porMonto =
            Comparator.comparingDouble(
                ProductoMasVendidoDTO::getMontoTotal
            ).reversed();

        return porProducto.values()
            .stream()
            .sorted(
                porUnidades.thenComparing(porMonto)
            )
            .collect(Collectors.toList());
    }

    // =========================================================
    // OBTENER USUARIO AUTENTICADO CON SUCURSAL
    // =========================================================

    private Usuario usuarioConSucursal() {

        Authentication auth =
            SecurityContextHolder
                .getContext()
                .getAuthentication();

        if (auth == null
                || !auth.isAuthenticated()
                || auth.getPrincipal() == null
                || "anonymousUser".equals(
                    auth.getPrincipal()
                )) {

            throw new RuntimeException(
                "No hay un usuario autenticado"
            );
        }

        String email = auth.getName();

        Usuario usuario = usuarioRepository
            .findByEmail(email)
            .orElseThrow(() -> new RuntimeException(
                "Usuario no encontrado"
            ));

        if (usuario.getSucursal() == null) {

            throw new IllegalArgumentException(
                "Tu cuenta no tiene una sucursal asignada. " +
                "Contacta al administrador."
            );
        }

        return usuario;
    }

    // =========================================================
    // CALCULAR RANGO DE PERIODO
    // =========================================================

    private RangoPeriodo calcularRangoPeriodo(
            String periodo) {

        LocalDate hoy = LocalDate.now();

        DateTimeFormatter fmtDia =
            DateTimeFormatter.ofPattern(
                "d 'de' MMMM 'de' yyyy",
                new Locale("es")
            );

        DateTimeFormatter fmtMes =
            DateTimeFormatter.ofPattern(
                "MMMM 'de' yyyy",
                new Locale("es")
            );

        String periodoNormalizado =
            periodo == null
                ? "hoy"
                : periodo.trim().toLowerCase();

        LocalDate desdeFecha;
        LocalDate hastaFecha;
        String etiqueta;

        switch (periodoNormalizado) {

            case "hoy":

                desdeFecha = hoy;
                hastaFecha = hoy;
                etiqueta = hoy.format(fmtDia);
                break;

            case "mes":

                desdeFecha =
                    hoy.withDayOfMonth(1);

                hastaFecha =
                    hoy.withDayOfMonth(
                        hoy.lengthOfMonth()
                    );

                etiqueta = hoy.format(fmtMes);
                break;

            case "mes_anterior":

                YearMonth mesAnterior =
                    YearMonth.now().minusMonths(1);

                desdeFecha =
                    mesAnterior.atDay(1);

                hastaFecha =
                    mesAnterior.atEndOfMonth();

                etiqueta =
                    mesAnterior.format(fmtMes);
                break;

            default:

                throw new IllegalArgumentException(
                    "Periodo no válido. Usa: hoy, " +
                    "mes o mes_anterior."
                );
        }

        return new RangoPeriodo(
            periodoNormalizado,
            etiqueta,
            desdeFecha.atStartOfDay(),
            hastaFecha.atTime(
                23,
                59,
                59,
                999999999
            )
        );
    }

    // =========================================================
    // CLASE AUXILIAR PARA LOS RANGOS
    // =========================================================

    private static class RangoPeriodo {

        final String periodo;
        final String etiqueta;
        final LocalDateTime desde;
        final LocalDateTime hasta;

        RangoPeriodo(
                String periodo,
                String etiqueta,
                LocalDateTime desde,
                LocalDateTime hasta) {

            this.periodo = periodo;
            this.etiqueta = etiqueta;
            this.desde = desde;
            this.hasta = hasta;
        }
    }

    // =========================================================
    // HU-018: CLIENTES FRECUENTES
    // =========================================================

    public List<ClienteFrecuenteDTO>
            obtenerClientesFrecuentes(
                String periodo,
                int minimoCompras) {

        if (minimoCompras < 1) {

            throw new IllegalArgumentException(
                "El mínimo de compras debe ser al menos 1."
            );
        }

        Usuario usuario = usuarioConSucursal();

        Long sucursalId =
            usuario.getSucursal().getId();

        RangoPeriodo rango =
            calcularRangoPeriodo(periodo);

        List<Venta> ventas = ventaRepository
            .findByFechaBetweenAndSucursalIdAndClienteRutaIdIsNotNull(
                rango.desde,
                rango.hasta,
                sucursalId
            );

        Map<Long, ClienteFrecuenteDTO> porCliente =
            new LinkedHashMap<>();

        for (Venta venta : ventas) {

            Long cid =
                venta.getClienteRutaId();

            String nombre =
                venta.getNombreCliente() != null
                    ? venta.getNombreCliente()
                    : "Cliente #" + cid;

            double monto =
                venta.getTotal() != null
                    ? venta.getTotal()
                    : 0.0;

            ClienteFrecuenteDTO fila =
                porCliente.computeIfAbsent(
                    cid,
                    k -> new ClienteFrecuenteDTO(
                        cid,
                        nombre,
                        0L,
                        0.0,
                        false
                    )
                );

            fila.setNombre(nombre);

            fila.setTotalCompras(
                fila.getTotalCompras() + 1
            );

            fila.setMontoTotal(
                fila.getMontoTotal() + monto
            );
        }

        return porCliente.values()
            .stream()
            .peek(c ->
                c.setEsFrecuente(
                    c.getTotalCompras() >= minimoCompras
                )
            )
            .sorted(
                Comparator.comparingLong(
                    ClienteFrecuenteDTO::getTotalCompras
                ).reversed()
            )
            .collect(Collectors.toList());
    }

    // =========================================================
    // HU-045 Y HU-046: PATRONES DE CONSUMO
    // =========================================================

    @Transactional(readOnly = true)
    public Map<String, Object> obtenerPatronesConsumo(
            LocalDate inicio,
            LocalDate fin) {

        // Validar rango de fechas.

        if (inicio == null
                || fin == null
                || inicio.isAfter(fin)
                || ChronoUnit.DAYS.between(
                    inicio,
                    fin
                ) > 366) {

            throw new ResponseStatusException(
                HttpStatus.BAD_REQUEST,
                "Rango de fechas inválido (máximo 366 días)"
            );
        }

        // Obtener sucursal del usuario autenticado.

        Usuario usuario = usuarioConSucursal();

        Long sucursalId =
            usuario.getSucursal().getId();

        String sucursalNombre =
            usuario.getSucursal().getNombre();

        // Consultar ventas del rango seleccionado.

        List<Venta> ventas = ventaRepository
            .findByFechaBetweenAndSucursalId(
                inicio.atStartOfDay(),
                fin.atTime(LocalTime.MAX),
                sucursalId
            );

        // =====================================================
        // AGRUPAR VENTAS POR HORA
        // =====================================================

        Map<Integer, List<Venta>> porHora =
            ventas.stream()
                .filter(v ->
                    v.getFecha() != null
                )
                .collect(Collectors.groupingBy(
                    v -> v.getFecha().getHour()
                ));

        List<Map<String, Object>> franjas =
            new ArrayList<>();

        for (int hora = 0; hora < 24; hora++) {

            List<Venta> grupo =
                porHora.getOrDefault(
                    hora,
                    Collections.emptyList()
                );

            Map<String, Object> fila =
                new LinkedHashMap<>();

            fila.put("hora", hora);

            fila.put(
                "franja",
                String.format(
                    "%02d:00 - %02d:00",
                    hora,
                    (hora + 1) % 24
                )
            );

            fila.put(
                "numeroVentas",
                grupo.size()
            );

            double montoHorario =
                grupo.stream()
                    .mapToDouble(v ->
                        v.getTotal() != null
                            ? v.getTotal()
                            : 0.0
                    )
                    .sum();

            fila.put(
                "montoTotal",
                montoHorario
            );

            franjas.add(fila);
        }

        // =====================================================
        // AGRUPAR VENTAS POR DÍA
        // =====================================================

        Map<LocalDate, List<Venta>> porDia =
            ventas.stream()
                .filter(v ->
                    v.getFecha() != null
                )
                .collect(Collectors.groupingBy(
                    v -> v.getFecha().toLocalDate()
                ));

        List<Map<String, Object>> dias =
            new ArrayList<>();

        Locale localeMX =
            new Locale("es", "MX");

        for (
            LocalDate fecha = inicio;
            !fecha.isAfter(fin);
            fecha = fecha.plusDays(1)
        ) {

            List<Venta> grupo =
                porDia.getOrDefault(
                    fecha,
                    Collections.emptyList()
                );

            Map<String, Object> fila =
                new LinkedHashMap<>();

            fila.put(
                "fecha",
                fecha.toString()
            );

            fila.put(
                "diaSemana",
                fecha.getDayOfWeek()
                    .getDisplayName(
                        TextStyle.FULL,
                        localeMX
                    )
            );

            fila.put(
                "numeroVentas",
                grupo.size()
            );

            double montoDiario =
                grupo.stream()
                    .mapToDouble(v ->
                        v.getTotal() != null
                            ? v.getTotal()
                            : 0.0
                    )
                    .sum();

            fila.put(
                "montoTotal",
                montoDiario
            );

            dias.add(fila);
        }

        // =====================================================
        // IDENTIFICAR LA HORA DE MAYOR CONSUMO
        // =====================================================

        Map<String, Object> pico =
            franjas.stream()
                .max(
                    Comparator.comparingLong(
                        f -> ((Number)
                            f.get("numeroVentas")
                        ).longValue()
                    )
                )
                .orElse(null);

        // =====================================================
        // CALCULAR TOTALES GENERALES
        // =====================================================

        long numeroVentas = ventas.size();

        double montoTotal =
            ventas.stream()
                .mapToDouble(v ->
                    v.getTotal() != null
                        ? v.getTotal()
                        : 0.0
                )
                .sum();

        // =====================================================
        // CONSTRUIR RESPUESTA PARA REACT
        // =====================================================

        Map<String, Object> resultado =
            new LinkedHashMap<>();

        resultado.put(
            "inicio",
            inicio.toString()
        );

        resultado.put(
            "fin",
            fin.toString()
        );

        resultado.put(
            "sucursalNombre",
            sucursalNombre
        );

        resultado.put(
            "numeroVentas",
            numeroVentas
        );

        resultado.put(
            "montoTotal",
            montoTotal
        );

        resultado.put(
            "franjaMayorConsumo",
            ventas.isEmpty() ? null : pico
        );

        resultado.put(
            "franjas",
            franjas
        );

        resultado.put(
            "dias",
            dias
        );

        return resultado;
    }
}
