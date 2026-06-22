package com.utvt.ApiSpringCafeSoft.service;

import com.utvt.ApiSpringCafeSoft.dto.ProductoDTO;
import com.utvt.ApiSpringCafeSoft.dto.ProductoInsumoDTO;
import com.utvt.ApiSpringCafeSoft.model.Inventario;
import com.utvt.ApiSpringCafeSoft.model.Producto;
import com.utvt.ApiSpringCafeSoft.model.ProductoInsumo;
import com.utvt.ApiSpringCafeSoft.repository.InventarioRepository;
import com.utvt.ApiSpringCafeSoft.repository.ProductoInsumoRepository;
import com.utvt.ApiSpringCafeSoft.repository.ProductoRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class ProductoService {

    @Autowired
    private ProductoRepository productoRepository;

    @Autowired
    private ProductoInsumoRepository productoInsumoRepository;

    @Autowired
    private InventarioRepository inventarioRepository;

    // Convertir Entity a DTO
    private ProductoDTO convertToDTO(Producto producto) {
        ProductoDTO dto = new ProductoDTO(
            producto.getId(),
            producto.getNombre(),
            producto.getPrecio(),
            producto.getDescripcion(),
            producto.getImagen()
        );

        List<ProductoInsumoDTO> insumosDTO = producto.getInsumos().stream()
            .map(this::convertInsumoToDTO)
            .collect(Collectors.toList());
        dto.setInsumos(insumosDTO);

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

    // Convertir DTO a Entity
    private Producto convertToEntity(ProductoDTO dto) {
        Producto producto = new Producto();
        producto.setId(dto.getId());
        producto.setNombre(dto.getNombre());
        producto.setPrecio(dto.getPrecio());
        producto.setDescripcion(dto.getDescripcion());
        producto.setImagen(dto.getImagen());
        return producto;
    }

    // 1. Crear producto
    @Transactional
    public ProductoDTO crearProducto(ProductoDTO productoDTO) {
        Producto producto = convertToEntity(productoDTO);
        
        // Guardar producto primero para obtener ID
        Producto savedProducto = productoRepository.save(producto);
        
        // Procesar insumos
        if (productoDTO.getInsumos() != null && !productoDTO.getInsumos().isEmpty()) {
            for (ProductoInsumoDTO insumoDTO : productoDTO.getInsumos()) {
                ProductoInsumo productoInsumo = createProductoInsumo(insumoDTO, savedProducto);
                savedProducto.addInsumo(productoInsumo);
            }
            // Guardar con los insumos
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

    // 2. Obtener todos los productos
    public List<ProductoDTO> obtenerTodosLosProductos() {
        return productoRepository.findAllWithInsumos().stream()
            .map(this::convertToDTO)
            .collect(Collectors.toList());
    }

    // 3. Obtener producto por ID
    public ProductoDTO obtenerProductoPorId(Long id) {
        Producto producto = productoRepository.findByIdWithInsumos(id);
        if (producto == null) {
            throw new RuntimeException("Producto no encontrado con ID: " + id);
        }
        return convertToDTO(producto);
    }

    // 4. Actualizar producto
    @Transactional
    public ProductoDTO actualizarProducto(Long id, ProductoDTO productoDTO) {
        Producto existingProducto = productoRepository.findByIdWithInsumos(id);
        if (existingProducto == null) {
            throw new RuntimeException("Producto no encontrado con ID: " + id);
        }

        // Actualizar campos básicos
        existingProducto.setNombre(productoDTO.getNombre());
        existingProducto.setPrecio(productoDTO.getPrecio());
        existingProducto.setDescripcion(productoDTO.getDescripcion());
        existingProducto.setImagen(productoDTO.getImagen());

        // Eliminar insumos existentes
        productoInsumoRepository.deleteByProductoId(id);
        existingProducto.getInsumos().clear();

        // Agregar nuevos insumos
        if (productoDTO.getInsumos() != null && !productoDTO.getInsumos().isEmpty()) {
            for (ProductoInsumoDTO insumoDTO : productoDTO.getInsumos()) {
                ProductoInsumo productoInsumo = createProductoInsumo(insumoDTO, existingProducto);
                existingProducto.addInsumo(productoInsumo);
            }
        }

        Producto updatedProducto = productoRepository.save(existingProducto);
        return convertToDTO(updatedProducto);
    }

    // 5. Eliminar producto
    @Transactional
    public void eliminarProducto(Long id) {
        if (!productoRepository.existsById(id)) {
            throw new RuntimeException("Producto no encontrado con ID: " + id);
        }
        productoInsumoRepository.deleteByProductoId(id);
        productoRepository.deleteById(id);
    }

    // 6. Métodos adicionales de consulta
    public List<ProductoDTO> buscarPorNombre(String nombre) {
        return productoRepository.findByNombreContainingIgnoreCase(nombre).stream()
            .map(this::convertToDTO)
            .collect(Collectors.toList());
    }

    public List<ProductoDTO> buscarPorRangoPrecio(Double precioMin, Double precioMax) {
        return productoRepository.findByPrecioBetween(precioMin, precioMax).stream()
            .map(this::convertToDTO)
            .collect(Collectors.toList());
    }

    public List<ProductoDTO> buscarPorInsumo(Long insumoId) {
        return productoRepository.findProductosByInsumoId(insumoId).stream()
            .map(this::convertToDTO)
            .collect(Collectors.toList());
    }
}