package com.utvt.ApiSpringCafeSoft.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * HU-041: Dashboard de ventas.
 * Respuesta del endpoint GET /api/ventas/dashboard.
 *
 * Reune en una sola llamada los indicadores del periodo
 * (ventas del periodo, numero de transacciones y ticket promedio),
 * la tendencia diaria y el ranking de productos mas vendidos
 * (HU-042).
 *
 * Los indicadores se heredan del MISMO reporte de ventas
 * (VentaService#obtenerReporteVentas), por lo que siempre
 * coinciden con los valores del reporte de ventas.
 */
@Schema(description = "Dashboard de ventas del periodo seleccionado")
public class DashboardVentasDTO extends ReporteVentasDTO {

    @Schema(description = "Monto total entre el numero de transacciones", example = "287.50")
    private Double ticketPromedio;

    @Schema(description = "Dias del periodo con al menos una venta", example = "6")
    private long diasConVentas;

    @Schema(description = "Productos ordenados por unidades vendidas (mayor primero)")
    private List<ProductoMasVendidoDTO> topProductos = new ArrayList<>();

    @Schema(description = "Momento en que se genero la respuesta")
    private LocalDateTime actualizadoEn;

    /**
     * Construye el dashboard a partir del reporte de ventas y del
     * ranking de productos, de modo que los indicadores y el reporte
     * compartan exactamente la misma fuente de datos.
     *
     * @param reporte      reporte de ventas del periodo (HU-016)
     * @param topProductos ranking de productos del periodo (HU-017)
     * @return dashboard listo para serializar
     */
    public static DashboardVentasDTO crearDesde(
            ReporteVentasDTO reporte,
            List<ProductoMasVendidoDTO> topProductos) {

        DashboardVentasDTO dashboard = new DashboardVentasDTO();
        dashboard.setPeriodo(reporte.getPeriodo());
        dashboard.setPeriodoEtiqueta(reporte.getPeriodoEtiqueta());
        dashboard.setDesde(reporte.getDesde());
        dashboard.setHasta(reporte.getHasta());
        dashboard.setSucursalId(reporte.getSucursalId());
        dashboard.setSucursalNombre(reporte.getSucursalNombre());
        dashboard.setNumeroVentas(reporte.getNumeroVentas());
        dashboard.setMontoTotal(reporte.getMontoTotal());
        dashboard.setSubtotal(reporte.getSubtotal());
        dashboard.setImpuestos(reporte.getImpuestos());
        dashboard.setTicketPromedio(calcularTicketPromedio(
                reporte.getMontoTotal(), reporte.getNumeroVentas()));

        List<ReporteVentasDTO.DesgloseDia> porDia = reporte.getDesglosePorDia() != null
                ? reporte.getDesglosePorDia()
                : new ArrayList<>();
        dashboard.setDesglosePorDia(porDia);
        dashboard.setDiasConVentas(porDia.size());

        dashboard.setDesglosePorMetodoPago(reporte.getDesglosePorMetodoPago() != null
                ? reporte.getDesglosePorMetodoPago()
                : new ArrayList<>());
        dashboard.setTopProductos(topProductos != null ? topProductos : new ArrayList<>());

        return dashboard;
    }

    /** Ticket promedio redondeado a dos decimales; 0 cuando no hay ventas. */
    public static double calcularTicketPromedio(Double montoTotal, long numeroVentas) {
        if (numeroVentas <= 0 || montoTotal == null) {
            return 0.0;
        }
        return Math.round((montoTotal / numeroVentas) * 100.0) / 100.0;
    }

    // ── Getters y setters ──

    public Double getTicketPromedio() {
        return ticketPromedio;
    }

    public void setTicketPromedio(Double ticketPromedio) {
        this.ticketPromedio = ticketPromedio;
    }

    public long getDiasConVentas() {
        return diasConVentas;
    }

    public void setDiasConVentas(long diasConVentas) {
        this.diasConVentas = diasConVentas;
    }

    public List<ProductoMasVendidoDTO> getTopProductos() {
        return topProductos;
    }

    public void setTopProductos(List<ProductoMasVendidoDTO> topProductos) {
        this.topProductos = topProductos;
    }

    public LocalDateTime getActualizadoEn() {
        return actualizadoEn;
    }

    public void setActualizadoEn(LocalDateTime actualizadoEn) {
        this.actualizadoEn = actualizadoEn;
    }
}
