
package com.utvt.ApiSpringCafeSoft.service;

import com.utvt.ApiSpringCafeSoft.model.*;
import com.utvt.ApiSpringCafeSoft.repository.ProductoRepository;
import com.utvt.ApiSpringCafeSoft.repository.VentaRepository;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AnaliticaProductosServiceTest {

    @Mock
    private VentaRepository ventas;

    @Mock
    private ProductoRepository productos;

    @InjectMocks
    private AnaliticaProductosService servicio;

    private static final LocalDate FECHA =
        LocalDate.of(2026, 11, 15);

    private Producto producto(Long id, String nombre) {
        Producto p = new Producto();
        p.setId(id);
        p.setNombre(nombre);
        p.setPrecio(50.0);
        return p;
    }

    private Venta ticket(
            Long clienteId,
            LocalDate fecha,
            Producto... lista) {

        Venta venta = new Venta();
        venta.setClienteRutaId(clienteId);

        if (fecha != null) {
            venta.setFecha(fecha.atTime(12, 0));
        }

        for (Producto p : lista) {
            VentaDetalle detalle = new VentaDetalle();
            detalle.setProducto(p);
            detalle.setCantidad(1);
            detalle.setSubtotal(50.0);
            venta.addDetalle(detalle);
        }

        return venta;
    }

    // =================================================
    // HU-055: COMBINACIONES FRECUENTES
    // =================================================

    @Test
    void combinacionesDetectaProductosFrecuentes() {

        Producto a = producto(1L, "Garrafon");
        Producto b = producto(2L, "Agua");
        Producto c = producto(3L, "Botella");

        when(ventas.findAll()).thenReturn(List.of(
            ticket(1L, FECHA, a, b),
            ticket(2L, FECHA, a, b),
            ticket(3L, FECHA, a, c)
        ));

        var resultado =
            servicio.combinaciones(null, null, 2, 10);

        assertEquals(1, resultado.size());
        assertEquals(2L, resultado.get(0).frecuencia());

        assertEquals(
            66.6667,
            resultado.get(0).soporte(),
            0.001
        );

        assertEquals(
            "Garrafon",
            resultado.get(0).productos().get(0).nombre()
        );

        assertEquals(
            "Agua",
            resultado.get(0).productos().get(1).nombre()
        );
    }

    @Test
    void combinacionesOrdenaPorFrecuencia() {

        Producto a = producto(1L, "A");
        Producto b = producto(2L, "B");
        Producto c = producto(3L, "C");

        when(ventas.findAll()).thenReturn(List.of(
            ticket(1L, FECHA, a, b, c),
            ticket(2L, FECHA, a, b),
            ticket(3L, FECHA, a, b)
        ));

        var resultado =
            servicio.combinaciones(null, null, 1, 10);

        assertEquals(3, resultado.size());
        assertEquals(3L, resultado.get(0).frecuencia());
        assertEquals(1L, resultado.get(1).frecuencia());
        assertEquals(1L, resultado.get(2).frecuencia());

        assertEquals(
            2L,
            resultado.get(1).productos().get(1).id()
                == 3L
                ? resultado.get(2).productos().get(0).id()
                : resultado.get(1).productos().get(0).id()
        );
    }

    @Test
    void combinacionesRespetaLimite() {

        Producto a = producto(1L, "A");
        Producto b = producto(2L, "B");
        Producto c = producto(3L, "C");

        when(ventas.findAll()).thenReturn(List.of(
            ticket(1L, FECHA, a, b, c)
        ));

        var resultado =
            servicio.combinaciones(null, null, 1, 1);

        assertEquals(1, resultado.size());
        assertEquals(1L, resultado.get(0).frecuencia());
    }

    @Test
    void combinacionesFiltraPorFechas() {

        Producto a = producto(1L, "A");
        Producto b = producto(2L, "B");

        when(ventas.findAll()).thenReturn(List.of(
            ticket(1L, FECHA.minusDays(10), a, b),
            ticket(2L, FECHA, a, b),
            ticket(3L, FECHA.plusDays(10), a, b),
            ticket(4L, null, a, b)
        ));

        var resultado = servicio.combinaciones(
            FECHA.minusDays(1),
            FECHA.plusDays(1),
            1,
            10
        );

        assertEquals(1, resultado.size());
        assertEquals(1L, resultado.get(0).frecuencia());
        assertEquals(100.0, resultado.get(0).soporte(), 0.001);
    }

    @Test
    void combinacionesAceptaSoloFechaInicio() {

        Producto a = producto(1L, "A");
        Producto b = producto(2L, "B");

        when(ventas.findAll()).thenReturn(List.of(
            ticket(1L, FECHA.minusDays(1), a, b),
            ticket(2L, FECHA, a, b)
        ));

        var resultado =
            servicio.combinaciones(FECHA, null, 1, 10);

        assertEquals(1, resultado.size());
        assertEquals(1L, resultado.get(0).frecuencia());
    }

    @Test
    void combinacionesAceptaSoloFechaFin() {

        Producto a = producto(1L, "A");
        Producto b = producto(2L, "B");

        when(ventas.findAll()).thenReturn(List.of(
            ticket(1L, FECHA, a, b),
            ticket(2L, FECHA.plusDays(1), a, b)
        ));

        var resultado =
            servicio.combinaciones(null, FECHA, 1, 10);

        assertEquals(1, resultado.size());
        assertEquals(1L, resultado.get(0).frecuencia());
    }

    @Test
    void combinacionesNoDuplicaProductosDelTicket() {

        Producto a = producto(1L, "A");
        Producto b = producto(2L, "B");

        when(ventas.findAll()).thenReturn(List.of(
            ticket(1L, FECHA, a, a, b, b)
        ));

        var resultado =
            servicio.combinaciones(null, null, 1, 10);

        assertEquals(1, resultado.size());
        assertEquals(1L, resultado.get(0).frecuencia());
    }

    @Test
    void combinacionesIgnoraProductosInvalidos() {

        Producto a = producto(1L, "A");
        Producto b = producto(2L, "B");
        Producto sinId = producto(null, "Sin ID");

        when(ventas.findAll()).thenReturn(List.of(
            ticket(1L, FECHA, a, b, sinId, null)
        ));

        var resultado =
            servicio.combinaciones(null, null, 1, 10);

        assertEquals(1, resultado.size());
        assertEquals(2, resultado.get(0).productos().size());
    }

    @Test
    void combinacionesSinVentas() {

        when(ventas.findAll()).thenReturn(List.of());

        assertTrue(
            servicio.combinaciones(null, null, 1, 10)
                .isEmpty()
        );
    }

    @Test
    void combinacionesRechazaSoporteInvalido() {

        assertThrows(
            IllegalArgumentException.class,
            () -> servicio.combinaciones(null, null, 0, 10)
        );

        verifyNoInteractions(ventas);
    }

    @Test
    void combinacionesRechazaLimitesInvalidos() {

        assertThrows(
            IllegalArgumentException.class,
            () -> servicio.combinaciones(null, null, 1, 0)
        );

        assertThrows(
            IllegalArgumentException.class,
            () -> servicio.combinaciones(null, null, 1, 101)
        );

        verifyNoInteractions(ventas);
    }

    @Test
    void combinacionesRechazaFechasInvertidas() {

        assertThrows(
            IllegalArgumentException.class,
            () -> servicio.combinaciones(
                FECHA.plusDays(1),
                FECHA,
                1,
                10
            )
        );

        verifyNoInteractions(ventas);
    }

    // =================================================
    // HU-056: RECOMENDACIONES DE PRODUCTOS
    // =================================================

    @Test
    void recomendacionesGeneraTresProductos() {

        Producto a = producto(1L, "Garrafon");
        Producto b = producto(2L, "Botella");
        Producto c = producto(3L, "Agua mineral");
        Producto d = producto(4L, "Agua saborizada");

        when(ventas.findAll()).thenReturn(List.of(
            ticket(10L, FECHA, a),
            ticket(20L, FECHA, a, b),
            ticket(21L, FECHA, a, b, c),
            ticket(22L, FECHA, a, d)
        ));

        when(productos.findAll())
            .thenReturn(List.of(a, b, c, d));

        var resultado =
            servicio.recomendaciones(10L, 5);

        assertEquals(3, resultado.size());
        assertEquals(2L, resultado.get(0).productoId());
        assertEquals(2L, resultado.get(0).puntuacion());
        assertEquals("Botella", resultado.get(0).nombre());
        assertEquals(50.0, resultado.get(0).precio(), 0.001);

        assertEquals(
            "Comprado junto con productos del historial",
            resultado.get(0).motivo()
        );

        assertEquals(3L, resultado.get(1).productoId());
        assertEquals(4L, resultado.get(2).productoId());
    }

    @Test
    void recomendacionesSinHistorial() {

        when(ventas.findAll()).thenReturn(List.of());

        assertTrue(
            servicio.recomendaciones(10L, 5).isEmpty()
        );

        verifyNoInteractions(productos);
    }

    @Test
    void recomendacionesRespetaLimite() {

        Producto a = producto(1L, "A");
        Producto b = producto(2L, "B");
        Producto c = producto(3L, "C");

        when(ventas.findAll()).thenReturn(List.of(
            ticket(10L, FECHA, a),
            ticket(20L, FECHA, a, b, c)
        ));

        when(productos.findAll())
            .thenReturn(List.of(a, b, c));

        var resultado =
            servicio.recomendaciones(10L, 1);

        assertEquals(1, resultado.size());
        assertEquals(2L, resultado.get(0).productoId());
    }

    @Test
    void recomendacionesExcluyeProductosComprados() {

        Producto a = producto(1L, "A");
        Producto b = producto(2L, "B");
        Producto c = producto(3L, "C");

        when(ventas.findAll()).thenReturn(List.of(
            ticket(10L, FECHA, a, b),
            ticket(20L, FECHA, a, b, c)
        ));

        when(productos.findAll())
            .thenReturn(List.of(a, b, c));

        var resultado =
            servicio.recomendaciones(10L, 5);

        assertEquals(1, resultado.size());
        assertEquals(3L, resultado.get(0).productoId());
        assertEquals(2L, resultado.get(0).puntuacion());
    }

    @Test
    void recomendacionesIgnoraProductosSinRelacion() {

        Producto a = producto(1L, "A");
        Producto b = producto(2L, "B");
        Producto c = producto(3L, "C");
        Producto sinId = producto(null, "Sin ID");

        when(ventas.findAll()).thenReturn(List.of(
            ticket(10L, FECHA, a, null),
            ticket(20L, FECHA, b),
            ticket(21L, FECHA, a, c)
        ));

        when(productos.findAll())
            .thenReturn(List.of(a, b, c, sinId));

        var resultado =
            servicio.recomendaciones(10L, 5);

        assertEquals(1, resultado.size());
        assertEquals(3L, resultado.get(0).productoId());
    }

    @Test
    void recomendacionesExcluyeProductosFueraDelCatalogo() {

        Producto a = producto(1L, "A");
        Producto b = producto(2L, "B");

        when(ventas.findAll()).thenReturn(List.of(
            ticket(10L, FECHA, a),
            ticket(20L, FECHA, a, b)
        ));

        when(productos.findAll()).thenReturn(List.of(a));

        assertTrue(
            servicio.recomendaciones(10L, 5).isEmpty()
        );
    }

    @Test
    void recomendacionesRechazaClienteInvalido() {

        assertThrows(
            IllegalArgumentException.class,
            () -> servicio.recomendaciones(null, 5)
        );

        assertThrows(
            IllegalArgumentException.class,
            () -> servicio.recomendaciones(0L, 5)
        );

        verifyNoInteractions(ventas, productos);
    }

    @Test
    void recomendacionesRechazaLimitesInvalidos() {

        assertThrows(
            IllegalArgumentException.class,
            () -> servicio.recomendaciones(10L, 0)
        );

        assertThrows(
            IllegalArgumentException.class,
            () -> servicio.recomendaciones(10L, 31)
        );

        verifyNoInteractions(ventas, productos);
    }
}
