package com.utvt.ApiSpringCafeSoft.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * HU-016: Reporte de ventas diarias y mensuales.
 * Respuesta del endpoint GET /api/ventas/reporte.
 *
 * Los totales (número de ventas y monto total) se calculan a partir
 * de la MISMA lista de ventas que los desgloses, por lo que siempre
 * coinciden con las ventas registradas en el periodo y sucursal
 * seleccionados.
 */
@Schema(description = "Reporte de ventas de un periodo (diario o mensual)")
public class ReporteVentasDTO {

    @Schema(description = "Periodo seleccionado: hoy, mes o mes_anterior", example = "hoy")
    private String periodo;

    @Schema(description = "Etiqueta legible del periodo", example = "13 de octubre de 2026")
    private String periodoEtiqueta;

    @Schema(description = "Inicio del rango consultado", example = "2026-10-13T00:00:00")
    private LocalDateTime desde;

    @Schema(description = "Fin del rango consultado", example = "2026-10-13T23:59:59")
    private LocalDateTime hasta;

    @Schema(description = "ID de la sucursal del usuario que consulta", example = "1")
    private Long sucursalId;

    @Schema(description = "Nombre de la sucursal", example = "Bonafont")
    private String sucursalNombre;

    @Schema(description = "Número de ventas del periodo", example = "12")
    private long numeroVentas;

    @Schema(description = "Monto total vendido (suma de los totales con IVA)", example = "3450.00")
    private Double montoTotal;

    @Schema(description = "Suma de los subtotales (sin IVA)", example = "2974.14")
    private Double subtotal;

    @Schema(description = "Suma de los impuestos (IVA)", example = "475.86")
    private Double impuestos;

    @Schema(description = "Ventas agrupadas por día del periodo")
    private List<DesgloseDia> desglosePorDia = new ArrayList<>();

    @Schema(description = "Ventas agrupadas por método de pago")
    private List<DesgloseMetodoPago> desglosePorMetodoPago = new ArrayList<>();

    // ── Desglose por día ──
    @Schema(description = "Fila del desglose diario")
    public static class DesgloseDia {

        @Schema(description = "Fecha del día", example = "2026-10-13")
        private LocalDate fecha;

        @Schema(description = "Número de ventas del día", example = "5")
        private long numeroVentas;

        @Schema(description = "Monto total del día", example = "1200.00")
        private Double montoTotal;

        public DesgloseDia() {
        }

        public DesgloseDia(LocalDate fecha, long numeroVentas, Double montoTotal) {
            this.fecha = fecha;
            this.numeroVentas = numeroVentas;
            this.montoTotal = montoTotal;
        }

        public LocalDate getFecha() {
            return fecha;
        }

        public void setFecha(LocalDate fecha) {
            this.fecha = fecha;
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
    }

    // ── Desglose por método de pago ──
    @Schema(description = "Fila del desglose por método de pago")
    public static class DesgloseMetodoPago {

        @Schema(description = "Método de pago", example = "efectivo")
        private String metodoPago;

        @Schema(description = "Número de ventas con ese método", example = "8")
        private long numeroVentas;

        @Schema(description = "Monto total de ese método", example = "2100.00")
        private Double montoTotal;

        public DesgloseMetodoPago() {
        }

        public DesgloseMetodoPago(String metodoPago, long numeroVentas, Double montoTotal) {
            this.metodoPago = metodoPago;
            this.numeroVentas = numeroVentas;
            this.montoTotal = montoTotal;
        }

        public String getMetodoPago() {
            return metodoPago;
        }

        public void setMetodoPago(String metodoPago) {
            this.metodoPago = metodoPago;
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

    public List<DesgloseDia> getDesglosePorDia() {
        return desglosePorDia;
    }

    public void setDesglosePorDia(List<DesgloseDia> desglosePorDia) {
        this.desglosePorDia = desglosePorDia;
    }

    public List<DesgloseMetodoPago> getDesglosePorMetodoPago() {
        return desglosePorMetodoPago;
    }

    public void setDesglosePorMetodoPago(List<DesgloseMetodoPago> desglosePorMetodoPago) {
        this.desglosePorMetodoPago = desglosePorMetodoPago;
    }
}
