package com.utvt.ApiSpringCafeSoft.controller;

import com.utvt.ApiSpringCafeSoft.integration.traccar.TraccarDeviceDTO;
import com.utvt.ApiSpringCafeSoft.integration.traccar.TraccarPositionDTO;
import com.utvt.ApiSpringCafeSoft.integration.traccar.TraccarService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/traccar")
@CrossOrigin(origins = "*")
public class TraccarController {

    private final TraccarService traccarService;

    public TraccarController(TraccarService traccarService) {
        this.traccarService = traccarService;
    }

    @GetMapping("/devices")
    public ResponseEntity<?> listarDispositivos() {
        try {
            List<TraccarDeviceDTO> devices =
                    traccarService.listarDispositivos();
            return ResponseEntity.ok(devices);
        } catch (RuntimeException e) {
            return ResponseEntity.status(503)
                    .body(Map.of("error", e.getMessage()));
        }
    }

    @GetMapping("/positions")
    public ResponseEntity<?> listarPosiciones() {
        try {
            List<TraccarPositionDTO> posiciones =
                    traccarService.listarPosiciones();
            return ResponseEntity.ok(posiciones);
        } catch (RuntimeException e) {
            return ResponseEntity.status(503)
                    .body(Map.of("error", e.getMessage()));
        }
    }

    @GetMapping("/positions/repartidor/{id}")
    public ResponseEntity<?> posicionesPorRepartidor(
            @PathVariable Long id) {
        try {
            List<TraccarPositionDTO> posiciones =
                    traccarService.posicionesPorRepartidor(id);
            return ResponseEntity.ok(posiciones);
        } catch (RuntimeException e) {
            return ResponseEntity.status(503)
                    .body(Map.of("error", e.getMessage()));
        }
    }


    // GET /api/traccar/route/{traccarDeviceId}
@GetMapping("/route/{traccarDeviceId}")
public ResponseEntity<?> rutaDelDia(
        @PathVariable String traccarDeviceId) {
    try {
        return ResponseEntity.ok(
                traccarService.obtenerRutaDelDia(traccarDeviceId)
        );
    } catch (RuntimeException e) {
        return ResponseEntity.status(503)
                .body(Map.of("error", e.getMessage()));
    }
}

}
