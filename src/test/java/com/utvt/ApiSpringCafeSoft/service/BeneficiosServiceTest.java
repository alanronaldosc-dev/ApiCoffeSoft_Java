package com.utvt.ApiSpringCafeSoft.service;

import com.utvt.ApiSpringCafeSoft.dto.CotizacionCanjeDTO;
import com.utvt.ApiSpringCafeSoft.model.Cliente;
import com.utvt.ApiSpringCafeSoft.repository.ClienteRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class BeneficiosServiceTest {

    private ClienteRepository clienteRepository;
    private BeneficiosService beneficiosService;
    private Cliente cliente;

    @BeforeEach
    void setUp() {
        clienteRepository = mock(ClienteRepository.class);
        beneficiosService = new BeneficiosService(clienteRepository, 5);
        cliente = new Cliente();
        cliente.setId(1L);
        cliente.setActivo(true);
        cliente.setSaldoPuntos(500);
        when(clienteRepository.findById(1L)).thenReturn(Optional.of(cliente));
        when(clienteRepository.findWithLockById(1L)).thenReturn(Optional.of(cliente));
    }

    @Test
    // HU-023: la cotización limita el descuento sin modificar el saldo.
    void quoteCalculatesDiscountWithoutConsumingPoints() {
        CotizacionCanjeDTO request = new CotizacionCanjeDTO();
        request.setPuntosSolicitados(500);
        request.setTotalCompra(2.00);

        CotizacionCanjeDTO quote = beneficiosService.cotizar(1L, request);

        assertEquals(10, quote.getPuntosCanjeados());
        assertEquals(2.00, quote.getDescuento());
        assertEquals(500, cliente.getSaldoPuntos());
        verify(clienteRepository, never()).save(cliente);
    }

    @Test
    void consumesPointsOnlyAfterValidatingTheQuotedDiscount() {
        int remainingPoints = beneficiosService.consumir(1L, 100, 20.00, 50.00);

        assertEquals(400, remainingPoints);
        assertEquals(400, cliente.getSaldoPuntos());
        verify(clienteRepository).save(cliente);
    }

    @Test
    void rejectsInsufficientBalanceWithoutChangingIt() {
        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                () -> beneficiosService.consumir(1L, 501, 5.01, 100.00));

        assertEquals(HttpStatus.BAD_REQUEST, exception.getStatusCode());
        assertEquals(500, cliente.getSaldoPuntos());
        verify(clienteRepository, never()).save(cliente);
    }

    @Test
    void rejectsStaleOrForgedQuoteWithoutChangingBalance() {
        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                () -> beneficiosService.consumir(1L, 100, 19.99, 50.00));

        assertEquals(HttpStatus.CONFLICT, exception.getStatusCode());
        assertEquals(500, cliente.getSaldoPuntos());
        verify(clienteRepository, never()).save(cliente);
    }
}
