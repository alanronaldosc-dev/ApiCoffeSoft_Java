package com.utvt.ApiSpringCafeSoft.service;

import com.utvt.ApiSpringCafeSoft.dto.DashboardVentasDTO;
import com.utvt.ApiSpringCafeSoft.dto.ProductoMasVendidoDTO;
import com.utvt.ApiSpringCafeSoft.dto.ReporteVentasDTO;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

/**
 * HU-041 / HU-042: Dashboard de ventas.
 *
 * Compone el reporte de ventas (HU-016) y el ranking de productos
 * mas vendidos (HU-017) en una sola respuesta, de modo que el
 * dashboard y el reporte no puedan mostrar cifras distintas.
 *
 * No duplica la logica de calculo: delega en {@link VentaService}.
 */
@Service
public class VentaDashboardService {

    private final VentaService ventaService;

    public VentaDashboardService(VentaService ventaService) {
        this.ventaService = ventaService;
    }

    /**
     * Devuelve el dashboard del periodo para la sucursal del
     * usuario autenticado.
     *
     * @param periodo "hoy", "mes" o "mes_anterior"
     * @return indicadores del periodo, tendencia diaria y top de productos
     */
    public DashboardVentasDTO obtenerDashboard(String periodo) {

        ReporteVentasDTO reporte = ventaService.obtenerReporteVentas(periodo);
        List<ProductoMasVendidoDTO> topProductos =
                ventaService.obtenerProductosMasVendidos(periodo);

        DashboardVentasDTO dashboard =
                DashboardVentasDTO.crearDesde(reporte, topProductos);
        dashboard.setActualizadoEn(LocalDateTime.now());
        return dashboard;
    }
}
