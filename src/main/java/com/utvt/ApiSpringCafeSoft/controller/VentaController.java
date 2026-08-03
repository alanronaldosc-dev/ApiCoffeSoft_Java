package com.utvt.ApiSpringCafeSoft.controller;

import com.utvt.ApiSpringCafeSoft.dto.VentaDTO;
import com.utvt.ApiSpringCafeSoft.service.VentaService;
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

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/ventas")
@Tag(name = "Ventas", description = "API para la gestión de ventas con descuento automático de inventario")
public class VentaController {

    @Autowired
    private VentaService ventaService;

    @Operation(
        summary = "Crear una nueva venta", 
        description = "Registra una nueva venta y descuenta automáticamente los insumos del inventario. " +
                      "Ejemplo: Si vendes 1 Té Chai Latte, se descuentan 0.001kg de Té chai y 0.250L de Leche del inventario."
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "201", description = "✅ Venta creada exitosamente"),
        @ApiResponse(responseCode = "400", description = "❌ Datos inválidos"),
        @ApiResponse(responseCode = "404", description = "❌ Producto o usuario no encontrado"),
        @ApiResponse(responseCode = "409", description = "❌ Stock insuficiente de insumos"),
        @ApiResponse(responseCode = "500", description = "❌ Error interno del servidor")
    })
    @PostMapping
    public ResponseEntity<VentaDTO> crearVenta(@Valid @RequestBody VentaDTO ventaDTO) {
        VentaDTO nuevaVenta = ventaService.crearVenta(ventaDTO);
        return new ResponseEntity<>(nuevaVenta, HttpStatus.CREATED);
    }

    @Operation(summary = "Obtener todas las ventas", description = "Lista todas las ventas registradas")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "✅ Lista de ventas obtenida exitosamente"),
        @ApiResponse(responseCode = "500", description = "❌ Error interno del servidor")
    })
    @GetMapping
    public ResponseEntity<List<VentaDTO>> obtenerTodasLasVentas() {
        List<VentaDTO> ventas = ventaService.obtenerTodasLasVentas();
        return ResponseEntity.ok(ventas);
    }

    @Operation(summary = "Obtener una venta por ID", description = "Busca una venta específica por su ID")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "✅ Venta encontrada"),
        @ApiResponse(responseCode = "404", description = "❌ Venta no encontrada"),
        @ApiResponse(responseCode = "500", description = "❌ Error interno del servidor")
    })
    @GetMapping("/{id}")
    public ResponseEntity<VentaDTO> obtenerVentaPorId(
            @Parameter(description = "ID de la venta", required = true, example = "1")
            @PathVariable Long id) {
        VentaDTO venta = ventaService.obtenerVentaPorId(id);
        return ResponseEntity.ok(venta);
    }

    @Operation(summary = "Obtener una venta por folio", description = "Busca una venta específica por su folio")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "✅ Venta encontrada"),
        @ApiResponse(responseCode = "404", description = "❌ Venta no encontrada"),
        @ApiResponse(responseCode = "500", description = "❌ Error interno del servidor")
    })
    @GetMapping("/folio/{folio}")
    public ResponseEntity<VentaDTO> obtenerVentaPorFolio(
            @Parameter(description = "Folio de la venta", required = true, example = "V-0001")
            @PathVariable String folio) {
        VentaDTO venta = ventaService.obtenerVentaPorFolio(folio);
        return ResponseEntity.ok(venta);
    }

    @Operation(summary = "Obtener ventas por usuario", description = "Lista las ventas realizadas por un usuario específico")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "✅ Lista de ventas del usuario"),
        @ApiResponse(responseCode = "500", description = "❌ Error interno del servidor")
    })
    @GetMapping("/usuario/{usuarioId}")
    public ResponseEntity<List<VentaDTO>> obtenerVentasPorUsuario(
            @Parameter(description = "ID del usuario", required = true, example = "1")
            @PathVariable Long usuarioId) {
        List<VentaDTO> ventas = ventaService.obtenerVentasPorUsuario(usuarioId);
        return ResponseEntity.ok(ventas);
    }

    @Operation(summary = "Obtener ventas por rango de fechas", description = "Lista las ventas entre dos fechas")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "✅ Lista de ventas en el rango de fechas"),
        @ApiResponse(responseCode = "500", description = "❌ Error interno del servidor")
    })
    @GetMapping("/fechas")
    public ResponseEntity<List<VentaDTO>> obtenerVentasPorRangoFechas(
            @Parameter(description = "Fecha de inicio (formato: yyyy-MM-dd'T'HH:mm:ss)", required = true, example = "2026-06-01T00:00:00")
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime inicio,
            @Parameter(description = "Fecha de fin (formato: yyyy-MM-dd'T'HH:mm:ss)", required = true, example = "2026-06-30T23:59:59")
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime fin) {
        List<VentaDTO> ventas = ventaService.obtenerVentasPorRangoFechas(inicio, fin);
        return ResponseEntity.ok(ventas);
    }

    @Operation(summary = "Obtener ventas por método de pago", description = "Lista las ventas según el método de pago (efectivo o tarjeta)")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "✅ Lista de ventas por método de pago"),
        @ApiResponse(responseCode = "500", description = "❌ Error interno del servidor")
    })
    @GetMapping("/metodo-pago/{metodoPago}")
    public ResponseEntity<List<VentaDTO>> obtenerVentasPorMetodoPago(
            @Parameter(description = "Método de pago (efectivo o tarjeta)", required = true, example = "efectivo")
            @PathVariable String metodoPago) {
        List<VentaDTO> ventas = ventaService.obtenerVentasPorMetodoPago(metodoPago);
        return ResponseEntity.ok(ventas);
    }

    @Operation(summary = "Cancelar una venta", description = "Cancela una venta y repone los insumos al inventario")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "204", description = "✅ Venta cancelada exitosamente"),
        @ApiResponse(responseCode = "404", description = "❌ Venta no encontrada"),
        @ApiResponse(responseCode = "500", description = "❌ Error interno del servidor")
    })
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> cancelarVenta(
            @Parameter(description = "ID de la venta a cancelar", required = true, example = "1")
            @PathVariable Long id) {
        ventaService.cancelarVenta(id);
        return ResponseEntity.noContent().build();
    }
}