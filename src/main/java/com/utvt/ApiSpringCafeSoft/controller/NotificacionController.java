package com.utvt.ApiSpringCafeSoft.controller;

import com.utvt.ApiSpringCafeSoft.service.NotificacionService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/notificaciones")
@Tag(
    name = "Notificaciones",
    description = "Envío de notificaciones push mediante Expo Push Service"
)
public class NotificacionController {

    private final NotificacionService notificacionService;

    public NotificacionController(NotificacionService notificacionService) {
        this.notificacionService = notificacionService;
    }

    // =========================================================
    // NOTIFICACIÓN DE PRUEBA
    // =========================================================

    @Operation(
        summary = "Enviar una notificación de prueba",
        description =
            "Envía un mensaje desde el backend a un ExponentPushToken " +
            "para comprobar la configuración de Expo Push Service."
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "✅ Solicitud procesada (revisar el campo enviada)"
        )
    })
    @PostMapping("/prueba")
    public ResponseEntity<Map<String, Object>> enviarPrueba(
            @RequestBody Map<String, Object> body) {

        Object tokenRecibido = body.get("token");
        String token = tokenRecibido instanceof String
                ? (String) tokenRecibido
                : null;

        String titulo = (String) body.getOrDefault("title", "Prueba AquaSoft");
        String cuerpo = (String) body.getOrDefault("body", "Notificación de prueba");

        Object dataRecibida = body.get("data");

        @SuppressWarnings("unchecked")
        Map<String, Object> data = dataRecibida instanceof Map<?, ?>
                ? (Map<String, Object>) dataRecibida
                : null;

        boolean enviada = notificacionService.enviarPrueba(
                token,
                titulo,
                cuerpo,
                data
        );

        Map<String, Object> respuesta = new HashMap<>();
        respuesta.put("enviada", enviada);
        respuesta.put(
            "mensaje",
            enviada
                ? "Notificación enviada a Expo Push Service"
                : "No se pudo enviar. Revisa el token y la configuración."
        );

        return ResponseEntity.ok(respuesta);
    }
}
