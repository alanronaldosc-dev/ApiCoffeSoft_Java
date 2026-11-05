package com.utvt.ApiSpringCafeSoft.service;

import com.utvt.ApiSpringCafeSoft.dto.CotizacionCanjeDTO;
import com.utvt.ApiSpringCafeSoft.model.Cliente;
import com.utvt.ApiSpringCafeSoft.repository.ClienteRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.server.ResponseStatusException;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * HU-028: Tests unitarios de BeneficiosService
 * Cubre lógica de cotización y canje de puntos de fidelización.
 * Cobertura objetivo: >= 80%
 */
@ExtendWith(MockitoExtension.class)
class BeneficiosServiceTest {

    @Mock
    private ClienteRepository clienteRepository;

    // puntosPorPeso = 5 (por cada $1 se acumulan 5 puntos → 5 puntos = $1 descuento)
    private BeneficiosService beneficiosService;

    private Cliente clienteActivo;

    @BeforeEach
    void setUp() {
        beneficiosService = new BeneficiosService(clienteRepository, 5);

        clienteActivo = new Cliente();
        clienteActivo.setId(1L);
        clienteActivo.setNombre("Juan Pérez");
        clienteActivo.setDomicilio("Calle 1");
        clienteActivo.setPrecioPorGarrafon(50.0);
        clienteActivo.setActivo(true);
        clienteActivo.setSaldoPuntos(500);
    }

    // ================================================================
    // CONSTRUCTOR — Validación de configuración
    // ================================================================

    @Test
    @DisplayName("HU-028 - Lanza excepción si puntosPorPeso es 0")
    void constructor_puntosPorPesoInvalido_lanzaExcepcion() {
        assertThrows(IllegalStateException.class,
                () -> new BeneficiosService(clienteRepository, 0));
    }

    @Test
    @DisplayName("HU-028 - Lanza excepción si puntosPorPeso es negativo")
    void constructor_puntosPorPesoNegativo_lanzaExcepcion() {
        assertThrows(IllegalStateException.class,
                () -> new BeneficiosService(clienteRepository, -1));
    }

    // ================================================================
    // COTIZAR — Casos exitosos
    // ================================================================

    @Test
    @DisplayName("HU-028 - Cotizar canje exitoso con puntos suficientes")
    void cotizar_exitoso_devuelveCotizacion() {
        CotizacionCanjeDTO solicitud = new CotizacionCanjeDTO();
        solicitud.setPuntosSolicitados(100);
        solicitud.setTotalCompra(150.0); // límite = 150 * 5 = 750 puntos

        when(clienteRepository.findById(1L)).thenReturn(Optional.of(clienteActivo));

        CotizacionCanjeDTO resultado = beneficiosService.cotizar(1L, solicitud);

        assertNotNull(resultado);
        assertEquals(100, resultado.getPuntosCanjeados());
        assertEquals(20.0, resultado.getDescuento()); // 100 / 5 = 20.0
    }

    @Test
    @DisplayName("HU-028 - Cotizar limita puntos al tope del total de compra")
    void cotizar_limitadoPorTotalCompra() {
        // totalCompra = 5.0 → límite = 5 * 5 = 25 puntos
        // puntos solicitados = 100, pero solo se pueden canjear 25
        CotizacionCanjeDTO solicitud = new CotizacionCanjeDTO();
        solicitud.setPuntosSolicitados(100);
        solicitud.setTotalCompra(5.0);

        when(clienteRepository.findById(1L)).thenReturn(Optional.of(clienteActivo));

        CotizacionCanjeDTO resultado = beneficiosService.cotizar(1L, solicitud);

        assertEquals(25, resultado.getPuntosCanjeados());
        assertEquals(5.0, resultado.getDescuento()); // 25 / 5 = 5.0
    }

    @Test
    @DisplayName("HU-028 - Cotizar con puntos exactamente iguales al saldo")
    void cotizar_puntosExactosAlSaldo() {
        CotizacionCanjeDTO solicitud = new CotizacionCanjeDTO();
        solicitud.setPuntosSolicitados(500); // saldo exacto
        solicitud.setTotalCompra(1000.0);    // límite = 5000, sin restricción

        when(clienteRepository.findById(1L)).thenReturn(Optional.of(clienteActivo));

        CotizacionCanjeDTO resultado = beneficiosService.cotizar(1L, solicitud);

        assertEquals(500, resultado.getPuntosCanjeados());
        assertEquals(100.0, resultado.getDescuento()); // 500 / 5 = 100
    }

    // ================================================================
    // COTIZAR — Casos de error
    // ================================================================

    @Test
    @DisplayName("HU-028 - Cotizar con cliente inexistente lanza 404")
    void cotizar_clienteNoExiste_lanza404() {
        when(clienteRepository.findById(99L)).thenReturn(Optional.empty());

        CotizacionCanjeDTO solicitud = new CotizacionCanjeDTO();
        solicitud.setPuntosSolicitados(100);
        solicitud.setTotalCompra(150.0);

        ResponseStatusException ex = assertThrows(ResponseStatusException.class,
                () -> beneficiosService.cotizar(99L, solicitud));

        assertEquals(404, ex.getStatusCode().value());
    }

    @Test
    @DisplayName("HU-028 - Cotizar con cliente inactivo lanza 400")
    void cotizar_clienteInactivo_lanza400() {
        clienteActivo.setActivo(false);
        when(clienteRepository.findById(1L)).thenReturn(Optional.of(clienteActivo));

        CotizacionCanjeDTO solicitud = new CotizacionCanjeDTO();
        solicitud.setPuntosSolicitados(100);
        solicitud.setTotalCompra(150.0);

        ResponseStatusException ex = assertThrows(ResponseStatusException.class,
                () -> beneficiosService.cotizar(1L, solicitud));

        assertEquals(400, ex.getStatusCode().value());
    }

    @Test
    @DisplayName("HU-028 - Cotizar con saldo insuficiente lanza 400")
    void cotizar_saldoInsuficiente_lanza400() {
        clienteActivo.setSaldoPuntos(50); // menos de los 100 solicitados
        when(clienteRepository.findById(1L)).thenReturn(Optional.of(clienteActivo));

        CotizacionCanjeDTO solicitud = new CotizacionCanjeDTO();
        solicitud.setPuntosSolicitados(100);
        solicitud.setTotalCompra(150.0);

        ResponseStatusException ex = assertThrows(ResponseStatusException.class,
                () -> beneficiosService.cotizar(1L, solicitud));

        assertEquals(400, ex.getStatusCode().value());
    }

    @Test
    @DisplayName("HU-028 - Cotizar con puntos solicitados cero lanza 400")
    void cotizar_puntosCero_lanza400() {
        when(clienteRepository.findById(1L)).thenReturn(Optional.of(clienteActivo));

        CotizacionCanjeDTO solicitud = new CotizacionCanjeDTO();
        solicitud.setPuntosSolicitados(0);
        solicitud.setTotalCompra(150.0);

        assertThrows(ResponseStatusException.class,
                () -> beneficiosService.cotizar(1L, solicitud));
    }

    @Test
    @DisplayName("HU-028 - Cotizar con total compra cero lanza 400")
    void cotizar_totalCompraCero_lanzaExcepcion() {
        when(clienteRepository.findById(1L)).thenReturn(Optional.of(clienteActivo));

        CotizacionCanjeDTO solicitud = new CotizacionCanjeDTO();
        solicitud.setPuntosSolicitados(100);
        solicitud.setTotalCompra(0.0);

        assertThrows(ResponseStatusException.class,
                () -> beneficiosService.cotizar(1L, solicitud));
    }

    @Test
    @DisplayName("HU-028 - Cotizar cuando totalCompra no permite canjear ningún punto lanza excepción")
    void cotizar_puntosCanjeablesNulos_lanzaIllegalArgument() {
        // Con puntosPorPeso=5 y totalCompra=0.19 → límite = 0 puntos
        when(clienteRepository.findById(1L)).thenReturn(Optional.of(clienteActivo));

        CotizacionCanjeDTO solicitud = new CotizacionCanjeDTO();
        solicitud.setPuntosSolicitados(1);
        solicitud.setTotalCompra(0.19); // 0.19 * 5 = 0.95 → floor = 0

        assertThrows(Exception.class,
                () -> beneficiosService.cotizar(1L, solicitud));
    }

    // ================================================================
    // CONSUMIR — Casos exitosos
    // ================================================================

    @Test
    @DisplayName("HU-028 - Consumir puntos exitosamente descuenta saldo del cliente")
    void consumir_exitoso_descuentaSaldo() {
        when(clienteRepository.findWithLockById(1L)).thenReturn(Optional.of(clienteActivo));
        when(clienteRepository.save(any(Cliente.class))).thenReturn(clienteActivo);

        // puntosSolicitados=100, descuento=20.0, totalCompra=150 → límite=750 ≥ 100 ✓
        int saldoRestante = beneficiosService.consumir(1L, 100, 20.0, 150.0);

        assertEquals(400, saldoRestante); // 500 - 100 = 400
        verify(clienteRepository, times(1)).save(clienteActivo);
    }

    @Test
    @DisplayName("HU-028 - Consumir todos los puntos disponibles")
    void consumir_todoElSaldo_dejaSaldoCero() {
        when(clienteRepository.findWithLockById(1L)).thenReturn(Optional.of(clienteActivo));
        when(clienteRepository.save(any(Cliente.class))).thenReturn(clienteActivo);

        // 500 puntos / 5 = $100 descuento, totalCompra = 1000
        int saldoRestante = beneficiosService.consumir(1L, 500, 100.0, 1000.0);

        assertEquals(0, saldoRestante);
        verify(clienteRepository, times(1)).save(clienteActivo);
    }

    // ================================================================
    // CONSUMIR — Casos de error
    // ================================================================

    @Test
    @DisplayName("HU-028 - Consumir sin clienteId lanza 400")
    void consumir_sinClienteId_lanza400() {
        ResponseStatusException ex = assertThrows(ResponseStatusException.class,
                () -> beneficiosService.consumir(null, 100, 20.0, 150.0));

        assertEquals(400, ex.getStatusCode().value());
    }

    @Test
    @DisplayName("HU-028 - Consumir con cliente inexistente lanza 404")
    void consumir_clienteNoExiste_lanza404() {
        when(clienteRepository.findWithLockById(99L)).thenReturn(Optional.empty());

        ResponseStatusException ex = assertThrows(ResponseStatusException.class,
                () -> beneficiosService.consumir(99L, 100, 20.0, 150.0));

        assertEquals(404, ex.getStatusCode().value());
    }

    @Test
    @DisplayName("HU-028 - Consumir con cliente inactivo lanza 400")
    void consumir_clienteInactivo_lanza400() {
        clienteActivo.setActivo(false);
        when(clienteRepository.findWithLockById(1L)).thenReturn(Optional.of(clienteActivo));

        ResponseStatusException ex = assertThrows(ResponseStatusException.class,
                () -> beneficiosService.consumir(1L, 100, 20.0, 150.0));

        assertEquals(400, ex.getStatusCode().value());
    }

    @Test
    @DisplayName("HU-028 - Consumir con saldo insuficiente lanza 400")
    void consumir_saldoInsuficiente_lanza400() {
        clienteActivo.setSaldoPuntos(50);
        when(clienteRepository.findWithLockById(1L)).thenReturn(Optional.of(clienteActivo));

        ResponseStatusException ex = assertThrows(ResponseStatusException.class,
                () -> beneficiosService.consumir(1L, 100, 20.0, 150.0));

        assertEquals(400, ex.getStatusCode().value());
    }

    @Test
    @DisplayName("HU-028 - Consumir con cotización desactualizada lanza 409")
    void consumir_cotizacionDesactualizada_lanza409() {
        when(clienteRepository.findWithLockById(1L)).thenReturn(Optional.of(clienteActivo));

        // descuentoSolicitado incorrecto: debería ser 20.0 pero enviamos 25.0
        ResponseStatusException ex = assertThrows(ResponseStatusException.class,
                () -> beneficiosService.consumir(1L, 100, 25.0, 150.0));

        assertEquals(409, ex.getStatusCode().value());
    }

    @Test
    @DisplayName("HU-028 - Consumir con descuento nulo lanza 409")
    void consumir_descuentoNulo_lanza409() {
        when(clienteRepository.findWithLockById(1L)).thenReturn(Optional.of(clienteActivo));

        ResponseStatusException ex = assertThrows(ResponseStatusException.class,
                () -> beneficiosService.consumir(1L, 100, null, 150.0));

        assertEquals(409, ex.getStatusCode().value());
    }

    @Test
    @DisplayName("HU-028 - Consumir con puntos nulos lanza 400")
    void consumir_puntosNulos_lanza400() {
        when(clienteRepository.findWithLockById(1L)).thenReturn(Optional.of(clienteActivo));

        assertThrows(ResponseStatusException.class,
                () -> beneficiosService.consumir(1L, null, 20.0, 150.0));
    }
}
