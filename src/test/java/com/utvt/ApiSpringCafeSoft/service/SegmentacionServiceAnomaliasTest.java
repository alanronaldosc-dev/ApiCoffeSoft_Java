
package com.utvt.ApiSpringCafeSoft.service;

import com.utvt.ApiSpringCafeSoft.model.Cliente;
import com.utvt.ApiSpringCafeSoft.repository.ClienteRepository;
import com.utvt.ApiSpringCafeSoft.repository.SegmentacionRepository;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class SegmentacionServiceAnomaliasTest {

    @Mock
    private ClienteRepository clientes;

    @Mock
    private SegmentacionRepository ventas;

    @InjectMocks
    private SegmentacionService service;

    private final LocalDateTime ahora = LocalDateTime.now();

    private Cliente crearCliente() {
        Cliente cliente = new Cliente();
        cliente.setId(1L);
        cliente.setNombre("Juan");
        return cliente;
    }

    private void configurarRiesgo(
            LocalDateTime primera,
            LocalDateTime ultima,
            long cantidad,
            long recientes) {

        when(clientes.findAll())
            .thenReturn(List.of(crearCliente()));

        when(ventas.actividadRiesgoPorCliente(any(LocalDateTime.class)))
            .thenReturn(Collections.singletonList(new Object[]{
                1L, cantidad, primera, ultima, recientes
            }));
    }

    @Test
    void pedidosDentroDeTresHoras_detectaAnomalia() {

        when(ventas.ventasParaDeteccionAnomalias()).thenReturn(List.of(
            new Object[]{1L, ahora.minusHours(2), 100.0},
            new Object[]{1L, ahora, 100.0}
        ));

        configurarRiesgo(
            ahora.minusHours(2), ahora, 2, 2
        );

        var resultado = service.listarAnomalias();

        assertEquals(1, resultado.size());
        assertEquals("PEDIDOS_DUPLICADOS", resultado.get(0).tipo());
        assertEquals("Juan", resultado.get(0).nombre());
        assertEquals("PEDIDOS_EN_3_HORAS", resultado.get(0).metrica());
        assertEquals(2.0, resultado.get(0).valorDetectado());
        assertEquals(ahora, resultado.get(0).fechaDeteccion());
    }

    @Test
    void pedidosSeparadosPorMasDeTresHoras_noDetectaDuplicado() {

        when(ventas.ventasParaDeteccionAnomalias()).thenReturn(List.of(
            new Object[]{1L, ahora.minusHours(4), 100.0},
            new Object[]{1L, ahora, 100.0}
        ));

        configurarRiesgo(
            ahora.minusHours(4), ahora, 2, 2
        );

        var resultado = service.listarAnomalias();

        assertTrue(resultado.isEmpty());
    }

    @Test
    void compraSuperiorACincoVecesElPromedio_detectaAnomalia() {

        when(ventas.ventasParaDeteccionAnomalias()).thenReturn(List.of(
            new Object[]{1L, ahora.minusDays(32), 100.0},
            new Object[]{1L, ahora.minusDays(22), 100.0},
            new Object[]{1L, ahora.minusDays(12), 100.0},
            new Object[]{1L, ahora.minusDays(2), 600.0}
        ));

        configurarRiesgo(
            ahora.minusDays(32), ahora.minusDays(2), 4, 1
        );

        var resultado = service.listarAnomalias();

        assertEquals(1, resultado.size());

        assertEquals(
            "COMPRA_ATIPICA_CLIENTE_EN_RIESGO",
            resultado.get(0).tipo()
        );

        assertEquals("Juan", resultado.get(0).nombre());

        assertEquals(
            "TOTAL_VS_PROMEDIO_HISTORICO",
            resultado.get(0).metrica()
        );

        assertEquals(600.0, resultado.get(0).valorDetectado());
    }

    @Test
    void compraExactamenteCincoVecesElPromedio_noDetectaAnomalia() {

        when(ventas.ventasParaDeteccionAnomalias()).thenReturn(List.of(
            new Object[]{1L, ahora.minusDays(10), 100.0},
            new Object[]{1L, ahora.minusDays(2), 500.0}
        ));

        configurarRiesgo(
            ahora.minusDays(10), ahora.minusDays(2), 2, 1
        );

        var resultado = service.listarAnomalias();

        assertTrue(resultado.isEmpty());
    }

    @Test
    void sinVentas_noGeneraAnomalias() {

        when(ventas.ventasParaDeteccionAnomalias())
            .thenReturn(Collections.emptyList());

        when(clientes.findAll())
            .thenReturn(Collections.emptyList());

        when(ventas.actividadRiesgoPorCliente(any(LocalDateTime.class)))
            .thenReturn(Collections.emptyList());

        var resultado = service.listarAnomalias();

        assertNotNull(resultado);
        assertTrue(resultado.isEmpty());
    }
}
