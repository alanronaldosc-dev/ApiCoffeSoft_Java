package com.utvt.ApiSpringCafeSoft.service;

import com.utvt.ApiSpringCafeSoft.model.Sucursal;
import com.utvt.ApiSpringCafeSoft.model.Usuario;
import com.utvt.ApiSpringCafeSoft.model.Venta;
import com.utvt.ApiSpringCafeSoft.repository.UsuarioRepository;
import com.utvt.ApiSpringCafeSoft.repository.VentaRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class VentaReportesServiceTest {
    @Mock VentaRepository ventaRepository;
    @Mock UsuarioRepository usuarioRepository;
    @InjectMocks VentaService service;

    @BeforeEach
    void autenticar() {
        SecurityContextHolder.getContext().setAuthentication(
            new UsernamePasswordAuthenticationToken("pruebas@cafesoft.mx", "n/a", List.of())
        );
    }

    @AfterEach
    void limpiar() {
        SecurityContextHolder.clearContext();
    }

    private void configurarSucursal() {
        Sucursal sucursal = new Sucursal();
        sucursal.setId(7L);
        sucursal.setNombre("Sucursal Centro");
        Usuario usuario = new Usuario();
        usuario.setId(2L);
        usuario.setEmail("pruebas@cafesoft.mx");
        usuario.setSucursal(sucursal);
        when(usuarioRepository.findByEmail("pruebas@cafesoft.mx"))
            .thenReturn(Optional.of(usuario));
    }

    private Venta venta(LocalDateTime fecha, Double total) {
        Venta v = new Venta();
        v.setFecha(fecha);
        v.setTotal(total);
        return v;
    }

    @SuppressWarnings("unchecked")
    private List<Map<String, Object>> filas(Map<String, Object> reporte, String clave) {
        return (List<Map<String, Object>>) reporte.get(clave);
    }

    @Test
    void rechazaFechaInicialNula() {
        ResponseStatusException e = assertThrows(ResponseStatusException.class,
            () -> service.obtenerPatronesConsumo(null, LocalDate.of(2026, 11, 10)));
        assertEquals(HttpStatus.BAD_REQUEST, e.getStatusCode());
        verifyNoInteractions(ventaRepository);
    }

    @Test
    void rechazaFechaFinalNula() {
        assertThrows(ResponseStatusException.class,
            () -> service.obtenerPatronesConsumo(LocalDate.of(2026, 11, 10), null));
    }

    @Test
    void rechazaFechasInvertidas() {
        assertThrows(ResponseStatusException.class,
            () -> service.obtenerPatronesConsumo(LocalDate.of(2026, 11, 11), LocalDate.of(2026, 11, 10)));
    }

    @Test
    void rechazaPeriodoMayorA366Dias() {
        assertThrows(ResponseStatusException.class,
            () -> service.obtenerPatronesConsumo(LocalDate.of(2025, 1, 1), LocalDate.of(2026, 11, 10)));
    }

    @Test
    void sinVentasGenera24FranjasYDiasEnCero() {
        configurarSucursal();
        when(ventaRepository.findByFechaBetweenAndSucursalId(any(), any(), eq(7L)))
            .thenReturn(List.of());
        Map<String, Object> r = service.obtenerPatronesConsumo(
            LocalDate.of(2026, 11, 9), LocalDate.of(2026, 11, 11));
        assertEquals(0, ((Number) r.get("numeroVentas")).intValue());
        assertEquals(0.0, ((Number) r.get("montoTotal")).doubleValue());
        assertNull(r.get("franjaMayorConsumo"));
        assertEquals("Sucursal Centro", r.get("sucursalNombre"));
        assertEquals(24, filas(r, "franjas").size());
        assertEquals(3, filas(r, "dias").size());
        assertTrue(filas(r, "dias").stream().allMatch(d -> ((Number) d.get("numeroVentas")).intValue() == 0));
    }

    @Test
    void agrupaVentasPorHoraYDiaConTotales() {
        configurarSucursal();
        when(ventaRepository.findByFechaBetweenAndSucursalId(any(), any(), eq(7L)))
            .thenReturn(List.of(
                venta(LocalDateTime.of(2026, 11, 9, 9, 10), 100.0),
                venta(LocalDateTime.of(2026, 11, 9, 9, 50), 50.0),
                venta(LocalDateTime.of(2026, 11, 10, 14, 0), 30.0)
            ));
        Map<String, Object> r = service.obtenerPatronesConsumo(
            LocalDate.of(2026, 11, 9), LocalDate.of(2026, 11, 11));
        assertEquals(3, ((Number) r.get("numeroVentas")).intValue());
        assertEquals(180.0, ((Number) r.get("montoTotal")).doubleValue(), 0.001);
        assertEquals(2, ((Number) filas(r, "franjas").get(9).get("numeroVentas")).intValue());
        assertEquals(150.0, ((Number) filas(r, "franjas").get(9).get("montoTotal")).doubleValue(), 0.001);
        assertEquals(2, ((Number) filas(r, "dias").get(0).get("numeroVentas")).intValue());
        assertEquals(0, ((Number) filas(r, "dias").get(2).get("numeroVentas")).intValue());
        assertEquals(9, ((Number) ((Map<?, ?>) r.get("franjaMayorConsumo")).get("hora")).intValue());
    }

    @Test
    void totalNuloSeCuentaComoCero() {
        configurarSucursal();
        when(ventaRepository.findByFechaBetweenAndSucursalId(any(), any(), eq(7L)))
            .thenReturn(List.of(venta(LocalDateTime.of(2026, 11, 9, 12, 0), null)));
        Map<String, Object> r = service.obtenerPatronesConsumo(
            LocalDate.of(2026, 11, 9), LocalDate.of(2026, 11, 9));
        assertEquals(1, ((Number) r.get("numeroVentas")).intValue());
        assertEquals(0.0, ((Number) r.get("montoTotal")).doubleValue());
    }

    @Test
    void exigeUsuarioConSucursal() {
        Usuario usuario = new Usuario();
        usuario.setEmail("pruebas@cafesoft.mx");
        when(usuarioRepository.findByEmail("pruebas@cafesoft.mx"))
            .thenReturn(Optional.of(usuario));
        assertThrows(IllegalArgumentException.class,
            () -> service.obtenerPatronesConsumo(LocalDate.of(2026, 11, 9), LocalDate.of(2026, 11, 9)));
        verifyNoInteractions(ventaRepository);
    }

    @Test
    void noAceptaUsuarioDesconocido() {
        when(usuarioRepository.findByEmail("pruebas@cafesoft.mx"))
            .thenReturn(Optional.empty());
        assertThrows(RuntimeException.class,
            () -> service.obtenerPatronesConsumo(LocalDate.of(2026, 11, 9), LocalDate.of(2026, 11, 9)));
    }
}
