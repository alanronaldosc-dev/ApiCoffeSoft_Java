package com.utvt.ApiSpringCafeSoft.controller;

import com.utvt.ApiSpringCafeSoft.dto.VentaDTO;
import com.utvt.ApiSpringCafeSoft.service.VentaService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class VentaControllerTest {
    @Mock VentaService service;
    @InjectMocks VentaController controller;
    private final LocalDate inicio = LocalDate.of(2026, 11, 1);
    private final LocalDate fin = LocalDate.of(2026, 11, 3);

    @Test void crearVentaDevuelve201() {
        VentaDTO entrada = new VentaDTO();
        VentaDTO salida = new VentaDTO();
        when(service.crearVenta(entrada)).thenReturn(salida);
        ResponseEntity<VentaDTO> r = controller.crearVenta(entrada);
        assertEquals(HttpStatus.CREATED, r.getStatusCode());
        assertSame(salida, r.getBody());
    }
    @Test void listarVentasDevuelve200() {
        when(service.obtenerTodasLasVentas()).thenReturn(List.of());
        assertEquals(HttpStatus.OK, controller.obtenerTodasLasVentas().getStatusCode());
        assertTrue(controller.obtenerTodasLasVentas().getBody().isEmpty());
    }
    @Test void obtenerVentaPorId() {
        VentaDTO dto = new VentaDTO();
        when(service.obtenerVentaPorId(3L)).thenReturn(dto);
        assertSame(dto, controller.obtenerVentaPorId(3L).getBody());
    }
    @Test void obtenerVentaPorFolio() {
        VentaDTO dto = new VentaDTO();
        when(service.obtenerVentaPorFolio("V-0003")).thenReturn(dto);
        assertSame(dto, controller.obtenerVentaPorFolio("V-0003").getBody());
    }
    @Test void obtenerVentasPorUsuario() {
        when(service.obtenerVentasPorUsuario(2L)).thenReturn(List.of());
        assertTrue(controller.obtenerVentasPorUsuario(2L).getBody().isEmpty());
    }
    @Test void obtenerVentasPorFechas() {
        LocalDateTime a = inicio.atStartOfDay(), b = fin.atStartOfDay();
        when(service.obtenerVentasPorRangoFechas(a,b)).thenReturn(List.of());
        assertEquals(HttpStatus.OK, controller.obtenerVentasPorRangoFechas(a,b).getStatusCode());
    }
    @Test void obtenerVentasPorMetodoPago() {
        when(service.obtenerVentasPorMetodoPago("efectivo")).thenReturn(List.of());
        assertEquals(HttpStatus.OK, controller.obtenerVentasPorMetodoPago("efectivo").getStatusCode());
    }
    @Test void pedidosPendientes() {
        when(service.obtenerPedidosPendientes()).thenReturn(List.of());
        assertEquals(HttpStatus.OK, controller.obtenerPedidosPendientes().getStatusCode());
    }
    @Test void entregarPedido() {
        VentaDTO dto = new VentaDTO();
        when(service.marcarPedidoEntregado(4L)).thenReturn(dto);
        assertSame(dto, controller.marcarPedidoEntregado(4L).getBody());
    }
    @Test void cambiarEstado() {
        VentaDTO dto = new VentaDTO();
        when(service.actualizarEstadoPedido(4L,"ENTREGADA")).thenReturn(dto);
        assertSame(dto, controller.cambiarEstadoPedido(4L,"ENTREGADA").getBody());
    }
    @Test void cancelarVentaDevuelve204() {
        assertEquals(HttpStatus.NO_CONTENT, controller.cancelarVenta(4L).getStatusCode());
        verify(service).cancelarVenta(4L);
    }
    @Test void reporteVentasDevuelve200() {
        assertEquals(HttpStatus.OK, controller.obtenerReporteVentas("mes").getStatusCode());
        verify(service).obtenerReporteVentas("mes");
    }
    @Test void topProductosDevuelve200() {
        when(service.obtenerProductosMasVendidos("hoy")).thenReturn(List.of());
        assertEquals(HttpStatus.OK, controller.obtenerProductosMasVendidos("hoy").getStatusCode());
    }
    @Test void clientesFrecuentesDevuelve200() {
        assertEquals(HttpStatus.OK, controller.obtenerClientesFrecuentes("mes",3).getStatusCode());
        verify(service).obtenerClientesFrecuentes("mes",3);
    }
    @Test void clientesFrecuentesArgumentoInvalidoDevuelve400() {
        when(service.obtenerClientesFrecuentes("mes",0)).thenThrow(new IllegalArgumentException("Mínimo inválido"));
        ResponseEntity<?> r = controller.obtenerClientesFrecuentes("mes",0);
        assertEquals(HttpStatus.BAD_REQUEST, r.getStatusCode());
        assertEquals("Mínimo inválido", ((Map<?,?>)r.getBody()).get("mensaje"));
    }
    @Test void clientesFrecuentesErrorRuntimeDevuelve400() {
        when(service.obtenerClientesFrecuentes("mes",3)).thenThrow(new RuntimeException("Error de consulta"));
        assertEquals(HttpStatus.BAD_REQUEST, controller.obtenerClientesFrecuentes("mes",3).getStatusCode());
    }
    @Test void patronesConRangoInvalidoDevuelve400() {
        assertEquals(HttpStatus.BAD_REQUEST, controller.patronesConsumo(fin,inicio).getStatusCode());
        verifyNoInteractions(service);
    }
    @Test void patronesConDatosDevuelve200() {
        Map<String,Object> datos = Map.of("numeroVentas",2);
        when(service.obtenerPatronesConsumo(inicio,fin)).thenReturn(datos);
        assertSame(datos, controller.patronesConsumo(inicio,fin).getBody());
    }
    @Test void exportarRangoInvalidoDevuelve400() {
        assertEquals(HttpStatus.BAD_REQUEST, controller.exportarPatronesConsumo(fin,inicio).getStatusCode());
        verifyNoInteractions(service);
    }
    @Test void exportarSinFilasGeneraCsv() {
        Map<String,Object> datos = Map.of("sucursalNombre","Centro", "numeroVentas",0L,"montoTotal",0.0);
        when(service.obtenerPatronesConsumo(inicio,fin)).thenReturn(datos);
        ResponseEntity<byte[]> r = controller.exportarPatronesConsumo(inicio,fin);
        assertEquals(HttpStatus.OK, r.getStatusCode());
        String csv = new String(r.getBody(), java.nio.charset.StandardCharsets.UTF_8);
        assertTrue(csv.contains("Sucursal;Centro"));
        assertTrue(csv.contains("Franja horaria"));
        assertTrue(r.getHeaders().getFirst("Content-Disposition").contains("consumo_"));
    }
    @Test void exportarConFilasIncluyeFranjasYDias() {
        Map<String,Object> datos = Map.of(
            "sucursalNombre","Centro", "numeroVentas",1L,"montoTotal",50.0,
            "franjas", List.of(Map.of("franja","09:00 - 10:00", "numeroVentas",1, "montoTotal",50.0)),
            "dias", List.of(Map.of("fecha","2026-11-01", "diaSemana","domingo", "numeroVentas",1, "montoTotal",50.0))
        );
        when(service.obtenerPatronesConsumo(inicio,fin)).thenReturn(datos);
        String csv = new String(controller.exportarPatronesConsumo(inicio,fin).getBody(), java.nio.charset.StandardCharsets.UTF_8);
        assertTrue(csv.contains("09:00 - 10:00;1;50.0"));
        assertTrue(csv.contains("2026-11-01;domingo;1;50.0"));
    }
}
