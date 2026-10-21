package com.utvt.ApiSpringCafeSoft.exception;

/**
 * Excepción para recursos que no existen (respuesta HTTP 404).
 */
public class RecursoNoEncontradoException extends RuntimeException {

    public RecursoNoEncontradoException(String message) {
        super(message);
    }
}
