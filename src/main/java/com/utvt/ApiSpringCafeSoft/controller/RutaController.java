package com.utvt.ApiSpringCafeSoft.controller;

import com.utvt.ApiSpringCafeSoft.model.Ruta;
import com.utvt.ApiSpringCafeSoft.service.RutaService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/rutas")
@CrossOrigin(origins = "*")
public class RutaController {

    @Autowired
    private RutaService rutaService;

    // ============================================
    // CRUD RUTAS
    // ============================================

    @PostMapping
    public ResponseEntity<Ruta> crearRuta(@RequestBody Map<String, Object> body) {

        String nombre = (String) body.get("nombre");
        Long repartidorId = body.get("repartidorId") != null
                ? Long.valueOf(body.get("repartidorId").toString()) : null;
        String diasReparto = (String) body.get("diasReparto");
        @SuppressWarnings("unchecked")
        List<Object> clienteIdsRaw = (List<Object>) body.get("clienteIds");
        List<Long> clienteIds = clienteIdsRaw == null ? null :
                clienteIdsRaw.stream().map(o -> Long.valueOf(o.toString())).toList();

        return ResponseEntity.ok(
                rutaService.crearRuta(nombre, repartidorId, diasReparto, clienteIds)
        );
    }

    @GetMapping
    public ResponseEntity<List<Ruta>> obtenerRutas() {
        return ResponseEntity.ok(rutaService.obtenerRutas());
    }

    @GetMapping("/{id}")
    public ResponseEntity<Ruta> obtenerRuta(@PathVariable Long id) {
        return ResponseEntity.ok(rutaService.obtenerPorId(id));
    }

    @PutMapping("/{id}")
    public ResponseEntity<Ruta> actualizarRuta(@PathVariable Long id,
                                               @RequestBody Map<String, Object> body) {

        String nombre = (String) body.get("nombre");
        Long repartidorId = body.get("repartidorId") != null
                ? Long.valueOf(body.get("repartidorId").toString()) : null;
        String diasReparto = (String) body.get("diasReparto");
        @SuppressWarnings("unchecked")
        List<Object> clienteIdsRaw = (List<Object>) body.get("clienteIds");
        List<Long> clienteIds = clienteIdsRaw == null ? null :
                clienteIdsRaw.stream().map(o -> Long.valueOf(o.toString())).toList();

        return ResponseEntity.ok(
                rutaService.actualizarRuta(id, nombre, repartidorId, diasReparto, clienteIds)
        );
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Map<String, String>> eliminarRuta(@PathVariable Long id) {
        rutaService.eliminarRuta(id);
        return ResponseEntity.ok(Map.of("mensaje", "Ruta eliminada correctamente"));
    }

    // ============================================
    // ACTIVAR RUTA
    // ============================================

    @PutMapping("/{id}/activar")
    public ResponseEntity<Ruta> activarRuta(@PathVariable Long id,
                                            @RequestBody Map<String, Long> body) {

        return ResponseEntity.ok(
                rutaService.activarRuta(id, body.get("cargaId"))
        );
    }

    // ============================================
    // CARGAS DISPONIBLES
    // ============================================

    @GetMapping("/cargas/disponibles")
    public ResponseEntity<?> cargasDisponibles() {
        return ResponseEntity.ok(rutaService.cargasDisponibles());
    }
}
