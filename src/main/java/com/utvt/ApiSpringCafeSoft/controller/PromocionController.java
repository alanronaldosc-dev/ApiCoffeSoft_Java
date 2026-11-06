
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
    description = "Gestion de promociones y auditoria"
)
public class PromocionController {

    @Autowired
    private PromocionService promocionService;

    // Consultar todas.
    @Operation(summary = "Consultar todas las promociones")
    @GetMapping({"", "/"})
    public ResponseEntity<List<PromocionDTO>> obtenerTodas() {
        return ResponseEntity.ok(
            promocionService.obtenerTodas()
        );
    }

    // HU-039: consultar por estado.
    @Operation(summary = "Filtrar promociones por estado")
    @GetMapping("/estado")
    public ResponseEntity<List<PromocionDTO>> consultarPorEstado(
        @RequestParam(defaultValue = "TODAS") String estado
    ) {
        return ResponseEntity.ok(
            promocionService.obtenerPorEstado(estado)
        );
    }

    // Consultar vigentes.
    @Operation(summary = "Consultar promociones vigentes")
    @GetMapping("/vigentes")
    public ResponseEntity<List<PromocionDTO>> obtenerVigentes() {
        return ResponseEntity.ok(
            promocionService.obtenerVigentes()
        );
    }

    // Consultar vigentes por sucursal.
    @Operation(
        summary = "Consultar promociones vigentes por sucursal"
    )
    @GetMapping("/vigentes/sucursal/{sucursalId}")
    public ResponseEntity<List<PromocionDTO>> obtenerVigentesPorSucursal(
        @PathVariable Long sucursalId
    ) {
        return ResponseEntity.ok(
            promocionService.obtenerVigentesPorSucursal(sucursalId)
        );
    }

    // Consultar por ID.
    @Operation(summary = "Consultar promocion por ID")
    @GetMapping("/{id}")
    public ResponseEntity<PromocionDTO> obtenerPorId(
        @PathVariable Long id
    ) {
        return ResponseEntity.ok(
            promocionService.obtenerPorId(id)
        );
    }

    // Validar aplicacion.
    @Operation(summary = "Validar aplicacion de promocion")
    @GetMapping("/{id}/validar")
    public ResponseEntity<PromocionDTO> validarAplicacion(
        @PathVariable Long id
    ) {
        return ResponseEntity.ok(
            promocionService.validarAplicacion(id)
        );
    }

    // Crear promocion.
    @Operation(summary = "Crear promocion")
    @PostMapping({"", "/"})
    public ResponseEntity<PromocionDTO> crearPromocion(
        @Valid @RequestBody PromocionDTO dto
    ) {
        return ResponseEntity.status(HttpStatus.CREATED)
            .body(promocionService.crearPromocion(dto));
    }

    // Actualizar promocion.
    @Operation(summary = "Actualizar promocion")
    @PutMapping("/{id}")
    public ResponseEntity<PromocionDTO> actualizarPromocion(
        @PathVariable Long id,
        @Valid @RequestBody PromocionDTO dto
    ) {
        return ResponseEntity.ok(
            promocionService.actualizarPromocion(id, dto)
        );
    }

    // HU-040: desactivacion con auditoria.
    @Operation(
        summary = "Desactivar promocion",
        description = "Registra fecha y administrador responsable."
    )
    @PutMapping("/{id}/desactivar")
    public ResponseEntity<PromocionDTO> desactivarPromocion(
        @PathVariable Long id
    ) {
        return ResponseEntity.ok(
            promocionService.desactivarPromocion(id)
        );
    }

    // Eliminar promocion.
    @Operation(summary = "Eliminar promocion")
    @DeleteMapping("/{id}")
    public ResponseEntity<Map<String, String>> eliminarPromocion(
        @PathVariable Long id
    ) {
        promocionService.eliminarPromocion(id);

        return ResponseEntity.ok(
            Map.of(
                "mensaje",
                "Promocion eliminada correctamente."
            )
        );
    }
}
