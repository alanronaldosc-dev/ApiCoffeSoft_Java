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

import java.util.Map;
import java.util.stream.Collectors;

/**
 * Manejo global de errores: devuelve JSON con "mensaje" y el código
 * HTTP correcto (403 / 404 / 400 / 429) para que la app web pueda
 * mostrar al usuario qué salió mal.
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    // Sin permiso para el recurso (IDOR / autorización)
    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<Map<String, String>> accesoDenegado(AccessDeniedException ex) {
        return ResponseEntity.status(HttpStatus.FORBIDDEN)
                .body(Map.of("mensaje", ex.getMessage()));
    }

    // Recurso no existe
    @ExceptionHandler(RecursoNoEncontradoException.class)
    public ResponseEntity<Map<String, String>> noEncontrado(RecursoNoEncontradoException ex) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(Map.of("mensaje", ex.getMessage()));
    }

    // Validación del servicio (p. ej. URL de Google Maps inválida)
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<Map<String, String>> argumentoInvalido(IllegalArgumentException ex) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(Map.of("mensaje", ex.getMessage()));
    }

    // Errores de validación de Bean Validation (@Valid)
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, String>> validacionFallida(MethodArgumentNotValidException ex) {
        String mensaje = ex.getBindingResult().getFieldErrors().stream()
                .map(e -> e.getField() + ": " + e.getDefaultMessage())
                .collect(Collectors.joining("; "));
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(Map.of("mensaje", mensaje));
    }

    // Cuerpo de la petición malformado (JSON inválido, tipos incorrectos)
    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<Map<String, String>> cuerpoNoLegible(HttpMessageNotReadableException ex) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(Map.of("mensaje",
                        "El cuerpo de la petición no es válido: "
                                + ex.getMessage()));
    }

    // Cualquier otro error inesperado (útil para diagnóstico)
    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String, String>> errorInesperado(Exception ex) {
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of("mensaje",
                        "Error interno: " + ex.getMessage()));
    }

    // Rate limiting
    @ExceptionHandler(RateLimiterService.RateLimitException.class)
    public ResponseEntity<Map<String, String>> rateLimitExcedido(RateLimiterService.RateLimitException ex) {
        return ResponseEntity.status(429)
                .body(Map.of("mensaje", ex.getMessage()));
    }
}
