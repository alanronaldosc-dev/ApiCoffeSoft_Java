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
 * Los indicadores se copian del MISMO reporte de ventas
 * (VentaService#obtenerReporteVentas), por lo que siempre
 * coinciden con los valores del reporte de ventas.
 */
@Schema(description = "Dashboard de ventas del periodo seleccionado")
public class DashboardVentasDTO {

    @Schema(description = "Periodo seleccionado: hoy, mes o mes_anterior", example = "mes")
    private String periodo;

    @Schema(description = "Etiqueta legible del periodo", example = "noviembre de 2026")
    private String periodoEtiqueta;

    @Schema(description = "Inicio del rango consultado", example = "2026-11-01T00:00:00")
    private LocalDateTime desde;

    @Schema(description = "Fin del rango consultado", example = "2026-11-30T23:59:59")
    private LocalDateTime hasta;

    @Schema(description = "ID de la sucursal del usuario que consulta", example = "1")
    private Long sucursalId;

    @Schema(description = "Nombre de la sucursal", example = "Bonafont")
    private String sucursalNombre;

    @Schema(description = "Numero de transacciones del periodo", example = "12")
    private long numeroVentas;

    @Schema(description = "Monto total vendido (suma de los totales con IVA)", example = "3450.00")
    private Double montoTotal;

    @Schema(description = "Suma de los subtotales (sin IVA)", example = "2974.14")
    private Double subtotal;

    @Schema(description = "Suma de los impuestos (IVA)", example = "475.86")
    private Double impuestos;

    @Schema(description = "Monto total entre el numero de transacciones", example = "287.50")
    private Double ticketPromedio;

    @Schema(description = "Dias del periodo con al menos una venta", example = "6")
    private long diasConVentas;

    @Schema(description = "Ventas agrupadas por dia del periodo")
    private List<ReporteVentasDTO.DesgloseDia> desglosePorDia = new ArrayList<>();

    @Schema(description = "Ventas agrupadas por metodo de pago")
    private List<ReporteVentasDTO.DesgloseMetodoPago> desglosePorMetodoPago = new ArrayList<>();

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

    public String getPeriodo() {
        return periodo;
    }

    public void setPeriodo(String periodo) {
        this.periodo = periodo;
    }

    public String getPeriodoEtiqueta() {
        return periodoEtiqueta;
    }

    public void setPeriodoEtiqueta(String periodoEtiqueta) {
        this.periodoEtiqueta = periodoEtiqueta;
    }

    public LocalDateTime getDesde() {
        return desde;
    }

    public void setDesde(LocalDateTime desde) {
        this.desde = desde;
    }

    public LocalDateTime getHasta() {
        return hasta;
    }

    public void setHasta(LocalDateTime hasta) {
        this.hasta = hasta;
    }

    public Long getSucursalId() {
        return sucursalId;
    }

    public void setSucursalId(Long sucursalId) {
        this.sucursalId = sucursalId;
    }

    public String getSucursalNombre() {
        return sucursalNombre;
    }

    public void setSucursalNombre(String sucursalNombre) {
        this.sucursalNombre = sucursalNombre;
    }

    public long getNumeroVentas() {
        return numeroVentas;
    }

    public void setNumeroVentas(long numeroVentas) {
        this.numeroVentas = numeroVentas;
    }

    public Double getMontoTotal() {
        return montoTotal;
    }

    public void setMontoTotal(Double montoTotal) {
        this.montoTotal = montoTotal;
    }

    public Double getSubtotal() {
        return subtotal;
    }

    public void setSubtotal(Double subtotal) {
        this.subtotal = subtotal;
    }

    public Double getImpuestos() {
        return impuestos;
    }

    public void setImpuestos(Double impuestos) {
        this.impuestos = impuestos;
    }

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

    public List<ReporteVentasDTO.DesgloseDia> getDesglosePorDia() {
        return desglosePorDia;
    }

    public void setDesglosePorDia(List<ReporteVentasDTO.DesgloseDia> desglosePorDia) {
        this.desglosePorDia = desglosePorDia;
    }

    public List<ReporteVentasDTO.DesgloseMetodoPago> getDesglosePorMetodoPago() {
        return desglosePorMetodoPago;
    }

    public void setDesglosePorMetodoPago(
            List<ReporteVentasDTO.DesgloseMetodoPago> desglosePorMetodoPago) {
        this.desglosePorMetodoPago = desglosePorMetodoPago;
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
