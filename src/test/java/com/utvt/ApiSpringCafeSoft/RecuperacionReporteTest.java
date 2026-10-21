package com.utvt.ApiSpringCafeSoft;
import com.utvt.ApiSpringCafeSoft.model.EntregaPedido;
import com.utvt.ApiSpringCafeSoft.repository.EntregaPedidoRepository;
import com.utvt.ApiSpringCafeSoft.service.EntregaPedidoService;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;
import java.time.LocalDate;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
class RecuperacionReporteTest {
    @Test void rechazaRangoInvertido() {
        var s=new EntregaPedidoService();
        assertThrows(RuntimeException.class,()->s.generarReporteEntregas(LocalDate.of(2026,10,21),LocalDate.of(2026,10,20),null));
    }
    @Test void sumaEntregasYEnvases() {
        var repo=mock(EntregaPedidoRepository.class);
        var s=new EntregaPedidoService(); ReflectionTestUtils.setField(s,"entregaPedidoRepository",repo);
        var dia=LocalDate.of(2026,10,21);
        var e=new EntregaPedido();e.setId(1L);e.setFecha(dia.atTime(10,0));
        e.setGarrafonesEntregados(3.0);e.setEnvasesVaciosRecibidos(2.0);e.setMontoCobrado(60.0);e.setMetodoCobro("EFECTIVO");
        when(repo.findByResultadoAndFechaBetweenOrderByFechaAsc("ENTREGADO",dia.atStartOfDay(),dia.plusDays(1).atStartOfDay())).thenReturn(List.of(e));
        var r=s.generarReporteEntregas(dia,dia,null);
        assertEquals(1,r.getTotalEntregas());assertEquals(3.0,r.getTotalGarrafonesVendidos());
        assertEquals(2.0,r.getTotalEnvasesRetornados());assertEquals(60.0,r.getTotalEfectivoCobrado());
    }
}
