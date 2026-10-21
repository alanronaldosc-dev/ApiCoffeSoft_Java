package com.utvt.ApiSpringCafeSoft.controller;

import com.utvt.ApiSpringCafeSoft.dto.LiquidacionDTO;
import com.utvt.ApiSpringCafeSoft.dto.RegistrarLiquidacionDTO;
import com.utvt.ApiSpringCafeSoft.dto.ResumenLiquidacionDTO;
import com.utvt.ApiSpringCafeSoft.service.LiquidacionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;

/** HU-016: liquidación de efectivo y garrafones al cierre del turno. */
@RestController
@RequestMapping("/api/liquidaciones")
@CrossOrigin(origins = "*")
@Tag(name = "HU-016 Liquidaciones", description = "Resumen financiero y cierre de turno")
public class LiquidacionController {

    @Autowired
    private LiquidacionService liquidacionService;

    @GetMapping("/repartidor/{repartidorId}/resumen")
    @Operation(summary = "Obtener resumen de liquidación de un repartidor")
    public ResponseEntity<?> obtenerResumen(
            @PathVariable Long repartidorId,
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
            LocalDate fecha) {
        try {
            ResumenLiquidacionDTO resumen = liquidacionService.obtenerResumen(repartidorId, fecha);
            return ResponseEntity.ok(resumen);
        } catch (RuntimeException e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", e.getMessage());
            return new ResponseEntity<>(error, HttpStatus.BAD_REQUEST);
        }
    }

    @PostMapping
    @Operation(summary = "Registrar cierre de turno y validar diferencias")
    public ResponseEntity<?> registrarLiquidacion(
            @Valid @RequestBody RegistrarLiquidacionDTO request) {
        try {
            LiquidacionDTO liquidacion = liquidacionService.registrarLiquidacion(request);
            return new ResponseEntity<>(liquidacion, HttpStatus.CREATED);
        } catch (RuntimeException e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", e.getMessage());
            return new ResponseEntity<>(error, HttpStatus.BAD_REQUEST);
        }
    }
}
