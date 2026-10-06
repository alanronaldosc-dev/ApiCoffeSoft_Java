package com.utvt.ApiSpringCafeSoft.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;

/**
 * HU-015
 * Datos enviados por el repartidor al realizar
 * una venta durante su ruta.
 */
public class RegistrarVentaRutaDTO {

    @NotNull(message = "El cliente es obligatorio")
    private Long clienteId;

    @NotNull(message = "El repartidor es obligatorio")
    private Long repartidorId;

    @NotNull(message = "La cantidad de garrafones es obligatoria")
    @DecimalMin(
        value = "0.01",
        message = "Debe vender al menos un garrafón"
    )
    private Double garrafonesEntregados;

    @NotNull(message = "Los envases vacíos son obligatorios")
    @DecimalMin(
        value = "0.0",
        message = "Los envases vacíos no pueden ser negativos"
    )
    private Double envasesVaciosRecibidos;

    @NotNull(message = "El precio por garrafón es obligatorio")
    @DecimalMin(
        value = "0.0",
        inclusive = false,
        message = "El precio debe ser mayor a cero"
    )
    private Double precioUnitario;

    @NotBlank(message = "El método de cobro es obligatorio")
    @Pattern(
        regexp = "^(EFECTIVO|TRANSFERENCIA)$",
        message = "El método de cobro debe ser EFECTIVO o TRANSFERENCIA"
    )
    private String metodoCobro;

    private String observaciones;

    public RegistrarVentaRutaDTO() {
    }

    public Long getClienteId() {
        return clienteId;
    }

    public void setClienteId(Long clienteId) {
        this.clienteId = clienteId;
    }

    public Long getRepartidorId() {
        return repartidorId;
    }

    public void setRepartidorId(Long repartidorId) {
        this.repartidorId = repartidorId;
    }

    public Double getGarrafonesEntregados() {
        return garrafonesEntregados;
    }

    public void setGarrafonesEntregados(Double garrafonesEntregados) {
        this.garrafonesEntregados = garrafonesEntregados;
    }

    public Double getEnvasesVaciosRecibidos() {
        return envasesVaciosRecibidos;
    }

    public void setEnvasesVaciosRecibidos(Double envasesVaciosRecibidos) {
        this.envasesVaciosRecibidos = envasesVaciosRecibidos;
    }

    public Double getPrecioUnitario() {
        return precioUnitario;
    }

    public void setPrecioUnitario(Double precioUnitario) {
        this.precioUnitario = precioUnitario;
    }

    public String getMetodoCobro() {
        return metodoCobro;
    }

    public void setMetodoCobro(String metodoCobro) {
        this.metodoCobro = metodoCobro;
    }

    public String getObservaciones() {
        return observaciones;
    }

    public void setObservaciones(String observaciones) {
        this.observaciones = observaciones;
    }
}