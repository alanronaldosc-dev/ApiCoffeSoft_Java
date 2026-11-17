package com.utvt.ApiSpringCafeSoft.service;

import com.utvt.ApiSpringCafeSoft.dto.DashboardVentasDTO;
import com.utvt.ApiSpringCafeSoft.dto.ProductoMasVendidoDTO;
import com.utvt.ApiSpringCafeSoft.dto.ReporteVentasDTO;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * HU-041 / HU-042: tests unitarios del dashboard de ventas.
 *
 * Verifican que los indicadores del dashboard se tomen del mismo
 * reporte de ventas, de modo que siempre coincidan con el reporte.
 */
@ExtendWith(MockitoExtension.class)
class VentaDashboardServiceTest {

    @Mock
    private VentaService ventaService;

    @InjectMocks
    private VentaDashboardService dashboardService;

    // ── Helpers ──

    private ReporteVentasDTO reporteConVentas() {
        ReporteVentasDTO.DesgloseDia dia1 =
                new ReporteVentasDTO.DesgloseDia(LocalDate.of(2026, 11, 10), 2, 700.0);
        ReporteVentasDTO.DesgloseDia dia2 =
                new ReporteVentasDTO.DesgloseDia(LocalDate.of(2026, 11, 11), 2, 700.0);

        ReporteVentasDTO.DesgloseMetodoPago efectivo =
                new ReporteVentasDTO.DesgloseMetodoPago("efectivo", 3, 1050.0);
        ReporteVentasDTO.DesgloseMetodoPago tarjeta =
                new ReporteVentasDTO.DesgloseMetodoPago("tarjeta", 1, 350.0);

        ReporteVentasDTO reporte = new ReporteVentasDTO();
        reporte.setPeriodo("mes");
        reporte.setPeriodoEtiqueta("noviembre de 2026");
        reporte.setDesde(LocalDateTime.of(2026, 11, 1, 0, 0));
        reporte.setHasta(LocalDateTime.of(2026, 11, 30, 23, 59, 59));
        reporte.setSucursalId(1L);
        reporte.setSucursalNombre("Bonafont");
        reporte.setNumeroVentas(4);
        reporte.setMontoTotal(1400.0);
        reporte.setSubtotal(1206.90);
        reporte.setImpuestos(193.10);
        reporte.setDesglosePorDia(new ArrayList<>(Arrays.asList(dia1, dia2)));
        reporte.setDesglosePorMetodoPago(
                new ArrayList<>(Arrays.asList(efectivo, tarjeta)));
        return reporte;
    }

    private List<ProductoMasVendidoDTO> topProductos() {
        return Arrays.asList(
                new ProductoMasVendidoDTO(12L, "Garrafon 20L", 8, 400.0),
                new ProductoMasVendidoDTO(13L, "Botella 1L", 5, 150.0));
    }

    // ── Tests ──

    @Test
    @DisplayName("El dashboard copia los indicadores del reporte de ventas")
    void obtenerDashboard_componeIndicadoresDelReporte() {
        when(ventaService.obtenerReporteVentas("mes")).thenReturn(reporteConVentas());
        when(ventaService.obtenerProductosMasVendidos("mes")).thenReturn(topProductos());

        DashboardVentasDTO dashboard = dashboardService.obtenerDashboard("mes");

        // CA HU-041.1 y HU-041.2: ventas del periodo y numero de transacciones.
        assertEquals(1400.0, dashboard.getMontoTotal(), 0.001);
        assertEquals(4, dashboard.getNumeroVentas());

        // CA HU-041.3: los valores son los mismos que los del reporte.
        ReporteVentasDTO reporte = reporteConVentas();
        assertEquals(reporte.getMontoTotal(), dashboard.getMontoTotal());
        assertEquals(reporte.getNumeroVentas(), dashboard.getNumeroVentas());
        assertEquals(reporte.getSubtotal(), dashboard.getSubtotal());
        assertEquals(reporte.getImpuestos(), dashboard.getImpuestos());

        // Contexto del periodo y la sucursal.
        assertEquals("mes", dashboard.getPeriodo());
        assertEquals("noviembre de 2026", dashboard.getPeriodoEtiqueta());
        assertEquals(1L, dashboard.getSucursalId());
        assertEquals("Bonafont", dashboard.getSucursalNombre());

        // Ticket promedio = 1400 / 4.
        assertEquals(350.0, dashboard.getTicketPromedio(), 0.001);
        assertEquals(2, dashboard.getDiasConVentas());
        assertEquals(2, dashboard.getTopProductos().size());
        assertEquals("Garrafon 20L", dashboard.getTopProductos().get(0).getNombre());
        assertNotNull(dashboard.getActualizadoEn());
    }

    @Test
    @DisplayName("Un periodo sin ventas devuelve ticket promedio 0 y listas vacias")
    void obtenerDashboard_sinVentas_ticketPromedioCero() {
        ReporteVentasDTO vacio = new ReporteVentasDTO();
        vacio.setPeriodo("hoy");
        vacio.setPeriodoEtiqueta("16 de noviembre de 2026");
        vacio.setNumeroVentas(0);
        vacio.setMontoTotal(0.0);
        vacio.setDesglosePorDia(null);
        vacio.setDesglosePorMetodoPago(null);

        when(ventaService.obtenerReporteVentas("hoy")).thenReturn(vacio);
        when(ventaService.obtenerProductosMasVendidos("hoy"))
                .thenReturn(Collections.emptyList());

        DashboardVentasDTO dashboard = dashboardService.obtenerDashboard("hoy");

        assertEquals(0, dashboard.getNumeroVentas());
        assertEquals(0.0, dashboard.getTicketPromedio(), 0.001);
        assertEquals(0, dashboard.getDiasConVentas());
        assertTrue(dashboard.getDesglosePorDia().isEmpty());
        assertTrue(dashboard.getDesglosePorMetodoPago().isEmpty());
        assertTrue(dashboard.getTopProductos().isEmpty());
    }

    @Test
    @DisplayName("Un periodo no valido se propaga desde el reporte")
    void obtenerDashboard_periodoInvalido_lanzaExcepcion() {
        when(ventaService.obtenerReporteVentas("semestre"))
                .thenThrow(new IllegalArgumentException(
                        "Periodo no valido. Usa: hoy, mes o mes_anterior."));

        IllegalArgumentException error = assertThrows(
                IllegalArgumentException.class,
                () -> dashboardService.obtenerDashboard("semestre"));

        assertTrue(error.getMessage().contains("Periodo no valido"));
        verify(ventaService, never()).obtenerProductosMasVendidos(anyString());
    }

    @Test
    @DisplayName("Sin ranking de productos el dashboard devuelve una lista vacia")
    void crearDesde_sinTopProductos_listaVacia() {
        DashboardVentasDTO dashboard = DashboardVentasDTO.crearDesde(reporteConVentas(), null);

        assertNotNull(dashboard.getTopProductos());
        assertTrue(dashboard.getTopProductos().isEmpty());
        assertEquals(1400.0, dashboard.getMontoTotal(), 0.001);
    }

    @Test
    @DisplayName("El ticket promedio se redondea a dos decimales")
    void calcularTicketPromedio_redondeaDosDecimales() {
        assertEquals(333.33, DashboardVentasDTO.calcularTicketPromedio(1000.0, 3), 0.001);
        assertEquals(0.0, DashboardVentasDTO.calcularTicketPromedio(1000.0, 0), 0.001);
        assertEquals(0.0, DashboardVentasDTO.calcularTicketPromedio(null, 3), 0.001);
    }
}
