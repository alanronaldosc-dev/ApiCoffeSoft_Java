package com.utvt.ApiSpringCafeSoft.service;

import com.utvt.ApiSpringCafeSoft.dto.CotizacionCanjeDTO;
import com.utvt.ApiSpringCafeSoft.dto.CotizacionCanjeRequestDTO;
import com.utvt.ApiSpringCafeSoft.model.Usuario;
import com.utvt.ApiSpringCafeSoft.repository.UsuarioRepository;
import com.utvt.ApiSpringCafeSoft.repository.VentaRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PuntosServiceTest {

    @Mock
    private UsuarioRepository usuarioRepository;

    @Mock
    private VentaRepository ventaRepository;

    @InjectMocks
    private PuntosService puntosService;

    @Test
    void quoteCapsDiscountToFivePointsPerPesoAndDoesNotConsumeBalance() {
        Usuario cliente = cliente(100);
        when(usuarioRepository.findById(31L)).thenReturn(Optional.of(cliente));
        CotizacionCanjeRequestDTO solicitud = new CotizacionCanjeRequestDTO();
        solicitud.setPuntosSolicitados(80);
        solicitud.setTotalCompra(BigDecimal.TEN);

        CotizacionCanjeDTO cotizacion = puntosService.cotizar(31L, solicitud);

        assertEquals(50, cotizacion.getPuntosCanjeados());
        assertEquals(new BigDecimal("10.00"), cotizacion.getDescuento());
        assertEquals(100, cliente.getSaldoPuntos());
    }

    @Test
    void rejectsQuoteWhenRequestedPointsExceedBalance() {
        when(usuarioRepository.findById(31L)).thenReturn(Optional.of(cliente(20)));
        CotizacionCanjeRequestDTO solicitud = new CotizacionCanjeRequestDTO();
        solicitud.setPuntosSolicitados(21);
        solicitud.setTotalCompra(BigDecimal.TEN);

        ResponseStatusException error = assertThrows(
                ResponseStatusException.class,
                () -> puntosService.cotizar(31L, solicitud));

        assertEquals(400, error.getStatusCode().value());
    }

    @Test
    void deductsOnlyThePointsCoveredBySaleTotal() {
        Usuario cliente = cliente(100);
        when(usuarioRepository.findByIdForUpdate(31L)).thenReturn(Optional.of(cliente));
        when(usuarioRepository.save(cliente)).thenReturn(cliente);

        PuntosService.CanjeAplicado canje = puntosService.aplicarCanje(
                31L, 80, BigDecimal.TEN);

        assertEquals(50, canje.puntosCanjeados());
        assertEquals(new BigDecimal("10.00"), canje.descuento());
        assertEquals(50, canje.saldoPuntosRestante());
        assertSame(cliente, canje.cliente());
        assertEquals(50, cliente.getSaldoPuntos());
        verify(usuarioRepository).findByIdForUpdate(31L);
    }

    private Usuario cliente(int saldo) {
        Usuario cliente = new Usuario();
        cliente.setId(31L);
        cliente.setUserTipo(2);
        cliente.setActivo(true);
        cliente.setSaldoPuntos(saldo);
        return cliente;
    }
}
