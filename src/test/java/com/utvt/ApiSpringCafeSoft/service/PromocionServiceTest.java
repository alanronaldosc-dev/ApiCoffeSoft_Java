package com.utvt.ApiSpringCafeSoft.service;

import com.utvt.ApiSpringCafeSoft.dto.PromocionDTO;
import com.utvt.ApiSpringCafeSoft.model.Promocion;
import com.utvt.ApiSpringCafeSoft.model.Sucursal;
import com.utvt.ApiSpringCafeSoft.model.Usuario;
import com.utvt.ApiSpringCafeSoft.repository.PromocionRepository;
import com.utvt.ApiSpringCafeSoft.repository.UsuarioRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * HU-028: Tests unitarios de PromocionService
 * Cubre creación, consulta, validación y desactivación de promociones.
 * Incluye validaciones de seguridad (CA-1, CA-2, CA-3).
 * Cobertura objetivo: >= 80%
 */
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class PromocionServiceTest {

    @Mock private PromocionRepository promocionRepository;
    @Mock private UsuarioRepository usuarioRepository;
    @Mock private Authentication authentication;
    @Mock private SecurityContext securityContext;

    @InjectMocks
    private PromocionService promocionService;

    private Sucursal sucursal;
    private Usuario usuarioAdmin;
    private Promocion promocionActiva;
    private PromocionDTO dtoBase;

    @BeforeEach
    void setUp() {
        sucursal = new Sucursal();
        sucursal.setId(1L);
        sucursal.setNombre("Sucursal Centro");
        sucursal.setDireccion("Calle 1");
        sucursal.setTelefono("7200000001");

        usuarioAdmin = new Usuario();
        usuarioAdmin.setId(1L);
        usuarioAdmin.setEmail("admin@gmail.com");
        usuarioAdmin.setNombre("Admin");
        usuarioAdmin.setPassword("hashed");
        usuarioAdmin.setDireccion("Calle 1");
        usuarioAdmin.setTelefono("7200000000");
        usuarioAdmin.setUserTipo(0);
        usuarioAdmin.setActivo(true);
        usuarioAdmin.setSucursal(sucursal);

        promocionActiva = new Promocion();
        promocionActiva.setId(1L);
        promocionActiva.setNombre("Promo Verano");
        promocionActiva.setDescripcion("10% de descuento");
        promocionActiva.setDescuentoPorcentaje(10.0);
        promocionActiva.setFechaInicio(LocalDateTime.now().minusDays(1));
        promocionActiva.setFechaFin(LocalDateTime.now().plusDays(30));
        promocionActiva.setActiva(true);
        promocionActiva.setMinGarrafones(3);
        promocionActiva.setAcumulaPuntos(true);
        promocionActiva.setCreatedAt(LocalDateTime.now().minusDays(1));
        promocionActiva.setSucursal(sucursal);

        dtoBase = buildPromocionDTO(
                LocalDateTime.now().plusDays(1),
                LocalDateTime.now().plusDays(30)
        );

        // Configurar SecurityContextHolder con el mock
        SecurityContextHolder.setContext(securityContext);
        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.getName()).thenReturn("admin@gmail.com");
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    // ================================================================
    // CREAR PROMOCIÓN — Casos exitosos
    // ================================================================

    @Test
    @DisplayName("HU-028 - Crear promoción válida exitosamente")
    void crearPromocion_valida_exitoso() {
        when(usuarioRepository.findByEmail("admin@gmail.com"))
                .thenReturn(Optional.of(usuarioAdmin));
        when(promocionRepository.save(any(Promocion.class))).thenReturn(promocionActiva);

        PromocionDTO resultado = promocionService.crearPromocion(dtoBase);

        assertNotNull(resultado);
        assertEquals("Promo Verano", resultado.getNombre());
        verify(promocionRepository, times(1)).save(any(Promocion.class));
    }

    // ================================================================
    // CREAR PROMOCIÓN — Validaciones CA-1 (fechas coherentes)
    // ================================================================

    @Test
    @DisplayName("HU-028 - CA-1: Lanza excepción si fechaFin es anterior a fechaInicio")
    void crearPromocion_fechaFinAnterior_lanzaExcepcion() {
        when(usuarioRepository.findByEmail("admin@gmail.com"))
                .thenReturn(Optional.of(usuarioAdmin));

        PromocionDTO dto = buildPromocionDTO(
                LocalDateTime.now().plusDays(10),
                LocalDateTime.now().plusDays(1) // fin antes que inicio
        );

        assertThrows(IllegalArgumentException.class,
                () -> promocionService.crearPromocion(dto));
        verify(promocionRepository, never()).save(any());
    }

    @Test
    @DisplayName("HU-028 - Lanza excepción si usuario no tiene sucursal asignada")
    void crearPromocion_sinSucursal_lanzaExcepcion() {
        usuarioAdmin.setSucursal(null);
        when(usuarioRepository.findByEmail("admin@gmail.com"))
                .thenReturn(Optional.of(usuarioAdmin));

        assertThrows(IllegalArgumentException.class,
                () -> promocionService.crearPromocion(dtoBase));
    }

    @Test
    @DisplayName("HU-028 - Lanza excepción si usuario autenticado no existe")
    void crearPromocion_usuarioNoExiste_lanzaExcepcion() {
        when(usuarioRepository.findByEmail("admin@gmail.com"))
                .thenReturn(Optional.empty());

        assertThrows(RuntimeException.class,
                () -> promocionService.crearPromocion(dtoBase));
    }

    // ================================================================
    // OBTENER PROMOCIONES
    // ================================================================

    @Test
    @DisplayName("HU-028 - Obtener todas las promociones devuelve lista")
    void obtenerTodas_devuelveLista() {
        when(promocionRepository.findAll()).thenReturn(List.of(promocionActiva));

        List<PromocionDTO> resultado = promocionService.obtenerTodas();

        assertEquals(1, resultado.size());
        assertEquals("Promo Verano", resultado.get(0).getNombre());
    }

    @Test
    @DisplayName("HU-028 - Obtener todas devuelve lista vacía cuando no hay promociones")
    void obtenerTodas_listaVacia() {
        when(promocionRepository.findAll()).thenReturn(List.of());

        List<PromocionDTO> resultado = promocionService.obtenerTodas();

        assertTrue(resultado.isEmpty());
    }

    @Test
    @DisplayName("HU-028 - CA-2: Obtener solo promociones vigentes")
    void obtenerVigentes_devuelveVigentes() {
        when(promocionRepository.findVigentes(any(LocalDateTime.class)))
                .thenReturn(List.of(promocionActiva));

        List<PromocionDTO> resultado = promocionService.obtenerVigentes();

        assertEquals(1, resultado.size());
        assertTrue(Boolean.TRUE.equals(resultado.get(0).getVigente()));
    }

    @Test
    @DisplayName("HU-028 - CA-2: Obtener vigentes por sucursal")
    void obtenerVigentesPorSucursal_devuelveLista() {
        when(promocionRepository.findVigentesBySucursal(eq(1L), any(LocalDateTime.class)))
                .thenReturn(List.of(promocionActiva));

        List<PromocionDTO> resultado = promocionService.obtenerVigentesPorSucursal(1L);

        assertEquals(1, resultado.size());
        assertEquals(1L, resultado.get(0).getSucursalId());
    }

    @Test
    @DisplayName("HU-028 - Obtener promoción por ID existente")
    void obtenerPorId_encontrada() {
        when(promocionRepository.findById(1L)).thenReturn(Optional.of(promocionActiva));

        PromocionDTO resultado = promocionService.obtenerPorId(1L);

        assertNotNull(resultado);
        assertEquals(1L, resultado.getId());
    }

    @Test
    @DisplayName("HU-028 - Obtener promoción por ID inexistente lanza excepción")
    void obtenerPorId_noEncontrada_lanzaExcepcion() {
        when(promocionRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(RuntimeException.class,
                () -> promocionService.obtenerPorId(99L));
    }

    // ================================================================
    // ACTUALIZAR PROMOCIÓN
    // ================================================================

    @Test
    @DisplayName("HU-028 - Actualizar promoción exitosamente")
    void actualizarPromocion_exitoso() {
        when(promocionRepository.findById(1L)).thenReturn(Optional.of(promocionActiva));
        when(promocionRepository.save(any(Promocion.class))).thenReturn(promocionActiva);

        PromocionDTO dto = buildPromocionDTO(
                LocalDateTime.now().plusDays(1),
                LocalDateTime.now().plusDays(60)
        );
        dto.setNombre("Promo Actualizada");

        PromocionDTO resultado = promocionService.actualizarPromocion(1L, dto);

        assertNotNull(resultado);
        verify(promocionRepository, times(1)).save(any(Promocion.class));
    }

    @Test
    @DisplayName("HU-028 - CA-3: No permite reactivar promoción expirada sin nueva fechaFin")
    void actualizarPromocion_reactivarExpirada_lanzaExcepcion() {
        promocionActiva.setFechaFin(LocalDateTime.now().minusDays(5)); // ya expiró
        promocionActiva.setActiva(false);
        when(promocionRepository.findById(1L)).thenReturn(Optional.of(promocionActiva));

        PromocionDTO dto = buildPromocionDTO(
                LocalDateTime.now().minusDays(10),
                null // sin nueva fechaFin
        );
        dto.setActiva(true); // intenta reactivar

        assertThrows(IllegalArgumentException.class,
                () -> promocionService.actualizarPromocion(1L, dto));
    }

    @Test
    @DisplayName("HU-028 - Actualizar con fechas incoherentes lanza excepción")
    void actualizarPromocion_fechasIncoherentes_lanzaExcepcion() {
        when(promocionRepository.findById(1L)).thenReturn(Optional.of(promocionActiva));

        PromocionDTO dto = buildPromocionDTO(
                LocalDateTime.now().plusDays(10),
                LocalDateTime.now().plusDays(1) // fin antes que inicio
        );

        assertThrows(IllegalArgumentException.class,
                () -> promocionService.actualizarPromocion(1L, dto));
    }

    @Test
    @DisplayName("HU-028 - Actualizar promoción inexistente lanza excepción")
    void actualizarPromocion_noExiste_lanzaExcepcion() {
        when(promocionRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(RuntimeException.class,
                () -> promocionService.actualizarPromocion(99L, dtoBase));
    }

    // ================================================================
    // DESACTIVAR PROMOCIÓN — CA-3
    // ================================================================

    @Test
    @DisplayName("HU-028 - CA-3: Desactivar promoción activa exitosamente")
    void desactivarPromocion_exitoso() {
        when(promocionRepository.findById(1L)).thenReturn(Optional.of(promocionActiva));
        promocionActiva.setActiva(false);
        when(promocionRepository.save(any(Promocion.class))).thenReturn(promocionActiva);

        PromocionDTO resultado = promocionService.desactivarPromocion(1L);

        assertNotNull(resultado);
        assertFalse(resultado.getActiva());
        verify(promocionRepository, times(1)).save(any(Promocion.class));
    }

    @Test
    @DisplayName("HU-028 - Desactivar promoción inexistente lanza excepción")
    void desactivarPromocion_noExiste_lanzaExcepcion() {
        when(promocionRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(RuntimeException.class,
                () -> promocionService.desactivarPromocion(99L));
    }

    // ================================================================
    // ELIMINAR PROMOCIÓN
    // ================================================================

    @Test
    @DisplayName("HU-028 - Eliminar promoción existente")
    void eliminarPromocion_exitoso() {
        when(promocionRepository.existsById(1L)).thenReturn(true);

        promocionService.eliminarPromocion(1L);

        verify(promocionRepository, times(1)).deleteById(1L);
    }

    @Test
    @DisplayName("HU-028 - Eliminar promoción inexistente lanza excepción")
    void eliminarPromocion_noExiste_lanzaExcepcion() {
        when(promocionRepository.existsById(99L)).thenReturn(false);

        assertThrows(RuntimeException.class,
                () -> promocionService.eliminarPromocion(99L));
        verify(promocionRepository, never()).deleteById(any());
    }

    // ================================================================
    // VALIDAR APLICACIÓN — CA-2/CA-3
    // ================================================================

    @Test
    @DisplayName("HU-028 - CA-2/CA-3: Validar aplicación de promoción vigente exitosa")
    void validarAplicacion_vigente_exitoso() {
        when(promocionRepository.findById(1L)).thenReturn(Optional.of(promocionActiva));

        PromocionDTO resultado = promocionService.validarAplicacion(1L);

        assertNotNull(resultado);
        assertTrue(Boolean.TRUE.equals(resultado.getActiva()));
    }

    @Test
    @DisplayName("HU-028 - CA-3: Validar aplicación de promoción desactivada lanza excepción")
    void validarAplicacion_desactivada_lanzaExcepcion() {
        promocionActiva.setActiva(false);
        when(promocionRepository.findById(1L)).thenReturn(Optional.of(promocionActiva));

        assertThrows(IllegalStateException.class,
                () -> promocionService.validarAplicacion(1L));
    }

    @Test
    @DisplayName("HU-028 - CA-2: Validar aplicación de promoción no iniciada lanza excepción")
    void validarAplicacion_noIniciada_lanzaExcepcion() {
        promocionActiva.setFechaInicio(LocalDateTime.now().plusDays(5));
        when(promocionRepository.findById(1L)).thenReturn(Optional.of(promocionActiva));

        assertThrows(IllegalStateException.class,
                () -> promocionService.validarAplicacion(1L));
    }

    @Test
    @DisplayName("HU-028 - CA-2: Validar aplicación de promoción expirada lanza excepción")
    void validarAplicacion_expirada_lanzaExcepcion() {
        promocionActiva.setFechaFin(LocalDateTime.now().minusDays(1));
        when(promocionRepository.findById(1L)).thenReturn(Optional.of(promocionActiva));

        assertThrows(IllegalStateException.class,
                () -> promocionService.validarAplicacion(1L));
    }

    @Test
    @DisplayName("HU-028 - Validar aplicación de promoción inexistente lanza excepción")
    void validarAplicacion_noExiste_lanzaExcepcion() {
        when(promocionRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(RuntimeException.class,
                () -> promocionService.validarAplicacion(99L));
    }

    // ================================================================
    // HELPER
    // ================================================================

    private PromocionDTO buildPromocionDTO(LocalDateTime inicio, LocalDateTime fin) {
        PromocionDTO dto = new PromocionDTO();
        dto.setNombre("Promo Verano");
        dto.setDescripcion("10% de descuento");
        dto.setDescuentoPorcentaje(10.0);
        dto.setFechaInicio(inicio);
        dto.setFechaFin(fin);
        dto.setActiva(true);
        dto.setMinGarrafones(3);
        dto.setAcumulaPuntos(true);
        return dto;
    }
}
