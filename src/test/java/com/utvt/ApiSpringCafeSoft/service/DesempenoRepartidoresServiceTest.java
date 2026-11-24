package com.utvt.ApiSpringCafeSoft.service;

import com.utvt.ApiSpringCafeSoft.dto.DesempenoRepartidorDTO;
import com.utvt.ApiSpringCafeSoft.dto.DesempenoRepartidoresDTO;
import com.utvt.ApiSpringCafeSoft.model.Sucursal;
import com.utvt.ApiSpringCafeSoft.model.Usuario;
import com.utvt.ApiSpringCafeSoft.repository.EntregaPedidoRepository;
import com.utvt.ApiSpringCafeSoft.repository.UsuarioRepository;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DesempenoRepartidoresServiceTest {

    @Mock
    private EntregaPedidoRepository entregaPedidoRepository;

    @Mock
    private UsuarioRepository usuarioRepository;

    @InjectMocks
    private DesempenoRepartidoresService service;

    @AfterEach
    void limpiarSeguridad() {
        SecurityContextHolder.clearContext();
    }

    private void autenticarAdministrador() {
        var autenticacion = new UsernamePasswordAuthenticationToken(
            "admin@cafesoft.com",
            "token",
            List.of(new SimpleGrantedAuthority("ROLE_ADMIN"))
        );

        SecurityContextHolder.getContext()
            .setAuthentication(autenticacion);
    }

    private Usuario crearAdministrador() {
        Sucursal sucursal = new Sucursal();
        sucursal.setId(7L);
        sucursal.setNombre("Sucursal Central");

        Usuario admin = new Usuario();
        admin.setId(1L);
        admin.setEmail("admin@cafesoft.com");
        admin.setNombre("Administrador");
        admin.setSucursal(sucursal);

        return admin;
    }

    @Test
    void obtenerDesempeno_ordenaCorrectamenteLosDosRankings() {

        autenticarAdministrador();

        when(usuarioRepository.findByEmail("admin@cafesoft.com"))
            .thenReturn(Optional.of(crearAdministrador()));

        when(entregaPedidoRepository.obtenerDesempenoPorSucursalYPeriodo(
                eq(7L),
                any(LocalDateTime.class),
                any(LocalDateTime.class)))
            .thenReturn(List.of(
                new DesempenoRepartidorDTO(1L, "Ana", 3L, 150.0),
                new DesempenoRepartidorDTO(2L, "Bruno", 5L, 100.0),
                new DesempenoRepartidorDTO(3L, "Carla", 3L, 200.0),
                new DesempenoRepartidorDTO(4L, "Aaron", 3L, 200.0)
            ));

        DesempenoRepartidoresDTO resultado =
            service.obtenerDesempenoRepartidores("mes", null, null);

        assertEquals(7L, resultado.getSucursalId());
        assertEquals("Sucursal Central", resultado.getSucursalNombre());

        assertEquals(14L, resultado.getTotalVentas());
        assertEquals(650.0, resultado.getMontoTotal(), 0.001);

        List<DesempenoRepartidorDTO> ventas =
            resultado.getRankingVentas();

        assertEquals(4, ventas.size());
        assertEquals("Bruno", ventas.get(0).getNombre());
        assertEquals("Aaron", ventas.get(1).getNombre());
        assertEquals("Carla", ventas.get(2).getNombre());
        assertEquals("Ana", ventas.get(3).getNombre());

        List<DesempenoRepartidorDTO> monto =
            resultado.getRankingMonto();

        assertEquals(4, monto.size());
        assertEquals("Aaron", monto.get(0).getNombre());
        assertEquals("Carla", monto.get(1).getNombre());
        assertEquals("Ana", monto.get(2).getNombre());
        assertEquals("Bruno", monto.get(3).getNombre());

        for (int i = 0; i < 4; i++) {
            assertEquals(i + 1, ventas.get(i).getPosicion());
            assertEquals(i + 1, monto.get(i).getPosicion());
        }

        verify(entregaPedidoRepository)
            .obtenerDesempenoPorSucursalYPeriodo(
                eq(7L),
                any(LocalDateTime.class),
                any(LocalDateTime.class)
            );
    }

    @Test
    void obtenerDesempeno_sinEntregasDevuelveRankingsVacios() {

        autenticarAdministrador();

        when(usuarioRepository.findByEmail("admin@cafesoft.com"))
            .thenReturn(Optional.of(crearAdministrador()));

        when(entregaPedidoRepository.obtenerDesempenoPorSucursalYPeriodo(
                eq(7L),
                any(LocalDateTime.class),
                any(LocalDateTime.class)))
            .thenReturn(List.of());

        DesempenoRepartidoresDTO resultado =
            service.obtenerDesempenoRepartidores("mes", null, null);

        assertTrue(resultado.getRankingVentas().isEmpty());
        assertTrue(resultado.getRankingMonto().isEmpty());
        assertEquals(0L, resultado.getTotalVentas());
        assertEquals(0.0, resultado.getMontoTotal(), 0.001);
    }

    @Test
    void obtenerDesempeno_sinAutenticacionLanzaExcepcion() {

        SecurityContextHolder.clearContext();

        assertThrows(
            IllegalStateException.class,
            () -> service.obtenerDesempenoRepartidores(
                "mes", null, null
            )
        );

        verifyNoInteractions(usuarioRepository);
        verifyNoInteractions(entregaPedidoRepository);
    }

    @Test
    void obtenerDesempeno_usuarioSinSucursalLanzaExcepcion() {

        autenticarAdministrador();

        Usuario admin = crearAdministrador();
        admin.setSucursal(null);

        when(usuarioRepository.findByEmail("admin@cafesoft.com"))
            .thenReturn(Optional.of(admin));

        assertThrows(
            IllegalArgumentException.class,
            () -> service.obtenerDesempenoRepartidores(
                "mes", null, null
            )
        );

        verifyNoInteractions(entregaPedidoRepository);
    }

    @Test
    void obtenerDesempeno_usuarioNoEncontradoLanzaExcepcion() {

        autenticarAdministrador();

        when(usuarioRepository.findByEmail("admin@cafesoft.com"))
            .thenReturn(Optional.empty());

        assertThrows(
            IllegalStateException.class,
            () -> service.obtenerDesempenoRepartidores(
                "mes", null, null
            )
        );

        verifyNoInteractions(entregaPedidoRepository);
    }

    @Test
    void obtenerDesempeno_respetaRangoPersonalizado() {

        autenticarAdministrador();

        when(usuarioRepository.findByEmail("admin@cafesoft.com"))
            .thenReturn(Optional.of(crearAdministrador()));

        LocalDateTime inicio =
            LocalDateTime.of(2026, 11, 1, 0, 0);

        LocalDateTime fin =
            LocalDateTime.of(2026, 11, 15, 23, 59, 59);

        when(entregaPedidoRepository.obtenerDesempenoPorSucursalYPeriodo(
                7L, inicio, fin))
            .thenReturn(List.of());

        service.obtenerDesempenoRepartidores(
            "personalizado",
            "2026-11-01",
            "2026-11-15"
        );

        verify(entregaPedidoRepository)
            .obtenerDesempenoPorSucursalYPeriodo(
                7L, inicio, fin
            );
    }
}