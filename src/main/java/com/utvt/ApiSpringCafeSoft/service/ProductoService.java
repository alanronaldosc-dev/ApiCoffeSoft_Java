package com.utvt.ApiSpringCafeSoft.service;

import com.utvt.ApiSpringCafeSoft.dto.ProductoDTO;
import com.utvt.ApiSpringCafeSoft.dto.ProductoInsumoDTO;
import com.utvt.ApiSpringCafeSoft.model.*;
import com.utvt.ApiSpringCafeSoft.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class ProductoService {

    @Autowired private ProductoRepository productoRepository;
    @Autowired private ProductoInsumoRepository productoInsumoRepository;
    @Autowired private InventarioRepository inventarioRepository;
    @Autowired private CategoriaRepository categoriaRepository;
    @Autowired private LoteRepository loteRepository;
    @Autowired private CargaRepository cargaRepository;
    @Autowired private VentaDetalleRepository ventaDetalleRepository;
    @Autowired private SucursalRepository sucursalRepository;

    private ProductoDTO convertToDTO(Producto producto) {
        String imagenBase64 = null;
        if (producto.getImagen() != null) {
            imagenBase64 = java.util.Base64.getEncoder().encodeToString(producto.getImagen());
        }
        ProductoDTO dto = new ProductoDTO(
            producto.getId(),
            producto.getNombre(),
            producto.getPrecio(),
            producto.getDescripcion(),
            imagenBase64
        );
        List<ProductoInsumoDTO> insumosDTO = producto.getInsumos().stream()
            .map(this::convertInsumoToDTO)
            .collect(Collectors.toList());
        dto.setInsumos(insumosDTO);
        if (producto.getCategoria() != null) {
            dto.setCategoriaId(producto.getCategoria().getId());
            dto.setCategoriaNombre(producto.getCategoria().getNombre());
        }
        // ── Sucursal ──
        if (producto.getSucursal() != null) {
            dto.setSucursalId(producto.getSucursal().getId());
            dto.setSucursalNombre(producto.getSucursal().getNombre());
        }
        return dto;
    }

    private ProductoInsumoDTO convertInsumoToDTO(ProductoInsumo productoInsumo) {
        ProductoInsumoDTO dto = new ProductoInsumoDTO(
            productoInsumo.getInsumo().getId(),
            productoInsumo.getCantidad(),
            productoInsumo.getUnidadMedida()
        );
        dto.setId(productoInsumo.getId());
        dto.setInsumoNombre(productoInsumo.getInsumo().getNombre());
        return dto;
    }

    private Producto convertToEntity(ProductoDTO dto) {
        Producto producto = new Producto();
        producto.setId(dto.getId());
        producto.setNombre(dto.getNombre());
        producto.setPrecio(dto.getPrecio());
        producto.setDescripcion(dto.getDescripcion());
        if (dto.getImagen() != null && !dto.getImagen().isEmpty()) {
            producto.setImagen(java.util.Base64.getDecoder().decode(dto.getImagen()));
        }
        return producto;
    }

    @Transactional
    public ProductoDTO crearProducto(ProductoDTO productoDTO) {
        Producto producto = convertToEntity(productoDTO);

        // ── Asignar sucursal si viene en el DTO ──
        if (productoDTO.getSucursalId() != null) {
            Sucursal sucursal = sucursalRepository.findById(productoDTO.getSucursalId())
                .orElseThrow(() -> new RuntimeException("Sucursal no encontrada con ID: " + productoDTO.getSucursalId()));
            producto.setSucursal(sucursal);
        }

        Producto savedProducto = productoRepository.save(producto);

        if (productoDTO.getInsumos() != null && !productoDTO.getInsumos().isEmpty()) {
            for (ProductoInsumoDTO insumoDTO : productoDTO.getInsumos()) {
                ProductoInsumo productoInsumo = createProductoInsumo(insumoDTO, savedProducto);
                savedProducto.addInsumo(productoInsumo);
            }
            savedProducto = productoRepository.save(savedProducto);
        }

        if (productoDTO.getCategoriaId() != null) {
            Categoria categoria = categoriaRepository.findById(productoDTO.getCategoriaId())
                .orElseThrow(() -> new RuntimeException("Categoría no encontrada con ID: " + productoDTO.getCategoriaId()));
            savedProducto.setCategoria(categoria);
            savedProducto = productoRepository.save(savedProducto);
        }

        return convertToDTO(savedProducto);
    }

    private ProductoInsumo createProductoInsumo(ProductoInsumoDTO dto, Producto producto) {
        Inventario insumo = inventarioRepository.findById(dto.getInsumoId())
            .orElseThrow(() -> new RuntimeException("Insumo no encontrado con ID: " + dto.getInsumoId()));
        ProductoInsumo productoInsumo = new ProductoInsumo();
        productoInsumo.setProducto(producto);
        productoInsumo.setInsumo(insumo);
        productoInsumo.setCantidad(dto.getCantidad());
        productoInsumo.setUnidadMedida(dto.getUnidadMedida());
        return productoInsumo;
    }

    public List<ProductoDTO> obtenerTodosLosProductos() {
        return productoRepository.findAllWithInsumos().stream()
            .map(this::convertToDTO).collect(Collectors.toList());
    }

    public ProductoDTO obtenerProductoPorId(Long id) {
        Producto producto = productoRepository.findByIdWithInsumos(id);
        if (producto == null) throw new RuntimeException("Producto no encontrado con ID: " + id);
        return convertToDTO(producto);
    }

    @Transactional
    public ProductoDTO actualizarProducto(Long id, ProductoDTO productoDTO) {
        Producto existingProducto = productoRepository.findByIdWithInsumos(id);
        if (existingProducto == null) throw new RuntimeException("Producto no encontrado con ID: " + id);
        existingProducto.setNombre(productoDTO.getNombre());
        existingProducto.setPrecio(productoDTO.getPrecio());
        existingProducto.setDescripcion(productoDTO.getDescripcion());
        if (productoDTO.getImagen() != null && !productoDTO.getImagen().isEmpty()) {
            existingProducto.setImagen(java.util.Base64.getDecoder().decode(productoDTO.getImagen()));
        } else {
            existingProducto.setImagen(null);
        }
        productoInsumoRepository.deleteByProductoId(id);
        existingProducto.getInsumos().clear();
        if (productoDTO.getInsumos() != null && !productoDTO.getInsumos().isEmpty()) {
            for (ProductoInsumoDTO insumoDTO : productoDTO.getInsumos()) {
                ProductoInsumo productoInsumo = createProductoInsumo(insumoDTO, existingProducto);
                existingProducto.addInsumo(productoInsumo);
            }
        }
        if (productoDTO.getCategoriaId() != null) {
            Categoria categoria = categoriaRepository.findById(productoDTO.getCategoriaId())
                .orElseThrow(() -> new RuntimeException("Categoría no encontrada con ID: " + productoDTO.getCategoriaId()));
            existingProducto.setCategoria(categoria);
        } else {
            existingProducto.setCategoria(null);
        }
        return convertToDTO(productoRepository.save(existingProducto));
    }

    @Transactional
    public void eliminarProducto(Long id) {
        if (!productoRepository.existsById(id)) throw new RuntimeException("Producto no encontrado con ID: " + id);
        ventaDetalleRepository.deleteByProductoId(id);
        productoInsumoRepository.deleteByProductoId(id);
        loteRepository.deleteByProductoId(id);
        inventarioRepository.findByProductoId(id).ifPresent(inv -> {
            cargaRepository.deleteByInventarioId(inv.getId());
            inventarioRepository.delete(inv);
        });
        productoRepository.deleteById(id);
    }

    public List<ProductoDTO> buscarPorNombre(String nombre) {
        return productoRepository.findByNombreContainingIgnoreCase(nombre).stream()
            .map(this::convertToDTO).collect(Collectors.toList());
    }

    public List<ProductoDTO> buscarPorRangoPrecio(Double precioMin, Double precioMax) {
        return productoRepository.findByPrecioBetween(precioMin, precioMax).stream()
            .map(this::convertToDTO).collect(Collectors.toList());
    }

    public List<ProductoDTO> buscarPorInsumo(Long insumoId) {
        return productoRepository.findProductosByInsumoId(insumoId).stream()
            .map(this::convertToDTO).collect(Collectors.toList());
    }

    public List<ProductoDTO> obtenerPorCategoria(Long categoriaId) {
        return productoRepository.findByCategoriaId(categoriaId).stream()
            .map(this::convertToDTO).collect(Collectors.toList());
    }

    // ── Filtrar por sucursal ──
    public List<ProductoDTO> obtenerPorSucursal(Long sucursalId) {
        return productoRepository.findBySucursalId(sucursalId).stream()
            .map(this::convertToDTO).collect(Collectors.toList());
    }
}
