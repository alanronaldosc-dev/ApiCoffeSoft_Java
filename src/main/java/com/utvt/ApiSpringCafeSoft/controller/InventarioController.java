package com.utvt.ApiSpringCafeSoft.controller;

import com.utvt.ApiSpringCafeSoft.dto.InventarioDTO;
import com.utvt.ApiSpringCafeSoft.service.InventarioService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/inventario")
@Tag(name = "Inventario", description = "API para la gestión del inventario de insumos")
public class InventarioController {

    @Autowired
    private InventarioService inventarioService;

    // ==================== CRUD BÁSICO ====================

    @Operation(summary = "Crear un nuevo insumo", description = "Registra un nuevo insumo en el inventario")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "201", description = "Insumo creado exitosamente"),
        @ApiResponse(responseCode = "400", description = "Datos inválidos"),
        @ApiResponse(responseCode = "500", description = "Error interno del servidor")
    })
    @PostMapping
    public ResponseEntity<InventarioDTO> crearInsumo(@Valid @RequestBody InventarioDTO inventarioDTO) {
        InventarioDTO nuevoInsumo = inventarioService.crearInsumo(inventarioDTO);
        return new ResponseEntity<>(nuevoInsumo, HttpStatus.CREATED);
    }

    @Operation(summary = "Obtener todos los insumos", description = "Lista todos los insumos registrados en el inventario")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Lista de insumos obtenida exitosamente"),
        @ApiResponse(responseCode = "500", description = "Error interno del servidor")
    })
    @GetMapping
    public ResponseEntity<List<InventarioDTO>> obtenerTodosLosInsumos() {
        List<InventarioDTO> insumos = inventarioService.obtenerTodosLosInsumos();
        return ResponseEntity.ok(insumos);
    }

    @Operation(summary = "Obtener un insumo por ID", description = "Busca un insumo específico por su ID")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Insumo encontrado"),
        @ApiResponse(responseCode = "404", description = "Insumo no encontrado"),
        @ApiResponse(responseCode = "500", description = "Error interno del servidor")
    })
    @GetMapping("/{id}")
    public ResponseEntity<InventarioDTO> obtenerInsumoPorId(
            @Parameter(description = "ID del insumo", required = true, example = "1")
            @PathVariable Long id) {
        InventarioDTO insumo = inventarioService.obtenerInsumoPorId(id);
        return ResponseEntity.ok(insumo);
    }

    @Operation(summary = "Actualizar un insumo", description = "Actualiza los datos de un insumo existente")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Insumo actualizado exitosamente"),
        @ApiResponse(responseCode = "400", description = "Datos inválidos"),
        @ApiResponse(responseCode = "404", description = "Insumo no encontrado"),
        @ApiResponse(responseCode = "500", description = "Error interno del servidor")
    })
    @PutMapping("/{id}")
    public ResponseEntity<InventarioDTO> actualizarInsumo(
            @Parameter(description = "ID del insumo a actualizar", required = true, example = "1")
            @PathVariable Long id,
            @Valid @RequestBody InventarioDTO inventarioDTO) {
        InventarioDTO insumoActualizado = inventarioService.actualizarInsumo(id, inventarioDTO);
        return ResponseEntity.ok(insumoActualizado);
    }

    @Operation(summary = "Eliminar un insumo", description = "Elimina un insumo del inventario por su ID")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "204", description = "Insumo eliminado exitosamente"),
        @ApiResponse(responseCode = "404", description = "Insumo no encontrado"),
        @ApiResponse(responseCode = "500", description = "Error interno del servidor")
    })
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminarInsumo(
            @Parameter(description = "ID del insumo a eliminar", required = true, example = "1")
            @PathVariable Long id) {
        inventarioService.eliminarInsumo(id);
        return ResponseEntity.noContent().build();
    }

   // HU - Consulta de existencias actuales:
// Permite consultar el inventario, buscar existencias por nombre
// e identificar insumos con bajo stock y stock crítico. 

    // ==================== MÉTODOS ADICIONALES DE CONSULTA ====================

    @Operation(summary = "Buscar insumos por nombre", description = "Busca insumos que contengan el texto en su nombre")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Búsqueda exitosa"),
        @ApiResponse(responseCode = "500", description = "Error interno del servidor")
    })
    @GetMapping("/buscar/nombre")
    public ResponseEntity<List<InventarioDTO>> buscarPorNombre(
            @Parameter(description = "Texto a buscar en el nombre", required = true, example = "café")
            @RequestParam String nombre) {
        List<InventarioDTO> insumos = inventarioService.buscarPorNombre(nombre);
        return ResponseEntity.ok(insumos);
    }

    @Operation(summary = "Buscar insumos por tipo", description = "Busca insumos que contengan el texto en su tipo")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Búsqueda exitosa"),
        @ApiResponse(responseCode = "500", description = "Error interno del servidor")
    })
    @GetMapping("/buscar/tipo")
    public ResponseEntity<List<InventarioDTO>> buscarPorTipo(
            @Parameter(description = "Texto a buscar en el tipo", required = true, example = "grano")
            @RequestParam String tipo) {
        List<InventarioDTO> insumos = inventarioService.buscarPorTipo(tipo);
        return ResponseEntity.ok(insumos);
    }

    @Operation(summary = "Buscar insumos por proveedor", description = "Busca insumos que contengan el texto en el proveedor")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Búsqueda exitosa"),
        @ApiResponse(responseCode = "500", description = "Error interno del servidor")
    })
    @GetMapping("/buscar/proveedor")
    public ResponseEntity<List<InventarioDTO>> buscarPorProveedor(
            @Parameter(description = "Texto a buscar en el proveedor", required = true, example = "Proveedor Local")
            @RequestParam String proveedor) {
        List<InventarioDTO> insumos = inventarioService.buscarPorProveedor(proveedor);
        return ResponseEntity.ok(insumos);
    }

    @Operation(summary = "Obtener insumos con bajo stock", description = "Lista los insumos cuya cantidad es menor o igual a la cantidad mínima")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Lista de insumos con bajo stock"),
        @ApiResponse(responseCode = "500", description = "Error interno del servidor")
    })
    @GetMapping("/bajo-stock")
    public ResponseEntity<List<InventarioDTO>> obtenerInsumosBajoStock() {
        List<InventarioDTO> insumos = inventarioService.obtenerInsumosBajoStock();
        return ResponseEntity.ok(insumos);
    }

    @Operation(summary = "Obtener insumos con stock crítico", description = "Lista los insumos cuya cantidad es menor al 50% de la cantidad mínima")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Lista de insumos con stock crítico"),
        @ApiResponse(responseCode = "500", description = "Error interno del servidor")
    })
    @GetMapping("/stock-critico")
    public ResponseEntity<List<InventarioDTO>> obtenerInsumosStockCritico() {
        List<InventarioDTO> insumos = inventarioService.obtenerInsumosStockCritico();
        return ResponseEntity.ok(insumos);
    }

    @Operation(summary = "Obtener insumos por unidad de medida", description = "Lista los insumos según la unidad de medida")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Lista de insumos por unidad de medida"),
        @ApiResponse(responseCode = "500", description = "Error interno del servidor")
    })
    @GetMapping("/unidad/{unidad}")
    public ResponseEntity<List<InventarioDTO>> obtenerPorUnidadMedida(
            @Parameter(description = "Unidad de medida (litros, kilogramos, piezas)", required = true, example = "kilogramos")
            @PathVariable String unidad) {
        List<InventarioDTO> insumos = inventarioService.obtenerPorUnidadMedida(unidad);
        return ResponseEntity.ok(insumos);
    }

    @Operation(summary = "Obtener insumos que caducan antes de una fecha", description = "Lista los insumos que caducan antes de la fecha especificada")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Lista de insumos que caducan antes de la fecha"),
        @ApiResponse(responseCode = "500", description = "Error interno del servidor")
    })
    @GetMapping("/caducan-antes")
    public ResponseEntity<List<InventarioDTO>> obtenerInsumosCaducanAntesDe(
            @Parameter(description = "Fecha límite (formato: yyyy-MM-dd)", required = true, example = "2026-12-31")
            @RequestParam String fecha) {
        List<InventarioDTO> insumos = inventarioService.obtenerInsumosCaducanAntesDe(fecha);
        return ResponseEntity.ok(insumos);
    }

    @Operation(summary = "Obtener insumos por rango de precio", description = "Lista los insumos cuyo precio está dentro del rango especificado")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Lista de insumos en el rango de precio"),
        @ApiResponse(responseCode = "500", description = "Error interno del servidor")
    })
    @GetMapping("/rango-precio")
    public ResponseEntity<List<InventarioDTO>> obtenerInsumosPorRangoPrecio(
            @Parameter(description = "Precio mínimo", required = true, example = "10.0")
            @RequestParam Double precioMin,
            @Parameter(description = "Precio máximo", required = true, example = "200.0")
            @RequestParam Double precioMax) {
        List<InventarioDTO> insumos = inventarioService.obtenerInsumosPorRangoPrecio(precioMin, precioMax);
        return ResponseEntity.ok(insumos);
    }
}
