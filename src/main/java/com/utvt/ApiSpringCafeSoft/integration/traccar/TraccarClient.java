package com.utvt.ApiSpringCafeSoft.integration.traccar;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

@Component
public class TraccarClient {

    private static final Logger log =
            LoggerFactory.getLogger(TraccarClient.class);

    private final TraccarConfig config;
    private final RestTemplate restTemplate;

    // JSESSIONID en memoria — se regenera si expira
    private String sessionCookie = null;

    public TraccarClient(TraccarConfig config) {
        this.config = config;
        this.restTemplate = new RestTemplate();
    }

    // ============================================
    // LOGIN — obtiene JSESSIONID
    // ============================================

    public void login() {
        try {
            String url = config.getBaseUrl() + "/api/session";

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

            MultiValueMap<String, String> body = new LinkedMultiValueMap<>();
            body.add("email", config.getEmail());
            body.add("password", config.getPassword());

            HttpEntity<MultiValueMap<String, String>> request =
                    new HttpEntity<>(body, headers);

            ResponseEntity<String> response =
                    restTemplate.postForEntity(url, request, String.class);

            List<String> cookies = response.getHeaders()
                    .get(HttpHeaders.SET_COOKIE);

            if (cookies != null) {
                for (String cookie : cookies) {
                    if (cookie.startsWith("JSESSIONID")) {
                        sessionCookie = cookie.split(";")[0];
                        log.info("Traccar login exitoso");
                        return;
                    }
                }
            }

            log.warn("Traccar login: no se recibió JSESSIONID");

        } catch (Exception e) {
            log.error("Error en login Traccar: {}", e.getMessage());
            throw new RuntimeException(
                    "El servicio de rastreo no está disponible");
        }
    }

    // ============================================
    // OBTENER DISPOSITIVOS
    // ============================================

    public List<TraccarDeviceDTO> getDevices() {
        return executeWithRetry(() -> {
            String url = config.getBaseUrl() + "/api/devices";

            ResponseEntity<TraccarDeviceDTO[]> response =
                    restTemplate.exchange(
                            url,
                            HttpMethod.GET,
                            buildAuthRequest(),
                            TraccarDeviceDTO[].class
                    );

            TraccarDeviceDTO[] arr = response.getBody();
            return arr != null ? Arrays.asList(arr) : Collections.emptyList();
        });
    }

    // ============================================
    // OBTENER POSICIONES
    // ============================================

    public List<TraccarPositionDTO> getPositions() {
        return executeWithRetry(() -> {
            String url = config.getBaseUrl() + "/api/positions";

            ResponseEntity<TraccarPositionDTO[]> response =
                    restTemplate.exchange(
                            url,
                            HttpMethod.GET,
                            buildAuthRequest(),
                            TraccarPositionDTO[].class
                    );

            TraccarPositionDTO[] arr = response.getBody();
            return arr != null ? Arrays.asList(arr) : Collections.emptyList();
        });
    }

    // ============================================
    // HELPERS INTERNOS
    // ============================================

    private HttpEntity<Void> buildAuthRequest() {
        HttpHeaders headers = new HttpHeaders();
        if (sessionCookie != null) {
            headers.set(HttpHeaders.COOKIE, sessionCookie);
        }
        return new HttpEntity<>(headers);
    }

    /**
     * Si la sesión expiró (401), hace re-login y reintenta una vez.
     */
    private <T> T executeWithRetry(TraccarOperation<T> operation) {
        if (sessionCookie == null) {
            login();
        }
        try {
            return operation.execute();
        } catch (Exception e) {
            String msg = e.getMessage() != null ? e.getMessage() : "";
            if (msg.contains("401") || msg.contains("Unauthorized")) {
                log.warn("Sesión Traccar expirada, re-login...");
                sessionCookie = null;
                login();
                return operation.execute();
            }
            log.error("Error consultando Traccar: {}", msg);
            throw new RuntimeException(
                    "El servicio de rastreo no está disponible");
        }
    }

    @FunctionalInterface
    private interface TraccarOperation<T> {
        T execute();
    }

    // ============================================
// HISTORIAL DE RUTA DEL DÍA
// ============================================

public List<TraccarPositionDTO> getRoute(Long deviceId) {
    return executeWithRetry(() -> {
        // Rango: desde medianoche de hoy hasta ahora (UTC)
        java.time.ZonedDateTime ahora =
                java.time.ZonedDateTime.now(java.time.ZoneOffset.UTC);
        java.time.ZonedDateTime inicio =
                ahora.toLocalDate().atStartOfDay(java.time.ZoneOffset.UTC);

        String from = inicio.format(
                java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'"));
        String to = ahora.format(
                java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'"));

        String url = config.getBaseUrl()
                + "/api/reports/route?deviceId=" + deviceId
                + "&from=" + from
                + "&to=" + to;

        ResponseEntity<TraccarPositionDTO[]> response =
                restTemplate.exchange(
                        url,
                        org.springframework.http.HttpMethod.GET,
                        buildAuthRequest(),
                        TraccarPositionDTO[].class
                );

        TraccarPositionDTO[] arr = response.getBody();
        return arr != null ? java.util.Arrays.asList(arr) : java.util.Collections.emptyList();
    });
}

}
