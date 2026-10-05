package com.utvt.ApiSpringCafeSoft.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;

/** HU-016: datos que captura el encargado al cerrar el turno. */
public class RegistrarLiquidacionDTO {
    @NotNull(message = "El repartidor es obligatorio")
    private Long repartidorId;

    private LocalDate fecha;

    @NotNull(message = "Los garrafones devueltos son obligatorios")
    @DecimalMin(value = "0.0", message = "Los garrafones devueltos no pueden ser negativos")
    private Double garrafonesNoVendidosDevueltos;

    @NotNull(message = "El efectivo entregado es obligatorio")
    @DecimalMin(value = "0.0", message = "El efectivo entregado no puede ser negativo")
    private Double efectivoEntregado;

    private String observaciones;

    public Long getRepartidorId() { return repartidorId; }
    public void setRepartidorId(Long repartidorId) { this.repartidorId = repartidorId; }
    public LocalDate getFecha() { return fecha; }
    public void setFecha(LocalDate fecha) { this.fecha = fecha; }
    public Double getGarrafonesNoVendidosDevueltos() { return garrafonesNoVendidosDevueltos; }
    public void setGarrafonesNoVendidosDevueltos(Double value) { this.garrafonesNoVendidosDevueltos = value; }
    public Double getEfectivoEntregado() { return efectivoEntregado; }
    public void setEfectivoEntregado(Double efectivoEntregado) { this.efectivoEntregado = efectivoEntregado; }
    public String getObservaciones() { return observaciones; }
    public void setObservaciones(String observaciones) { this.observaciones = observaciones; }
}
