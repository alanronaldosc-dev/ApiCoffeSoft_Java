package com.utvt.ApiSpringCafeSoft.service;

import com.utvt.ApiSpringCafeSoft.model.Ruta;
import com.utvt.ApiSpringCafeSoft.repository.CargaRepository;
import com.utvt.ApiSpringCafeSoft.repository.ClienteRepository;
import com.utvt.ApiSpringCafeSoft.repository.RutaRepository;
import com.utvt.ApiSpringCafeSoft.repository.UsuarioRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RutaServiceTest {

    @Mock
    private RutaRepository rutaRepository;

    @Mock
    private UsuarioRepository usuarioRepository;

    @Mock
    private ClienteRepository clienteRepository;

    @Mock
    private CargaRepository cargaRepository;

    @InjectMocks
    private RutaService service;

    @Test
    void obtenerRutas_listaVacia_retornaListaVacia() {
        when(rutaRepository.findAll()).thenReturn(List.of());
        assertTrue(service.obtenerRutas().isEmpty());
    }

    @Test
    void obtenerRutas_conRutas_retornaLista() {
        Ruta r = new Ruta();
        r.setId(1L);
        when(rutaRepository.findAll()).thenReturn(List.of(r));
        assertEquals(1, service.obtenerRutas().size());
    }

    @Test
    void obtenerPorId_existente_retornaRuta() {
        Ruta r = new Ruta();
        r.setId(5L);
        when(rutaRepository.findById(5L)).thenReturn(Optional.of(r));
        assertEquals(5L, service.obtenerPorId(5L).getId());
    }

    @Test
    void obtenerPorId_noExistente_lanzaExcepcion() {
        when(rutaRepository.findById(99L)).thenReturn(Optional.empty());
        assertThrows(RuntimeException.class, () -> service.obtenerPorId(99L));
    }

    @Test
    void eliminarRuta_noExistente_lanzaExcepcion() {
        when(rutaRepository.findById(99L)).thenReturn(Optional.empty());
        assertThrows(RuntimeException.class, () -> service.eliminarRuta(99L));
    }
}
