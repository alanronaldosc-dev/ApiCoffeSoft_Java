package com.utvt.ApiSpringCafeSoft.service;

import com.utvt.ApiSpringCafeSoft.dto.LoteDTO;
import com.utvt.ApiSpringCafeSoft.model.Insumo;
import com.utvt.ApiSpringCafeSoft.model.Inventario;
import com.utvt.ApiSpringCafeSoft.model.Lote;
import com.utvt.ApiSpringCafeSoft.model.Producto;
import com.utvt.ApiSpringCafeSoft.model.ProductoInsumo;
import com.utvt.ApiSpringCafeSoft.model.Proveedor;
import com.utvt.ApiSpringCafeSoft.repository.InsumoRepository;
import com.utvt.ApiSpringCafeSoft.repository.InventarioRepository;
import com.utvt.ApiSpringCafeSoft.repository.LoteRepository;
import com.utvt.ApiSpringCafeSoft.repository.ProductoRepository;
import com.utvt.ApiSpringCafeSoft.repository.ProveedorRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class LoteService {

    @Autowired private LoteRepository loteRepository;
    @Autowired private InsumoRepository insumoRepository;
    @Autowired private InventarioRepository inventarioRepository;
    @Autowired private ProveedorRepository proveedorRepository;
    @Autowired private ProductoRepository productoRepository;

    private LoteDTO convertToDTO(Lote lote) {
        Long proveedorId = null;
        String proveedorNombre = null;
        if (lote.getProveedor() != null) {
            proveedorId = lote.getProveedor().getId();
            proveedorNombre = lote.getProveedor().getNombreEmpresa();
        } else if (lote.getInsumo() != null && lote.getInsumo().getProveedor() != null) {
            proveedorId = lote.getInsumo().getProveedor().getId();
            proveedorNombre = lote.getInsumo().getProveedor().getNombreEmpresa();
        }

        Long insumoId = lote.getInsumo() != null ? lote.getInsumo().getId() : null;
        String insumoNombre = lote.getInsumo() != null ? lote.getInsumo().getNombre() : null;
        String insumoUnidad = lote.getInsumo() != null ? lote.getInsumo().getUnidadMedida() : null;

        // Proteger contra lotes viejos que no tienen tipoLote
        String tipoLote = lote.getTipoLote() != null ? lote.getTipoLote() : "insumo";

        return new LoteDTO(
            lote.getId(),
            insumoId,
            insumoNombre,
            insumoUnidad,
            proveedorId,
            proveedorNombre,
            lote.getCantidad(),
            lote.getFechaCaducidad(),
            lote.getFechaEntrada(),
            lote.getObservaciones(),
            lote.getProductoId(),
            lote.getProductoNombre(),
            tipoLote
        );
    }

    private Proveedor resolverProveedor(Long proveedorId, Insumo insumo) {
        if (proveedorId != null) {
            return proveedorRepository.findById(proveedorId)
                .orElseThrow(() -> new RuntimeException("Proveedor no encontrado con ID: " + proveedorId));
        }
        return insumo.getProveedor();
    }

    @Transactional
    public LoteDTO registrarLote(LoteDTO dto) {
        Insumo insumo = insumoRepository.findById(dto.getInsumoId())
            .orElseThrow(() -> new RuntimeException("Insumo no encontrado con ID: " + dto.getInsumoId()));

        Proveedor proveedor = resolverProveedor(dto.getProveedorId(), insumo);
        Lote lote = new Lote(insumo, proveedor, dto.getCantidad(), dto.getFechaCaducidad(), dto.getObservaciones());
        loteRepository.save(lote);

        String nombreProveedor = proveedor != null ? proveedor.getNombreEmpresa() : null;

        List<Inventario> existentes = inventarioRepository.findByNombreContainingIgnoreCase(insumo.getNombre());
        Inventario inventario = existentes.stream()
            .filter(i -> i.getUnidadMedida().equals(insumo.getUnidadMedida()))
            .findFirst().orElse(null);

        if (inventario != null) {
            inventario.setCantidad(inventario.getCantidad() + dto.getCantidad());
            if (nombreProveedor != null) inventario.setProveedor(nombreProveedor);
            inventarioRepository.save(inventario);
        } else {
            inventarioRepository.save(new Inventario(
                insumo.getNombre(), insumo.getTipo(), dto.getCantidad(),
                insumo.getUnidadMedida(), 0.0, dto.getFechaCaducidad().toString(),
                nombreProveedor, insumo.getPrecio()
            ));
        }
        return convertToDTO(lote);
    }

    /**
     * Produce unidades de un producto:
     * 1. Verifica que hay suficientes insumos en inventario
     * 2. Descuenta insumos del inventario (receta × cantidad producida)
     * 3. Suma al inventario de productos
     * 4. Registra un lote de producción
     */
    @Transactional
    public LoteDTO producirProducto(Long productoId, Double cantidadProducida, String fechaCaducidadStr, String observaciones) {
        Producto producto = productoRepository.findByIdWithInsumos(productoId);
        if (producto == null) {
            throw new RuntimeException("Producto no encontrado con ID: " + productoId);
        }

        // 1. Verificar y descontar insumos
        for (ProductoInsumo pi : producto.getInsumos()) {
            Inventario invInsumo = pi.getInsumo();
            double necesario = pi.getCantidad() * cantidadProducida;

            if (invInsumo.getCantidad() < necesario) {
                throw new RuntimeException(
                    "Stock insuficiente de '" + invInsumo.getNombre() +
                    "'. Necesario: " + necesario +
                    ", Disponible: " + invInsumo.getCantidad()
                );
            }
            invInsumo.setCantidad(invInsumo.getCantidad() - necesario);
            inventarioRepository.save(invInsumo);
        }

        // 2. Sumar al inventario de productos
        Inventario invProducto = inventarioRepository.findByProductoId(productoId).orElse(null);
        if (invProducto != null) {
            invProducto.setCantidad(invProducto.getCantidad() + cantidadProducida);
            inventarioRepository.save(invProducto);
        } else {
            Inventario nuevo = new Inventario(
                producto.getNombre(), "producto", cantidadProducida,
                "piezas", 1.0, fechaCaducidadStr, null, producto.getPrecio()
            );
            nuevo.setProductoId(productoId);
            inventarioRepository.save(nuevo);
        }

        // 3. Registrar lote de producción
        LocalDate fechaCaducidad = fechaCaducidadStr != null && !fechaCaducidadStr.isEmpty()
            ? LocalDate.parse(fechaCaducidadStr)
            : LocalDate.now().plusMonths(1);

        Lote lote = new Lote(productoId, producto.getNombre(), cantidadProducida, fechaCaducidad, observaciones);
        loteRepository.save(lote);

        return convertToDTO(lote);
    }

    public List<LoteDTO> obtenerTodos() {
        return loteRepository.findAll().stream().map(this::convertToDTO).collect(Collectors.toList());
    }

    public List<LoteDTO> obtenerPorInsumo(Long insumoId) {
        return loteRepository.findByInsumoId(insumoId).stream().map(this::convertToDTO).collect(Collectors.toList());
    }

    public List<LoteDTO> obtenerPorProveedor(Long proveedorId) {
        return loteRepository.findByProveedorId(proveedorId).stream().map(this::convertToDTO).collect(Collectors.toList());
    }

    @Transactional
    public void eliminarLote(Long id) {
        if (!loteRepository.existsById(id))
            throw new RuntimeException("Lote no encontrado con ID: " + id);
        loteRepository.deleteById(id);
    }
}
