package com.utvt.ApiSpringCafeSoft.controller;

import com.utvt.ApiSpringCafeSoft.dto.CampanaDTO;
import com.utvt.ApiSpringCafeSoft.service.CampanaService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * HU-037: API REST para gestión de campañas por segmento.
 *
 * Endpoints:
 * POST   /api/campanas              → Crear campaña
 * GET    /api/campanas              → Listar todas
 * GET    /api/campanas/vigentes     → Solo las vigentes ahora
 * GET    /api/campanas/segmento/{s} → Vigentes de un segmento
 * GET    /api/campanas/{id}         → Por ID
 * PUT    /api/campanas/{id}         → Actualizar
 * PUT    /api/campanas/{id}/desactivar → Desactivar manualmente
 * DELETE /api/campanas/{id}         → Eliminar
 */
@RestController
@RequestMapping("/api/campanas")
@CrossOrigin(origins = "*")
@Tag(name = "📢 Campañas", description = "HU-037: Programación de campañas por segmento de clientes")
public class CampanaController {

    private final CampanaService campanaService;

    public CampanaController(CampanaService campanaService) {
        this.campanaService = campanaService;
    }

    // ── CA-1: Crear campaña con segmento, promoción y periodo ──
    @Operation(
        summary = "Crear campaña",
        description = "Crea una campaña asociada a un segmento y una promoción. " +
                      "Se activa automáticamente si la fecha de inicio ya llegó."
    )
    @PostMapping
    public ResponseEntity<CampanaDTO> crearCampana(@Valid @RequestBody CampanaDTO dto) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(campanaService.crearCampana(dto));
    }

    // ── Listar todas ──
    @Operation(summary = "Listar todas las campañas")
    @GetMapping
    public ResponseEntity<List<CampanaDTO>> obtenerTodas() {
        return ResponseEntity.ok(campanaService.obtenerTodas());
    }

    // ── CA-2: Listar solo las vigentes ahora ──
    @Operation(
        summary = "Campañas vigentes",
        description = "Devuelve únicamente las campañas activas cuyo periodo incluye el momento actual."
    )
    @GetMapping("/vigentes")
    public ResponseEntity<List<CampanaDTO>> obtenerVigentes() {
        return ResponseEntity.ok(campanaService.obtenerVigentes());
    }

    // ── CA-2: Vigentes por segmento ──
    @Operation(
        summary = "Campañas vigentes por segmento",
        description = "Filtra las campañas vigentes para un segmento específico: " +
                      "ACTIVO, POTENCIAL, DORMIDO o POCO_FRECUENTE."
    )
    @GetMapping("/segmento/{segmento}")
    public ResponseEntity<List<CampanaDTO>> obtenerPorSegmento(
            @PathVariable String segmento) {
        return ResponseEntity.ok(
                campanaService.obtenerVigentesPorSegmento(segmento));
    }

    // ── Obtener por ID ──
    @Operation(summary = "Obtener campaña por ID")
    @GetMapping("/{id}")
    public ResponseEntity<CampanaDTO> obtenerPorId(@PathVariable Long id) {
        return ResponseEntity.ok(campanaService.obtenerPorId(id));
    }

    // ── Actualizar ──
    @Operation(summary = "Actualizar campaña")
    @PutMapping("/{id}")
    public ResponseEntity<CampanaDTO> actualizar(
            @PathVariable Long id,
            @Valid @RequestBody CampanaDTO dto) {
        return ResponseEntity.ok(campanaService.actualizarCampana(id, dto));
    }

    // ── CA-3: Desactivar manualmente ──
    @Operation(
        summary = "Desactivar campaña manualmente",
        description = "Permite desactivar una campaña antes de que expire su periodo."
    )
    @PutMapping("/{id}/desactivar")
    public ResponseEntity<CampanaDTO> desactivar(@PathVariable Long id) {
        return ResponseEntity.ok(campanaService.desactivarCampana(id));
    }

    // ── Eliminar ──
    @Operation(summary = "Eliminar campaña")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminar(@PathVariable Long id) {
        campanaService.eliminarCampana(id);
        return ResponseEntity.noContent().build();
    }

    // ── Endpoint de sincronización manual (para testing / admin) ──
    @Operation(
        summary = "Sincronizar estados",
        description = "Activa campañas pendientes y desactiva las expiradas. " +
                      "Normalmente lo hace el scheduler automático cada minuto."
    )
    @PostMapping("/sincronizar")
    public ResponseEntity<Map<String, Integer>> sincronizar() {
        int activadas = campanaService.activarPendientes();
        int desactivadas = campanaService.desactivarExpiradas();
        return ResponseEntity.ok(Map.of(
                "activadas", activadas,
                "desactivadas", desactivadas));
    }
}
