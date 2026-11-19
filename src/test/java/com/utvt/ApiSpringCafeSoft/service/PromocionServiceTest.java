package com.utvt.ApiSpringCafeSoft.service;

import com.utvt.ApiSpringCafeSoft.dto.PromocionDTO;
import com.utvt.ApiSpringCafeSoft.model.Promocion;
import com.utvt.ApiSpringCafeSoft.model.Sucursal;
import com.utvt.ApiSpringCafeSoft.model.Usuario;
import com.utvt.ApiSpringCafeSoft.repository.PromocionRepository;
import com.utvt.ApiSpringCafeSoft.repository.UsuarioRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Tests unitarios para PromocionService.
 * Usan Mockito para aislar la lógica del servicio sin BD.
 */
@ExtendWith(MockitoExtension.class)
class PromocionServiceTest {

    @Mock
    private PromocionRepository promocionRepository;

    @Mock
    private UsuarioRepository usuarioRepository;

    @InjectMocks
    private PromocionService promocionService;

    // ── Helpers ──

    private Promocion promocionVigente() {
        Promocion p = new Promocion();
        p.setId(1L);
        p.setNombre("Promo Test");
        p.setDescripcion("Descripcion");
        p.setDescuentoPorcentaje(10.0);
        p.setFechaInicio(LocalDateTime.now().minusDays(1));
        p.setFechaFin(LocalDateTime.now().plusDays(1));
        p.setActiva(true);
        p.setMinGarrafones(0);
        p.setAcumulaPuntos(true);
        p.setCreatedAt(LocalDateTime.now());
        Sucursal s = new Sucursal();
        s.setId(1L);
        s.setNombre("Sucursal Central");
        p.setSucursal(s);
        return p;
    }

    private PromocionDTO dtoConFechasValidas() {
        PromocionDTO dto = new PromocionDTO();
        dto.setNombre("Promo Nueva");
        dto.setDescripcion("Desc");
        dto.setDescuentoPorcentaje(15.0);
        dto.setFechaInicio(LocalDateTime.now().plusDays(1));
        dto.setFechaFin(LocalDateTime.now().plusDays(10));
        dto.setActiva(true);
        return dto;
    }

    /** Mockea un SecurityContext con usuario autenticado como ROLE_ADMIN. */
    private void mockAdminContext(String email) {
        Collection<GrantedAuthority> authorities =
                List.of(new SimpleGrantedAuthority("ROLE_ADMIN"));

        Authentication auth = mock(Authentication.class);
        when(auth.isAuthenticated()).thenReturn(true);
        when(auth.getPrincipal()).thenReturn(email);
        when(auth.getName()).thenReturn(email);
        doReturn(authorities).when(auth).getAuthorities();

        SecurityContext ctx = mock(SecurityContext.class);
        when(ctx.getAuthentication()).thenReturn(auth);
        SecurityContextHolder.setContext(ctx);
    }

    private Usuario adminConSucursal(String email) {
        Sucursal sucursal = new Sucursal();
        sucursal.setId(1L);
        sucursal.setNombre("Sucursal Central");

        Usuario usuario = new Usuario();
        usuario.setId(1L);
        usuario.setEmail(email);
        usuario.setUserTipo(0);   // 0 = ADMIN
        usuario.setActivo(true);
        usuario.setSucursal(sucursal);
        return usuario;
    }

    // ── Tests ──

    @Test
    @DisplayName("crearPromocion: lanza excepción cuando fechaFin es anterior a fechaInicio (CA-1)")
    void crearPromocion_fechaFinAntesDeFechaInicio_lanzaExcepcion() {
        // Necesitamos autenticación para llegar a la validación de fechas
        String email = "admin@cafesoft.com";
        mockAdminContext(email);
        when(usuarioRepository.findByEmail(email))
                .thenReturn(Optional.of(adminConSucursal(email)));

        PromocionDTO dto = dtoConFechasValidas();
        dto.setFechaInicio(LocalDateTime.now().plusDays(5));
        dto.setFechaFin(LocalDateTime.now().plusDays(1)); // fin antes que inicio

        // El service lanza ResponseStatusException (400 BAD_REQUEST)
        ResponseStatusException ex = assertThrows(
                ResponseStatusException.class,
                () -> promocionService.crearPromocion(dto)
        );

        assertTrue(ex.getReason().toLowerCase().contains("fin"));
        verify(promocionRepository, never()).save(any());
    }

    @Test
    @DisplayName("crearPromocion: crea correctamente cuando las fechas son válidas")
    void crearPromocion_fechasValidas_guardaPromocion() {
        String email = "admin@cafesoft.com";
        mockAdminContext(email);
        when(usuarioRepository.findByEmail(email))
                .thenReturn(Optional.of(adminConSucursal(email)));

        PromocionDTO dto = dtoConFechasValidas();

        Sucursal sucursal = new Sucursal();
        sucursal.setId(1L);
        sucursal.setNombre("Sucursal Central");

        Promocion guardada = new Promocion();
        guardada.setId(99L);
        guardada.setNombre(dto.getNombre());
        guardada.setDescuentoPorcentaje(dto.getDescuentoPorcentaje());
        guardada.setFechaInicio(dto.getFechaInicio());
        guardada.setFechaFin(dto.getFechaFin());
        guardada.setActiva(true);
        guardada.setMinGarrafones(0);
        guardada.setAcumulaPuntos(true);
        guardada.setCreatedAt(LocalDateTime.now());
        guardada.setSucursal(sucursal);

        when(promocionRepository.save(any(Promocion.class))).thenReturn(guardada);

        PromocionDTO resultado = promocionService.crearPromocion(dto);

        assertNotNull(resultado);
        assertEquals(99L, resultado.getId());
        verify(promocionRepository, times(1)).save(any(Promocion.class));
    }

    @Test
    @DisplayName("obtenerTodas: retorna lista vacía cuando no hay promociones")
    void obtenerTodas_sinPromociones_retornaListaVacia() {
        when(promocionRepository.findAll()).thenReturn(List.of());

        List<PromocionDTO> resultado = promocionService.obtenerTodas();

        assertNotNull(resultado);
        assertTrue(resultado.isEmpty());
    }

    @Test
    @DisplayName("obtenerTodas: retorna lista con elementos cuando existen promociones")
    void obtenerTodas_conPromociones_retornaLista() {
        when(promocionRepository.findAll()).thenReturn(List.of(promocionVigente()));

        List<PromocionDTO> resultado = promocionService.obtenerTodas();

        assertEquals(1, resultado.size());
        assertEquals("Promo Test", resultado.get(0).getNombre());
    }

    @Test
    @DisplayName("obtenerPorId: lanza excepción cuando la promoción no existe")
    void obtenerPorId_noExiste_lanzaExcepcion() {
        when(promocionRepository.findById(99L)).thenReturn(Optional.empty());

        ResponseStatusException ex = assertThrows(
                ResponseStatusException.class,
                () -> promocionService.obtenerPorId(99L)
        );

        assertTrue(ex.getReason().contains("99"));
    }

    @Test
    @DisplayName("obtenerPorId: retorna DTO cuando la promoción existe")
    void obtenerPorId_existe_retornaDTO() {
        when(promocionRepository.findById(1L)).thenReturn(Optional.of(promocionVigente()));

        PromocionDTO resultado = promocionService.obtenerPorId(1L);

        assertNotNull(resultado);
        assertEquals(1L, resultado.getId());
        assertEquals("Promo Test", resultado.getNombre());
    }

    @Test
    @DisplayName("eliminarPromocion: lanza excepción cuando la promoción no existe")
    void eliminarPromocion_noExiste_lanzaExcepcion() {
        String email = "admin@cafesoft.com";
        mockAdminContext(email);
        when(usuarioRepository.findByEmail(email))
                .thenReturn(Optional.of(adminConSucursal(email)));
        when(promocionRepository.findByIdForUpdate(99L)).thenReturn(Optional.empty());

        assertThrows(ResponseStatusException.class,
                () -> promocionService.eliminarPromocion(99L));
    }

    @Test
    @DisplayName("eliminarPromocion: elimina correctamente cuando existe")
    void eliminarPromocion_existe_eliminaCorrectamente() {
        String email = "admin@cafesoft.com";
        mockAdminContext(email);
        when(usuarioRepository.findByEmail(email))
                .thenReturn(Optional.of(adminConSucursal(email)));

        Promocion p = promocionVigente(); // sin fechaDesactivacion
        when(promocionRepository.findByIdForUpdate(1L)).thenReturn(Optional.of(p));
        doNothing().when(promocionRepository).delete(p);

        assertDoesNotThrow(() -> promocionService.eliminarPromocion(1L));
        verify(promocionRepository, times(1)).delete(p);
    }

    @Test
    @DisplayName("desactivarPromocion: retorna DTO con activa=false (CA-3)")
    void desactivarPromocion_existente_retornaInactiva() {
        String email = "admin@cafesoft.com";
        mockAdminContext(email);
        when(usuarioRepository.findByEmail(email))
                .thenReturn(Optional.of(adminConSucursal(email)));

        Promocion p = promocionVigente();
        when(promocionRepository.findByIdForUpdate(1L)).thenReturn(Optional.of(p));
        when(promocionRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        PromocionDTO resultado = promocionService.desactivarPromocion(1L);

        assertFalse(resultado.getActiva());
    }

    @Test
    @DisplayName("validarAplicacion: lanza ResponseStatusException cuando está desactivada (CA-3)")
    void validarAplicacion_desactivada_lanzaExcepcion() {
        Promocion p = promocionVigente();
        p.setActiva(false);
        when(promocionRepository.findById(1L)).thenReturn(Optional.of(p));

        ResponseStatusException ex = assertThrows(
                ResponseStatusException.class,
                () -> promocionService.validarAplicacion(1L)
        );
        assertTrue(ex.getReason().toLowerCase().contains("desactivada"));
    }

    @Test
    @DisplayName("validarAplicacion: lanza ResponseStatusException cuando aún no ha iniciado (CA-3)")
    void validarAplicacion_noIniciada_lanzaExcepcion() {
        Promocion p = promocionVigente();
        p.setFechaInicio(LocalDateTime.now().plusDays(2));
        p.setFechaFin(LocalDateTime.now().plusDays(5));
        when(promocionRepository.findById(1L)).thenReturn(Optional.of(p));

        ResponseStatusException ex = assertThrows(
                ResponseStatusException.class,
                () -> promocionService.validarAplicacion(1L)
        );
        assertTrue(ex.getReason().toLowerCase().contains("no ha iniciado"));
    }

    @Test
    @DisplayName("validarAplicacion: lanza ResponseStatusException cuando ya expiró (CA-3)")
    void validarAplicacion_expirada_lanzaExcepcion() {
        Promocion p = promocionVigente();
        p.setFechaInicio(LocalDateTime.now().minusDays(5));
        p.setFechaFin(LocalDateTime.now().minusDays(1));
        when(promocionRepository.findById(1L)).thenReturn(Optional.of(p));

        ResponseStatusException ex = assertThrows(
                ResponseStatusException.class,
                () -> promocionService.validarAplicacion(1L)
        );
        assertTrue(ex.getReason().toLowerCase().contains("vencido"));
    }

    @Test
    @DisplayName("validarAplicacion: retorna DTO cuando la promoción está vigente (CA-2/CA-3)")
    void validarAplicacion_vigente_retornaDTO() {
        when(promocionRepository.findById(1L)).thenReturn(Optional.of(promocionVigente()));

        PromocionDTO resultado = promocionService.validarAplicacion(1L);

        assertNotNull(resultado);
        assertTrue(resultado.getVigente());
    }
}
