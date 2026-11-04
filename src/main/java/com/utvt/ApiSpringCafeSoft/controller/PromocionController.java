
package com.utvt.ApiSpringCafeSoft.controller;

import com.utvt.ApiSpringCafeSoft.dto.PromocionDTO;
import com.utvt.ApiSpringCafeSoft.service.PromocionService;

import io.swagger.v3.oas.annotations.Operation;
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
@Tag(
    name = "Promociones",
    description = "Gestión y consulta de promociones"
)
public class PromocionController {

    @Autowired
    private PromocionService promocionService;

    // =====================================================
    // CONSULTAR TODAS LAS PROMOCIONES
    // =====================================================

    @Operation(
        summary = "Consultar todas las promociones"
    )
    @GetMapping({"", "/"})
    public ResponseEntity<List<PromocionDTO>> obtenerTodas() {

        return ResponseEntity.ok(
            promocionService.obtenerTodas()
        );
    }

    // =====================================================
    // HU-039: CONSULTAR PROMOCIONES POR ESTADO
    // =====================================================

    @Operation(
        summary = "Filtrar promociones por estado",
        description =
            "Permite consultar promociones vigentes, " +
            "vencidas, programadas o desactivadas."
    )
    @GetMapping("/estado")
    public ResponseEntity<?> consultarPorEstado(
        @RequestParam(defaultValue = "TODAS") String estado
    ) {

        try {

            List<PromocionDTO> promociones =
                promocionService.obtenerPorEstado(estado);

            return ResponseEntity.ok(promociones);

        } catch (IllegalArgumentException e) {

            return ResponseEntity.badRequest().body(
                Map.of("mensaje", e.getMessage())
            );
        }
    }

    // =====================================================
    // CONSULTAR PROMOCIONES VIGENTES
    // =====================================================

    @Operation(
        summary = "Consultar promociones vigentes"
    )
    @GetMapping("/vigentes")
    public ResponseEntity<List<PromocionDTO>> obtenerVigentes() {

        return ResponseEntity.ok(
            promocionService.obtenerVigentes()
        );
    }

    // =====================================================
    // CONSULTAR VIGENTES POR SUCURSAL
    // =====================================================

    @Operation(
        summary = "Consultar promociones vigentes por sucursal"
    )
    @GetMapping("/vigentes/sucursal/{sucursalId}")
    public ResponseEntity<List<PromocionDTO>> obtenerVigentesPorSucursal(
        @PathVariable Long sucursalId
    ) {

        return ResponseEntity.ok(
            promocionService.obtenerVigentesPorSucursal(
                sucursalId
            )
        );
    }

    // =====================================================
    // CONSULTAR PROMOCIÓN POR ID
    // =====================================================

    @Operation(
        summary = "Consultar promoción por ID"
    )
    @GetMapping("/{id}")
    public ResponseEntity<?> obtenerPorId(
        @PathVariable Long id
    ) {

        try {

            return ResponseEntity.ok(
                promocionService.obtenerPorId(id)
            );

        } catch (IllegalArgumentException e) {

            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(Map.of("mensaje", e.getMessage()));
        }
    }

    // =====================================================
    // VALIDAR APLICACIÓN DE PROMOCIÓN
    // =====================================================

    @Operation(
        summary = "Validar si una promoción puede aplicarse"
    )
    @GetMapping("/{id}/validar")
    public ResponseEntity<?> validarAplicacion(
        @PathVariable Long id
    ) {

        try {

            return ResponseEntity.ok(
                promocionService.validarAplicacion(id)
            );

        } catch (IllegalStateException e) {

            return ResponseEntity.status(HttpStatus.CONFLICT)
                .body(Map.of("mensaje", e.getMessage()));

        } catch (IllegalArgumentException e) {

            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(Map.of("mensaje", e.getMessage()));
        }
    }

    // =====================================================
    // CREAR PROMOCIÓN
    // =====================================================

    @Operation(
        summary = "Crear una nueva promoción"
    )
    @PostMapping({"", "/"})
    public ResponseEntity<?> crearPromocion(
        @Valid @RequestBody PromocionDTO dto
    ) {

        try {

            PromocionDTO nueva =
                promocionService.crearPromocion(dto);

            return ResponseEntity.status(HttpStatus.CREATED)
                .body(nueva);

        } catch (IllegalArgumentException e) {

            return ResponseEntity.badRequest()
                .body(Map.of("mensaje", e.getMessage()));
        }
    }

    // =====================================================
    // ACTUALIZAR PROMOCIÓN
    // =====================================================

    @Operation(
        summary = "Actualizar una promoción existente"
    )
    @PutMapping("/{id}")
    public ResponseEntity<?> actualizarPromocion(
        @PathVariable Long id,
        @Valid @RequestBody PromocionDTO dto
    ) {

        try {

            PromocionDTO actualizada =
                promocionService.actualizarPromocion(
                    id,
                    dto
                );

            return ResponseEntity.ok(actualizada);

        } catch (IllegalArgumentException e) {

            return ResponseEntity.badRequest()
                .body(Map.of("mensaje", e.getMessage()));
        }
    }

    // =====================================================
    // DESACTIVAR PROMOCIÓN
    // =====================================================

    @Operation(
        summary = "Desactivar una promoción"
    )
    @PutMapping("/{id}/desactivar")
    public ResponseEntity<?> desactivarPromocion(
        @PathVariable Long id
    ) {

        try {

            PromocionDTO desactivada =
                promocionService.desactivarPromocion(id);

            return ResponseEntity.ok(desactivada);

        } catch (IllegalArgumentException e) {

            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(Map.of("mensaje", e.getMessage()));
        }
    }

    // =====================================================
    // ELIMINAR PROMOCIÓN
    // =====================================================

    @Operation(
        summary = "Eliminar una promoción"
    )
    @DeleteMapping("/{id}")
    public ResponseEntity<?> eliminarPromocion(
        @PathVariable Long id
    ) {

        try {

            promocionService.eliminarPromocion(id);

            return ResponseEntity.ok(
                Map.of(
                    "mensaje",
                    "Promoción eliminada correctamente."
                )
            );

        } catch (IllegalArgumentException e) {

            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(Map.of("mensaje", e.getMessage()));
        }
    }
}
