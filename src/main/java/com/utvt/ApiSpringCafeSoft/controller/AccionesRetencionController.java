package com.utvt.ApiSpringCafeSoft.controller;

import com.utvt.ApiSpringCafeSoft.dto.AccionRetencionDTO;
import com.utvt.ApiSpringCafeSoft.dto.NuevaAccionRetencionDTO;
import com.utvt.ApiSpringCafeSoft.dto.ResumenAccionesDTO;
import com.utvt.ApiSpringCafeSoft.service.RetencionService;

import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/** HU-081: acciones de retención sobre clientes en riesgo (solo administradores). */
@RestController
@RequestMapping("/api/acciones-retencion")
@CrossOrigin(origins = "*")
@PreAuthorize("hasRole('ADMIN')")
public class AccionesRetencionController {

    private final RetencionService service;

    public AccionesRetencionController(RetencionService service) {
        this.service = service;
    }

    @GetMapping("/resumen")
    public ResponseEntity<List<ResumenAccionesDTO>> resumen() {
        return ResponseEntity.ok(service.resumen());
    }

    @GetMapping("/cliente/{clienteId}")
    public ResponseEntity<List<AccionRetencionDTO>> listar(@PathVariable Long clienteId) {
        return ResponseEntity.ok(service.listar(clienteId));
    }

    @PostMapping("/cliente/{clienteId}")
    public ResponseEntity<AccionRetencionDTO> registrar(
            @PathVariable Long clienteId,
            @Valid @RequestBody NuevaAccionRetencionDTO solicitud) {
        return ResponseEntity.ok(service.registrar(clienteId, solicitud));
    }

    @PutMapping("/cliente/{clienteId}/{accionId}/atender")
    public ResponseEntity<AccionRetencionDTO> atender(
            @PathVariable Long clienteId,
            @PathVariable Long accionId) {
        return ResponseEntity.ok(service.atender(clienteId, accionId));
    }
}
