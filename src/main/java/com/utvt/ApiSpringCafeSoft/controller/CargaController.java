package com.utvt.ApiSpringCafeSoft.controller;

import com.utvt.ApiSpringCafeSoft.dto.CargaDTO;
import com.utvt.ApiSpringCafeSoft.service.CargaService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

import jakarta.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

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

    /**
     * 
     * (HU-006): agrega confirmacion de carga por repartidor
     * 
     */
    @PostMapping
    @Operation(
            summary = "Registrar carga inicial",
            description = "Asigna una cantidad de garrafones del inventario a un repartidor"
    )
    public ResponseEntity<CargaDTO> crearCarga(
            @Valid @RequestBody CargaDTO cargaDTO) {

        CargaDTO nuevaCarga = cargaService.crearCarga(cargaDTO);

        return new ResponseEntity<>(
                nuevaCarga,
                HttpStatus.CREATED
        );
    }

    /**
     * Obtener todas las cargas registradas.
     */
    @GetMapping
    @Operation(
            summary = "Obtener todas las cargas",
            description = "Obtiene todas las cargas registradas en el sistema"
    )
    public ResponseEntity<List<CargaDTO>> obtenerTodas() {

        List<CargaDTO> cargas = cargaService.obtenerTodas();

        return ResponseEntity.ok(cargas);
    }

    /**
     * Obtener una carga por ID.
     */
    @GetMapping("/{id}")
    @Operation(
            summary = "Obtener carga por ID",
            description = "Obtiene los datos de una carga específica"
    )
    public ResponseEntity<CargaDTO> obtenerPorId(
            @PathVariable Long id) {

        CargaDTO carga = cargaService.obtenerPorId(id);

        return ResponseEntity.ok(carga);
    }

    /**
     * HU-006
     * Obtener todas las cargas de un repartidor.
     */
    @GetMapping("/repartidor/{repartidorId}")
    @Operation(
            summary = "Obtener cargas de un repartidor",
            description = "Consulta las cargas asignadas a un repartidor"
    )
    public ResponseEntity<List<CargaDTO>> obtenerPorRepartidor(
            @PathVariable Long repartidorId) {

        List<CargaDTO> cargas =
                cargaService.obtenerCargasPorRepartidor(repartidorId);

        return ResponseEntity.ok(cargas);
    }

    /**
     * HU-006
     * Obtener únicamente las cargas pendientes.
     */
    @GetMapping("/repartidor/{repartidorId}/pendientes")
    @Operation(
            summary = "Obtener cargas pendientes",
            description = "Consulta las cargas que el repartidor todavía debe aceptar"
    )
    public ResponseEntity<List<CargaDTO>> obtenerPendientes(
            @PathVariable Long repartidorId) {

        List<CargaDTO> cargas =
                cargaService.obtenerCargasPendientes(repartidorId);

        return ResponseEntity.ok(cargas);
    }

    /**
     * HU-006
     * El repartidor acepta la carga.
     */
    @PutMapping("/{id}/aceptar")
    @Operation(
            summary = "Aceptar carga",
            description = "Cambia el estado de la carga de PENDIENTE a CARGA EN TRÁNSITO"
    )
    public ResponseEntity<CargaDTO> aceptarCarga(
            @PathVariable Long id) {

        CargaDTO cargaAceptada =
                cargaService.aceptarCarga(id);

        return ResponseEntity.ok(cargaAceptada);
    }
}