package com.utvt.ApiSpringCafeSoft.service;

import com.utvt.ApiSpringCafeSoft.dto.VentaDTO;
import com.utvt.ApiSpringCafeSoft.dto.VentaDetalleDTO;
import com.utvt.ApiSpringCafeSoft.dto.ReporteVentasDTO;
import com.utvt.ApiSpringCafeSoft.dto.ProductoMasVendidoDTO;
import com.utvt.ApiSpringCafeSoft.model.*;
import com.utvt.ApiSpringCafeSoft.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class VentaService {

    @Autowired private VentaRepository ventaRepository;
    @Autowired private ProductoRepository productoRepository;
    @Autowired private InventarioRepository inventarioRepository;
    @Autowired private UsuarioRepository usuarioRepository;
    @Autowired private LoteRepository loteRepository;

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
        dto.setDetalles(venta.getDetalles().stream()
            .map(this::convertDetalleToDTO).collect(Collectors.toList()));

        // ── Sucursal ──
        if (venta.getSucursal() != null) {
            dto.setSucursalId(venta.getSucursal().getId());
            dto.setSucursalNombre(venta.getSucursal().getNombre());
        }

        return dto;
    }

    private VentaDetalleDTO convertDetalleToDTO(VentaDetalle detalle) {
        VentaDetalleDTO dto = new VentaDetalleDTO();
        dto.setId(detalle.getId());
        dto.setProductoId(detalle.getProducto().getId());
        dto.setProductoNombre(detalle.getProducto().getNombre());
        dto.setCantidad(detalle.getCantidad());
        dto.setPrecioUnitario(detalle.getPrecioUnitario());
        dto.setSubtotal(detalle.getSubtotal());
        return dto;
    }

    private String generarFolio() {
        // Se ejecuta dentro de la misma transacción SERIALIZABLE de crearVenta,
        // por lo que el SELECT ya está protegido por el lock de la transacción.
        // El uso de findLastFolio() con la BD bloqueada evita folios duplicados
        // bajo carga concurrente.
        String lastFolio = ventaRepository.findLastFolio();
        if (lastFolio == null) return "V-0001";
        int numero = Integer.parseInt(lastFolio.substring(2)) + 1;
        return String.format("V-%04d", numero);
    }

    @Transactional(isolation = Isolation.READ_COMMITTED)
    public VentaDTO crearVenta(VentaDTO ventaDTO) {
        Usuario usuario = usuarioRepository.findById(ventaDTO.getUsuarioId())
            .orElseThrow(() -> new RuntimeException("Usuario no encontrado con ID: " + ventaDTO.getUsuarioId()));

        // Validar stock antes de procesar
        for (VentaDetalleDTO detalleDTO : ventaDTO.getDetalles()) {
            Producto producto = productoRepository.findByIdWithInsumos(detalleDTO.getProductoId());
            if (producto == null) throw new RuntimeException("Producto no encontrado con ID: " + detalleDTO.getProductoId());
            validarStock(producto, detalleDTO.getCantidad());
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

        // ── Asignar la sucursal del usuario que realiza la venta ──
        if (usuario.getSucursal() != null) {
            venta.setSucursal(usuario.getSucursal());
        }

        // Efectivo y cambio
        if ("efectivo".equals(ventaDTO.getMetodoPago()) && ventaDTO.getMontoEfectivo() != null) {
            venta.setMontoEfectivo(ventaDTO.getMontoEfectivo());
        }

        double subtotal = 0.0;

        for (VentaDetalleDTO detalleDTO : ventaDTO.getDetalles()) {
            Producto producto = productoRepository.findByIdWithInsumos(detalleDTO.getProductoId());
            if (producto == null) throw new RuntimeException("Producto no encontrado con ID: " + detalleDTO.getProductoId());
            descontarPorLotes(producto, detalleDTO.getCantidad());

            Double precioUnitario = detalleDTO.getPrecioUnitario() != null
                ? detalleDTO.getPrecioUnitario() : producto.getPrecio();
            Double subtotalDetalle = precioUnitario * detalleDTO.getCantidad();
            subtotal += subtotalDetalle;

            VentaDetalle detalle = new VentaDetalle();
            detalle.setProducto(producto);
            detalle.setCantidad(detalleDTO.getCantidad());
            detalle.setPrecioUnitario(precioUnitario);
            detalle.setSubtotal(subtotalDetalle);
            venta.addDetalle(detalle);
        }

        double impuestos = subtotal * 0.16;
        double descuento = ventaDTO.getDescuento() != null ? ventaDTO.getDescuento() : 0.0;
        double total = subtotal + impuestos - descuento;

        venta.setSubtotal(subtotal);
        venta.setImpuestos(impuestos);
        venta.setDescuento(descuento);
        venta.setTotal(total);

        if ("efectivo".equals(ventaDTO.getMetodoPago()) && ventaDTO.getMontoEfectivo() != null) {
            double cambio = ventaDTO.getMontoEfectivo() - total;
            if (cambio < 0) throw new RuntimeException("El monto en efectivo es insuficiente");
            venta.setCambio(cambio);
        }

        return convertToDTO(ventaRepository.save(venta));
    }

    private void validarStock(Producto producto, Integer cantidadVendida) {
        for (ProductoInsumo pi : producto.getInsumos()) {
            // Re-leer con bloqueo para obtener el valor actual antes de validar
            Inventario inv = inventarioRepository
                .findByIdForUpdate(pi.getInsumo().getId())
                .orElseThrow(() -> new RuntimeException(
                    "Inventario no encontrado para insumo ID: " + pi.getInsumo().getId()));
            double necesario = pi.getCantidad() * cantidadVendida;
            if (inv.getCantidad() < necesario) {
                throw new RuntimeException("Stock insuficiente para: " + inv.getNombre()
                    + " | Disponible: " + inv.getCantidad()
                    + " | Necesario: " + necesario + " " + inv.getUnidadMedida());
            }
        }
    }

    private void descontarPorLotes(Producto producto, Integer cantidadVendida) {
        for (ProductoInsumo pi : producto.getInsumos()) {
            // Re-leer con bloqueo pesimista: garantiza que ninguna otra
            // transacción concurrente pueda leer ni escribir este registro
            // hasta que la transacción actual haga commit (HU-017).
            Inventario inv = inventarioRepository
                .findByIdForUpdate(pi.getInsumo().getId())
                .orElseThrow(() -> new RuntimeException(
                    "Inventario no encontrado para insumo ID: " + pi.getInsumo().getId()));

            double aDescontar = pi.getCantidad() * cantidadVendida;

            // Doble chequeo dentro del lock: evita ventas en negativo
            // si otra transacción consumió stock entre la validación inicial
            // y este punto.
            if (inv.getCantidad() < aDescontar) {
                throw new RuntimeException("Stock insuficiente para: " + inv.getNombre()
                    + " | Disponible: " + inv.getCantidad()
                    + " | Necesario: " + aDescontar + " " + inv.getUnidadMedida());
            }

            inv.setCantidad(inv.getCantidad() - aDescontar);
            inventarioRepository.save(inv);

            // Consumir lotes con bloqueo pesimista (FEFO)
            List<Lote> lotes = loteRepository
                .findLotesDisponiblesByNombreYUnidadForUpdate(
                    inv.getNombre(), inv.getUnidadMedida());

            double restante = aDescontar;
            for (Lote lote : lotes) {
                if (restante <= 0) break;
                double disponibleEnLote = lote.getCantidad();
                if (disponibleEnLote >= restante) {
                    lote.setCantidad(disponibleEnLote - restante);
                    restante = 0;
                } else {
                    restante -= disponibleEnLote;
                    lote.setCantidad(0.0);
                }
                loteRepository.save(lote);
            }
        }
    }

    public List<VentaDTO> obtenerTodasLasVentas() {
        return ventaRepository.findAll().stream().map(this::convertToDTO).collect(Collectors.toList());
    }

    public VentaDTO obtenerVentaPorId(Long id) {
        return convertToDTO(ventaRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Venta no encontrada con ID: " + id)));
    }

    public VentaDTO obtenerVentaPorFolio(String folio) {
        Venta venta = ventaRepository.findByFolio(folio);
        if (venta == null) throw new RuntimeException("Venta no encontrada con folio: " + folio);
        return convertToDTO(venta);
    }

    public List<VentaDTO> obtenerVentasPorUsuario(Long usuarioId) {
        return ventaRepository.findByUsuarioId(usuarioId).stream()
            .map(this::convertToDTO).collect(Collectors.toList());
    }

    public List<VentaDTO> obtenerVentasPorRangoFechas(LocalDateTime inicio, LocalDateTime fin) {
        return ventaRepository.findByFechaBetween(inicio, fin).stream()
            .map(this::convertToDTO).collect(Collectors.toList());
    }

    public List<VentaDTO> obtenerVentasPorMetodoPago(String metodoPago) {
        return ventaRepository.findByMetodoPago(metodoPago).stream()
            .map(this::convertToDTO).collect(Collectors.toList());
    }

    public List<VentaDTO> obtenerPedidosPendientes() {
        return ventaRepository
            .findByEstadoPedidoOrderByFechaAsc("PENDIENTE")
            .stream()
            .map(this::convertToDTO)
            .collect(Collectors.toList());
    }

    @Transactional
    public VentaDTO marcarPedidoEntregado(Long id) {
        Venta venta = ventaRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Pedido no encontrado con ID: " + id));
        venta.setEstadoPedido("ENTREGADA");
        return convertToDTO(ventaRepository.save(venta));
    }

    @Transactional
    public void cancelarVenta(Long id) {
        ventaRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Venta no encontrada con ID: " + id));
        ventaRepository.deleteById(id);
    }

    // =========================================================
    // HU-016: REPORTE DE VENTAS DIARIAS Y MENSUALES
    // =========================================================

    /**
     * Genera el reporte de ventas del periodo seleccionado
     * ("hoy", "mes" o "mes_anterior") para la sucursal del
     * usuario autenticado (JWT).
     *
     * Criterio de aceptación 2: los totales y los desgloses
     * se calculan a partir de la MISMA lista de ventas, por
     * lo que siempre coinciden con las ventas registradas.
     */
    public ReporteVentasDTO obtenerReporteVentas(String periodo) {

        // ── Usuario autenticado y su sucursal (JWT) ──
        Usuario usuario = usuarioConSucursal();
        Long sucursalId = usuario.getSucursal().getId();
        String sucursalNombre = usuario.getSucursal().getNombre();

        // ── Resolver el rango del periodo ──
        RangoPeriodo rango = calcularRangoPeriodo(periodo);
        String periodoNormalizado = rango.periodo;
        LocalDateTime desde = rango.desde;
        LocalDateTime hasta = rango.hasta;

        // ── Ventas registradas del periodo en la sucursal ──
        List<Venta> ventas =
                ventaRepository.findByFechaBetweenAndSucursalId(desde, hasta, sucursalId);

        // ── Totales del periodo ──
        long numeroVentas = ventas.size();
        double montoTotal = ventas.stream()
                .mapToDouble(v -> v.getTotal() != null ? v.getTotal() : 0.0).sum();
        double subtotal = ventas.stream()
                .mapToDouble(v -> v.getSubtotal() != null ? v.getSubtotal() : 0.0).sum();
        double impuestos = ventas.stream()
                .mapToDouble(v -> v.getImpuestos() != null ? v.getImpuestos() : 0.0).sum();

        // ── Desglose por día (orden cronológico) ──
        Map<LocalDate, List<Venta>> porDia = ventas.stream()
                .collect(Collectors.groupingBy(
                        v -> v.getFecha().toLocalDate(),
                        LinkedHashMap::new,
                        Collectors.toList()));
        List<ReporteVentasDTO.DesgloseDia> desgloseDia = porDia.entrySet().stream()
                .sorted(Map.Entry.comparingByKey())
                .map(e -> new ReporteVentasDTO.DesgloseDia(
                        e.getKey(),
                        e.getValue().size(),
                        e.getValue().stream()
                                .mapToDouble(v -> v.getTotal() != null ? v.getTotal() : 0.0).sum()))
                .collect(Collectors.toList());

        // ── Desglose por método de pago (mayor monto primero) ──
        Map<String, List<Venta>> porMetodo = ventas.stream()
                .collect(Collectors.groupingBy(Venta::getMetodoPago));
        List<ReporteVentasDTO.DesgloseMetodoPago> desglosePago = porMetodo.entrySet().stream()
                .map(e -> new ReporteVentasDTO.DesgloseMetodoPago(
                        e.getKey(),
                        e.getValue().size(),
                        e.getValue().stream()
                                .mapToDouble(v -> v.getTotal() != null ? v.getTotal() : 0.0).sum()))
                .sorted((a, b) -> Double.compare(b.getMontoTotal(), a.getMontoTotal()))
                .collect(Collectors.toList());

        ReporteVentasDTO reporte = new ReporteVentasDTO();
        reporte.setPeriodo(periodoNormalizado);
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

    /**
     * Ranking de productos ordenado por unidades vendidas
     * (descendente) en el periodo seleccionado, para la
     * sucursal del usuario autenticado.
     *
     * Los datos se calculan a partir de las MISMAS ventas
     * del periodo y la sucursal que el reporte de ventas,
     * por lo que siempre corresponden al periodo
     * seleccionado.
     */
    public List<ProductoMasVendidoDTO> obtenerProductosMasVendidos(String periodo) {

        Usuario usuario = usuarioConSucursal();
        Long sucursalId = usuario.getSucursal().getId();

        RangoPeriodo rango = calcularRangoPeriodo(periodo);

        List<Venta> ventas = ventaRepository
                .findByFechaBetweenAndSucursalId(rango.desde, rango.hasta, sucursalId);

        // ── Acumular unidades y monto por producto ──
        Map<Long, ProductoMasVendidoDTO> porProducto = new LinkedHashMap<>();
        for (Venta venta : ventas) {
            for (VentaDetalle detalle : venta.getDetalles()) {
                Producto producto = detalle.getProducto();
                ProductoMasVendidoDTO fila = porProducto.computeIfAbsent(
                        producto.getId(),
                        k -> new ProductoMasVendidoDTO(
                                producto.getId(),
                                producto.getNombre(),
                                0L,
                                0.0));
                fila.setUnidadesVendidas(
                        fila.getUnidadesVendidas()
                                + (detalle.getCantidad() != null ? detalle.getCantidad() : 0));
                fila.setMontoTotal(
                        fila.getMontoTotal()
                                + (detalle.getSubtotal() != null ? detalle.getSubtotal() : 0.0));
            }
        }

        // ── Ordenar por unidades vendidas (mayor primero) ──
        Comparator<ProductoMasVendidoDTO> porUnidades =
                Comparator.comparingLong(ProductoMasVendidoDTO::getUnidadesVendidas).reversed();
        Comparator<ProductoMasVendidoDTO> porMonto =
                Comparator.comparingDouble(ProductoMasVendidoDTO::getMontoTotal).reversed();

        return porProducto.values().stream()
                .sorted(porUnidades.thenComparing(porMonto))
                .collect(Collectors.toList());
    }

    // ── Helpers compartidos por los reportes ──

    /**
     * Devuelve el usuario autenticado (JWT) y valida
     * que tenga una sucursal asignada.
     */
    private Usuario usuarioConSucursal() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated()
                || auth.getPrincipal() == null
                || "anonymousUser".equals(auth.getPrincipal())) {
            throw new RuntimeException("No hay un usuario autenticado");
        }
        String email = auth.getName();
        Usuario usuario = usuarioRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));
        if (usuario.getSucursal() == null) {
            throw new IllegalArgumentException(
                    "Tu cuenta no tiene una sucursal asignada. Contacta al administrador.");
        }
        return usuario;
    }

    /**
     * Resuelve el rango de fechas y la etiqueta del
     * periodo: "hoy", "mes" o "mes_anterior".
     */
    private RangoPeriodo calcularRangoPeriodo(String periodo) {
        LocalDate hoy = LocalDate.now();

        DateTimeFormatter fmtDia =
                DateTimeFormatter.ofPattern("d 'de' MMMM 'de' yyyy", new Locale("es"));
        DateTimeFormatter fmtMes =
                DateTimeFormatter.ofPattern("MMMM 'de' yyyy", new Locale("es"));

        String periodoNormalizado =
                periodo == null ? "hoy" : periodo.trim().toLowerCase();

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
                desdeFecha = hoy.withDayOfMonth(1);
                hastaFecha = hoy.withDayOfMonth(hoy.lengthOfMonth());
                etiqueta = hoy.format(fmtMes);
                break;
            case "mes_anterior":
                YearMonth mesAnterior = YearMonth.now().minusMonths(1);
                desdeFecha = mesAnterior.atDay(1);
                hastaFecha = mesAnterior.atEndOfMonth();
                etiqueta = mesAnterior.format(fmtMes);
                break;
            default:
                throw new IllegalArgumentException(
                        "Periodo no válido. Usa: hoy, mes o mes_anterior.");
        }

        return new RangoPeriodo(
                periodoNormalizado,
                etiqueta,
                desdeFecha.atStartOfDay(),
                hastaFecha.atTime(23, 59, 59, 999999999));
    }

    /** Rango de fechas y etiqueta de un periodo de reporte. */
    private static class RangoPeriodo {
        final String periodo;
        final String etiqueta;
        final LocalDateTime desde;
        final LocalDateTime hasta;

        RangoPeriodo(String periodo, String etiqueta, LocalDateTime desde, LocalDateTime hasta) {
            this.periodo = periodo;
            this.etiqueta = etiqueta;
            this.desde = desde;
            this.hasta = hasta;
        }
    }
}
