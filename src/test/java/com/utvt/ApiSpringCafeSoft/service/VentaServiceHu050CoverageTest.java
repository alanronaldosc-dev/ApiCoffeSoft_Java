package com.utvt.ApiSpringCafeSoft.service;

import com.utvt.ApiSpringCafeSoft.dto.ProductoMasVendidoDTO;
import com.utvt.ApiSpringCafeSoft.dto.ReporteVentasDTO;
import com.utvt.ApiSpringCafeSoft.model.Producto;
import com.utvt.ApiSpringCafeSoft.model.Sucursal;
import com.utvt.ApiSpringCafeSoft.model.Usuario;
import com.utvt.ApiSpringCafeSoft.model.Venta;
import com.utvt.ApiSpringCafeSoft.model.VentaDetalle;
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

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.YearMonth;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

/**
 * Pruebas adicionales para cubrir las ramas introducidas por HU-050:
 * periodos predefinidos, rango personalizado, validaciones y ranking.
 */
@ExtendWith(MockitoExtension.class)
class VentaServiceHu050CoverageTest {

    @Mock
    private VentaRepository ventaRepository;

    @Mock
    private UsuarioRepository usuarioRepository;

    @InjectMocks
    private VentaService service;

    @BeforeEach
    void autenticarUsuario() {
        SecurityContextHolder.getContext().setAuthentication(
            new UsernamePasswordAuthenticationToken(
                "hu050@cafesoft.mx", "", List.of()
            )
        );
    }

    @AfterEach
    void limpiarAutenticacion() {
        SecurityContextHolder.clearContext();
    }

    private void prepararUsuarioConSucursal() {
        Sucursal sucursal = new Sucursal();
        sucursal.setId(7L);
        sucursal.setNombre("Centro");

        Usuario usuario = new Usuario();
        usuario.setEmail("hu050@cafesoft.mx");
        usuario.setSucursal(sucursal);

        when(usuarioRepository.findByEmail("hu050@cafesoft.mx"))
            .thenReturn(Optional.of(usuario));
    }

    private void prepararVentasVacias() {
        when(ventaRepository.findByFechaBetweenAndSucursalId(
            any(LocalDateTime.class),
            any(LocalDateTime.class),
            eq(7L)
        )).thenReturn(List.of());
    }

    private Venta ventaEn(LocalDate fecha) {
        Venta venta = new Venta();
        venta.setFecha(fecha.atTime(12, 0));
        venta.setTotal(100.0);
        venta.setSubtotal(86.21);
        venta.setImpuestos(13.79);
        venta.setMetodoPago("efectivo");
        return venta;
    }

    private VentaDetalle detalle(Producto producto, Integer cantidad, Double subtotal) {
        VentaDetalle detalle = new VentaDetalle();
        detalle.setProducto(producto);
        detalle.setCantidad(cantidad);
        detalle.setSubtotal(subtotal);
        return detalle;
    }

    private Producto producto(Long id, String nombre) {
        Producto producto = new Producto();
        producto.setId(id);
        producto.setNombre(nombre);
        return producto;
    }

    @Test
    void periodoNuloUsaHoyPorDefecto() {
        prepararUsuarioConSucursal();
        prepararVentasVacias();
        LocalDate hoy = LocalDate.now();

        ReporteVentasDTO reporte = service.obtenerReporteVentas(null, null, null);

        assertEquals("hoy", reporte.getPeriodo());
        assertEquals(hoy.atStartOfDay(), reporte.getDesde());
        assertEquals(hoy.atTime(LocalTime.MAX), reporte.getHasta());
    }

    @Test
    void periodoNormalizaMayusculasYEspacios() {
        prepararUsuarioConSucursal();
        prepararVentasVacias();

        ReporteVentasDTO reporte = service.obtenerReporteVentas("  SEMANA  ", null, null);

        assertEquals("semana", reporte.getPeriodo());
    }

    @Test
    void periodoSemanaComprendeLunesADomingo() {
        prepararUsuarioConSucursal();
        prepararVentasVacias();
        LocalDate hoy = LocalDate.now();
        LocalDate lunes = hoy.with(DayOfWeek.MONDAY);
        LocalDate domingo = lunes.plusDays(6);

        ReporteVentasDTO reporte = service.obtenerReporteVentas("semana", null, null);

        assertEquals(lunes.atStartOfDay(), reporte.getDesde());
        assertEquals(domingo.atTime(LocalTime.MAX), reporte.getHasta());
    }

    @Test
    void periodoAnioComprendeTodoElAnio() {
        prepararUsuarioConSucursal();
        prepararVentasVacias();
        LocalDate hoy = LocalDate.now();

        ReporteVentasDTO reporte = service.obtenerReporteVentas("anio", null, null);

        assertEquals(LocalDate.of(hoy.getYear(), 1, 1).atStartOfDay(), reporte.getDesde());
        assertEquals(LocalDate.of(hoy.getYear(), 12, 31).atTime(LocalTime.MAX), reporte.getHasta());
    }

    @Test
    void periodoAnioAceptaAliasConEnie() {
        prepararUsuarioConSucursal();
        prepararVentasVacias();

        ReporteVentasDTO reporte = service.obtenerReporteVentas("año", null, null);

        assertEquals("año", reporte.getPeriodo());
    }

    @Test
    void periodoMesComprendePrimerYUltimoDia() {
        prepararUsuarioConSucursal();
        prepararVentasVacias();
        YearMonth mes = YearMonth.now();

        ReporteVentasDTO reporte = service.obtenerReporteVentas("mes", null, null);

        assertEquals(mes.atDay(1).atStartOfDay(), reporte.getDesde());
        assertEquals(mes.atEndOfMonth().atTime(LocalTime.MAX), reporte.getHasta());
    }

    @Test
    void periodoMesAnteriorComprendeElMesAnteriorCompleto() {
        prepararUsuarioConSucursal();
        prepararVentasVacias();
        YearMonth mesAnterior = YearMonth.now().minusMonths(1);

        ReporteVentasDTO reporte = service.obtenerReporteVentas("mes_anterior", null, null);

        assertEquals(mesAnterior.atDay(1).atStartOfDay(), reporte.getDesde());
        assertEquals(mesAnterior.atEndOfMonth().atTime(LocalTime.MAX), reporte.getHasta());
    }

    @Test
    void periodoPersonalizadoUsaFechasIncluyendoTodoElDiaFinal() {
        prepararUsuarioConSucursal();
        prepararVentasVacias();
        LocalDate desde = LocalDate.of(2026, 1, 10);
        LocalDate hasta = LocalDate.of(2026, 1, 20);

        ReporteVentasDTO reporte = service.obtenerReporteVentas("personalizado", desde, hasta);

        assertEquals("personalizado", reporte.getPeriodo());
        assertEquals(desde.atStartOfDay(), reporte.getDesde());
        assertEquals(hasta.atTime(LocalTime.MAX), reporte.getHasta());
        verify(ventaRepository).findByFechaBetweenAndSucursalId(
            desde.atStartOfDay(), hasta.atTime(LocalTime.MAX), 7L
        );
    }

    @Test
    void periodoPersonalizadoRechazaFechaInicialAusente() {
        prepararUsuarioConSucursal();

        assertThrows(IllegalArgumentException.class, () ->
            service.obtenerReporteVentas("personalizado", null, LocalDate.of(2026, 1, 20))
        );

        verifyNoInteractions(ventaRepository);
    }

    @Test
    void periodoPersonalizadoRechazaFechaFinalAusente() {
        prepararUsuarioConSucursal();

        assertThrows(IllegalArgumentException.class, () ->
            service.obtenerReporteVentas("personalizado", LocalDate.of(2026, 1, 10), null)
        );

        verifyNoInteractions(ventaRepository);
    }

    @Test
    void periodoPersonalizadoRechazaFechasInvertidas() {
        prepararUsuarioConSucursal();

        assertThrows(IllegalArgumentException.class, () ->
            service.obtenerReporteVentas(
                "personalizado",
                LocalDate.of(2026, 1, 21),
                LocalDate.of(2026, 1, 20)
            )
        );

        verifyNoInteractions(ventaRepository);
    }

    @Test
    void periodoDesconocidoEsRechazado() {
        prepararUsuarioConSucursal();

        assertThrows(IllegalArgumentException.class, () ->
            service.obtenerReporteVentas("quincena", null, null)
        );

        verifyNoInteractions(ventaRepository);
    }

    @Test
    void productosMasVendidosAceptaRangoPersonalizado() {
        prepararUsuarioConSucursal();
        prepararVentasVacias();
        LocalDate desde = LocalDate.of(2026, 2, 1);
        LocalDate hasta = LocalDate.of(2026, 2, 15);

        List<ProductoMasVendidoDTO> resultado =
            service.obtenerProductosMasVendidos("personalizado", desde, hasta);

        assertTrue(resultado.isEmpty());
        verify(ventaRepository).findByFechaBetweenAndSucursalId(
            desde.atStartOfDay(), hasta.atTime(LocalTime.MAX), 7L
        );
    }

    @Test
    void productosMasVendidosRechazaRangoPersonalizadoIncompleto() {
        prepararUsuarioConSucursal();

        assertThrows(IllegalArgumentException.class, () ->
            service.obtenerProductosMasVendidos("personalizado", LocalDate.of(2026, 2, 1), null)
        );

        verifyNoInteractions(ventaRepository);
    }

    @Test
    void productosMasVendidosOrdenaPorUnidadesYDesempataPorMonto() {
        prepararUsuarioConSucursal();

        Venta venta = ventaEn(LocalDate.now());
        Producto cuatroUnidades = producto(1L, "Producto A");
        Producto tresUnidadesMenorMonto = producto(2L, "Producto B");
        Producto tresUnidadesMayorMonto = producto(3L, "Producto C");
        Producto datosNulos = producto(4L, "Producto D");

        venta.addDetalle(detalle(cuatroUnidades, 4, 20.0));
        venta.addDetalle(detalle(tresUnidadesMenorMonto, 3, 30.0));
        venta.addDetalle(detalle(tresUnidadesMayorMonto, 3, 90.0));
        venta.addDetalle(detalle(datosNulos, null, null));

        when(ventaRepository.findByFechaBetweenAndSucursalId(
            any(LocalDateTime.class), any(LocalDateTime.class), eq(7L)
        )).thenReturn(List.of(venta));

        List<ProductoMasVendidoDTO> resultado =
            service.obtenerProductosMasVendidos("hoy", null, null);

        assertEquals(4, resultado.size());
        assertEquals("Producto A", resultado.get(0).getNombre());
        assertEquals("Producto C", resultado.get(1).getNombre());
        assertEquals("Producto B", resultado.get(2).getNombre());
        assertEquals("Producto D", resultado.get(3).getNombre());
        assertEquals(0L, resultado.get(3).getUnidadesVendidas());
        assertEquals(0.0, resultado.get(3).getMontoTotal(), 0.0001);
    }

    @Test
    void reporteRechazaUsuarioNoAutenticado() {
        SecurityContextHolder.clearContext();

        assertThrows(RuntimeException.class, () ->
            service.obtenerReporteVentas("hoy", null, null)
        );

        verifyNoInteractions(usuarioRepository, ventaRepository);
    }

    @Test
    void reporteRechazaUsuarioNoEncontrado() {
        when(usuarioRepository.findByEmail("hu050@cafesoft.mx"))
            .thenReturn(Optional.empty());

        assertThrows(RuntimeException.class, () ->
            service.obtenerReporteVentas("hoy", null, null)
        );

        verifyNoInteractions(ventaRepository);
    }

    @Test
    void reporteRechazaUsuarioSinSucursal() {
        Usuario usuario = new Usuario();
        usuario.setEmail("hu050@cafesoft.mx");
        when(usuarioRepository.findByEmail("hu050@cafesoft.mx"))
            .thenReturn(Optional.of(usuario));

        assertThrows(IllegalArgumentException.class, () ->
            service.obtenerReporteVentas("hoy", null, null)
        );

        verifyNoInteractions(ventaRepository);
    }
}
