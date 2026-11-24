package com.utvt.ApiSpringCafeSoft.controller;

import com.utvt.ApiSpringCafeSoft.dto.ProductoMasVendidoDTO;
import com.utvt.ApiSpringCafeSoft.dto.ReporteVentasDTO;
import com.utvt.ApiSpringCafeSoft.service.VentaService;

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
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

/** Pruebas de las ramas de éxito y error de los endpoints nuevos de HU-050. */
@ExtendWith(MockitoExtension.class)
class VentaControllerHu050CoverageTest {

    @Mock
    private VentaService ventaService;

    @InjectMocks
    private VentaController controller;

    @Test
    void reportePersonalizadoDevuelve200YDelegacionConFechas() {
        LocalDate desde = LocalDate.of(2026, 1, 1);
        LocalDate hasta = LocalDate.of(2026, 1, 31);
        ReporteVentasDTO esperado = new ReporteVentasDTO();

        when(ventaService.obtenerReporteVentas("personalizado", desde, hasta))
            .thenReturn(esperado);

        ResponseEntity<?> respuesta =
            controller.obtenerReporteVentas("personalizado", desde, hasta);

        assertEquals(HttpStatus.OK, respuesta.getStatusCode());
        assertSame(esperado, respuesta.getBody());
        verify(ventaService).obtenerReporteVentas("personalizado", desde, hasta);
    }

    @Test
    void reporteConPeriodoInvalidoDevuelve400ConMensaje() {
        LocalDate desde = LocalDate.of(2026, 1, 1);
        LocalDate hasta = LocalDate.of(2026, 1, 31);
        when(ventaService.obtenerReporteVentas("personalizado", desde, hasta))
            .thenThrow(new IllegalArgumentException("Rango inválido"));

        ResponseEntity<?> respuesta =
            controller.obtenerReporteVentas("personalizado", desde, hasta);

        assertEquals(HttpStatus.BAD_REQUEST, respuesta.getStatusCode());
        assertEquals("Rango inválido", ((Map<?, ?>) respuesta.getBody()).get("mensaje"));
    }

    @Test
    void topProductosDevuelve200YLista() {
        List<ProductoMasVendidoDTO> esperado = List.of(
            new ProductoMasVendidoDTO(5L, "Café", 8L, 240.0)
        );

        when(ventaService.obtenerProductosMasVendidos("semana", null, null))
            .thenReturn(esperado);

        ResponseEntity<?> respuesta =
            controller.obtenerProductosMasVendidos("semana", null, null);

        assertEquals(HttpStatus.OK, respuesta.getStatusCode());
        assertSame(esperado, respuesta.getBody());
        verify(ventaService).obtenerProductosMasVendidos("semana", null, null);
    }

    @Test
    void topProductosConRangoInvalidoDevuelve400ConMensaje() {
        LocalDate desde = LocalDate.of(2026, 2, 10);
        LocalDate hasta = LocalDate.of(2026, 2, 1);
        when(ventaService.obtenerProductosMasVendidos("personalizado", desde, hasta))
            .thenThrow(new IllegalArgumentException("La fecha inicial no puede ser posterior a la final."));

        ResponseEntity<?> respuesta =
            controller.obtenerProductosMasVendidos("personalizado", desde, hasta);

        assertEquals(HttpStatus.BAD_REQUEST, respuesta.getStatusCode());
        assertEquals(
            "La fecha inicial no puede ser posterior a la final.",
            ((Map<?, ?>) respuesta.getBody()).get("mensaje")
        );
    }
}
