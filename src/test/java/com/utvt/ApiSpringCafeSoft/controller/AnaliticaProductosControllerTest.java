
package com.utvt.ApiSpringCafeSoft.controller;

import com.utvt.ApiSpringCafeSoft.service.AnaliticaProductosService;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AnaliticaProductosControllerTest {

    @Mock
    private AnaliticaProductosService servicio;

    @InjectMocks
    private AnaliticaProductosController controller;

    // =================================================
    // HU-055: COMBINACIONES
    // =================================================

    @Test
    void combinacionesDevuelve200() {

        LocalDate inicio = LocalDate.of(2026, 11, 1);
        LocalDate fin = LocalDate.of(2026, 11, 15);

        var combinacion =
            new AnaliticaProductosService.Combinacion(
                List.of(
                    new AnaliticaProductosService.ProductoSimple(
                        1L, "Garrafon"
                    ),
                    new AnaliticaProductosService.ProductoSimple(
                        2L, "Botella"
                    )
                ),
                3L,
                75.0
            );

        var datos = List.of(combinacion);

        when(servicio.combinaciones(inicio, fin, 2, 10))
            .thenReturn(datos);

        ResponseEntity<?> respuesta =
            controller.combinaciones(inicio, fin, 2, 10);

        assertEquals(HttpStatus.OK, respuesta.getStatusCode());
        assertEquals(datos, respuesta.getBody());

        verify(servicio).combinaciones(inicio, fin, 2, 10);
    }

    @Test
    void combinacionesSinResultadosDevuelve200() {

        when(servicio.combinaciones(null, null, 2, 10))
            .thenReturn(List.of());

        ResponseEntity<?> respuesta =
            controller.combinaciones(null, null, 2, 10);

        assertEquals(HttpStatus.OK, respuesta.getStatusCode());
        assertEquals(List.of(), respuesta.getBody());
    }

    @Test
    void combinacionesInvalidasDevuelven400() {

        when(servicio.combinaciones(null, null, 0, 10))
            .thenThrow(
                new IllegalArgumentException("Soporte invalido")
            );

        ResponseEntity<?> respuesta =
            controller.combinaciones(null, null, 0, 10);

        assertEquals(
            HttpStatus.BAD_REQUEST,
            respuesta.getStatusCode()
        );

        assertEquals(
            Map.of("error", "Soporte invalido"),
            respuesta.getBody()
        );
    }

    // =================================================
    // HU-056: RECOMENDACIONES
    // =================================================

    @Test
    void recomendacionesDevuelve200() {

        var recomendacion =
            new AnaliticaProductosService.Recomendacion(
                2L,
                "Botella",
                50.0,
                3L,
                "Comprado junto con productos del historial"
            );

        var datos = List.of(recomendacion);

        when(servicio.recomendaciones(10L, 5))
            .thenReturn(datos);

        ResponseEntity<?> respuesta =
            controller.recomendaciones(10L, 5);

        assertEquals(HttpStatus.OK, respuesta.getStatusCode());
        assertEquals(datos, respuesta.getBody());

        verify(servicio).recomendaciones(10L, 5);
    }

    @Test
    void recomendacionesSinHistorialDevuelve200() {

        when(servicio.recomendaciones(10L, 5))
            .thenReturn(List.of());

        ResponseEntity<?> respuesta =
            controller.recomendaciones(10L, 5);

        assertEquals(HttpStatus.OK, respuesta.getStatusCode());
        assertEquals(List.of(), respuesta.getBody());
    }

    @Test
    void recomendacionesInvalidasDevuelven400() {

        when(servicio.recomendaciones(0L, 5))
            .thenThrow(
                new IllegalArgumentException("Cliente invalido")
            );

        ResponseEntity<?> respuesta =
            controller.recomendaciones(0L, 5);

        assertEquals(
            HttpStatus.BAD_REQUEST,
            respuesta.getStatusCode()
        );

        assertEquals(
            Map.of("error", "Cliente invalido"),
            respuesta.getBody()
        );
    }
}
