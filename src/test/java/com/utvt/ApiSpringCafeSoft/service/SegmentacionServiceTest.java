
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

import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SegmentacionServiceTest {

    @Mock
    private ClienteRepository clienteRepository;

    @Mock
    private SegmentacionRepository segmentacionRepository;

    @InjectMocks
    private SegmentacionService segmentacionService;

    private Cliente cliente;

    @BeforeEach
    void configurar() {
        cliente = new Cliente();
        cliente.setId(1L);
        cliente.setNombre("Cliente de prueba");
    }

    private Object[] actividad(
            long clienteId,
            long compras,
            LocalDateTime fecha) {

        return new Object[] {
                clienteId,
                compras,
                fecha
        };
    }

    @Test
    void clienteSinComprasDebeSerPotencial() {

        when(clienteRepository.findById(1L))
                .thenReturn(Optional.of(cliente));

        when(segmentacionRepository.actividadPorCliente())
                .thenReturn(List.of());

        var resultado = segmentacionService.porCliente(1L);

        assertEquals("POTENCIAL", resultado.segmento());
        assertEquals(0, resultado.compras());
        assertNull(resultado.ultimaCompra());
    }

    @Test
    void clienteConTresComprasRecientesDebeSerActivo() {

        when(clienteRepository.findById(1L))
                .thenReturn(Optional.of(cliente));

        when(segmentacionRepository.actividadPorCliente())
                .thenReturn(List.<Object[]>of(
                        actividad(
                                1L,
                                3L,
                                LocalDateTime.now().minusDays(5)
                        )
                ));

        var resultado = segmentacionService.porCliente(1L);

        assertEquals("ACTIVO", resultado.segmento());
        assertEquals(3, resultado.compras());
    }

    @Test
    void clienteConMasDeNoventaDiasDebeSerDormido() {

        when(clienteRepository.findById(1L))
                .thenReturn(Optional.of(cliente));

        when(segmentacionRepository.actividadPorCliente())
                .thenReturn(List.<Object[]>of(
                        actividad(
                                1L,
                                2L,
                                LocalDateTime.now().minusDays(100)
                        )
                ));

        var resultado = segmentacionService.porCliente(1L);

        assertEquals("DORMIDO", resultado.segmento());
    }

    @Test
    void clienteConUnaCompraRecienteDebeSerPocoFrecuente() {

        when(clienteRepository.findById(1L))
                .thenReturn(Optional.of(cliente));

        when(segmentacionRepository.actividadPorCliente())
                .thenReturn(List.<Object[]>of(
                        actividad(
                                1L,
                                1L,
                                LocalDateTime.now().minusDays(10)
                        )
                ));

        var resultado = segmentacionService.porCliente(1L);

        assertEquals("POCO_FRECUENTE", resultado.segmento());
    }

    @Test
    void segmentoInvalidoDebeResponder400() {

        ResponseStatusException error = assertThrows(
                ResponseStatusException.class,
                () -> segmentacionService.listar("INVALIDO")
        );

        assertEquals(
                HttpStatus.BAD_REQUEST,
                error.getStatusCode()
        );
    }

    @Test
    void clienteInexistenteDebeResponder404() {

        when(clienteRepository.findById(99L))
                .thenReturn(Optional.empty());

        ResponseStatusException error = assertThrows(
                ResponseStatusException.class,
                () -> segmentacionService.porCliente(99L)
        );

        assertEquals(
                HttpStatus.NOT_FOUND,
                error.getStatusCode()
        );
    }

    @Test
    void filtroDebeMostrarSoloClientesActivos() {

        Cliente segundo = new Cliente();
        segundo.setId(2L);
        segundo.setNombre("Cliente sin compras");

        when(clienteRepository.findAll())
                .thenReturn(List.of(cliente, segundo));

        when(segmentacionRepository.actividadPorCliente())
                .thenReturn(List.<Object[]>of(
                        actividad(
                                1L,
                                4L,
                                LocalDateTime.now().minusDays(3)
                        )
                ));

        var resultado = segmentacionService.listar("ACTIVO");

        assertEquals(1, resultado.size());
        assertEquals(1L, resultado.get(0).clienteId());
        assertEquals("ACTIVO", resultado.get(0).segmento());
    }

    @Test
    void resumenDebeContarLosSegmentos() {

        Cliente segundo = new Cliente();
        segundo.setId(2L);
        segundo.setNombre("Cliente potencial");

        when(clienteRepository.findAll())
                .thenReturn(List.of(cliente, segundo));

        when(segmentacionRepository.actividadPorCliente())
                .thenReturn(List.<Object[]>of(
                        actividad(
                                1L,
                                3L,
                                LocalDateTime.now().minusDays(2)
                        )
                ));

        var resultado = segmentacionService.resumen();

        assertEquals(2, resultado.total());
        assertEquals(1L, resultado.segmentos().get("ACTIVO"));
        assertEquals(1L, resultado.segmentos().get("POTENCIAL"));
        assertEquals(0L, resultado.segmentos().get("DORMIDO"));
        assertEquals(0L, resultado.segmentos().get("POCO_FRECUENTE"));
    }
}
