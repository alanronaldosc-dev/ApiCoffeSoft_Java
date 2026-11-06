package com.utvt.ApiSpringCafeSoft.service;

import com.utvt.ApiSpringCafeSoft.dto.CampanaDTO;
import com.utvt.ApiSpringCafeSoft.model.Campana;
import com.utvt.ApiSpringCafeSoft.model.Promocion;
import com.utvt.ApiSpringCafeSoft.model.Sucursal;
import com.utvt.ApiSpringCafeSoft.model.Usuario;
import com.utvt.ApiSpringCafeSoft.repository.CampanaRepository;
import com.utvt.ApiSpringCafeSoft.repository.PromocionRepository;
import com.utvt.ApiSpringCafeSoft.repository.UsuarioRepository;
import org.junit.jupiter.api.*;
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
 * HU-037: Suite completa de pruebas para CampanaService.
 *
 * Incluye:
 * - Pruebas UNITARIAS: lógica interna de cada método
 * - Pruebas de ACEPTACIÓN: validación de los criterios de la HU
 * - Pruebas OPERACIONALES: scheduler y sincronización automática
 */
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class CampanaServiceTest {

    @Mock private CampanaRepository campanaRepository;
    @Mock private PromocionRepository promocionRepository;
    @Mock private UsuarioRepository usuarioRepository;
    @Mock private Authentication authentication;
    @Mock private SecurityContext securityContext;

    @InjectMocks
    private CampanaService campanaService;

    private Sucursal sucursal;
    private Usuario usuarioAdmin;
    private Promocion promocion;
    private Campana campanaActiva;
    private Campana campanaFutura;
    private Campana campanaExpirada;

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

        promocion = new Promocion();
        promocion.setId(1L);
        promocion.setNombre("Promo 10%");
        promocion.setDescuentoPorcentaje(10.0);
        promocion.setFechaInicio(LocalDateTime.now().minusDays(1));
        promocion.setFechaFin(LocalDateTime.now().plusDays(30));
        promocion.setActiva(true);
        promocion.setSucursal(sucursal);

        // Campaña activa: dentro del periodo
        campanaActiva = buildCampana(1L, "Campaña Activa", "DORMIDO",
                LocalDateTime.now().minusDays(1),
                LocalDateTime.now().plusDays(30),
                true);

        // Campaña futura: aún no ha comenzado
        campanaFutura = buildCampana(2L, "Campaña Futura", "POTENCIAL",
                LocalDateTime.now().plusDays(5),
                LocalDateTime.now().plusDays(35),
                false);

        // Campaña expirada: ya terminó
        campanaExpirada = buildCampana(3L, "Campaña Expirada", "ACTIVO",
                LocalDateTime.now().minusDays(30),
                LocalDateTime.now().minusDays(1),
                true);

        // Mock del SecurityContext
        SecurityContextHolder.setContext(securityContext);
        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.getName()).thenReturn("admin@gmail.com");
        when(usuarioRepository.findByEmail("admin@gmail.com"))
                .thenReturn(Optional.of(usuarioAdmin));
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    // ================================================================
    // PRUEBAS UNITARIAS — Lógica interna
    // ================================================================

    @Nested
    @DisplayName("Pruebas Unitarias — Creación de campaña")
    class PruebasUnitariasCreacion {

        @Test
        @DisplayName("U-01: Crear campaña con todos los campos válidos")
        void crearCampana_valida_exitoso() {
            when(promocionRepository.findById(1L)).thenReturn(Optional.of(promocion));
            when(campanaRepository.save(any(Campana.class))).thenReturn(campanaActiva);

            CampanaDTO dto = buildDTO("DORMIDO",
                    LocalDateTime.now().minusDays(1),
                    LocalDateTime.now().plusDays(30));

            CampanaDTO resultado = campanaService.crearCampana(dto);

            assertNotNull(resultado);
            assertEquals("Campaña Activa", resultado.getNombre());
            verify(campanaRepository, times(1)).save(any(Campana.class));
        }

        @Test
        @DisplayName("U-02: Campaña con fechaInicio ya pasada se activa inmediatamente")
        void crearCampana_fechaInicioYaPaso_activaInmediatamente() {
            when(promocionRepository.findById(1L)).thenReturn(Optional.of(promocion));

            Campana campanaGuardada = buildCampana(1L, "Test", "DORMIDO",
                    LocalDateTime.now().minusHours(1),
                    LocalDateTime.now().plusDays(10), true);
            when(campanaRepository.save(any(Campana.class))).thenReturn(campanaGuardada);

            CampanaDTO dto = buildDTO("DORMIDO",
                    LocalDateTime.now().minusHours(1),
                    LocalDateTime.now().plusDays(10));

            CampanaDTO resultado = campanaService.crearCampana(dto);

            assertTrue(resultado.getActiva());
        }

        @Test
        @DisplayName("U-03: Campaña con fechaInicio futura se crea como inactiva")
        void crearCampana_fechaInicioFutura_creaInactiva() {
            when(promocionRepository.findById(1L)).thenReturn(Optional.of(promocion));
            when(campanaRepository.save(any(Campana.class))).thenReturn(campanaFutura);

            CampanaDTO dto = buildDTO("POTENCIAL",
                    LocalDateTime.now().plusDays(5),
                    LocalDateTime.now().plusDays(35));

            CampanaDTO resultado = campanaService.crearCampana(dto);

            assertFalse(resultado.getActiva());
        }

        @Test
        @DisplayName("U-04: Lanza excepción si segmento es inválido")
        void crearCampana_segmentoInvalido_lanzaExcepcion() {
            CampanaDTO dto = buildDTO("INVALIDO",
                    LocalDateTime.now().plusDays(1),
                    LocalDateTime.now().plusDays(10));

            assertThrows(IllegalArgumentException.class,
                    () -> campanaService.crearCampana(dto));
            verify(campanaRepository, never()).save(any());
        }

        @Test
        @DisplayName("U-05: Lanza excepción si fechaFin anterior a fechaInicio")
        void crearCampana_fechasIncoherentes_lanzaExcepcion() {
            when(promocionRepository.findById(1L)).thenReturn(Optional.of(promocion));

            CampanaDTO dto = buildDTO("DORMIDO",
                    LocalDateTime.now().plusDays(10),
                    LocalDateTime.now().plusDays(1));

            assertThrows(IllegalArgumentException.class,
                    () -> campanaService.crearCampana(dto));
        }

        @Test
        @DisplayName("U-06: Lanza excepción si la promoción no existe")
        void crearCampana_promocionNoExiste_lanzaExcepcion() {
            when(promocionRepository.findById(99L)).thenReturn(Optional.empty());

            CampanaDTO dto = buildDTO("DORMIDO",
                    LocalDateTime.now().plusDays(1),
                    LocalDateTime.now().plusDays(10));
            dto.setPromocionId(99L);

            assertThrows(RuntimeException.class,
                    () -> campanaService.crearCampana(dto));
        }

        @Test
        @DisplayName("U-07: Lanza excepción si usuario no tiene sucursal asignada")
        void crearCampana_sinSucursal_lanzaExcepcion() {
            usuarioAdmin.setSucursal(null);
            when(promocionRepository.findById(1L)).thenReturn(Optional.of(promocion));

            CampanaDTO dto = buildDTO("DORMIDO",
                    LocalDateTime.now().plusDays(1),
                    LocalDateTime.now().plusDays(10));

            assertThrows(IllegalArgumentException.class,
                    () -> campanaService.crearCampana(dto));
        }
    }

    @Nested
    @DisplayName("Pruebas Unitarias — Consulta y gestión")
    class PruebasUnitariasConsulta {

        @Test
        @DisplayName("U-08: Obtener todas las campañas")
        void obtenerTodas_devuelveLista() {
            when(campanaRepository.findAll())
                    .thenReturn(List.of(campanaActiva, campanaFutura));

            List<CampanaDTO> resultado = campanaService.obtenerTodas();

            assertEquals(2, resultado.size());
        }

        @Test
        @DisplayName("U-09: Obtener campaña por ID existente")
        void obtenerPorId_encontrada() {
            when(campanaRepository.findById(1L))
                    .thenReturn(Optional.of(campanaActiva));

            CampanaDTO resultado = campanaService.obtenerPorId(1L);

            assertNotNull(resultado);
            assertEquals(1L, resultado.getId());
        }

        @Test
        @DisplayName("U-10: Obtener por ID inexistente lanza excepción")
        void obtenerPorId_noExiste_lanzaExcepcion() {
            when(campanaRepository.findById(99L)).thenReturn(Optional.empty());

            assertThrows(RuntimeException.class,
                    () -> campanaService.obtenerPorId(99L));
        }

        @Test
        @DisplayName("U-11: Desactivar campaña manualmente")
        void desactivarCampana_exitoso() {
            when(campanaRepository.findById(1L))
                    .thenReturn(Optional.of(campanaActiva));
            campanaActiva.setActiva(false);
            when(campanaRepository.save(any(Campana.class)))
                    .thenReturn(campanaActiva);

            CampanaDTO resultado = campanaService.desactivarCampana(1L);

            assertFalse(resultado.getActiva());
            verify(campanaRepository, times(1)).save(any(Campana.class));
        }

        @Test
        @DisplayName("U-12: Eliminar campaña existente")
        void eliminarCampana_exitoso() {
            when(campanaRepository.existsById(1L)).thenReturn(true);

            campanaService.eliminarCampana(1L);

            verify(campanaRepository, times(1)).deleteById(1L);
        }

        @Test
        @DisplayName("U-13: Eliminar campaña inexistente lanza excepción")
        void eliminarCampana_noExiste_lanzaExcepcion() {
            when(campanaRepository.existsById(99L)).thenReturn(false);

            assertThrows(RuntimeException.class,
                    () -> campanaService.eliminarCampana(99L));
            verify(campanaRepository, never()).deleteById(any());
        }

        @Test
        @DisplayName("U-14: Actualizar campaña con segmento inválido lanza excepción")
        void actualizarCampana_segmentoInvalido_lanzaExcepcion() {
            when(campanaRepository.findById(1L))
                    .thenReturn(Optional.of(campanaActiva));

            CampanaDTO dto = buildDTO("INVALIDO",
                    LocalDateTime.now().plusDays(1),
                    LocalDateTime.now().plusDays(10));

            assertThrows(IllegalArgumentException.class,
                    () -> campanaService.actualizarCampana(1L, dto));
        }
    }

    // ================================================================
    // PRUEBAS DE ACEPTACIÓN — Criterios de la HU-037
    // ================================================================

    @Nested
    @DisplayName("Pruebas de Aceptación — Criterios HU-037")
    class PruebasAceptacion {

        @Test
        @DisplayName("CA-1: La campaña se crea con segmento, promoción y periodo")
        void CA1_campanaCreadaConSegmentoPromocionYPeriodo() {
            when(promocionRepository.findById(1L)).thenReturn(Optional.of(promocion));
            when(campanaRepository.save(any(Campana.class))).thenReturn(campanaActiva);

            CampanaDTO dto = buildDTO("DORMIDO",
                    LocalDateTime.now().plusDays(1),
                    LocalDateTime.now().plusDays(30));

            CampanaDTO resultado = campanaService.crearCampana(dto);

            // Verifica que tiene los tres elementos
            assertNotNull(resultado.getSegmento(), "Debe tener segmento");
            assertNotNull(resultado.getPromocionId(), "Debe tener promoción");
            assertNotNull(resultado.getFechaInicio(), "Debe tener fechaInicio");
            assertNotNull(resultado.getFechaFin(), "Debe tener fechaFin");
            assertEquals("DORMIDO", resultado.getSegmento());
            assertEquals(1L, resultado.getPromocionId());
        }

        @Test
        @DisplayName("CA-2: El administrador ve la campaña activa en la fecha de inicio")
        void CA2_campanaActivaEnFechaDeInicio() {
            when(campanaRepository.findVigentes(any(LocalDateTime.class)))
                    .thenReturn(List.of(campanaActiva));

            List<CampanaDTO> vigentes = campanaService.obtenerVigentes();

            assertFalse(vigentes.isEmpty(), "Debe haber campañas vigentes");
            assertTrue(vigentes.stream().anyMatch(c -> Boolean.TRUE.equals(c.getActiva())),
                    "Al menos una campaña debe estar activa");
        }

        @Test
        @DisplayName("CA-2: Filtrar campañas vigentes por segmento DORMIDO")
        void CA2_campanaVigenteFiltraPorSegmento() {
            when(campanaRepository.findVigentesPorSegmento(eq("DORMIDO"), any()))
                    .thenReturn(List.of(campanaActiva));

            List<CampanaDTO> resultado =
                    campanaService.obtenerVigentesPorSegmento("DORMIDO");

            assertEquals(1, resultado.size());
            assertEquals("DORMIDO", resultado.get(0).getSegmento());
        }

        @Test
        @DisplayName("CA-2: No aparecen campañas expiradas entre las vigentes")
        void CA2_campanasExpiradasNoApareceEnVigentes() {
            // Las campañas expiradas no pasan el filtro de vigentes
            when(campanaRepository.findVigentes(any(LocalDateTime.class)))
                    .thenReturn(List.of(campanaActiva)); // expirada NO incluida

            List<CampanaDTO> vigentes = campanaService.obtenerVigentes();

            assertTrue(vigentes.stream()
                    .noneMatch(c -> c.getNombre().equals("Campaña Expirada")),
                    "Las campañas expiradas no deben aparecer como vigentes");
        }

        @Test
        @DisplayName("CA-3: El administrador no tiene que desactivar campañas manualmente")
        void CA3_desactivacionAutomatica_sinIntervencionManual() {
            // El scheduler desactiva automáticamente
            when(campanaRepository.desactivarExpiradas(any(LocalDateTime.class)))
                    .thenReturn(1);

            int desactivadas = campanaService.desactivarExpiradas();

            assertEquals(1, desactivadas,
                    "El sistema debe desactivar automáticamente las campañas expiradas");
            verify(campanaRepository, times(1))
                    .desactivarExpiradas(any(LocalDateTime.class));
        }

        @Test
        @DisplayName("CA-3: Campaña expirada se desactiva sin acción del admin")
        void CA3_campanaExpiradaSeDesactivaAutomaticamente() {
            when(campanaRepository.desactivarExpiradas(any(LocalDateTime.class)))
                    .thenReturn(2);

            int total = campanaService.desactivarExpiradas();

            assertTrue(total > 0, "Debe desactivar campañas expiradas automáticamente");
        }

        @Test
        @DisplayName("CA-1+CA-2: Campaña pendiente se activa al llegar su fecha")
        void CA1_CA2_campanaSeActivaAlLlegarFecha() {
            when(campanaRepository.activarPendientes(any(LocalDateTime.class)))
                    .thenReturn(1);

            int activadas = campanaService.activarPendientes();

            assertEquals(1, activadas,
                    "La campaña debe activarse automáticamente al llegar su fecha de inicio");
        }

        @Test
        @DisplayName("CA-1: Segmento inválido no permite crear campaña")
        void CA1_segmentoInvalido_bloqueaCreacion() {
            CampanaDTO dto = buildDTO("INVALIDO",
                    LocalDateTime.now().plusDays(1),
                    LocalDateTime.now().plusDays(10));

            assertThrows(IllegalArgumentException.class,
                    () -> campanaService.crearCampana(dto),
                    "El sistema no debe permitir segmentos que no existen");
        }
    }

    // ================================================================
    // PRUEBAS OPERACIONALES — Scheduler y sincronización automática
    // ================================================================

    @Nested
    @DisplayName("Pruebas Operacionales — Scheduler automático")
    class PruebasOperacionales {

        @Test
        @DisplayName("OP-01: Scheduler activa campañas pendientes y desactiva expiradas")
        void scheduler_sincronizaEstados_correctamente() {
            when(campanaRepository.activarPendientes(any(LocalDateTime.class)))
                    .thenReturn(2);
            when(campanaRepository.desactivarExpiradas(any(LocalDateTime.class)))
                    .thenReturn(1);

            // Simular ejecución del scheduler
            campanaService.sincronizarEstados();

            verify(campanaRepository, times(1))
                    .activarPendientes(any(LocalDateTime.class));
            verify(campanaRepository, times(1))
                    .desactivarExpiradas(any(LocalDateTime.class));
        }

        @Test
        @DisplayName("OP-02: Scheduler no lanza excepción si no hay campañas que procesar")
        void scheduler_sinCampanas_noLanzaExcepcion() {
            when(campanaRepository.activarPendientes(any(LocalDateTime.class)))
                    .thenReturn(0);
            when(campanaRepository.desactivarExpiradas(any(LocalDateTime.class)))
                    .thenReturn(0);

            assertDoesNotThrow(() -> campanaService.sincronizarEstados(),
                    "El scheduler no debe fallar si no hay campañas que procesar");
        }

        @Test
        @DisplayName("OP-03: activarPendientes usa timestamp actual")
        void activarPendientes_usaTimestampActual() {
            when(campanaRepository.activarPendientes(any(LocalDateTime.class)))
                    .thenReturn(3);

            int resultado = campanaService.activarPendientes();

            assertEquals(3, resultado);
            verify(campanaRepository).activarPendientes(
                    argThat(ts -> !ts.isAfter(LocalDateTime.now())));
        }

        @Test
        @DisplayName("OP-04: desactivarExpiradas usa timestamp actual")
        void desactivarExpiradas_usaTimestampActual() {
            when(campanaRepository.desactivarExpiradas(any(LocalDateTime.class)))
                    .thenReturn(2);

            int resultado = campanaService.desactivarExpiradas();

            assertEquals(2, resultado);
            verify(campanaRepository).desactivarExpiradas(
                    argThat(ts -> !ts.isAfter(LocalDateTime.now())));
        }

        @Test
        @DisplayName("OP-05: Múltiples campañas de distintos segmentos se procesan en un ciclo")
        void scheduler_procesaMultiplesSegmentos() {
            when(campanaRepository.activarPendientes(any()))
                    .thenReturn(4); // ACTIVO, POTENCIAL, DORMIDO, POCO_FRECUENTE
            when(campanaRepository.desactivarExpiradas(any()))
                    .thenReturn(2);

            campanaService.sincronizarEstados();

            verify(campanaRepository, times(1)).activarPendientes(any());
            verify(campanaRepository, times(1)).desactivarExpiradas(any());
        }

        @Test
        @DisplayName("OP-06: Obtener vigentes por segmento POCO_FRECUENTE")
        void obtenerVigentesPorSegmento_pocofrecuente_funciona() {
            Campana campanaPF = buildCampana(4L, "Campaña PF", "POCO_FRECUENTE",
                    LocalDateTime.now().minusDays(1),
                    LocalDateTime.now().plusDays(10), true);

            when(campanaRepository.findVigentesPorSegmento(eq("POCO_FRECUENTE"), any()))
                    .thenReturn(List.of(campanaPF));

            List<CampanaDTO> resultado =
                    campanaService.obtenerVigentesPorSegmento("POCO_FRECUENTE");

            assertEquals(1, resultado.size());
            assertEquals("POCO_FRECUENTE", resultado.get(0).getSegmento());
        }

        @Test
        @DisplayName("OP-07: Segmento inválido en obtenerVigentesPorSegmento lanza excepción")
        void obtenerVigentesPorSegmento_invalido_lanzaExcepcion() {
            assertThrows(IllegalArgumentException.class,
                    () -> campanaService.obtenerVigentesPorSegmento("DESCONOCIDO"));
        }
    }

    // ================================================================
    // HELPERS
    // ================================================================

    private Campana buildCampana(Long id, String nombre, String segmento,
                                  LocalDateTime inicio, LocalDateTime fin,
                                  boolean activa) {
        Campana c = new Campana();
        c.setId(id);
        c.setNombre(nombre);
        c.setSegmento(segmento);
        c.setPromocion(promocion);
        c.setFechaInicio(inicio);
        c.setFechaFin(fin);
        c.setActiva(activa);
        c.setSucursal(sucursal);
        c.setCreatedAt(LocalDateTime.now());
        return c;
    }

    private CampanaDTO buildDTO(String segmento,
                                 LocalDateTime inicio,
                                 LocalDateTime fin) {
        CampanaDTO dto = new CampanaDTO();
        dto.setNombre("Campaña Activa");
        dto.setDescripcion("Prueba HU-037");
        dto.setSegmento(segmento);
        dto.setPromocionId(1L);
        dto.setFechaInicio(inicio);
        dto.setFechaFin(fin);
        return dto;
    }
}
