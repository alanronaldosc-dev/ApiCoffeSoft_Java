package com.utvt.ApiSpringCafeSoft.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.utvt.ApiSpringCafeSoft.model.Usuario;
import com.utvt.ApiSpringCafeSoft.model.Venta;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.Map;

/**
 * Envío de notificaciones push a través de Expo Push Service.
 *
 * Expo recibe el mensaje y lo entrega a FCM (Android) o APNs
 * (iOS). El dispositivo debe haber registrado previamente su
 * ExponentPushToken en la tabla usuarios (columna push_token).
 */
@Service
public class NotificacionService {

    private final RestTemplate restTemplate;

    @Autowired
    private ObjectMapper objectMapper;

    public NotificacionService() {
        // Evita que una falla de red con Expo bloquee la venta
        // durante demasiado tiempo.
        SimpleClientHttpRequestFactory factory =
                new SimpleClientHttpRequestFactory();
        factory.setConnectTimeout(5000);
        factory.setReadTimeout(5000);
        this.restTemplate = new RestTemplate(factory);
    }

    @Value("${app.notificaciones.enabled:true}")
    private boolean habilitado;

    @Value("${app.notificaciones.expo-url:https://exp.host/--/api/v2/push/send}")
    private String expoUrl;

    @Value("${app.notificaciones.channel-id:pedidos}")
    private String channelId;

    @Value("${app.notificaciones.access-token:}")
    private String accessToken;

    // =========================================================
    // NOTIFICACIONES DE PEDIDOS
    // =========================================================

    /**
     * Envía una notificación al usuario que registró la venta
     * cuando su pedido cambia de estado.
     *
     * @return true si Expo aceptó el mensaje.
     */
    public boolean enviarCambioEstado(Venta venta, String estado) {

        if (venta == null || venta.getUsuario() == null) {
            return false;
        }

        Usuario usuario = venta.getUsuario();

        if (!tokenValido(usuario.getPushToken())) {
            return false;
        }

        Map<String, Object> data = new HashMap<>();
        data.put("tipo", "PEDIDO_ESTADO");
        data.put("pedidoId", venta.getId());
        data.put("folio", venta.getFolio());
        data.put("estado", estado);
        data.put("pantalla", "PedidosRepartidor");

        return enviar(
                usuario.getPushToken(),
                titulo(estado),
                cuerpo(venta, estado),
                data
        );
    }

    // =========================================================
    // NOTIFICACIÓN DE PRUEBA (Parte 3 de la práctica)
    // =========================================================

    /**
     * Permite enviar un mensaje manual desde el backend para
     * comprobar la configuración de Expo sin depender de una venta.
     */
    public boolean enviarPrueba(
            String token,
            String titulo,
            String cuerpo,
            Map<String, Object> data) {

        if (!tokenValido(token)) {
            return false;
        }

        Map<String, Object> payload = data != null
                ? new HashMap<>(data)
                : new HashMap<>();

        payload.putIfAbsent("tipo", "PRUEBA");

        return enviar(
                token,
                titulo != null && !titulo.isBlank()
                        ? titulo
                        : "Prueba AquaSoft",
                cuerpo != null && !cuerpo.isBlank()
                        ? cuerpo
                        : "Notificación de prueba",
                payload
        );
    }

    // =========================================================
    // ENVÍO A EXPO PUSH SERVICE
    // =========================================================

    private boolean enviar(
            String token,
            String titulo,
            String cuerpo,
            Map<String, Object> data) {

        if (!habilitado) {
            System.out.println("Notificaciones push deshabilitadas por configuración");
            return false;
        }

        Map<String, Object> mensaje = new HashMap<>();
        mensaje.put("to", token.trim());
        mensaje.put("sound", "default");
        mensaje.put("title", titulo);
        mensaje.put("body", cuerpo);
        mensaje.put("data", data);
        mensaje.put("channelId", channelId);
        mensaje.put("priority", "high");

        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);

            // Opcional: requerido si el proyecto tiene Push Security activo.
            if (accessToken != null && !accessToken.isBlank()) {
                headers.setBearerAuth(accessToken.trim());
            }

            ResponseEntity<String> respuesta = restTemplate.postForEntity(
                    expoUrl,
                    new HttpEntity<>(mensaje, headers),
                    String.class
            );

            if (!respuesta.getStatusCode().is2xxSuccessful()) {
                System.err.println(
                        "Expo Push respondió HTTP " + respuesta.getStatusCode()
                );
                return false;
            }

            return respuestaAceptada(respuesta.getBody());

        } catch (Exception e) {
            System.err.println(
                    "No se pudo enviar la notificación push: " + e.getMessage()
            );
            return false;
        }
    }

    /**
     * Expo responde con data.status = "ok" o "error".
     * El mensaje puede venir como objeto o como arreglo.
     */
    private boolean respuestaAceptada(String body) {

        if (body == null || body.isBlank()) {
            return true;
        }

        try {
            JsonNode root = objectMapper.readTree(body);
            JsonNode data = root.path("data");

            if (data.isArray()) {
                data = data.get(0);
            }

            String status = data.path("status").asText("ok");

            if ("error".equalsIgnoreCase(status)) {
                System.err.println(
                        "Expo Push rechazó el mensaje: "
                        + data.path("message").asText("sin detalle")
                );
                return false;
            }

            return true;

        } catch (Exception e) {
            // Si no se puede interpretar la respuesta, el HTTP 2xx
            // sigue siendo un indicio suficiente de aceptación.
            return true;
        }
    }

    private boolean tokenValido(String token) {
        if (token == null || token.isBlank()) {
            return false;
        }

        String valor = token.trim();

        return (valor.startsWith("ExponentPushToken[")
                || valor.startsWith("ExpoPushToken["))
                && valor.endsWith("]");
    }

    // =========================================================
    // MENSAJES
    // =========================================================

    private String titulo(String estado) {
        return switch (estado) {
            case "PENDIENTE" -> "Solicitud registrada";
            case "RECIBIDA" -> "Solicitud recibida";
            case "EN_PROCESO" -> "Pedido en proceso";
            case "ENTREGADA" -> "Pedido finalizado";
            default -> "Actualización de tu pedido";
        };
    }

    private String cuerpo(Venta venta, String estado) {

        String folio = venta.getFolio() != null
                ? venta.getFolio()
                : "#" + venta.getId();

        return switch (estado) {
            case "PENDIENTE" -> "Recibimos tu pedido " + folio + ".";
            case "RECIBIDA" -> "El personal ya recibió tu pedido " + folio + ".";
            case "EN_PROCESO" -> "Tu pedido " + folio + " está en preparación.";
            case "ENTREGADA" -> "Tu pedido " + folio + " fue entregado.";
            default -> "Tu pedido " + folio + " cambió de estado.";
        };
    }
}
