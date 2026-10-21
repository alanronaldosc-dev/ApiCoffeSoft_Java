package com.utvt.ApiSpringCafeSoft;

import com.utvt.ApiSpringCafeSoft.model.Cliente;
import com.utvt.ApiSpringCafeSoft.model.EntregaPedido;
import com.utvt.ApiSpringCafeSoft.repository.ClienteRepository;
import com.utvt.ApiSpringCafeSoft.repository.EntregaPedidoRepository;
import com.utvt.ApiSpringCafeSoft.service.EntregaPedidoService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class RecuperacionHistorialTest {
    private EntregaPedidoRepository entregas;
    private EntregaPedidoService service;
    @BeforeEach void preparar() {
        entregas = mock(EntregaPedidoRepository.class);
        ClienteRepository clientes = mock(ClienteRepository.class);
        Cliente cliente = new Cliente(); cliente.setId(7L); cliente.setNombre("Cliente prueba");
        when(clientes.findById(7L)).thenReturn(Optional.of(cliente));
        service = new EntregaPedidoService();
        ReflectionTestUtils.setField(service, "entregaPedidoRepository", entregas);
        ReflectionTestUtils.setField(service, "clienteRepository", clientes);
    }
    @Test void clienteSinEntregas() {
        when(entregas.findByVentaClienteRutaIdAndResultadoOrderByFechaDesc(7L,"ENTREGADO"))
            .thenReturn(List.of());
        var h=service.obtenerHistorialCliente(7L);
        assertEquals(0,h.getTotalPedidos()); assertNull(h.getDiasDesdeUltimoPedido());
        assertTrue(h.getPedidos().isEmpty()); assertEquals(0.0,h.getPromedioGarrafones());
    }
    @Test void promedioYFrecuenciaConDosEntregas() {
        var ultima = new EntregaPedido(); ultima.setId(1L);
        ultima.setFecha(LocalDate.now().minusDays(2).atTime(12,0)); ultima.setGarrafonesEntregados(4.0);
        var anterior = new EntregaPedido(); anterior.setId(2L);
        anterior.setFecha(LocalDate.now().minusDays(8).atTime(12,0)); anterior.setGarrafonesEntregados(2.0);
        when(entregas.findByVentaClienteRutaIdAndResultadoOrderByFechaDesc(7L,"ENTREGADO"))
            .thenReturn(List.of(ultima,anterior));
        var h=service.obtenerHistorialCliente(7L);
        assertEquals(2,h.getTotalPedidos()); assertEquals(3.0,h.getPromedioGarrafones());
        assertEquals(6L,h.getFrecuenciaPromedioDias()); assertEquals(2L,h.getDiasDesdeUltimoPedido());
    }
}
