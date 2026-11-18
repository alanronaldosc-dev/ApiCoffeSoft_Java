package com.utvt.ApiSpringCafeSoft.service;

import com.utvt.ApiSpringCafeSoft.dto.ClienteFrecuenteDTO;
import com.utvt.ApiSpringCafeSoft.dto.ProductoMasVendidoDTO;
import com.utvt.ApiSpringCafeSoft.dto.ReporteVentasDTO;
import com.utvt.ApiSpringCafeSoft.model.*;
import com.utvt.ApiSpringCafeSoft.repository.UsuarioRepository;
import com.utvt.ApiSpringCafeSoft.repository.VentaRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class VentaServiceCoberturaTest {
    @Mock VentaRepository ventaRepository;
    @Mock UsuarioRepository usuarioRepository;
    @InjectMocks VentaService service;

    @BeforeEach void login() {
        SecurityContextHolder.getContext().setAuthentication(
            new UsernamePasswordAuthenticationToken("test@cafesoft.mx", "", List.of()));
    }
    @AfterEach void logout() { SecurityContextHolder.clearContext(); }

    private void sucursal() {
        Sucursal s = new Sucursal(); s.setId(7L); s.setNombre("Centro");
        Usuario u = new Usuario(); u.setEmail("test@cafesoft.mx"); u.setSucursal(s);
        when(usuarioRepository.findByEmail("test@cafesoft.mx")).thenReturn(Optional.of(u));
    }
    private Venta venta(double total, String pago) {
        Venta v = new Venta(); v.setFecha(LocalDateTime.now()); v.setTotal(total);
        v.setSubtotal(total / 1.16); v.setImpuestos(total - total / 1.16);
        v.setMetodoPago(pago); return v;
    }
    private void ventas(List<Venta> datos) {
        when(ventaRepository.findByFechaBetweenAndSucursalId(any(), any(), eq(7L)))
            .thenReturn(datos);
    }

    @Test void reporteSinVentas() {
        sucursal(); ventas(List.of());
        ReporteVentasDTO r = service.obtenerReporteVentas("hoy");
        assertEquals(0, r.getNumeroVentas()); assertEquals(0, r.getMontoTotal());
        assertTrue(r.getDesglosePorDia().isEmpty());
    }
    @Test void reporteAgrupaDiasYMetodos() {
        sucursal();
        Venta a = venta(116, "efectivo"); Venta b = venta(232, "tarjeta");
        Venta c = venta(58, "efectivo");
        ventas(List.of(a,b,c));
        ReporteVentasDTO r = service.obtenerReporteVentas("hoy");
        assertEquals(3, r.getNumeroVentas()); assertEquals(406, r.getMontoTotal(), .001);
        assertEquals(1, r.getDesglosePorDia().size());
        assertEquals(2, r.getDesglosePorMetodoPago().size());
    }
    @Test void reporteToleraImportesNulos() {
        sucursal(); Venta a = venta(100,"efectivo");
        a.setTotal(null); a.setSubtotal(null); a.setImpuestos(null);
        ventas(List.of(a));
        ReporteVentasDTO r = service.obtenerReporteVentas("hoy");
        assertEquals(0, r.getMontoTotal()); assertEquals(0, r.getSubtotal());
        assertEquals(0, r.getImpuestos());
    }
    @Test void reporteAceptaMes() {
        sucursal(); ventas(List.of());
        assertEquals("mes", service.obtenerReporteVentas("mes").getPeriodo());
    }
    @Test void reporteAceptaMesAnterior() {
        sucursal(); ventas(List.of());
        assertEquals("mes_anterior", service.obtenerReporteVentas("mes_anterior").getPeriodo());
    }
    @Test void reporteRechazaPeriodoInvalido() {
        sucursal();
        assertThrows(IllegalArgumentException.class, () -> service.obtenerReporteVentas("semana"));
    }
    @Test void productosSinVentas() {
        sucursal(); ventas(List.of());
        assertTrue(service.obtenerProductosMasVendidos("hoy").isEmpty());
    }
    @Test void productosAgrupaYOrdena() {
        sucursal();
        Producto p = new Producto(); p.setId(11L); p.setNombre("Cafe");
        VentaDetalle d1 = new VentaDetalle(); d1.setProducto(p); d1.setCantidad(2); d1.setSubtotal(80.0);
        VentaDetalle d2 = new VentaDetalle(); d2.setProducto(p); d2.setCantidad(3); d2.setSubtotal(120.0);
        Venta v1 = venta(80,"efectivo"); v1.addDetalle(d1);
        Venta v2 = venta(120,"tarjeta"); v2.addDetalle(d2);
        ventas(List.of(v1,v2));
        List<ProductoMasVendidoDTO> r = service.obtenerProductosMasVendidos("hoy");
        assertEquals(1,r.size()); assertEquals(5,r.get(0).getUnidadesVendidas());
        assertEquals(200,r.get(0).getMontoTotal(),.001);
    }
    @Test void clientesRechazaMinimoCero() {
        assertThrows(IllegalArgumentException.class, () -> service.obtenerClientesFrecuentes("hoy",0));
    }
    @Test void clientesSinCompras() {
        sucursal();
        when(ventaRepository.findByFechaBetweenAndSucursalIdAndClienteRutaIdIsNotNull(any(),any(),eq(7L)))
            .thenReturn(List.of());
        assertTrue(service.obtenerClientesFrecuentes("hoy",2).isEmpty());
    }
    @Test void clientesAgrupaCompras() {
        sucursal();
        Venta a = venta(100,"efectivo"); a.setClienteRutaId(42L); a.setNombreCliente("Ana");
        Venta b = venta(50,"tarjeta"); b.setClienteRutaId(42L); b.setNombreCliente("Ana");
        when(ventaRepository.findByFechaBetweenAndSucursalIdAndClienteRutaIdIsNotNull(any(),any(),eq(7L)))
            .thenReturn(List.of(a,b));
        List<ClienteFrecuenteDTO> r = service.obtenerClientesFrecuentes("hoy",2);
        assertEquals(1,r.size()); assertEquals(2,r.get(0).getTotalCompras());
        assertEquals(150,r.get(0).getMontoTotal(),.001);
        assertTrue(r.get(0).isEsFrecuente());
    }
    @Test void usuarioSinSucursalRechazado() {
        Usuario u = new Usuario(); u.setEmail("test@cafesoft.mx");
        when(usuarioRepository.findByEmail("test@cafesoft.mx")).thenReturn(Optional.of(u));
        assertThrows(IllegalArgumentException.class, () -> service.obtenerReporteVentas("hoy"));
    }
}
