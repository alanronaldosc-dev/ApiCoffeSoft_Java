package com.utvt.ApiSpringCafeSoft.service;

import com.utvt.ApiSpringCafeSoft.dto.PromocionDTO;
import com.utvt.ApiSpringCafeSoft.model.Promocion;
import com.utvt.ApiSpringCafeSoft.repository.PromocionRepository;
import com.utvt.ApiSpringCafeSoft.repository.UsuarioRepository;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PromocionVigenciaTest {

    @Mock
    private PromocionRepository promocionRepository;

    @Mock
    private UsuarioRepository usuarioRepository;

    @InjectMocks
    private PromocionService promocionService;

    private Promocion promocion;

    @BeforeEach
    void prepararDatos() {
        LocalDateTime ahora = LocalDateTime.now();

        promocion = new Promocion();
        promocion.setId(10L);
        promocion.setNombre("Promocion QA");
        promocion.setDescripcion("Descuento de prueba");
        promocion.setDescuentoPorcentaje(10.0);
        promocion.setActiva(true);
        promocion.setFechaInicio(ahora.minusDays(1));
        promocion.setFechaFin(ahora.plusDays(1));
    }

    private void configurarPromocion() {
        when(promocionRepository.findById(10L))
                .thenReturn(Optional.of(promocion));
    }

    // QA-01: Promocion vigente
    @Test
    void promocionVigenteSePuedeAplicar() {
        configurarPromocion();

        PromocionDTO resultado = promocionService.validarAplicacion(10L);

        assertNotNull(resultado);
        assertEquals("Promocion QA", resultado.getNombre());
        verify(promocionRepository).findById(10L);
    }

    // QA-02: Promocion vencida — el service lanza ResponseStatusException 409
    @Test
    void promocionVencidaSeRechaza() {
        LocalDateTime ahora = LocalDateTime.now();
        promocion.setFechaInicio(ahora.minusDays(3));
        promocion.setFechaFin(ahora.minusDays(1));
        configurarPromocion();

        ResponseStatusException error = assertThrows(
                ResponseStatusException.class,
                () -> promocionService.validarAplicacion(10L)
        );

        // El service lanza "La promocion ha vencido."
        assertTrue(error.getReason().toLowerCase().contains("vencido"));
    }

    // QA-03: Promocion que aun no comienza
    @Test
    void promocionFuturaSeRechaza() {
        LocalDateTime ahora = LocalDateTime.now();
        promocion.setFechaInicio(ahora.plusDays(2));
        promocion.setFechaFin(ahora.plusDays(5));
        configurarPromocion();

        ResponseStatusException error = assertThrows(
                ResponseStatusException.class,
                () -> promocionService.validarAplicacion(10L)
        );

        // El service lanza "La promocion aun no ha iniciado."
        assertTrue(error.getReason().toLowerCase().contains("no ha iniciado"));
    }

    // QA-04: Promocion desactivada
    @Test
    void promocionDesactivadaSeRechaza() {
        promocion.setActiva(false);
        configurarPromocion();

        ResponseStatusException error = assertThrows(
                ResponseStatusException.class,
                () -> promocionService.validarAplicacion(10L)
        );

        // El service lanza "La promocion esta desactivada y no puede aplicarse."
        assertTrue(error.getReason().toLowerCase().contains("desactivada"));
    }

    // QA-05: Promocion inexistente
    @Test
    void promocionInexistenteSeRechaza() {
        when(promocionRepository.findById(999L))
                .thenReturn(Optional.empty());

        ResponseStatusException error = assertThrows(
                ResponseStatusException.class,
                () -> promocionService.validarAplicacion(999L)
        );

        assertTrue(error.getReason().toLowerCase().contains("no encontrada"));
    }
}
