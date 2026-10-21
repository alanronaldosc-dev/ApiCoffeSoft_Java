package com.utvt.ApiSpringCafeSoft.service;

import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.Instant;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Limitador de velocidad en memoria (ventana deslizante).
 *
 * Protege operaciones destructivas (eliminar, baja) contra intentos
 * automatizados o repetición rápida desde la consola del navegador,
 * algo que un window.confirm no puede evitar.
 */
@Service
public class RateLimiterService {

    private final Map<String, Deque<Instant>> ventanas = new ConcurrentHashMap<>();

    /**
     * Registra un intento y lanza excepción si se supera el límite.
     *
     * @param operacion     identificador de la operación (ej. "eliminar-cliente")
     * @param usuarioId     id del usuario que ejecuta la operación
     * @param maxPermitidos máximo de ejecuciones permitidas en la ventana
     * @param ventana       duración de la ventana
     * @throws RateLimitException si se excede el límite
     */
    public void verificar(String operacion, Long usuarioId,
                          int maxPermitidos, Duration ventana) {
        String clave = operacion + ":" + usuarioId;
        Instant ahora = Instant.now();
        Instant limite = ahora.minus(ventana);

        Deque<Instant> eventos =
                ventanas.computeIfAbsent(clave, k -> new ArrayDeque<>());

        synchronized (eventos) {
            // Descartar eventos fuera de la ventana
            while (!eventos.isEmpty() && eventos.peekFirst().isBefore(limite)) {
                eventos.pollFirst();
            }

            if (eventos.size() >= maxPermitidos) {
                throw new RateLimitException(
                        "Demasiados intentos consecutivos. "
                                + "Espera unos momentos antes de repetir esta operación."
                );
            }

            eventos.addLast(ahora);
        }
    }

    public static class RateLimitException extends RuntimeException {
        public RateLimitException(String message) {
            super(message);
        }
    }
}
