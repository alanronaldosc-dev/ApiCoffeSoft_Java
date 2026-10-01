package com.utvt.ApiSpringCafeSoft.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;

/** HU-015: datos capturados por el repartidor al confirmar una entrega. */
public class ConfirmarEntregaDTO {

    @NotNull(message = "El repartidor es obligatorio")
    private Long repartidorId;

    @NotNull(message = "Los garrafones entregados son obligatorios")
    @DecimalMin(value = "0.01", message = "Debe entregar al menos un garrafón")
    private Double garrafonesEntregados;

    @NotNull(message = "Los envases vacíos recibidos son obligatorios")
    @DecimalMin(value = "0.0", message = "Los envases vacíos no pueden ser negativos")
    private Double envasesVaciosRecibidos;

    @NotBlank(message = "El método de cobro es obligatorio")
    @Pattern(regexp = "^(EFECTIVO|TRANSFERENCIA)$", message = "El método de cobro debe ser EFECTIVO o TRANSFERENCIA")
    private String metodoCobro;

    @NotNull(message = "El monto cobrado es obligatorio")
    @DecimalMin(value = "0.0", message = "El monto cobrado no puede ser negativo")
    private Double montoCobrado;

    private String observaciones;

    public Long getRepartidorId() { return repartidorId; }
    public void setRepartidorId(Long repartidorId) { this.repartidorId = repartidorId; }

    public Double getGarrafonesEntregados() { return garrafonesEntregados; }
    public void setGarrafonesEntregados(Double garrafonesEntregados) { this.garrafonesEntregados = garrafonesEntregados; }

    public Double getEnvasesVaciosRecibidos() { return envasesVaciosRecibidos; }
    public void setEnvasesVaciosRecibidos(Double envasesVaciosRecibidos) { this.envasesVaciosRecibidos = envasesVaciosRecibidos; }

    public String getMetodoCobro() { return metodoCobro; }
    public void setMetodoCobro(String metodoCobro) { this.metodoCobro = metodoCobro; }

    public Double getMontoCobrado() { return montoCobrado; }
    public void setMontoCobrado(Double montoCobrado) { this.montoCobrado = montoCobrado; }

    public String getObservaciones() { return observaciones; }
    public void setObservaciones(String observaciones) { this.observaciones = observaciones; }
}
