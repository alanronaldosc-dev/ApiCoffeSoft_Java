package com.utvt.ApiSpringCafeSoft.controller;

import com.utvt.ApiSpringCafeSoft.dto.DashboardStockDTO;
import com.utvt.ApiSpringCafeSoft.service.DashboardStockService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/dashboard")
@Tag(
    name = "Dashboard de Garrafones",
    description = "Consulta del stock actual de garrafones"
)
public class DashboardStockController {

    @Autowired
    private DashboardStockService dashboardStockService;

    @GetMapping("/stock-garrafones")
    @Operation(
        summary = "Obtener resumen del stock de garrafones"
    )
    public ResponseEntity<DashboardStockDTO> obtenerStockGarrafones() {

        return ResponseEntity.ok(
                dashboardStockService.obtenerStockGarrafones()
        );
    }
}