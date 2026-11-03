package com.utvt.ApiSpringCafeSoft.service;

import com.utvt.ApiSpringCafeSoft.dto.VentaDTO;
import com.utvt.ApiSpringCafeSoft.dto.VentaDetalleDTO;
import com.utvt.ApiSpringCafeSoft.model.*;
import com.utvt.ApiSpringCafeSoft.repository.*;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;

@Service
public class VentaService {

    private final VentaRepository ventaRepository;
    private final ProductoRepository productoRepository;
    private final InventarioRepository inventarioRepository;
    private final UsuarioRepository usuarioRepository;
    private final LoteRepository loteRepository;
    private final PromocionService promocionService;
    private final SegmentacionClienteService segmentacionClienteService;
    private final PuntosService puntosService;

    public VentaService(
            VentaRepository ventaRepository,
            ProductoRepository productoRepository,
            InventarioRepository inventarioRepository,
            UsuarioRepository usuarioRepository,
            LoteRepository loteRepository,
            PromocionService promocionService,
            SegmentacionClienteService segmentacionClienteService,
            PuntosService puntosService) {
        this.ventaRepository = ventaRepository;
        this.productoRepository = productoRepository;
        this.inventarioRepository = inventarioRepository;
        this.usuarioRepository = usuarioRepository;
        this.loteRepository = loteRepository;
        this.promocionService = promocionService;
        this.segmentacionClienteService = segmentacionClienteService;
        this.puntosService = puntosService;
    }

    private VentaDTO convertToDTO(Venta venta) {
        VentaDTO dto = new VentaDTO();
        dto.setId(venta.getId());
        dto.setFolio(venta.getFolio());
        dto.setFecha(venta.getFecha());
        dto.setSubtotal(venta.getSubtotal());
        dto.setImpuestos(venta.getImpuestos());
        dto.setDescuento(venta.getDescuento());
        dto.setPuntosCanjeados(venta.getPuntosCanjeados());
        dto.setTotal(venta.getTotal());
        dto.setMetodoPago(venta.getMetodoPago());
        dto.setMontoEfectivo(venta.getMontoEfectivo());
        dto.setCambio(venta.getCambio());
        dto.setNombreCliente(venta.getNombreCliente());
dto.setEstadoPedido(venta.getEstadoPedido());
        dto.setUsuarioId(venta.getUsuario().getId());
        dto.setUsuarioNombre(venta.getUsuario().getNombre());
        dto.setClienteId(venta.getCliente() == null ? null : venta.getCliente().getId());
        dto.setPromocionId(venta.getPromocion() == null ? null : venta.getPromocion().getId());
        dto.setObservaciones(venta.getObservaciones());
        dto.setCreatedAt(venta.getCreatedAt());
        dto.setDetalles(venta.getDetalles().stream()
            .map(this::convertDetalleToDTO).toList());
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
        String lastFolio = ventaRepository.findLastFolio();
        if (lastFolio == null) return "V-0001";
        int numero = Integer.parseInt(lastFolio.substring(2)) + 1;
        return String.format("V-%04d", numero);
    }

    @Transactional
    public VentaDTO crearVenta(VentaDTO ventaDTO) {
        Usuario usuario = usuarioRepository.findById(ventaDTO.getUsuarioId())
            .orElseThrow(() -> new ResponseStatusException(
                    HttpStatus.NOT_FOUND,
                    "Usuario no encontrado con ID: " + ventaDTO.getUsuarioId()));
        validarDescuentoSolicitado(ventaDTO);
        Usuario cliente = obtenerCliente(ventaDTO);
        List<Producto> productos = obtenerProductosValidandoStock(ventaDTO.getDetalles());

        Venta venta = new Venta();
        venta.setFolio(generarFolio());
        LocalDateTime ahora = LocalDateTime.now(ZoneId.systemDefault());
        venta.setFecha(ahora);
        venta.setMetodoPago(ventaDTO.getMetodoPago());
        venta.setUsuario(usuario);
        venta.setObservaciones(ventaDTO.getObservaciones());
        venta.setCreatedAt(ahora);
        venta.setNombreCliente(ventaDTO.getNombreCliente());
        venta.setEstadoPedido("PENDIENTE");

        if ("efectivo".equals(ventaDTO.getMetodoPago()) && ventaDTO.getMontoEfectivo() != null) {
            venta.setMontoEfectivo(ventaDTO.getMontoEfectivo());
        }

        double subtotal = agregarDetalles(venta, ventaDTO.getDetalles(), productos);
        int cantidadGarrafones = ventaDTO.getDetalles().stream()
                .mapToInt(VentaDetalleDTO::getCantidad)
                .sum();
        double impuestos = subtotal * 0.16;
        ResultadoDescuento resultadoDescuento = calcularDescuento(
                ventaDTO, cliente, cantidadGarrafones, subtotal, impuestos);
        venta.setCliente(resultadoDescuento.cliente());
        if (ventaDTO.getPromocionId() != null) {
            venta.setPromocion(promocionService.obtenerPorId(ventaDTO.getPromocionId()));
        }
        double descuento = resultadoDescuento.importe().doubleValue();
        double total = subtotal + impuestos - descuento;

        venta.setSubtotal(subtotal);
        venta.setImpuestos(impuestos);
        venta.setDescuento(descuento);
        venta.setPuntosCanjeados(resultadoDescuento.puntosCanjeados());
        venta.setTotal(total);

        descontarProductos(ventaDTO.getDetalles(), productos);
        calcularCambio(ventaDTO, venta, total);

        VentaDTO respuesta = convertToDTO(ventaRepository.save(venta));
        respuesta.setSaldoPuntosRestante(resultadoDescuento.saldoPuntosRestante());
        return respuesta;
    }

    private void validarDescuentoSolicitado(VentaDTO ventaDTO) {
        Double descuento = ventaDTO.getDescuento();
        Integer puntosCanjeados = ventaDTO.getPuntosCanjeados();
        if (puntosCanjeados == null || puntosCanjeados < 0) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST, "La cantidad de puntos no es válida.");
        }
        if (descuento != null && (!Double.isFinite(descuento) || descuento < 0)) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST, "El descuento debe ser un valor no negativo.");
        }
        if (ventaDTO.getPromocionId() != null && puntosCanjeados > 0) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "No se puede combinar una promoción con un canje de puntos.");
        }
        if (ventaDTO.getPromocionId() == null
                && puntosCanjeados == 0
                && descuento != null
                && descuento > 0) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "El descuento debe corresponder a una promoción o un canje validado.");
        }
    }

    private Usuario obtenerCliente(VentaDTO ventaDTO) {
        if (ventaDTO.getClienteId() == null || ventaDTO.getPuntosCanjeados() > 0) {
            return null;
        }
        return segmentacionClienteService.obtenerClienteActivo(ventaDTO.getClienteId());
    }

    private List<Producto> obtenerProductosValidandoStock(
            List<VentaDetalleDTO> detalles) {
        List<Producto> productos = new ArrayList<>();
        for (VentaDetalleDTO detalleDTO : detalles) {
            Producto producto = productoRepository.findByIdWithInsumos(detalleDTO.getProductoId());
            if (producto == null) {
                throw new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Producto no encontrado con ID: " + detalleDTO.getProductoId());
            }
            validarStock(producto, detalleDTO.getCantidad());
            productos.add(producto);
        }
        return productos;
    }

    private double agregarDetalles(
            Venta venta,
            List<VentaDetalleDTO> detalles,
            List<Producto> productos) {
        double subtotal = 0.0;
        for (int indice = 0; indice < detalles.size(); indice++) {
            VentaDetalleDTO detalleDTO = detalles.get(indice);
            Producto producto = productos.get(indice);
            double precioUnitario = producto.getPrecio();
            double subtotalDetalle = precioUnitario * detalleDTO.getCantidad();
            subtotal += subtotalDetalle;

            VentaDetalle detalle = new VentaDetalle();
            detalle.setProducto(producto);
            detalle.setCantidad(detalleDTO.getCantidad());
            detalle.setPrecioUnitario(precioUnitario);
            detalle.setSubtotal(subtotalDetalle);
            venta.addDetalle(detalle);
        }
        return subtotal;
    }

    private ResultadoDescuento calcularDescuento(
            VentaDTO ventaDTO,
            Usuario cliente,
            int cantidadGarrafones,
            double subtotal,
            double impuestos) {
        if (ventaDTO.getPromocionId() != null) {
            if (cliente == null) {
                throw new ResponseStatusException(
                        HttpStatus.BAD_REQUEST,
                        "Selecciona un cliente para validar la promoción.");
            }
            BigDecimal descuento = promocionService.calcularDescuento(
                    ventaDTO.getPromocionId(),
                    cliente.getId(),
                    cantidadGarrafones,
                    BigDecimal.valueOf(subtotal));
            return new ResultadoDescuento(descuento, 0, null, cliente);
        }
        if (ventaDTO.getPuntosCanjeados() == 0) {
            return new ResultadoDescuento(BigDecimal.ZERO, 0, null, cliente);
        }
        if (ventaDTO.getClienteId() == null) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Selecciona un cliente para validar el canje de puntos.");
        }
        PuntosService.CanjeAplicado canje = puntosService.aplicarCanje(
                ventaDTO.getClienteId(),
                ventaDTO.getPuntosCanjeados(),
                BigDecimal.valueOf(subtotal).add(BigDecimal.valueOf(impuestos)));
        return new ResultadoDescuento(
                canje.descuento(),
                canje.puntosCanjeados(),
                canje.saldoPuntosRestante(),
                canje.cliente());
    }

    private record ResultadoDescuento(
            BigDecimal importe,
            int puntosCanjeados,
            Integer saldoPuntosRestante,
            Usuario cliente) {
    }

    private void descontarProductos(
            List<VentaDetalleDTO> detalles,
            List<Producto> productos) {
        for (int indice = 0; indice < detalles.size(); indice++) {
            descontarPorLotes(productos.get(indice), detalles.get(indice).getCantidad());
        }
    }

    private void calcularCambio(VentaDTO ventaDTO, Venta venta, double total) {
        if (!"efectivo".equals(ventaDTO.getMetodoPago())
                || ventaDTO.getMontoEfectivo() == null) {
            return;
        }
        double cambio = ventaDTO.getMontoEfectivo() - total;
        if (cambio < 0) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST, "El monto en efectivo es insuficiente.");
        }
        venta.setCambio(cambio);
    }

    private void validarStock(Producto producto, Integer cantidadVendida) {
        for (ProductoInsumo pi : producto.getInsumos()) {
            Inventario inv = pi.getInsumo();
            double necesario = pi.getCantidad() * cantidadVendida;
            if (inv.getCantidad() < necesario) {
                throw new ResponseStatusException(HttpStatus.CONFLICT,
                    "Stock insuficiente para: " + inv.getNombre()
                    + " | Disponible: " + inv.getCantidad()
                    + " | Necesario: " + necesario + " " + inv.getUnidadMedida());
            }
        }
    }

    private void descontarPorLotes(Producto producto, Integer cantidadVendida) {
        for (ProductoInsumo pi : producto.getInsumos()) {
            Inventario inv = pi.getInsumo();
            double aDescontar = pi.getCantidad() * cantidadVendida;

            // Actualizar total en inventario
            inv.setCantidad(inv.getCantidad() - aDescontar);
            inventarioRepository.save(inv);

            // Descontar de lotes por caducidad más próxima, si mismo vencimiento el más antiguo
            List<Lote> lotes = loteRepository.findLotesDisponiblesByNombreYUnidad(
                inv.getNombre(), inv.getUnidadMedida());

            for (Lote lote : lotes) {
                if (aDescontar <= 0) break;
                double disponibleEnLote = lote.getCantidad();
                if (disponibleEnLote >= aDescontar) {
                    lote.setCantidad(disponibleEnLote - aDescontar);
                    aDescontar = 0;
                } else {
                    aDescontar -= disponibleEnLote;
                    lote.setCantidad(0.0);
                }
                loteRepository.save(lote);
            }
        }
    }

    @Transactional(readOnly = true)
    public List<VentaDTO> obtenerTodasLasVentas() {
        return ventaRepository.findAll().stream().map(this::convertToDTO).toList();
    }

    @Transactional(readOnly = true)
    public VentaDTO obtenerVentaPorId(Long id) {
        return convertToDTO(ventaRepository.findById(id)
            .orElseThrow(() -> new ResponseStatusException(
                    HttpStatus.NOT_FOUND, "Venta no encontrada con ID: " + id)));
    }

    @Transactional(readOnly = true)
    public VentaDTO obtenerVentaPorFolio(String folio) {
        Venta venta = ventaRepository.findByFolio(folio);
        if (venta == null) {
            throw new ResponseStatusException(
                    HttpStatus.NOT_FOUND, "Venta no encontrada con folio: " + folio);
        }
        return convertToDTO(venta);
    }

    @Transactional(readOnly = true)
    public List<VentaDTO> obtenerVentasPorUsuario(Long usuarioId) {
        return ventaRepository.findByUsuarioId(usuarioId).stream()
            .map(this::convertToDTO).toList();
    }

    @Transactional(readOnly = true)
    public List<VentaDTO> obtenerVentasPorRangoFechas(LocalDateTime inicio, LocalDateTime fin) {
        return ventaRepository.findByFechaBetween(inicio, fin).stream()
            .map(this::convertToDTO).toList();
    }

    @Transactional(readOnly = true)
    public List<VentaDTO> obtenerVentasPorMetodoPago(String metodoPago) {
        return ventaRepository.findByMetodoPago(metodoPago).stream()
            .map(this::convertToDTO).toList();
    }

    @Transactional(readOnly = true)
    public List<VentaDTO> obtenerPedidosPendientes() {
    return ventaRepository
        .findByEstadoPedidoOrderByFechaAsc("PENDIENTE")
        .stream()
        .map(this::convertToDTO)
        .toList();
}

@Transactional
public VentaDTO marcarPedidoEntregado(Long id) {

    Venta venta = ventaRepository.findById(id)
        .orElseThrow(() ->
        new ResponseStatusException(
                HttpStatus.NOT_FOUND, "Pedido no encontrado con ID: " + id)
        );

    venta.setEstadoPedido("ENTREGADA");

    return convertToDTO(ventaRepository.save(venta));
}

    @Transactional
    public void cancelarVenta(Long id) {
        Venta venta = ventaRepository.findById(id)
            .orElseThrow(() -> new ResponseStatusException(
                    HttpStatus.NOT_FOUND, "Venta no encontrada con ID: " + id));
        ventaRepository.delete(venta);
    }
}
