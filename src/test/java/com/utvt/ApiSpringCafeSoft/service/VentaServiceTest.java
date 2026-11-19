package com.utvt.ApiSpringCafeSoft.service;

import com.utvt.ApiSpringCafeSoft.dto.VentaDTO;
import com.utvt.ApiSpringCafeSoft.model.Venta;
import com.utvt.ApiSpringCafeSoft.model.Usuario;
import com.utvt.ApiSpringCafeSoft.model.Sucursal;
import com.utvt.ApiSpringCafeSoft.repository.*;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Tests unitarios para VentaService.
 * Usan Mockito para aislar la lógica del servicio sin BD.
 */
@ExtendWith(MockitoExtension.class)
class VentaServiceTest {

    @Mock private VentaRepository ventaRepository;
    @Mock private ProductoRepository productoRepository;
    @Mock private InventarioRepository inventarioRepository;
    @Mock private UsuarioRepository usuarioRepository;
    @Mock private LoteRepository loteRepository;
    @Mock private BeneficiosService beneficiosService;
    @Mock private NotificacionService notificacionService;

    @InjectMocks
    private VentaService ventaService;

    // ── Helpers ──

    private Venta ventaConUsuario() {
        Sucursal sucursal = new Sucursal();
        sucursal.setId(1L);
        sucursal.setNombre("Sucursal Central");

        Usuario usuario = new Usuario();
        usuario.setId(1L);
        usuario.setNombre("Juan Repartidor");
        usuario.setSucursal(sucursal);

        Venta venta = new Venta();
        venta.setId(10L);
        venta.setFolio("V-0001");
        venta.setFecha(LocalDateTime.now());
        venta.setSubtotal(100.0);
        venta.setImpuestos(16.0);
        venta.setDescuento(0.0);
        venta.setTotal(116.0);
        venta.setMetodoPago("efectivo");
        venta.setUsuario(usuario);
        venta.setSucursal(sucursal);
        venta.setEstadoPedido("PENDIENTE");
        venta.setDetalles(Collections.emptyList());
        venta.setCreatedAt(LocalDateTime.now());
        return venta;
    }

    // ── Tests: obtenerVentaPorId ──

    @Test
    @DisplayName("obtenerVentaPorId: lanza excepción cuando la venta no existe")
    void obtenerVentaPorId_noExiste_lanzaExcepcion() {
        when(ventaRepository.findById(99L)).thenReturn(Optional.empty());

        RuntimeException ex = assertThrows(
                RuntimeException.class,
                () -> ventaService.obtenerVentaPorId(99L)
        );

        assertTrue(ex.getMessage().contains("99"));
    }

    @Test
    @DisplayName("obtenerVentaPorId: retorna DTO cuando la venta existe")
    void obtenerVentaPorId_existe_retornaDTO() {
        when(ventaRepository.findById(10L)).thenReturn(Optional.of(ventaConUsuario()));

        VentaDTO resultado = ventaService.obtenerVentaPorId(10L);

        assertNotNull(resultado);
        assertEquals(10L, resultado.getId());
        assertEquals("V-0001", resultado.getFolio());
    }

    // ── Tests: obtenerTodasLasVentas ──

    @Test
    @DisplayName("obtenerTodasLasVentas: retorna lista vacía cuando no hay ventas")
    void obtenerTodasLasVentas_sinVentas_retornaListaVacia() {
        when(ventaRepository.findAll()).thenReturn(List.of());

        List<VentaDTO> resultado = ventaService.obtenerTodasLasVentas();

        assertNotNull(resultado);
        assertTrue(resultado.isEmpty());
    }

    @Test
    @DisplayName("obtenerTodasLasVentas: retorna lista con elementos cuando hay ventas")
    void obtenerTodasLasVentas_conVentas_retornaLista() {
        when(ventaRepository.findAll()).thenReturn(List.of(ventaConUsuario()));

        List<VentaDTO> resultado = ventaService.obtenerTodasLasVentas();

        assertEquals(1, resultado.size());
        assertEquals("V-0001", resultado.get(0).getFolio());
    }

    // ── Tests: obtenerVentasPorUsuario ──

    @Test
    @DisplayName("obtenerVentasPorUsuario: retorna lista de ventas del usuario")
    void obtenerVentasPorUsuario_conVentas_retornaLista() {
        when(ventaRepository.findByUsuarioId(1L)).thenReturn(List.of(ventaConUsuario()));

        List<VentaDTO> resultado = ventaService.obtenerVentasPorUsuario(1L);

        assertEquals(1, resultado.size());
    }

    @Test
    @DisplayName("obtenerVentasPorUsuario: retorna lista vacía si el usuario no tiene ventas")
    void obtenerVentasPorUsuario_sinVentas_retornaVacia() {
        when(ventaRepository.findByUsuarioId(99L)).thenReturn(List.of());

        List<VentaDTO> resultado = ventaService.obtenerVentasPorUsuario(99L);

        assertTrue(resultado.isEmpty());
    }

    // ── Tests: obtenerVentaPorFolio ──

    @Test
    @DisplayName("obtenerVentaPorFolio: lanza excepción cuando el folio no existe")
    void obtenerVentaPorFolio_noExiste_lanzaExcepcion() {
        when(ventaRepository.findByFolio("V-9999")).thenReturn(null);

        assertThrows(RuntimeException.class,
                () -> ventaService.obtenerVentaPorFolio("V-9999"));
    }

    @Test
    @DisplayName("obtenerVentaPorFolio: retorna DTO cuando el folio existe")
    void obtenerVentaPorFolio_existe_retornaDTO() {
        when(ventaRepository.findByFolio("V-0001")).thenReturn(ventaConUsuario());

        VentaDTO resultado = ventaService.obtenerVentaPorFolio("V-0001");

        assertNotNull(resultado);
        assertEquals("V-0001", resultado.getFolio());
    }

    // ── Tests: cancelarVenta ──

    @Test
    @DisplayName("cancelarVenta: lanza excepción cuando la venta no existe")
    void cancelarVenta_noExiste_lanzaExcepcion() {
        when(ventaRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(RuntimeException.class,
                () -> ventaService.cancelarVenta(99L));

        verify(ventaRepository, never()).deleteById(anyLong());
    }

    @Test
    @DisplayName("cancelarVenta: elimina correctamente cuando la venta existe")
    void cancelarVenta_existe_eliminaCorrectamente() {
        when(ventaRepository.findById(10L)).thenReturn(Optional.of(ventaConUsuario()));
        doNothing().when(ventaRepository).deleteById(10L);

        assertDoesNotThrow(() -> ventaService.cancelarVenta(10L));
        verify(ventaRepository, times(1)).deleteById(10L);
    }

    // ── Tests: marcarPedidoEntregado ──

    @Test
    @DisplayName("marcarPedidoEntregado: lanza excepción cuando el pedido no existe")
    void marcarPedidoEntregado_noExiste_lanzaExcepcion() {
        when(ventaRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(RuntimeException.class,
                () -> ventaService.marcarPedidoEntregado(99L));
    }

    @Test
    @DisplayName("marcarPedidoEntregado: cambia estado a ENTREGADA cuando el pedido existe")
    void marcarPedidoEntregado_existe_cambiaEstado() {
        Venta venta = ventaConUsuario();
        when(ventaRepository.findById(10L)).thenReturn(Optional.of(venta));
        when(ventaRepository.save(any(Venta.class))).thenAnswer(inv -> {
            Venta v = inv.getArgument(0);
            return v;
        });

        VentaDTO resultado = ventaService.marcarPedidoEntregado(10L);

        assertEquals("ENTREGADA", resultado.getEstadoPedido());
        verify(notificacionService).enviarCambioEstado(venta, "ENTREGADA");
    }

    @Test
    @DisplayName("actualizarEstadoPedido: rechaza un estado no permitido")
    void actualizarEstadoPedido_estadoInvalido_lanzaExcepcion() {
        assertThrows(
                IllegalArgumentException.class,
                () -> ventaService.actualizarEstadoPedido(10L, "VOLANDO")
        );

        verifyNoInteractions(notificacionService);
    }

    @Test
    @DisplayName("actualizarEstadoPedido: cambia el estado y envía la notificación")
    void actualizarEstadoPedido_estadoValido_cambiaYNotifica() {
        Venta venta = ventaConUsuario();

        when(ventaRepository.findById(10L)).thenReturn(Optional.of(venta));
        when(ventaRepository.save(any(Venta.class)))
                .thenAnswer(inv -> inv.getArgument(0));

        VentaDTO resultado =
                ventaService.actualizarEstadoPedido(10L, "EN_PROCESO");

        assertEquals("EN_PROCESO", resultado.getEstadoPedido());
        verify(notificacionService).enviarCambioEstado(venta, "EN_PROCESO");
    }

    // ── Tests: obtenerPedidosPendientes ──

    @Test
    @DisplayName("obtenerPedidosPendientes: retorna lista de pedidos pendientes")
    void obtenerPedidosPendientes_conPendientes_retornaLista() {
        when(ventaRepository.findByEstadoPedidoOrderByFechaAsc("PENDIENTE"))
                .thenReturn(List.of(ventaConUsuario()));

        List<VentaDTO> resultado = ventaService.obtenerPedidosPendientes();

        assertEquals(1, resultado.size());
        assertEquals("PENDIENTE", resultado.get(0).getEstadoPedido());
    }

    // ── Tests: crearVenta — validación de puntos negativos ──

    @Test
    @DisplayName("crearVenta: lanza excepción cuando los puntos canjeados son negativos")
    void crearVenta_puntosNegativos_lanzaExcepcion() {
        VentaDTO dto = new VentaDTO();
        dto.setUsuarioId(1L);
        dto.setPuntosCanjeados(-10);
        dto.setDetalles(List.of());

        Usuario usuario = new Usuario();
        usuario.setId(1L);
        usuario.setNombre("Test");
        Sucursal s = new Sucursal();
        s.setId(1L);
        usuario.setSucursal(s);

        when(usuarioRepository.findById(1L)).thenReturn(Optional.of(usuario));

        assertThrows(IllegalArgumentException.class,
                () -> ventaService.crearVenta(dto));
    }
}
