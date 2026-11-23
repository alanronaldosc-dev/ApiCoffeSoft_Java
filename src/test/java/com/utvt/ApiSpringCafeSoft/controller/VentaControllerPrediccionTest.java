package com.utvt.ApiSpringCafeSoft.controller;

import com.utvt.ApiSpringCafeSoft.dto.PrediccionProductosDTO;
import com.utvt.ApiSpringCafeSoft.service.VentaService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class VentaControllerPrediccionTest {

    @Mock
    private VentaService service;

    @InjectMocks
    private VentaController controller;

    @Test
    void prediccionDevuelve200() {
        PrediccionProductosDTO prediccion = new PrediccionProductosDTO();
        prediccion.setHorizonteDias(7);
        prediccion.setEsEstimacion(true);
        prediccion.setMasVendidos(List.of());
        prediccion.setMenosVendidos(List.of());

        when(service.obtenerPrediccionProductos(7)).thenReturn(prediccion);

        ResponseEntity<?> respuesta = controller.obtenerPrediccionProductos(7);

        assertEquals(HttpStatus.OK, respuesta.getStatusCode());
        assertSame(prediccion, respuesta.getBody());
    }

    @Test
    void horizonteNoValidoDevuelve400() {
        when(service.obtenerPrediccionProductos(15)).thenThrow(
            new IllegalArgumentException("Horizonte no válido. Usa: 7, 30 o 90 días.")
        );

        ResponseEntity<?> respuesta = controller.obtenerPrediccionProductos(15);

        assertEquals(HttpStatus.BAD_REQUEST, respuesta.getStatusCode());
        assertEquals(
            "Horizonte no válido. Usa: 7, 30 o 90 días.",
            ((Map<?, ?>) respuesta.getBody()).get("mensaje")
        );
    }
}
