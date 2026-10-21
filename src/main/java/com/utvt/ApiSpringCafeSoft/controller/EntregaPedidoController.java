package com.utvt.ApiSpringCafeSoft.controller;

import com.utvt.ApiSpringCafeSoft.dto.RegistrarVentaRutaDTO;
import com.utvt.ApiSpringCafeSoft.dto.ConfirmarEntregaDTO;
import com.utvt.ApiSpringCafeSoft.dto.EntregaPedidoDTO;
import com.utvt.ApiSpringCafeSoft.dto.ReportarIncidenciaEntregaDTO;
import com.utvt.ApiSpringCafeSoft.service.EntregaPedidoService;

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
 * HU-015:
 * Confirmación de entregas,
 * ventas en ruta e incidencias del repartidor.
 */
@RestController
@RequestMapping("/api/ventas")
@CrossOrigin(origins = "*")
@Tag(
    name = "HU-015 Entregas",
    description = "Confirmación de entregas e incidencias del repartidor"
)
public class EntregaPedidoController {

    @Autowired
    private EntregaPedidoService entregaPedidoService;

    // ============================================
    // REGISTRAR VENTA DURANTE LA RUTA
    // ============================================

    @PostMapping("/ruta/registrar")
    @Operation(
        summary = "Registrar venta realizada durante la ruta del repartidor"
    )
    public ResponseEntity<?> registrarVentaRuta(
            @Valid @RequestBody RegistrarVentaRutaDTO dto) {

        try {

            EntregaPedidoDTO entrega =
                    entregaPedidoService
                            .registrarVentaRuta(dto);

            return new ResponseEntity<>(
                    entrega,
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

    // ============================================
    // CONFIRMAR ENTREGA
    // ============================================

    @PutMapping("/{id}/confirmar-entrega")
    @Operation(
        summary = "Confirmar entrega con garrafones, envases y cobro"
    )
    public ResponseEntity<?> confirmarEntrega(
            @PathVariable Long id,
            @Valid @RequestBody ConfirmarEntregaDTO dto) {

        try {

            EntregaPedidoDTO entrega =
                    entregaPedidoService
                            .confirmarEntrega(
                                    id,
                                    dto
                            );

            return ResponseEntity.ok(
                    entrega
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
    // REPORTAR INCIDENCIA
    // ============================================

    @PutMapping("/{id}/reportar-incidencia")
    @Operation(
        summary = "Reportar cliente ausente o falta de envases"
    )
    public ResponseEntity<?> reportarIncidencia(
            @PathVariable Long id,
            @Valid @RequestBody ReportarIncidenciaEntregaDTO dto) {

        try {

            EntregaPedidoDTO entrega =
                    entregaPedidoService
                            .reportarIncidencia(
                                    id,
                                    dto
                            );

            return ResponseEntity.ok(
                    entrega
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
    // ENTREGAS REALIZADAS HOY
    // ============================================

    @GetMapping(
        "/repartidor/{repartidorId}/entregas-hoy"
    )
    @Operation(
        summary = "Obtener las entregas realizadas hoy por un repartidor"
    )
    public ResponseEntity<?> entregasHoy(
            @PathVariable Long repartidorId) {

        System.out.println(
            "ENTRO A ENTREGAS HOY -> repartidorId: "
                    + repartidorId
        );

        try {

            return ResponseEntity.ok(
                    entregaPedidoService
                            .entregasHoy(
                                    repartidorId
                            )
            );

        } catch (RuntimeException e) {

            System.out.println(
                "ERROR ENTREGAS HOY -> "
                        + e.getMessage()
            );

            return ResponseEntity
                    .badRequest()
                    .body(
                            Map.of(
                                    "error",
                                    e.getMessage()
                            )
                    );
        }
    }
}