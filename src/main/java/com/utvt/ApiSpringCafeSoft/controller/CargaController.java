
package com.utvt.ApiSpringCafeSoft.controller;

import com.utvt.ApiSpringCafeSoft.dto.CargaDTO;
import com.utvt.ApiSpringCafeSoft.dto.RegistrarCargasMultipleDTO;
import com.utvt.ApiSpringCafeSoft.service.CargaService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

import jakarta.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@RestController
@RequestMapping("/api/cargas")
@Tag(
    name = "Cargas",
    description = "Gestión de cargas iniciales de garrafones para repartidores"
)
public class CargaController {

    @Autowired
    private CargaService cargaService;

    // ============================================
    // HU-005: REGISTRAR CARGA INICIAL
    // ============================================

    @PostMapping
    @Operation(
        summary = "Registrar carga inicial",
        description = "Asigna garrafones del inventario a un repartidor"
    )
    public ResponseEntity<CargaDTO> crearCarga(
            @Valid @RequestBody CargaDTO cargaDTO) {

        CargaDTO nuevaCarga =
                cargaService.crearCarga(cargaDTO);

        return new ResponseEntity<>(
                nuevaCarga,
                HttpStatus.CREATED
        );
    }

    // ============================================
    // HU-015: REGISTRAR CARGAS MÚLTIPLES
    // ============================================

    @PostMapping("/multiple")
    @Operation(
        summary = "Registrar múltiples cargas",
        description = "Asigna varios tipos de garrafones a un repartidor"
    )
    public ResponseEntity<List<CargaDTO>> crearCargasMultiple(
            @Valid @RequestBody RegistrarCargasMultipleDTO dto) {

        List<CargaDTO> cargas =
                cargaService.crearCargasMultiples(
                        dto.getRepartidorId(),
                        dto.getItems()
                );

        return new ResponseEntity<>(
                cargas,
                HttpStatus.CREATED
        );
    }

    // ============================================
    // OBTENER TODAS LAS CARGAS
    // ============================================

    @GetMapping
    @Operation(
        summary = "Obtener todas las cargas",
        description = "Obtiene todas las cargas registradas"
    )
    public ResponseEntity<List<CargaDTO>> obtenerTodas() {

        return ResponseEntity.ok(
                cargaService.obtenerTodas()
        );
    }

    // ============================================
    // OBTENER CARGA POR ID
    // ============================================

    @GetMapping("/{id}")
    @Operation(
        summary = "Obtener carga por ID",
        description = "Obtiene una carga específica"
    )
    public ResponseEntity<CargaDTO> obtenerPorId(
            @PathVariable Long id) {

        return ResponseEntity.ok(
                cargaService.obtenerPorId(id)
        );
    }

    // ============================================
    // HU-006: CARGAS POR REPARTIDOR
    // ============================================

    @GetMapping("/repartidor/{repartidorId}")
    @Operation(
        summary = "Obtener cargas de un repartidor",
        description = "Consulta las cargas asignadas"
    )
    public ResponseEntity<List<CargaDTO>> obtenerPorRepartidor(
            @PathVariable Long repartidorId) {

        return ResponseEntity.ok(
                cargaService.obtenerCargasPorRepartidor(
                        repartidorId
                )
        );
    }

    // ============================================
    // HU-006: CARGAS PENDIENTES
    // ============================================

    @GetMapping("/repartidor/{repartidorId}/pendientes")
    @Operation(
        summary = "Obtener cargas pendientes",
        description = "Consulta las cargas pendientes del repartidor"
    )
    public ResponseEntity<List<CargaDTO>> obtenerPendientes(
            @PathVariable Long repartidorId) {

        return ResponseEntity.ok(
                cargaService.obtenerCargasPendientes(
                        repartidorId
                )
        );
    }

    // ============================================
    // HU-006: ACEPTAR CARGA CON SEGURIDAD
    // ============================================

    @PutMapping("/{id}/aceptar")
    @Operation(
        summary = "Aceptar carga de forma segura",
        description = "Solo el repartidor asignado puede aceptar una carga pendiente"
    )
    public ResponseEntity<CargaDTO> aceptarCarga(
            @PathVariable Long id,
            Authentication authentication) {

        // SEGURIDAD 1:
        // Exigir autenticación y rol REPARTIDOR.

        if (authentication == null ||
                !authentication.isAuthenticated()) {

            throw new ResponseStatusException(
                    HttpStatus.UNAUTHORIZED,
                    "Debes iniciar sesión"
            );
        }

        boolean esRepartidor = authentication
                .getAuthorities()
                .stream()
                .anyMatch(authority ->
                        "ROLE_REPARTIDOR".equals(
                                authority.getAuthority()
                        )
                );

        if (!esRepartidor) {

            throw new ResponseStatusException(
                    HttpStatus.FORBIDDEN,
                    "Solo los repartidores pueden aceptar cargas"
            );
        }

        // El JWT Filter establece el correo
        // como identidad autenticada.
        String correoAutenticado =
                authentication.getName();

        // SEGURIDAD 2 Y 3:
        // El servicio valida propiedad,
        // estado y concurrencia.

        CargaDTO cargaAceptada =
                cargaService.aceptarCarga(
                        id,
                        correoAutenticado
                );

        return ResponseEntity.ok(cargaAceptada);
    }
}
