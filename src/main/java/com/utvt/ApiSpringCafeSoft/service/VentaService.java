package com.utvt.ApiSpringCafeSoft.service;

import com.utvt.ApiSpringCafeSoft.dto.VentaDTO;
import com.utvt.ApiSpringCafeSoft.dto.VentaDetalleDTO;
import com.utvt.ApiSpringCafeSoft.model.*;
import com.utvt.ApiSpringCafeSoft.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class VentaService {

    @Autowired
    private VentaRepository ventaRepository;

    @Autowired
    private VentaDetalleRepository ventaDetalleRepository;

    @Autowired
    private ProductoRepository productoRepository;

    @Autowired
    private InventarioRepository inventarioRepository;

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private ProductoInsumoRepository productoInsumoRepository;

    // Convertir Entity a DTO
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
        dto.setUsuarioId(venta.getUsuario().getId());
        dto.setUsuarioNombre(venta.getUsuario().getNombre());
        dto.setObservaciones(venta.getObservaciones());
        dto.setCreatedAt(venta.getCreatedAt());

        List<VentaDetalleDTO> detallesDTO = venta.getDetalles().stream()
            .map(this::convertDetalleToDTO)
            .collect(Collectors.toList());
        dto.setDetalles(detallesDTO);

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

    // Generar folio automático
    private String generarFolio() {
        String lastFolio = ventaRepository.findLastFolio();
        if (lastFolio == null) {
            return "V-0001";
        }
        String numeroStr = lastFolio.substring(2);
        int numero = Integer.parseInt(numeroStr);
        numero++;
        return String.format("V-%04d", numero);
    }

    // 1. Crear venta (con descuento automático de inventario)
    @Transactional
    public VentaDTO crearVenta(VentaDTO ventaDTO) {
        // Validar que el usuario existe
        Usuario usuario = usuarioRepository.findById(ventaDTO.getUsuarioId())
            .orElseThrow(() -> new RuntimeException("Usuario no encontrado con ID: " + ventaDTO.getUsuarioId()));

        // Crear venta
        Venta venta = new Venta();
        venta.setFolio(generarFolio());
        venta.setFecha(LocalDateTime.now());
        venta.setMetodoPago(ventaDTO.getMetodoPago());
        venta.setUsuario(usuario);
        venta.setObservaciones(ventaDTO.getObservaciones());
        venta.setCreatedAt(LocalDateTime.now());

        double subtotal = 0.0;

        // Procesar cada detalle
        for (VentaDetalleDTO detalleDTO : ventaDTO.getDetalles()) {
            Producto producto = productoRepository.findById(detalleDTO.getProductoId())
                .orElseThrow(() -> new RuntimeException("Producto no encontrado con ID: " + detalleDTO.getProductoId()));

            // Validar stock de insumos para este producto
            validarStockInsumos(producto, detalleDTO.getCantidad());

            // Descontar insumos del inventario
            descontarInsumos(producto, detalleDTO.getCantidad());

            // Usar precio del producto si no se envía
            Double precioUnitario = detalleDTO.getPrecioUnitario() != null ? 
                detalleDTO.getPrecioUnitario() : producto.getPrecio();

            Double subtotalDetalle = precioUnitario * detalleDTO.getCantidad();
            subtotal += subtotalDetalle;

            // Crear detalle
            VentaDetalle detalle = new VentaDetalle();
            detalle.setProducto(producto);
            detalle.setCantidad(detalleDTO.getCantidad());
            detalle.setPrecioUnitario(precioUnitario);
            detalle.setSubtotal(subtotalDetalle);
            venta.addDetalle(detalle);
        }

        // Calcular impuestos (16%)
        double impuestos = subtotal * 0.16;
        
        // Aplicar descuento (si existe)
        double descuento = ventaDTO.getDescuento() != null ? ventaDTO.getDescuento() : 0.0;
        
        // Calcular total
        double total = subtotal + impuestos - descuento;

        venta.setSubtotal(subtotal);
        venta.setImpuestos(impuestos);
        venta.setDescuento(descuento);
        venta.setTotal(total);

        Venta savedVenta = ventaRepository.save(venta);
        return convertToDTO(savedVenta);
    }

    // Validar stock de insumos
    private void validarStockInsumos(Producto producto, Integer cantidad) {
        List<ProductoInsumo> insumos = producto.getInsumos();
        
        for (ProductoInsumo productoInsumo : insumos) {
            Inventario insumo = productoInsumo.getInsumo();
            Double cantidadNecesaria = productoInsumo.getCantidad() * cantidad;
            
            if (insumo.getCantidad() < cantidadNecesaria) {
                throw new RuntimeException(
                    "❌ Stock insuficiente para el insumo: " + insumo.getNombre() + 
                    "\n📦 Disponible: " + insumo.getCantidad() + " " + insumo.getUnidadMedida() +
                    "\n📋 Necesario: " + cantidadNecesaria + " " + insumo.getUnidadMedida()
                );
            }
        }
    }

    // Descontar insumos del inventario (regla de negocio)
    private void descontarInsumos(Producto producto, Integer cantidad) {
        List<ProductoInsumo> insumos = producto.getInsumos();
        
        for (ProductoInsumo productoInsumo : insumos) {
            Inventario insumo = productoInsumo.getInsumo();
            Double cantidadADescontar = productoInsumo.getCantidad() * cantidad;
            
            Double nuevaCantidad = insumo.getCantidad() - cantidadADescontar;
            insumo.setCantidad(nuevaCantidad);
            inventarioRepository.save(insumo);
            
            System.out.println("✅ Descontado: " + cantidadADescontar + " " + insumo.getUnidadMedida() + 
                               " de " + insumo.getNombre() + 
                               " - Nuevo stock: " + nuevaCantidad);
        }
    }

    // 2. Obtener todas las ventas
    public List<VentaDTO> obtenerTodasLasVentas() {
        return ventaRepository.findAll().stream()
            .map(this::convertToDTO)
            .collect(Collectors.toList());
    }

    // 3. Obtener venta por ID
    public VentaDTO obtenerVentaPorId(Long id) {
        Venta venta = ventaRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Venta no encontrada con ID: " + id));
        return convertToDTO(venta);
    }

    // 4. Obtener venta por folio
    public VentaDTO obtenerVentaPorFolio(String folio) {
        Venta venta = ventaRepository.findByFolio(folio);
        if (venta == null) {
            throw new RuntimeException("Venta no encontrada con folio: " + folio);
        }
        return convertToDTO(venta);
    }

    // 5. Obtener ventas por usuario
    public List<VentaDTO> obtenerVentasPorUsuario(Long usuarioId) {
        return ventaRepository.findByUsuarioId(usuarioId).stream()
            .map(this::convertToDTO)
            .collect(Collectors.toList());
    }

    // 6. Obtener ventas por rango de fechas
    public List<VentaDTO> obtenerVentasPorRangoFechas(LocalDateTime inicio, LocalDateTime fin) {
        return ventaRepository.findByFechaBetween(inicio, fin).stream()
            .map(this::convertToDTO)
            .collect(Collectors.toList());
    }

    // 7. Obtener ventas por método de pago
    public List<VentaDTO> obtenerVentasPorMetodoPago(String metodoPago) {
        return ventaRepository.findByMetodoPago(metodoPago).stream()
            .map(this::convertToDTO)
            .collect(Collectors.toList());
    }

    // 8. Cancelar venta (reponer inventario)
    @Transactional
    public void cancelarVenta(Long id) {
        Venta venta = ventaRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Venta no encontrada con ID: " + id));

        // Reponer insumos
        for (VentaDetalle detalle : venta.getDetalles()) {
            Producto producto = detalle.getProducto();
            Integer cantidad = detalle.getCantidad();
            
            List<ProductoInsumo> insumos = producto.getInsumos();
            for (ProductoInsumo productoInsumo : insumos) {
                Inventario insumo = productoInsumo.getInsumo();
                Double cantidadAReponer = productoInsumo.getCantidad() * cantidad;
                
                Double nuevaCantidad = insumo.getCantidad() + cantidadAReponer;
                insumo.setCantidad(nuevaCantidad);
                inventarioRepository.save(insumo);
                
                System.out.println("🔄 Repuesto: " + cantidadAReponer + " " + insumo.getUnidadMedida() + 
                                   " de " + insumo.getNombre() + 
                                   " - Nuevo stock: " + nuevaCantidad);
            }
        }

        ventaRepository.deleteById(id);
    }
}