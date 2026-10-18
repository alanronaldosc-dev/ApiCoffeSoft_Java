package com.utvt.ApiSpringCafeSoft.controller;

import com.utvt.ApiSpringCafeSoft.dto.LiquidacionDTO;
import com.utvt.ApiSpringCafeSoft.dto.RegistrarLiquidacionDTO;
import com.utvt.ApiSpringCafeSoft.dto.ResumenLiquidacionDTO;
import com.utvt.ApiSpringCafeSoft.service.LiquidacionService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

import jakarta.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

/**
 * HU-016:
 * liquidación por cierre de ruta.
 */
@RestController
@RequestMapping("/api/liquidaciones")
@CrossOrigin(origins = "*")
@Tag(
        name = "HU-016 Liquidaciones",
        description = "Resumen financiero y cierre de ruta"
)
public class LiquidacionController {

    @Autowired
    private LiquidacionService liquidacionService;

    // ============================================
    // RESUMEN DE UNA RUTA
    // ============================================

    @GetMapping("/ruta/{rutaId}/resumen")
    @Operation(
            summary = "Obtener resumen de liquidación de una ruta"
    )
    public ResponseEntity<?> obtenerResumen(
            @PathVariable Long rutaId) {

        try {

            ResumenLiquidacionDTO resumen =
                    liquidacionService
                            .obtenerResumen(
                                    rutaId
                            );

            return ResponseEntity.ok(
                    resumen
            );

        } catch (RuntimeException e) {

            Map<String, String> error =
                    new HashMap<>();

            error.put(
                    "error",
                    e.getMessage()
            );

            return new ResponseEntity<>(
                    error,
                    HttpStatus.BAD_REQUEST
            );
        }
    }

    // ============================================
    // REGISTRAR CIERRE DE RUTA
    // ============================================

    @PostMapping
    @Operation(
            summary = "Registrar cierre de ruta y validar diferencias"
    )
    public ResponseEntity<?> registrarLiquidacion(
            @Valid
            @RequestBody
            RegistrarLiquidacionDTO request) {

        try {

            LiquidacionDTO liquidacion =
                    liquidacionService
                            .registrarLiquidacion(
                                    request
                            );

            return new ResponseEntity<>(
                    liquidacion,
                    HttpStatus.CREATED
            );

        } catch (RuntimeException e) {

            Map<String, String> error =
                    new HashMap<>();

            error.put(
                    "error",
                    e.getMessage()
            );

            return new ResponseEntity<>(
                    error,
                    HttpStatus.BAD_REQUEST
            );
        }
    }
}