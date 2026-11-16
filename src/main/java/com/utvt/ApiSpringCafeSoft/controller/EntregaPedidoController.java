
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
import org.springframework.web.server.ResponseStatusException;

import java.util.HashMap;
import java.util.Map;

/**
 * HU-015:
 * Confirmación de entregas, ventas en ruta
 * e incidencias del repartidor.
 *
 * HU-040:
 * Se conservan los códigos HTTP correspondientes
 * cuando una promoción no puede utilizarse.
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
                    entregaPedidoService.registrarVentaRuta(dto);

            return ResponseEntity
                    .status(HttpStatus.CREATED)
                    .body(entrega);

        } catch (ResponseStatusException e) {

            /*
             * HU-040:
             * Conservamos el código HTTP original
             * enviado por el servicio.
             *
             * Ejemplos:
             * 403 - Sucursal no autorizada.
             * 404 - Promoción no encontrada.
             * 409 - Promoción desactivada.
             * 409 - Promoción fuera de vigencia.
             * 409 - No cumple el mínimo de garrafones.
             *
             * GlobalExceptionHandler procesará
             * estas excepciones.
             */
            throw e;

        } catch (RuntimeException e) {

            /*
             * Se conserva el comportamiento original
             * de HU-015 para otros errores del servicio.
             */
            Map<String, String> error = new HashMap<>();

            error.put(
                "error",
                e.getMessage() != null
                    ? e.getMessage()
                    : "No se pudo registrar la venta."
            );

            return ResponseEntity
                    .status(HttpStatus.BAD_REQUEST)
                    .body(error);
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
                    entregaPedidoService.confirmarEntrega(
                        id,
                        dto
                    );

            return ResponseEntity.ok(entrega);

        } catch (RuntimeException e) {

            Map<String, String> error = new HashMap<>();

            error.put(
                "error",
                e.getMessage() != null
                    ? e.getMessage()
                    : "No se pudo confirmar la entrega."
            );

            return ResponseEntity
                    .status(HttpStatus.BAD_REQUEST)
                    .body(error);
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
                    entregaPedidoService.reportarIncidencia(
                        id,
                        dto
                    );

            return ResponseEntity.ok(entrega);

        } catch (RuntimeException e) {

            Map<String, String> error = new HashMap<>();

            error.put(
                "error",
                e.getMessage() != null
                    ? e.getMessage()
                    : "No se pudo reportar la incidencia."
            );

            return ResponseEntity
                    .status(HttpStatus.BAD_REQUEST)
                    .body(error);
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
                entregaPedidoService.entregasHoy(
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
                            e.getMessage() != null
                                ? e.getMessage()
                                : "No se pudieron obtener las entregas."
                        )
                    );
        }
    }
}
