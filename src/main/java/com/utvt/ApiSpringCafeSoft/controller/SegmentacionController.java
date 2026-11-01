package com.utvt.ApiSpringCafeSoft.controller;

import com.utvt.ApiSpringCafeSoft.service.SegmentacionService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import java.util.List;

/** HU-035/HU-036: consultas exclusivas para administradores. */
@RestController
@RequestMapping("/api/clientes/segmentos")
@PreAuthorize("hasRole('ADMIN')")
public class SegmentacionController {
    private final SegmentacionService service;
    public SegmentacionController(SegmentacionService service) { this.service = service; }

    @GetMapping
    public ResponseEntity<List<SegmentacionService.SegmentoCliente>> listar(
        @RequestParam(required = false) String segmento) {
        return ResponseEntity.ok(service.listar(segmento));
    }
    @GetMapping("/resumen")
    public ResponseEntity<SegmentacionService.Resumen> resumen() {
        return ResponseEntity.ok(service.resumen());
    }
    @GetMapping("/cliente/{id}")
    public ResponseEntity<SegmentacionService.SegmentoCliente> cliente(@PathVariable Long id) {
        return ResponseEntity.ok(service.porCliente(id));
    }
}
