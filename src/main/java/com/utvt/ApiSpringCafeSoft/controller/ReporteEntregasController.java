package com.utvt.ApiSpringCafeSoft.controller;

import com.utvt.ApiSpringCafeSoft.dto.ReporteEntregasDTO;
import com.utvt.ApiSpringCafeSoft.service.EntregaPedidoService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.Map;

@RestController
@RequestMapping("/api/reportes")
@CrossOrigin(origins = "*")
@Tag(
        name = "HU-020 Reportes",
        description = "Reportes consolidados de entregas"
)
public class ReporteEntregasController {

    @Autowired
    private EntregaPedidoService entregaPedidoService;

    @GetMapping("/entregas")
    @Operation(
            summary = "Generar reporte consolidado de entregas por fechas y repartidor"
    )
    public ResponseEntity<?> reporteEntregas(

            @RequestParam
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
            LocalDate fechaInicio,

            @RequestParam
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
            LocalDate fechaFin,

            @RequestParam(required = false)
            Long repartidorId) {

        try {

            ReporteEntregasDTO reporte =
                    entregaPedidoService
                            .generarReporteEntregas(
                                    fechaInicio,
                                    fechaFin,
                                    repartidorId
                            );

            return ResponseEntity.ok(
                    reporte
            );

        } catch (RuntimeException e) {

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