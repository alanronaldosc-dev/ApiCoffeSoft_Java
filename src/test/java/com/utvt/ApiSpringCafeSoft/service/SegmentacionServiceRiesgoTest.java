package com.utvt.ApiSpringCafeSoft.service;

import com.utvt.ApiSpringCafeSoft.model.Cliente;
import com.utvt.ApiSpringCafeSoft.repository.ClienteRepository;
import com.utvt.ApiSpringCafeSoft.repository.SegmentacionRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class SegmentacionServiceRiesgoTest {

    @Mock
    private ClienteRepository clientes;

    @Mock
    private SegmentacionRepository ventas;

    @InjectMocks
    private SegmentacionService service;

    private Cliente cliente() {
        Cliente c = new Cliente();
        c.setId(1L);
        c.setNombre("Juan");
        c.setTelefono("5512345678");
        return c;
    }

    private void conVentas(Object[]... filas) {
        when(clientes.findAll()).thenReturn(List.of(cliente()));
        when(ventas.actividadRiesgoPorCliente(any())).thenReturn(List.of(filas));
    }

    @Test
    void sinCompras_clasificaRiesgoAlto() {
        conVentas();

        var resultado = service.listarRiesgo();

        assertEquals(1, resultado.size());
        assertEquals("ALTO", resultado.get(0).nivel());
        assertEquals("Sin compras registradas", resultado.get(0).motivo());
        assertNull(resultado.get(0).diasSinCompra());
        assertNotNull(resultado.get(0).fechaCalculo());
        assertEquals("5512345678", resultado.get(0).telefono());
    }

    @Test
    void treintaYCincoDiasSinPedido_clasificaRiesgoMedio() {
        conVentas(new Object[]{
                1L, 5L,
                LocalDateTime.now().minusDays(60),
                LocalDateTime.now().minusDays(35),
                5L
        });

        var resultado = service.listarRiesgo();

        assertEquals("MEDIO", resultado.get(0).nivel());
        assertTrue(resultado.get(0).motivo().contains("35 días sin pedido"));
        assertEquals(0.0, resultado.get(0).caidaFrecuencia());
    }

    @Test
    void caidaDeFrecuenciaSuperiorAlCincuentaPorCiento_clasificaRiesgoAlto() {
        conVentas(new Object[]{
                1L, 2L,
                LocalDateTime.now().minusDays(60),
                LocalDateTime.now().minusDays(55),
                0L
        });

        var resultado = service.listarRiesgo();

        assertEquals("ALTO", resultado.get(0).nivel());
        assertTrue(resultado.get(0).motivo().contains("Frecuencia bajó"));
        assertNotNull(resultado.get(0).datosReferencia());
    }

    @Test
    void comprasRecientesYFrecuenciaEstable_clasificaRiesgoBajo() {
        conVentas(new Object[]{
                1L, 4L,
                LocalDateTime.now().minusDays(30),
                LocalDateTime.now().minusDays(2),
                4L
        });

        var resultado = service.listarRiesgo();

        assertEquals("BAJO", resultado.get(0).nivel());
        assertEquals("Compras y frecuencia dentro de lo habitual", resultado.get(0).motivo());
    }
}
