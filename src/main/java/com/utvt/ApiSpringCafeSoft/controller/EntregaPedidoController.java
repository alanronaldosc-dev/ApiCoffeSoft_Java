package com.utvt.ApiSpringCafeSoft.controller;

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

/** HU-015: confirmación de entregas del repartidor. */
@RestController
@RequestMapping("/api/ventas")
@CrossOrigin(origins = "*")
@Tag(name = "HU-015 Entregas", description = "Confirmación de entregas e incidencias del repartidor")
public class EntregaPedidoController {

    @Autowired
    private EntregaPedidoService entregaPedidoService;

    @PutMapping("/{id}/confirmar-entrega")
    @Operation(summary = "Confirmar entrega con garrafones, envases y cobro")
    public ResponseEntity<?> confirmarEntrega(
            @PathVariable Long id,
            @Valid @RequestBody ConfirmarEntregaDTO dto) {
        try {
            EntregaPedidoDTO entrega = entregaPedidoService.confirmarEntrega(id, dto);
            return ResponseEntity.ok(entrega);
        } catch (RuntimeException e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", e.getMessage());
            return new ResponseEntity<>(error, HttpStatus.BAD_REQUEST);
        }
    }

    @PutMapping("/{id}/reportar-incidencia")
    @Operation(summary = "Reportar cliente ausente o falta de envases")
    public ResponseEntity<?> reportarIncidencia(
            @PathVariable Long id,
            @Valid @RequestBody ReportarIncidenciaEntregaDTO dto) {
        try {
            EntregaPedidoDTO entrega = entregaPedidoService.reportarIncidencia(id, dto);
            return ResponseEntity.ok(entrega);
        } catch (RuntimeException e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", e.getMessage());
            return new ResponseEntity<>(error, HttpStatus.BAD_REQUEST);
        }
    }
}
