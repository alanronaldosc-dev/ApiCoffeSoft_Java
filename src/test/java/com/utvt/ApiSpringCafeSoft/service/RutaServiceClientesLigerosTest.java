package com.utvt.ApiSpringCafeSoft.service;

import com.utvt.ApiSpringCafeSoft.dto.ClienteRutaDTO;
import com.utvt.ApiSpringCafeSoft.dto.RutaAsignadaDTO;
import com.utvt.ApiSpringCafeSoft.model.Cliente;
import com.utvt.ApiSpringCafeSoft.model.Producto;
import com.utvt.ApiSpringCafeSoft.model.Ruta;
import com.utvt.ApiSpringCafeSoft.model.Usuario;
import com.utvt.ApiSpringCafeSoft.repository.CargaRepository;
import com.utvt.ApiSpringCafeSoft.repository.ClienteRepository;
import com.utvt.ApiSpringCafeSoft.repository.RutaRepository;
import com.utvt.ApiSpringCafeSoft.repository.UsuarioRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RutaServiceClientesLigerosTest {

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

    private Ruta ruta(Long id, String estado) {
        Ruta ruta = new Ruta();
        ruta.setId(id);
        ruta.setNombre("Ruta " + id);
        ruta.setDiasReparto("Lunes,Martes");
        ruta.setFechaAsignacion(LocalDateTime.of(2026, 11, 23, 9, 0));
        ruta.setActiva(true);
        ruta.setEstado(estado);
        return ruta;
    }

    private Usuario repartidor() {
        Usuario usuario = new Usuario();
        usuario.setUserTipo(4);
        return usuario;
    }

    private Cliente cliente(Ruta ruta, Integer orden) {
        Producto producto = new Producto();
        producto.setId(16L);
        producto.setNombre("Garrafon Bonafont");
        producto.setImagen(new byte[2_000_000]);

        Cliente cliente = new Cliente();
        cliente.setId(5L);
        cliente.setNombre("Señor Antonio");
        cliente.setDomicilio("Calle Emiliano Zapata");
        cliente.setLinkGoogleMaps("https://maps.app.goo.gl/ejemplo");
        cliente.setDiasReparto("Lunes,Martes");
        cliente.setPrecioPorGarrafon(15.0);
        cliente.setFotografiaDomicilio(new byte[2_000_000]);
        cliente.setOrdenEnRuta(orden);
        cliente.setGarrafonPreferencia(producto);
        cliente.setRuta(ruta);
        return cliente;
    }

    @Test
    void clientesDeRutaNoExponenImagenes() throws Exception {
        Ruta ruta = ruta(1L, "EN_TRANSITO");

        when(rutaRepository.findById(1L)).thenReturn(Optional.of(ruta));
        when(clienteRepository.findByRutaId(1L)).thenReturn(
            Arrays.asList(cliente(ruta, 2), cliente(ruta, 1))
        );

        List<ClienteRutaDTO> resultado = service.obtenerClientesRuta(1L);

        assertEquals(2, resultado.size());
        assertEquals(1, resultado.get(0).getOrdenEnRuta());
        assertEquals("Señor Antonio", resultado.get(0).getNombre());
        assertEquals("Calle Emiliano Zapata", resultado.get(0).getDomicilio());
        assertEquals(
            "Garrafon Bonafont",
            resultado.get(0).getGarrafonPreferencia().getNombre()
        );
        assertEquals(1L, resultado.get(0).getRuta().getId());

        assertThrows(
            NoSuchFieldException.class,
            () -> ClienteRutaDTO.class.getDeclaredField("fotografiaDomicilio")
        );
        assertThrows(
            NoSuchFieldException.class,
            () -> ClienteRutaDTO.ProductoResumen.class.getDeclaredField("imagen")
        );
    }

    @Test
    void rutasAsignadasDevuelveSoloCamposNecesarios() {
        Ruta ruta = ruta(1L, "ACTIVA");

        when(usuarioRepository.findById(43L))
            .thenReturn(Optional.of(repartidor()));
        when(rutaRepository.findByRepartidorId(43L))
            .thenReturn(List.of(ruta));

        List<RutaAsignadaDTO> resultado =
            service.rutasAsignadasDelRepartidor(43L);

        assertEquals(1, resultado.size());
        assertEquals(1L, resultado.get(0).getId());
        assertEquals("Ruta 1", resultado.get(0).getNombre());
        assertEquals("Lunes,Martes", resultado.get(0).getDiasReparto());
        assertNotNull(resultado.get(0).getFechaAsignacion());
    }

    @Test
    void clientesEnCursoConservanElIdDeRuta() {
        Ruta ruta = ruta(24L, "EN_TRANSITO");

        when(usuarioRepository.findById(43L))
            .thenReturn(Optional.of(repartidor()));
        when(rutaRepository.findByRepartidorId(43L))
            .thenReturn(List.of(ruta));
        when(clienteRepository.findByRutaId(24L))
            .thenReturn(List.of(cliente(ruta, 1)));

        List<ClienteRutaDTO> resultado =
            service.clientesRutaEnCursoDelRepartidor(43L);

        assertEquals(1, resultado.size());
        assertEquals(24L, resultado.get(0).getRuta().getId());
    }
}
