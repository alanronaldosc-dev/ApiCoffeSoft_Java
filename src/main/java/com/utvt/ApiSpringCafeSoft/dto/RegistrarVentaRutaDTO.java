package com.utvt.ApiSpringCafeSoft.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;

import java.util.ArrayList;
import java.util.List;

/**
 * HU-015
 * Datos enviados por el repartidor al realizar
 * una venta durante su ruta.
 *
 * La venta puede incluir uno o varios tipos de
 * garrafón, siempre que cada garrafón lleno
 * esté asignado en la carga del repartidor.
 *
 * Los envases vacíos se reciben por tipo de
 * garrafón y se regresan al inventario de planta.
 */
public class RegistrarVentaRutaDTO {

    @NotNull(message = "El cliente es obligatorio")
    private Long clienteId;

    @NotNull(message = "El repartidor es obligatorio")
    private Long repartidorId;

    /**
     * Garrafones llenos entregados, agrupados
     * por tipo de garrafón (inventario).
     */
    @Valid
    private List<ItemGarrafonDTO> garrafones = new ArrayList<>();

    /**
     * Envases vacíos recibidos, agrupados
     * por tipo de garrafón (inventario).
     */
    @Valid
    private List<ItemGarrafonDTO> envasesVacios = new ArrayList<>();

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

    public List<ItemGarrafonDTO> getGarrafones() {
        return garrafones;
    }

    public void setGarrafones(List<ItemGarrafonDTO> garrafones) {
        this.garrafones = garrafones;
    }

    public List<ItemGarrafonDTO> getEnvasesVacios() {
        return envasesVacios;
    }

    public void setEnvasesVacios(List<ItemGarrafonDTO> envasesVacios) {
        this.envasesVacios = envasesVacios;
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
