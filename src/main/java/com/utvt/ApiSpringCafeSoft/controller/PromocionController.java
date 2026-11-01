package com.utvt.ApiSpringCafeSoft.controller;

import com.utvt.ApiSpringCafeSoft.dto.PromocionDTO;
import com.utvt.ApiSpringCafeSoft.service.PromocionService;

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
import java.util.Map;

@RestController
@RequestMapping("/api/promociones")
@Tag(name = "Promociones", description = "Gestión de promociones con vigencia controlada por fecha")
public class PromocionController {

    @Autowired
    private PromocionService promocionService;


    // =========================================================
    // CREAR PROMOCIÓN — CA-1: inicio y fin obligatorios
    // =========================================================

    @Operation(
        summary = "Crear una nueva promoción",
        description = "Registra una promoción con fecha de inicio y fin obligatorias."
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "201", description = "✅ Promoción creada exitosamente"),
        @ApiResponse(responseCode = "400", description = "❌ Datos inválidos o fechas incoherentes"),
        @ApiResponse(responseCode = "404", description = "❌ Sucursal no encontrada")
    })
    @PostMapping
    public ResponseEntity<?> crearPromocion(@Valid @RequestBody PromocionDTO dto) {
        try {
            PromocionDTO nueva = promocionService.crearPromocion(dto);
            return new ResponseEntity<>(nueva, HttpStatus.CREATED);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("mensaje", e.getMessage()));
        }
    }


    // =========================================================
    // LISTAR TODAS
    // =========================================================

    @Operation(summary = "Listar todas las promociones")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "✅ Lista obtenida correctamente")
    })
    @GetMapping
    public ResponseEntity<List<PromocionDTO>> obtenerTodas() {
        return ResponseEntity.ok(promocionService.obtenerTodas());
    }


    // =========================================================
    // LISTAR VIGENTES — CA-2: no se activan fuera del periodo
    // =========================================================

    @Operation(
        summary = "Listar promociones vigentes",
        description = "Devuelve solo las promociones activas cuya fecha actual esté dentro del periodo."
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "✅ Promociones vigentes obtenidas")
    })
    @GetMapping("/vigentes")
    public ResponseEntity<List<PromocionDTO>> obtenerVigentes() {
        return ResponseEntity.ok(promocionService.obtenerVigentes());
    }


    // =========================================================
    // VIGENTES POR SUCURSAL — CA-2
    // =========================================================

    @Operation(
        summary = "Listar promociones vigentes por sucursal",
        description = "Devuelve las promociones dentro de vigencia para una sucursal específica."
    )
    @GetMapping("/vigentes/sucursal/{sucursalId}")
    public ResponseEntity<List<PromocionDTO>> obtenerVigentesPorSucursal(
            @Parameter(description = "ID de la sucursal", required = true, example = "1")
            @PathVariable Long sucursalId) {
        return ResponseEntity.ok(
                promocionService.obtenerVigentesPorSucursal(sucursalId));
    }


    // =========================================================
    // OBTENER POR ID
    // =========================================================

    @Operation(summary = "Obtener una promoción por ID")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "✅ Promoción encontrada"),
        @ApiResponse(responseCode = "404", description = "❌ Promoción no encontrada")
    })
    @GetMapping("/{id}")
    public ResponseEntity<?> obtenerPorId(
            @Parameter(description = "ID de la promoción", required = true, example = "1")
            @PathVariable Long id) {
        try {
            return ResponseEntity.ok(promocionService.obtenerPorId(id));
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("mensaje", e.getMessage()));
        }
    }


    // =========================================================
    // VALIDAR SI PUEDE APLICARSE — CA-3
    // =========================================================

    @Operation(
        summary = "Validar si una promoción puede aplicarse",
        description = "CA-3: Verifica que la promoción esté activa y dentro de su periodo. " +
                      "Una promoción expirada o fuera de rango no puede aplicarse."
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "✅ La promoción puede aplicarse"),
        @ApiResponse(responseCode = "400", description = "❌ Fuera de periodo o desactivada"),
        @ApiResponse(responseCode = "404", description = "❌ No encontrada")
    })
    @GetMapping("/{id}/validar")
    public ResponseEntity<?> validarAplicacion(
            @Parameter(description = "ID de la promoción", required = true, example = "1")
            @PathVariable Long id) {
        try {
            return ResponseEntity.ok(promocionService.validarAplicacion(id));
        } catch (IllegalStateException e) {
            return ResponseEntity.badRequest().body(Map.of("mensaje", e.getMessage()));
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("mensaje", e.getMessage()));
        }
    }


    // =========================================================
    // ACTUALIZAR
    // =========================================================

    @Operation(summary = "Actualizar una promoción")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "✅ Promoción actualizada"),
        @ApiResponse(responseCode = "400", description = "❌ Datos inválidos"),
        @ApiResponse(responseCode = "404", description = "❌ Promoción no encontrada")
    })
    @PutMapping("/{id}")
    public ResponseEntity<?> actualizarPromocion(
            @PathVariable Long id,
            @Valid @RequestBody PromocionDTO dto) {
        try {
            return ResponseEntity.ok(promocionService.actualizarPromocion(id, dto));
        } catch (IllegalArgumentException | IllegalStateException e) {
            return ResponseEntity.badRequest().body(Map.of("mensaje", e.getMessage()));
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("mensaje", e.getMessage()));
        }
    }


    // =========================================================
    // DESACTIVAR — CA-3: control manual
    // =========================================================

    @Operation(
        summary = "Desactivar una promoción",
        description = "CA-3: Desactiva manualmente la promoción para que no pueda aplicarse, " +
                      "independientemente de si está dentro del periodo."
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "✅ Promoción desactivada"),
        @ApiResponse(responseCode = "404", description = "❌ No encontrada")
    })
    @PutMapping("/{id}/desactivar")
    public ResponseEntity<?> desactivarPromocion(@PathVariable Long id) {
        try {
            return ResponseEntity.ok(promocionService.desactivarPromocion(id));
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("mensaje", e.getMessage()));
        }
    }


    // =========================================================
    // ELIMINAR
    // =========================================================

    @Operation(summary = "Eliminar una promoción")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "204", description = "✅ Eliminada correctamente"),
        @ApiResponse(responseCode = "404", description = "❌ No encontrada")
    })
    @DeleteMapping("/{id}")
    public ResponseEntity<?> eliminarPromocion(@PathVariable Long id) {
        try {
            promocionService.eliminarPromocion(id);
            return ResponseEntity.noContent().build();
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("mensaje", e.getMessage()));
        }
    }
}
