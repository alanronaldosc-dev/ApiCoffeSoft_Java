package com.utvt.ApiSpringCafeSoft.controller;

import com.utvt.ApiSpringCafeSoft.dto.MermaGarrafonDTO;
import com.utvt.ApiSpringCafeSoft.service.MermaGarrafonService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

import jakarta.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/mermas")
@Tag(
        name = "Mermas de Garrafón",
        description = "Gestión de garrafones dañados o con fugas"
)
public class MermaGarrafonController {

    @Autowired
    private MermaGarrafonService mermaGarrafonService;

    /**
     * HU-009
     * Registrar una nueva merma.
     */
    @PostMapping
    @Operation(
            summary = "Registrar merma de garrafón",
            description = "Registra garrafones dañados y descuenta su cantidad de la responsabilidad del repartidor"
    )
    public ResponseEntity<MermaGarrafonDTO> registrarMerma(
            @Valid @RequestBody MermaGarrafonDTO dto) {

        MermaGarrafonDTO nuevaMerma =
                mermaGarrafonService.registrarMerma(dto);

        return new ResponseEntity<>(
                nuevaMerma,
                HttpStatus.CREATED
        );
    }

    /**
     * Consultar todas las mermas.
     */
    @GetMapping
    @Operation(
            summary = "Obtener todas las mermas",
            description = "Consulta el historial completo de mermas de garrafones"
    )
    public ResponseEntity<List<MermaGarrafonDTO>> obtenerTodas() {

        return ResponseEntity.ok(
                mermaGarrafonService.obtenerTodas()
        );
    }

    /**
     * Consultar una merma específica.
     */
    @GetMapping("/{id}")
    @Operation(summary = "Obtener merma por ID")
    public ResponseEntity<MermaGarrafonDTO> obtenerPorId(
            @PathVariable Long id) {

        return ResponseEntity.ok(
                mermaGarrafonService.obtenerPorId(id)
        );
    }

    /**
     * Consultar las mermas pertenecientes a una carga.
     */
    @GetMapping("/carga/{cargaId}")
    @Operation(summary = "Obtener mermas por carga")
    public ResponseEntity<List<MermaGarrafonDTO>> obtenerPorCarga(
            @PathVariable Long cargaId) {

        return ResponseEntity.ok(
                mermaGarrafonService.obtenerPorCarga(cargaId)
        );
    }

    /**
     * Consultar las mermas asociadas a un repartidor.
     */
    @GetMapping("/repartidor/{repartidorId}")
    @Operation(summary = "Obtener mermas por repartidor")
    public ResponseEntity<List<MermaGarrafonDTO>> obtenerPorRepartidor(
            @PathVariable Long repartidorId) {

        return ResponseEntity.ok(
                mermaGarrafonService.obtenerPorRepartidor(repartidorId)
        );
    }

    /**
     * Consultar las mermas por causa.
     */
    @GetMapping("/causa/{causa}")
    @Operation(summary = "Obtener mermas por causa")
    public ResponseEntity<List<MermaGarrafonDTO>> obtenerPorCausa(
            @PathVariable String causa) {

        return ResponseEntity.ok(
                mermaGarrafonService.obtenerPorCausa(causa)
        );
    }
}