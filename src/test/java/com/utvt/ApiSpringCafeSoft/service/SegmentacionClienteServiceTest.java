package com.utvt.ApiSpringCafeSoft.service;

import com.utvt.ApiSpringCafeSoft.model.SegmentoCliente;
import com.utvt.ApiSpringCafeSoft.repository.UsuarioRepository;
import com.utvt.ApiSpringCafeSoft.repository.VentaRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;

class SegmentacionClienteServiceTest {

    private SegmentacionClienteService segmentacionService;

    @BeforeEach
    void setUp() {
        segmentacionService = new SegmentacionClienteService(
                mock(UsuarioRepository.class), mock(VentaRepository.class));
    }

    @Test
    void classifiesCustomersByPurchaseCountAndRecency() {
        LocalDateTime ahora = LocalDateTime.now();

        assertEquals(
                SegmentoCliente.POTENCIAL,
                segmentacionService.clasificar(0, null, ahora));
        assertEquals(
                SegmentoCliente.DORMIDO,
                segmentacionService.clasificar(8, ahora.minusDays(91), ahora));
        assertEquals(
                SegmentoCliente.ACTIVO,
                segmentacionService.clasificar(3, ahora.minusDays(29), ahora));
        assertEquals(
                SegmentoCliente.POCO_FRECUENTE,
                segmentacionService.clasificar(2, ahora.minusDays(5), ahora));
    }
}
