package com.utvt.ApiSpringCafeSoft.service;

import com.utvt.ApiSpringCafeSoft.dto.ResumenLiquidacionDTO;
import com.utvt.ApiSpringCafeSoft.model.Usuario;
import com.utvt.ApiSpringCafeSoft.repository.CargaRepository;
import com.utvt.ApiSpringCafeSoft.repository.EntregaPedidoRepository;
import com.utvt.ApiSpringCafeSoft.repository.InventarioRepository;
import com.utvt.ApiSpringCafeSoft.repository.LiquidacionRepository;
import com.utvt.ApiSpringCafeSoft.repository.MovimientoPuntosRepository;
import com.utvt.ApiSpringCafeSoft.repository.UsuarioRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class LiquidacionServiceTest {

    @Mock private LiquidacionRepository liquidacionRepository;
    @Mock private UsuarioRepository usuarioRepository;
    @Mock private CargaRepository cargaRepository;
    @Mock private EntregaPedidoRepository entregaPedidoRepository;
    @Mock private InventarioRepository inventarioRepository;
    @Mock private MovimientoPuntosRepository movimientoPuntosRepository;

    @InjectMocks
    private LiquidacionService service;

    @Test
    void listarLiquidaciones_repartidorNoExistente_lanzaExcepcion() {
        when(usuarioRepository.findById(99L)).thenReturn(Optional.empty());
        assertThrows(RuntimeException.class, () -> service.listarLiquidaciones(99L));
    }

    @Test
    void listarLiquidaciones_repartidorExistente_listaVacia() {
        Usuario u = new Usuario();
        u.setId(1L);
        u.setUserTipo(4);
        when(usuarioRepository.findById(1L)).thenReturn(Optional.of(u));
        when(liquidacionRepository.findByRepartidorIdOrderByFechaOperacionDesc(1L)).thenReturn(List.of());

        var resultado = service.listarLiquidaciones(1L);

        assertTrue(resultado.isEmpty());
    }

    @Test
    void obtenerLiquidacionHoy_sinLiquidaciones_retornaNull() {
        Usuario u = new Usuario();
        u.setId(1L);
        u.setUserTipo(4);
        when(usuarioRepository.findById(1L)).thenReturn(Optional.of(u));
        when(liquidacionRepository.findByRepartidorIdAndFechaOperacionOrderByFechaCierreDesc(eq(1L), any(LocalDate.class)))
                .thenReturn(List.of());

        assertNull(service.obtenerLiquidacionHoy(1L));
    }

    @Test
    void obtenerResumen_sinCargasNiEntregas_retornaCeros() {
        Usuario u = new Usuario();
        u.setId(1L);
        u.setUserTipo(4);
        u.setNombre("Repartidor");
        when(usuarioRepository.findById(1L)).thenReturn(Optional.of(u));
        when(cargaRepository.findByRepartidorIdAndFechaHoraBetweenOrderByFechaHoraAsc(eq(1L), any(), any()))
                .thenReturn(List.of());
        when(entregaPedidoRepository.findByRepartidorIdAndFechaBetweenOrderByFechaAsc(eq(1L), any(), any()))
                .thenReturn(List.of());

        ResumenLiquidacionDTO resumen = service.obtenerResumen(1L, null);

        assertEquals(0.0, resumen.getCargaInicial());
        assertEquals(0, resumen.getEntregasRealizadas());
        assertEquals(0.0, resumen.getTotalCobrado());
    }
}
