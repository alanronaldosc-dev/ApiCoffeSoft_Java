package com.utvt.ApiSpringCafeSoft.service;

import com.utvt.ApiSpringCafeSoft.dto.VentaDTO;
import com.utvt.ApiSpringCafeSoft.dto.VentaDetalleDTO;
import com.utvt.ApiSpringCafeSoft.model.Producto;
import com.utvt.ApiSpringCafeSoft.model.Promocion;
import com.utvt.ApiSpringCafeSoft.model.Usuario;
import com.utvt.ApiSpringCafeSoft.model.Venta;
import com.utvt.ApiSpringCafeSoft.repository.InventarioRepository;
import com.utvt.ApiSpringCafeSoft.repository.LoteRepository;
import com.utvt.ApiSpringCafeSoft.repository.ProductoRepository;
import com.utvt.ApiSpringCafeSoft.repository.UsuarioRepository;
import com.utvt.ApiSpringCafeSoft.repository.VentaRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class VentaServicePromotionTest {

    @Mock private VentaRepository ventaRepository;
    @Mock private ProductoRepository productoRepository;
    @Mock private InventarioRepository inventarioRepository;
    @Mock private UsuarioRepository usuarioRepository;
    @Mock private LoteRepository loteRepository;
    @Mock private PromocionService promocionService;
    @Mock private SegmentacionClienteService segmentacionClienteService;
    @Mock private PuntosService puntosService;

    @InjectMocks
    private VentaService ventaService;

    @Test
    void rejectsUnverifiedDiscountSubmittedWithoutPromotion() {
        Usuario empleado = new Usuario();
        empleado.setId(5L);
        when(usuarioRepository.findById(5L)).thenReturn(Optional.of(empleado));
        VentaDTO request = ventaRequest();
        request.setDescuento(25.0);

        ResponseStatusException error = assertThrows(
                ResponseStatusException.class,
                () -> ventaService.crearVenta(request));

        assertEquals(400, error.getStatusCode().value());
        verify(ventaRepository, org.mockito.Mockito.never()).save(any(Venta.class));
    }

    @Test
    void ignoresClientDiscountAndAppliesServerCalculatedPromotion() {
        Usuario empleado = new Usuario();
        empleado.setId(5L);
        Usuario cliente = new Usuario();
        cliente.setId(31L);
        cliente.setUserTipo(2);
        cliente.setActivo(true);
        Producto producto = new Producto();
        producto.setId(11L);
        producto.setNombre("Garrafón");
        producto.setPrecio(100.0);
        Promocion promocion = new Promocion();
        promocion.setId(7L);

        when(usuarioRepository.findById(5L)).thenReturn(Optional.of(empleado));
        when(segmentacionClienteService.obtenerClienteActivo(31L)).thenReturn(cliente);
        when(productoRepository.findByIdWithInsumos(11L)).thenReturn(producto);
        when(ventaRepository.findLastFolio()).thenReturn(null);
        when(promocionService.calcularDescuento(
                org.mockito.ArgumentMatchers.eq(7L),
                org.mockito.ArgumentMatchers.eq(31L),
                org.mockito.ArgumentMatchers.eq(2),
                any(BigDecimal.class)))
                .thenReturn(BigDecimal.valueOf(30));
        when(promocionService.obtenerPorId(7L)).thenReturn(promocion);
        when(ventaRepository.save(any(Venta.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        VentaDTO request = ventaRequest();
        request.setClienteId(31L);
        request.setPromocionId(7L);
        request.setDescuento(999.0);
        request.setDetalles(List.of(new VentaDetalleDTO(11L, 2, 0.0)));

        VentaDTO result = ventaService.crearVenta(request);

        assertEquals(30.0, result.getDescuento());
        assertEquals(202.0, result.getTotal());
        assertEquals(31L, result.getClienteId());
        assertEquals(7L, result.getPromocionId());
    }

    @Test
    void ignoresClientPointsDiscountAndAppliesServerCalculatedRedemption() {
        Usuario empleado = new Usuario();
        empleado.setId(5L);
        Usuario cliente = new Usuario();
        cliente.setId(31L);
        cliente.setUserTipo(2);
        cliente.setActivo(true);
        Producto producto = new Producto();
        producto.setId(11L);
        producto.setNombre("Garrafón");
        producto.setPrecio(100.0);

        when(usuarioRepository.findById(5L)).thenReturn(Optional.of(empleado));
        when(productoRepository.findByIdWithInsumos(11L)).thenReturn(producto);
        when(ventaRepository.findLastFolio()).thenReturn(null);
        when(puntosService.aplicarCanje(31L, 10, BigDecimal.valueOf(232.0)))
                .thenReturn(new PuntosService.CanjeAplicado(
                        10, new BigDecimal("2.00"), 90, cliente));
        when(ventaRepository.save(any(Venta.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        VentaDTO request = ventaRequest();
        request.setClienteId(31L);
        request.setPuntosCanjeados(10);
        request.setDescuento(999.0);
        request.setDetalles(List.of(new VentaDetalleDTO(11L, 2, 0.0)));

        VentaDTO result = ventaService.crearVenta(request);

        assertEquals(2.0, result.getDescuento());
        assertEquals(230.0, result.getTotal());
        assertEquals(31L, result.getClienteId());
        assertEquals(10, result.getPuntosCanjeados());
        assertEquals(90, result.getSaldoPuntosRestante());
        verify(segmentacionClienteService, never()).obtenerClienteActivo(31L);
    }

    private VentaDTO ventaRequest() {
        VentaDTO request = new VentaDTO();
        request.setUsuarioId(5L);
        request.setMetodoPago("tarjeta");
        request.setDetalles(List.of());
        return request;
    }
}
