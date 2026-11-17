
package com.utvt.ApiSpringCafeSoft.service;

import com.utvt.ApiSpringCafeSoft.dto.VentaDTO;
import com.utvt.ApiSpringCafeSoft.dto.VentaDetalleDTO;
import com.utvt.ApiSpringCafeSoft.model.*;
import com.utvt.ApiSpringCafeSoft.repository.VentaRepository;
import com.utvt.ApiSpringCafeSoft.repository.UsuarioRepository;
import com.utvt.ApiSpringCafeSoft.repository.ProductoRepository;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class VentaServiceConsultasExtraTest {

    @Mock
    VentaRepository ventaRepository;

    @Mock
    UsuarioRepository usuarioRepository;

    @Mock
    ProductoRepository productoRepository;

    @InjectMocks
    VentaService service;

    // ============================================
    // DATOS DE PRUEBA
    // ============================================

    private Venta ventaValida() {

        Usuario usuario = new Usuario();
        usuario.setId(4L);
        usuario.setNombre("Cajero");

        Venta venta = new Venta();
        venta.setId(8L);
        venta.setUsuario(usuario);
        venta.setFolio("V-0008");
        venta.setFecha(
            LocalDateTime.of(2026, 11, 10, 12, 0)
        );
        venta.setMetodoPago("efectivo");
        venta.setDetalles(List.of());

        return venta;
    }

    // ============================================
    // PRUEBAS DE RANGO DE FECHAS
    // ============================================

    @Test
    void rangoFechasSinVentas() {

        LocalDateTime desde =
            LocalDateTime.of(2026, 11, 1, 0, 0);

        LocalDateTime hasta =
            LocalDateTime.of(2026, 11, 15, 23, 59);

        when(ventaRepository.findByFechaBetween(desde, hasta))
            .thenReturn(List.of());

        assertTrue(
            service.obtenerVentasPorRangoFechas(desde, hasta)
                .isEmpty()
        );

        verify(ventaRepository)
            .findByFechaBetween(desde, hasta);
    }

    @Test
    void rangoFechasConvierteVentasADto() {

        LocalDateTime desde =
            LocalDateTime.of(2026, 11, 1, 0, 0);

        LocalDateTime hasta =
            LocalDateTime.of(2026, 11, 15, 23, 59);

        when(ventaRepository.findByFechaBetween(desde, hasta))
            .thenReturn(List.of(ventaValida()));

        List<VentaDTO> resultado =
            service.obtenerVentasPorRangoFechas(desde, hasta);

        assertEquals(1, resultado.size());
        assertEquals(8L, resultado.get(0).getId());
        assertEquals("V-0008", resultado.get(0).getFolio());
    }

    // ============================================
    // PRUEBAS DE METODOS DE PAGO
    // ============================================

    @Test
    void metodoPagoConVentas() {

        when(ventaRepository.findByMetodoPago("efectivo"))
            .thenReturn(List.of(ventaValida()));

        assertEquals(
            1,
            service.obtenerVentasPorMetodoPago("efectivo")
                .size()
        );
    }

    @Test
    void metodoPagoSinVentas() {

        when(ventaRepository.findByMetodoPago("tarjeta"))
            .thenReturn(List.of());

        assertTrue(
            service.obtenerVentasPorMetodoPago("tarjeta")
                .isEmpty()
        );
    }

    // ============================================
    // PRUEBAS DE PEDIDOS PENDIENTES
    // ============================================

    @Test
    void pedidosPendientesConVenta() {

        when(
            ventaRepository.findByEstadoPedidoOrderByFechaAsc(
                "PENDIENTE"
            )
        ).thenReturn(List.of(ventaValida()));

        assertEquals(
            1,
            service.obtenerPedidosPendientes().size()
        );
    }

    // ============================================
    // PRUEBAS DE BUSQUEDA POR FOLIO
    // ============================================

    @Test
    void ventaPorFolioEncontrada() {

        when(ventaRepository.findByFolio("V-0008"))
            .thenReturn(ventaValida());

        assertEquals(
            "V-0008",
            service.obtenerVentaPorFolio("V-0008")
                .getFolio()
        );
    }

    @Test
    void ventaPorFolioInexistente() {

        when(ventaRepository.findByFolio("NO-EXISTE"))
            .thenReturn(null);

        assertThrows(
            RuntimeException.class,
            () -> service.obtenerVentaPorFolio("NO-EXISTE")
        );
    }

    // ============================================
    // PRUEBAS DE BUSQUEDA POR ID
    // ============================================

    @Test
    void ventaPorIdEncontrada() {

        when(ventaRepository.findById(8L))
            .thenReturn(Optional.of(ventaValida()));

        assertEquals(
            8L,
            service.obtenerVentaPorId(8L).getId()
        );
    }

    @Test
    void ventaPorIdInexistente() {

        when(ventaRepository.findById(99L))
            .thenReturn(Optional.empty());

        assertThrows(
            RuntimeException.class,
            () -> service.obtenerVentaPorId(99L)
        );
    }

    // ============================================
    // PRUEBAS DE ESTADOS
    // ============================================

    @Test
    void estadoInvalidoRechazado() {

        assertThrows(
            IllegalArgumentException.class,
            () -> service.actualizarEstadoPedido(
                8L,
                "DESCONOCIDO"
            )
        );

        verifyNoInteractions(ventaRepository);
    }

    @Test
    void estadoNuloRechazado() {

        assertThrows(
            IllegalArgumentException.class,
            () -> service.actualizarEstadoPedido(
                8L,
                null
            )
        );

        verifyNoInteractions(ventaRepository);
    }

    // ============================================
    // PRUEBAS DE CANCELACION
    // ============================================

    @Test
    void cancelarVentaInexistente() {

        when(ventaRepository.findById(99L))
            .thenReturn(Optional.empty());

        assertThrows(
            RuntimeException.class,
            () -> service.cancelarVenta(99L)
        );

        verify(ventaRepository, never())
            .deleteById(anyLong());
    }

    @Test
    void cancelarVentaExistente() {

        when(ventaRepository.findById(8L))
            .thenReturn(Optional.of(ventaValida()));

        assertDoesNotThrow(
            () -> service.cancelarVenta(8L)
        );

        verify(ventaRepository).deleteById(8L);
    }

    // ============================================
    // NUEVAS PRUEBAS DE CREAR VENTA
    // ============================================

    @Test
    void crearVentaRechazaUsuarioInexistente() {

        VentaDTO dto = new VentaDTO();
        dto.setUsuarioId(999L);
        dto.setDetalles(List.of());

        when(usuarioRepository.findById(999L))
            .thenReturn(Optional.empty());

        RuntimeException error = assertThrows(
            RuntimeException.class,
            () -> service.crearVenta(dto)
        );

        assertTrue(
            error.getMessage()
                .contains("Usuario no encontrado")
        );

        verify(usuarioRepository).findById(999L);

        verifyNoInteractions(productoRepository);
    }

    @Test
    void crearVentaRechazaProductoInexistente() {

        Usuario usuario = new Usuario();
        usuario.setId(4L);

        VentaDetalleDTO detalle = new VentaDetalleDTO();
        detalle.setProductoId(999L);
        detalle.setCantidad(1);

        VentaDTO dto = new VentaDTO();
        dto.setUsuarioId(4L);
        dto.setDetalles(List.of(detalle));

        when(usuarioRepository.findById(4L))
            .thenReturn(Optional.of(usuario));

        when(productoRepository.findByIdWithInsumos(999L))
            .thenReturn(null);

        RuntimeException error = assertThrows(
            RuntimeException.class,
            () -> service.crearVenta(dto)
        );

        assertTrue(
            error.getMessage()
                .contains("Producto no encontrado")
        );

        verify(usuarioRepository).findById(4L);

        verify(productoRepository)
            .findByIdWithInsumos(999L);
    }
}