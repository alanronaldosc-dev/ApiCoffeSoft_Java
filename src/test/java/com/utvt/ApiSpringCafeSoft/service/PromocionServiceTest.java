package com.utvt.ApiSpringCafeSoft.service;

import com.utvt.ApiSpringCafeSoft.model.Promocion;
import com.utvt.ApiSpringCafeSoft.model.SegmentoCliente;
import com.utvt.ApiSpringCafeSoft.repository.PromocionRepository;
import com.utvt.ApiSpringCafeSoft.repository.VentaRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PromocionServiceTest {

    @Mock
    private PromocionRepository promocionRepository;

    @Mock
    private VentaRepository ventaRepository;

    @Mock
    private SegmentacionClienteService segmentacionClienteService;

    private PromocionService promocionService;

    @BeforeEach
    void setUp() {
        promocionService = new PromocionService(
                promocionRepository, ventaRepository, segmentacionClienteService);
    }

    @Test
    void calculatesDiscountFromServerPromotionRatherThanRequestValue() {
        Promocion promocion = promocion(SegmentoCliente.ACTIVO, 15, 0);
        when(promocionRepository.findById(7L)).thenReturn(Optional.of(promocion));
        when(segmentacionClienteService.obtenerSegmento(31L))
                .thenReturn(SegmentoCliente.ACTIVO);

        BigDecimal descuento = promocionService.calcularDescuento(
                7L, 31L, 2, BigDecimal.valueOf(200));

        assertEquals(new BigDecimal("30.00"), descuento);
    }

    @Test
    void refusesExpiredPromotions() {
        Promocion promocion = promocion(SegmentoCliente.TODOS, 10, 0);
        promocion.setFechaInicio(LocalDateTime.now().minusDays(10));
        promocion.setFechaFin(LocalDateTime.now().minusDays(1));
        when(promocionRepository.findById(7L)).thenReturn(Optional.of(promocion));
        when(segmentacionClienteService.obtenerSegmento(31L))
                .thenReturn(SegmentoCliente.ACTIVO);

        ResponseStatusException error = assertThrows(
                ResponseStatusException.class,
                () -> promocionService.calcularDescuento(
                        7L, 31L, 2, BigDecimal.valueOf(200)));

        assertEquals(409, error.getStatusCode().value());
    }

    @Test
    void refusesPromotionsForAnotherCustomerSegment() {
        Promocion promocion = promocion(SegmentoCliente.DORMIDO, 10, 0);
        when(promocionRepository.findById(7L)).thenReturn(Optional.of(promocion));
        when(segmentacionClienteService.obtenerSegmento(31L))
                .thenReturn(SegmentoCliente.ACTIVO);

        ResponseStatusException error = assertThrows(
                ResponseStatusException.class,
                () -> promocionService.calcularDescuento(
                        7L, 31L, 2, BigDecimal.valueOf(200)));

        assertEquals(403, error.getStatusCode().value());
    }

    @Test
    void refusesPromotionWhenMinimumQuantityIsNotMet() {
        Promocion promocion = promocion(SegmentoCliente.TODOS, 10, 5);
        when(promocionRepository.findById(7L)).thenReturn(Optional.of(promocion));
        when(segmentacionClienteService.obtenerSegmento(31L))
                .thenReturn(SegmentoCliente.ACTIVO);

        ResponseStatusException error = assertThrows(
                ResponseStatusException.class,
                () -> promocionService.calcularDescuento(
                        7L, 31L, 2, BigDecimal.valueOf(200)));

        assertEquals(409, error.getStatusCode().value());
    }

    private Promocion promocion(
            SegmentoCliente segmento,
            int descuento,
            int minimo) {
        Promocion promocion = new Promocion();
        promocion.setId(7L);
        promocion.setNombre("Promo QA");
        promocion.setDescuentoPorcentaje(BigDecimal.valueOf(descuento));
        promocion.setFechaInicio(LocalDateTime.now().minusDays(1));
        promocion.setFechaFin(LocalDateTime.now().plusDays(1));
        promocion.setMinGarrafones(minimo);
        promocion.setSegmentoObjetivo(segmento);
        promocion.setActiva(true);
        return promocion;
    }
}
