
package com.utvt.ApiSpringCafeSoft.controller;

import com.utvt.ApiSpringCafeSoft.exception.RecursoNoEncontradoException;
import com.utvt.ApiSpringCafeSoft.service.RateLimiterService;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.server.ResponseStatusException;

import java.util.Map;
import java.util.stream.Collectors;

/**
 * Manejo global de excepciones de CafeSoft.
 *
 * Devuelve respuestas JSON con el campo "mensaje"
 * y conserva los codigos HTTP correspondientes.
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    // Acceso denegado: HTTP 403
    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<Map<String, String>> accesoDenegado(
            AccessDeniedException ex) {

        return ResponseEntity.status(HttpStatus.FORBIDDEN)
                .body(Map.of(
                        "mensaje",
                        ex.getMessage() != null
                                ? ex.getMessage()
                                : "Acceso denegado"
                ));
    }

    // Recurso no encontrado: HTTP 404
    @ExceptionHandler(RecursoNoEncontradoException.class)
    public ResponseEntity<Map<String, String>> noEncontrado(
            RecursoNoEncontradoException ex) {

        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(Map.of(
                        "mensaje",
                        ex.getMessage() != null
                                ? ex.getMessage()
                                : "Recurso no encontrado"
                ));
    }

    // Argumento invalido: HTTP 400
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<Map<String, String>> argumentoInvalido(
            IllegalArgumentException ex) {

        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(Map.of(
                        "mensaje",
                        ex.getMessage() != null
                                ? ex.getMessage()
                                : "Argumento invalido"
                ));
    }

    // Errores de Bean Validation: HTTP 400
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, String>> validacionFallida(
            MethodArgumentNotValidException ex) {

        String mensaje = ex.getBindingResult()
                .getFieldErrors()
                .stream()
                .map(e -> e.getField() + ": " + e.getDefaultMessage())
                .collect(Collectors.joining("; "));

        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(Map.of(
                        "mensaje",
                        mensaje.isBlank()
                                ? "Error de validacion"
                                : mensaje
                ));
    }

    // JSON malformado: HTTP 400
    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<Map<String, String>> cuerpoNoLegible(
            HttpMessageNotReadableException ex) {

        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(Map.of(
                        "mensaje",
                        "El cuerpo de la peticion no es valido: "
                                + ex.getMessage()
                ));
    }

    /*
     * Conserva los codigos de ResponseStatusException.
     *
     * Ejemplos:
     * 403 - Operacion no autorizada
     * 404 - Promocion inexistente
     * 409 - Promocion ya desactivada
     */
    @ExceptionHandler(ResponseStatusException.class)
    public ResponseEntity<Map<String, String>> errorConEstadoHttp(
            ResponseStatusException ex) {

        String mensaje = ex.getReason() != null
                ? ex.getReason()
                : "No se pudo completar la solicitud";

        return ResponseEntity
                .status(ex.getStatusCode())
                .body(Map.of("mensaje", mensaje));
    }

    // Limite de peticiones: HTTP 429
    @ExceptionHandler(RateLimiterService.RateLimitException.class)
    public ResponseEntity<Map<String, String>> rateLimitExcedido(
            RateLimiterService.RateLimitException ex) {

        return ResponseEntity.status(429)
                .body(Map.of(
                        "mensaje",
                        ex.getMessage() != null
                                ? ex.getMessage()
                                : "Limite de peticiones excedido"
                ));
    }

    // Errores inesperados: HTTP 500
    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String, String>> errorInesperado(
            Exception ex) {

        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of(
                        "mensaje",
                        "Ocurrio un error interno del servidor"
                ));
    }
}
