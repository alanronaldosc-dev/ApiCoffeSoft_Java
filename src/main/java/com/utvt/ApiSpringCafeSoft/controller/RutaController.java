package com.utvt.ApiSpringCafeSoft.controller;

import com.utvt.ApiSpringCafeSoft.model.Cliente;
import com.utvt.ApiSpringCafeSoft.model.Ruta;
import com.utvt.ApiSpringCafeSoft.service.RutaService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Controlador para la gestión de rutas.
 *
 * HU-013 / HU-014 / HU-015
 *
 * Permite:
 * - Crear rutas
 * - Editar rutas
 * - Eliminar rutas
 * - Asignar repartidor
 * - Asignar clientes
 * - Activar una ruta
 * - Consultar los clientes de una ruta
 * - Consultar los clientes de la ruta activa del repartidor
 */
@RestController
@RequestMapping("/api/rutas")
@CrossOrigin(origins = "*")
public class RutaController {

    @Autowired
    private RutaService rutaService;

    // ============================================
    // CREAR RUTA
    // ============================================

    @PostMapping
    public ResponseEntity<?> crearRuta(
            @RequestBody Map<String, Object> body) {

        try {

            String nombre =
                    body.get("nombre") != null
                            ? body.get("nombre").toString().trim()
                            : null;

            Long repartidorId =
                    body.get("repartidorId") != null
                            ? Long.valueOf(
                                    body.get("repartidorId").toString()
                            )
                            : null;

            String diasReparto =
                    body.get("diasReparto") != null
                            ? body.get("diasReparto").toString()
                            : null;

            @SuppressWarnings("unchecked")
            List<Object> clienteIdsRaw =
                    (List<Object>) body.get("clienteIds");

            List<Long> clienteIds =
                    clienteIdsRaw == null
                            ? null
                            : clienteIdsRaw
                                    .stream()
                                    .map(
                                            id ->
                                                    Long.valueOf(
                                                            id.toString()
                                                    )
                                    )
                                    .toList();

            if (nombre == null || nombre.isBlank()) {
                return ResponseEntity
                        .badRequest()
                        .body(
                                Map.of(
                                        "error",
                                        "El nombre de la ruta es obligatorio"
                                )
                        );
            }

            Ruta ruta =
                    rutaService.crearRuta(
                            nombre,
                            repartidorId,
                            diasReparto,
                            clienteIds
                    );

            return new ResponseEntity<>(
                    ruta,
                    HttpStatus.CREATED
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

    // ============================================
    // OBTENER TODAS LAS RUTAS
    // ============================================

    @GetMapping
    public ResponseEntity<?> obtenerRutas() {

        try {

            List<Ruta> rutas =
                    rutaService.obtenerRutas();

            return ResponseEntity.ok(rutas);

        } catch (RuntimeException e) {

            return ResponseEntity
                    .status(
                            HttpStatus.INTERNAL_SERVER_ERROR
                    )
                    .body(
                            Map.of(
                                    "error",
                                    e.getMessage()
                            )
                    );
        }
    }

    // ============================================
    // CARGAS DISPONIBLES
    // IMPORTANTE:
    // Este endpoint debe ir antes de /{id}
    // para mantener el código claro.
    // ============================================

    @GetMapping("/cargas/disponibles")
    public ResponseEntity<?> cargasDisponibles() {

        try {

            return ResponseEntity.ok(
                    rutaService.cargasDisponibles()
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

    // ============================================
    // HU-015
    // CLIENTES DE LA RUTA ACTIVA DEL REPARTIDOR
    // ============================================

    @GetMapping(
            "/repartidor/{repartidorId}/activa/clientes"
    )
    public ResponseEntity<?> clientesRutaActiva(
            @PathVariable Long repartidorId) {

        try {

            System.out.println(
                    "RUTA ACTIVA -> repartidorId: "
                            + repartidorId
            );

            return ResponseEntity.ok(
                    rutaService
                            .clientesRutaActivaDelRepartidor(
                                    repartidorId
                            )
            );

        } catch (RuntimeException e) {

            System.out.println(
                    "ERROR RUTA ACTIVA -> "
                            + e.getMessage()
            );

            return ResponseEntity
                    .status(HttpStatus.NOT_FOUND)
                    .body(
                            Map.of(
                                    "error",
                                    e.getMessage()
                            )
                    );
        }
    }

    // ============================================
    // HU-015
    // CLIENTES DE UNA RUTA
    // ============================================

    @GetMapping("/{id}/clientes")
    public ResponseEntity<?> clientesRuta(
            @PathVariable Long id) {

        try {

            return ResponseEntity.ok(
                    rutaService
                            .obtenerClientesRuta(
                                    id
                            )
            );

        } catch (RuntimeException e) {

            return ResponseEntity
                    .status(HttpStatus.NOT_FOUND)
                    .body(
                            Map.of(
                                    "error",
                                    e.getMessage()
                            )
                    );
        }
    }

    // ============================================
    // OBTENER RUTA POR ID
    // ============================================

    @GetMapping("/{id}")
    public ResponseEntity<?> obtenerRuta(
            @PathVariable Long id) {

        try {

            Ruta ruta =
                    rutaService.obtenerPorId(id);

            return ResponseEntity.ok(ruta);

        } catch (RuntimeException e) {

            return ResponseEntity
                    .status(HttpStatus.NOT_FOUND)
                    .body(
                            Map.of(
                                    "error",
                                    e.getMessage()
                            )
                    );
        }
    }

    // ============================================
    // ACTUALIZAR RUTA
    // ============================================

    @PutMapping("/{id}")
    public ResponseEntity<?> actualizarRuta(
            @PathVariable Long id,
            @RequestBody Map<String, Object> body) {

        try {

            String nombre =
                    body.get("nombre") != null
                            ? body.get("nombre").toString().trim()
                            : null;

            Long repartidorId =
                    body.get("repartidorId") != null
                            ? Long.valueOf(
                                    body.get("repartidorId").toString()
                            )
                            : null;

            String diasReparto =
                    body.get("diasReparto") != null
                            ? body.get("diasReparto").toString()
                            : null;

            @SuppressWarnings("unchecked")
            List<Object> clienteIdsRaw =
                    (List<Object>) body.get("clienteIds");

            List<Long> clienteIds =
                    clienteIdsRaw == null
                            ? null
                            : clienteIdsRaw
                                    .stream()
                                    .map(
                                            valor ->
                                                    Long.valueOf(
                                                            valor.toString()
                                                    )
                                    )
                                    .toList();

            Ruta ruta =
                    rutaService.actualizarRuta(
                            id,
                            nombre,
                            repartidorId,
                            diasReparto,
                            clienteIds
                    );

            return ResponseEntity.ok(ruta);

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

    // ============================================
    // DESACTIVAR RUTA
    // ============================================

    @PutMapping("/{id}/desactivar")
    public ResponseEntity<?> desactivarRuta(
            @PathVariable Long id) {

        try {
            return ResponseEntity.ok(
                    rutaService.desactivarRuta(id)
            );
        } catch (RuntimeException e) {
            return ResponseEntity
                    .badRequest()
                    .body(Map.of("error", e.getMessage()));
        }
    }

    // ============================================
    // ACTIVAR RUTA
    // ============================================

    @PutMapping("/{id}/activar")
    public ResponseEntity<?> activarRuta(
            @PathVariable Long id) {

        try {

            Ruta ruta =
                    rutaService.activarRuta(
                            id
                    );

            return ResponseEntity.ok(ruta);

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

    // ============================================
    // ELIMINAR RUTA
    // ============================================

    @DeleteMapping("/{id}")
    public ResponseEntity<?> eliminarRuta(
            @PathVariable Long id) {

        try {

            rutaService.eliminarRuta(id);

            Map<String, Object> respuesta =
                    new HashMap<>();

            respuesta.put(
                    "mensaje",
                    "Ruta eliminada correctamente"
            );

            respuesta.put(
                    "rutaId",
                    id
            );

            return ResponseEntity.ok(
                    respuesta
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