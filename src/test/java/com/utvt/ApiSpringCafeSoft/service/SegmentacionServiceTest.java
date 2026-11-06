package com.utvt.ApiSpringCafeSoft.service;

import com.utvt.ApiSpringCafeSoft.model.Cliente;
import com.utvt.ApiSpringCafeSoft.repository.ClienteRepository;
import com.utvt.ApiSpringCafeSoft.repository.SegmentacionRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@org.mockito.junit.jupiter.MockitoSettings(strictness = org.mockito.quality.Strictness.LENIENT)
class SegmentacionServiceTest {

    @Mock
    private ClienteRepository clientes;

    @Mock
    private SegmentacionRepository ventas;

    @InjectMocks
    private SegmentacionService service;

    @BeforeEach
    void setUp() {
        when(ventas.actividadPorCliente()).thenReturn(new java.util.ArrayList<Object[]>());
    }

    @Test
    void listarSinFiltro_clasificaClienteSinComprasComoPotencial() {
        Cliente c = new Cliente();
        c.setId(1L);
        c.setNombre("Juan");
        when(clientes.findAll()).thenReturn(List.of(c));

        var resultado = service.listar(null);

        assertEquals(1, resultado.size());
        assertEquals("POTENCIAL", resultado.get(0).segmento());
        assertEquals(0L, resultado.get(0).compras());
    }

    @Test
    void listarConSegmentoInvalido_lanzaBadRequest() {
        assertThrows(ResponseStatusException.class, () -> service.listar("INVALIDO"));
    }

    @Test
    void listarConFiltroActivo_filtraCorrectamente() {
        Cliente c = new Cliente();
        c.setId(1L);
        c.setNombre("Juan");
        when(clientes.findAll()).thenReturn(List.of(c));

        var resultado = service.listar("POTENCIAL");

        assertEquals(1, resultado.size());
        assertEquals("POTENCIAL", resultado.get(0).segmento());
    }

    @Test
    void porClienteNoExistente_lanzaNotFound() {
        when(clientes.findById(99L)).thenReturn(java.util.Optional.empty());
        assertThrows(ResponseStatusException.class, () -> service.porCliente(99L));
    }

    @Test
    void resumen_contieneLosCuatroSegmentos() {
        Cliente c = new Cliente();
        c.setId(1L);
        c.setNombre("Juan");
        when(clientes.findAll()).thenReturn(List.of(c));

        var resumen = service.resumen();

        assertEquals(1, resumen.total());
        assertTrue(resumen.segmentos().containsKey("ACTIVO"));
        assertTrue(resumen.segmentos().containsKey("POTENCIAL"));
        assertTrue(resumen.segmentos().containsKey("DORMIDO"));
        assertTrue(resumen.segmentos().containsKey("POCO_FRECUENTE"));
    }

    @Test
    void actividadConVentas_clasificaDormidoSiMasDe90Dias() {
        Cliente c = new Cliente();
        c.setId(1L);
        c.setNombre("Juan");
        when(clientes.findAll()).thenReturn(List.of(c));

        Object[] fila = new Object[]{1L, 2L, LocalDateTime.now().minusDays(100)};
        when(ventas.actividadPorCliente()).thenReturn(java.util.Collections.singletonList(fila));

        var resultado = service.listar(null);

        assertEquals(1, resultado.size());
        assertEquals("DORMIDO", resultado.get(0).segmento());
        assertEquals(2L, resultado.get(0).compras());
    }
}
