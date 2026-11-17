package com.utvt.ApiSpringCafeSoft.controller;

import com.utvt.ApiSpringCafeSoft.dto.DashboardVentasDTO;
import com.utvt.ApiSpringCafeSoft.service.VentaDashboardService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * HU-041: Dashboard de ventas.
 *
 * Expone en una sola llamada los indicadores del periodo
 * (ventas, transacciones, ticket promedio), la tendencia diaria
 * y el ranking de productos mas vendidos (HU-042).
 */
@RestController
@RequestMapping("/api/ventas")
@Tag(
    name = "Dashboard de ventas",
    description = "HU-041 / HU-042: indicadores del periodo y productos mas vendidos"
)
public class VentaDashboardController {

    private final VentaDashboardService ventaDashboardService;

    public VentaDashboardController(VentaDashboardService ventaDashboardService) {
        this.ventaDashboardService = ventaDashboardService;
    }

    @Operation(
        summary = "Dashboard de ventas del periodo",
        description =
            "Devuelve las ventas del periodo, el numero de transacciones, " +
            "el ticket promedio, la tendencia diaria y los productos mas " +
            "vendidos para la sucursal del usuario autenticado. Los valores " +
            "provienen del mismo reporte de ventas, por lo que coinciden con " +
            "GET /api/ventas/reporte."
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "Dashboard generado correctamente"
        ),
        @ApiResponse(
            responseCode = "400",
            description = "Periodo no valido o usuario sin sucursal"
        ),
        @ApiResponse(
            responseCode = "500",
            description = "Error interno del servidor"
        )
    })
    @GetMapping("/dashboard")
    public ResponseEntity<DashboardVentasDTO> obtenerDashboard(

            @Parameter(
                description = "Periodo: hoy, mes o mes_anterior",
                required = true,
                example = "mes"
            )
            @RequestParam(defaultValue = "hoy") String periodo) {

        return ResponseEntity.ok(ventaDashboardService.obtenerDashboard(periodo));
    }
}
