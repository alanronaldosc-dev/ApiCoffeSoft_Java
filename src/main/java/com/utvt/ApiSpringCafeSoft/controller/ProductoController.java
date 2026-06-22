package com.utvt.ApiSpringCafeSoft.controller;

import com.utvt.ApiSpringCafeSoft.dto.ProductoDTO;
import com.utvt.ApiSpringCafeSoft.service.ProductoService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/productos")
@Tag(name = "Productos", description = "API para la gestión de productos")
public class ProductoController {

    @Autowired
    private ProductoService productoService;

    // ==================== CRUD BÁSICO ====================

    @Operation(summary = "Crear un nuevo producto", description = "Registra un nuevo producto con sus insumos asociados")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "201", description = "Producto creado exitosamente"),
        @ApiResponse(responseCode = "400", description = "Datos inválidos"),
        @ApiResponse(responseCode = "404", description = "Insumo no encontrado"),
        @ApiResponse(responseCode = "500", description = "Error interno del servidor")
    })
    @PostMapping
    public ResponseEntity<ProductoDTO> crearProducto(@Valid @RequestBody ProductoDTO productoDTO) {
        ProductoDTO nuevoProducto = productoService.crearProducto(productoDTO);
        return new ResponseEntity<>(nuevoProducto, HttpStatus.CREATED);
    }

    @Operation(summary = "Obtener todos los productos", description = "Lista todos los productos registrados con sus insumos")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Lista de productos obtenida exitosamente"),
        @ApiResponse(responseCode = "500", description = "Error interno del servidor")
    })
    @GetMapping
    public ResponseEntity<List<ProductoDTO>> obtenerTodosLosProductos() {
        List<ProductoDTO> productos = productoService.obtenerTodosLosProductos();
        return ResponseEntity.ok(productos);
    }

    @Operation(summary = "Obtener un producto por ID", description = "Busca un producto específico por su ID con sus insumos")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Producto encontrado"),
        @ApiResponse(responseCode = "404", description = "Producto no encontrado"),
        @ApiResponse(responseCode = "500", description = "Error interno del servidor")
    })
    @GetMapping("/{id}")
    public ResponseEntity<ProductoDTO> obtenerProductoPorId(
            @Parameter(description = "ID del producto", required = true, example = "1")
            @PathVariable Long id) {
        ProductoDTO producto = productoService.obtenerProductoPorId(id);
        return ResponseEntity.ok(producto);
    }

    @Operation(summary = "Actualizar un producto", description = "Actualiza los datos de un producto existente y sus insumos")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Producto actualizado exitosamente"),
        @ApiResponse(responseCode = "400", description = "Datos inválidos"),
        @ApiResponse(responseCode = "404", description = "Producto o insumo no encontrado"),
        @ApiResponse(responseCode = "500", description = "Error interno del servidor")
    })
    @PutMapping("/{id}")
    public ResponseEntity<ProductoDTO> actualizarProducto(
            @Parameter(description = "ID del producto a actualizar", required = true, example = "1")
            @PathVariable Long id,
            @Valid @RequestBody ProductoDTO productoDTO) {
        ProductoDTO productoActualizado = productoService.actualizarProducto(id, productoDTO);
        return ResponseEntity.ok(productoActualizado);
    }

    @Operation(summary = "Eliminar un producto", description = "Elimina un producto del sistema por su ID")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "204", description = "Producto eliminado exitosamente"),
        @ApiResponse(responseCode = "404", description = "Producto no encontrado"),
        @ApiResponse(responseCode = "500", description = "Error interno del servidor")
    })
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminarProducto(
            @Parameter(description = "ID del producto a eliminar", required = true, example = "1")
            @PathVariable Long id) {
        productoService.eliminarProducto(id);
        return ResponseEntity.noContent().build();
    }

    // ==================== MÉTODOS ADICIONALES DE CONSULTA ====================

    @Operation(summary = "Buscar productos por nombre", description = "Busca productos que contengan el texto en su nombre")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Búsqueda exitosa"),
        @ApiResponse(responseCode = "500", description = "Error interno del servidor")
    })
    @GetMapping("/buscar/nombre")
    public ResponseEntity<List<ProductoDTO>> buscarPorNombre(
            @Parameter(description = "Texto a buscar en el nombre", required = true, example = "café")
            @RequestParam String nombre) {
        List<ProductoDTO> productos = productoService.buscarPorNombre(nombre);
        return ResponseEntity.ok(productos);
    }

    @Operation(summary = "Buscar productos por rango de precio", description = "Busca productos cuyo precio está dentro del rango especificado")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Búsqueda exitosa"),
        @ApiResponse(responseCode = "500", description = "Error interno del servidor")
    })
    @GetMapping("/buscar/precio")
    public ResponseEntity<List<ProductoDTO>> buscarPorRangoPrecio(
            @Parameter(description = "Precio mínimo", required = true, example = "10.0")
            @RequestParam Double precioMin,
            @Parameter(description = "Precio máximo", required = true, example = "200.0")
            @RequestParam Double precioMax) {
        List<ProductoDTO> productos = productoService.buscarPorRangoPrecio(precioMin, precioMax);
        return ResponseEntity.ok(productos);
    }

    @Operation(summary = "Buscar productos por insumo", description = "Busca productos que utilizan un insumo específico")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Búsqueda exitosa"),
        @ApiResponse(responseCode = "500", description = "Error interno del servidor")
    })
    @GetMapping("/buscar/insumo/{insumoId}")
    public ResponseEntity<List<ProductoDTO>> buscarPorInsumo(
            @Parameter(description = "ID del insumo", required = true, example = "1")
            @PathVariable Long insumoId) {
        List<ProductoDTO> productos = productoService.buscarPorInsumo(insumoId);
        return ResponseEntity.ok(productos);
    }
}