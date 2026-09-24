package com.utvt.ApiSpringCafeSoft.dto;

/**
     * 
     * (HU-006): agrega confirmacion de carga por repartidor
     * 
     */

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDateTime;

public class CargaDTO {

    private Long id;

    @NotNull(message = "El ID del repartidor es obligatorio")
    private Long repartidorId;

    @NotNull(message = "El ID del inventario es obligatorio")
    private Long inventarioId;

    @NotNull(message = "La cantidad es obligatoria")
    @DecimalMin(value = "0.01", message = "La cantidad debe ser mayor a 0")
    private Double cantidad;

    private LocalDateTime fechaHora;

    private String estado;

    private String repartidorNombre;

    private String inventarioNombre;

    private String tipoGarrafon;

    private String unidadMedida;

    public CargaDTO() {
    }

    public CargaDTO(Long id,
                    Long repartidorId,
                    Long inventarioId,
                    Double cantidad,
                    LocalDateTime fechaHora,
                    String estado,
                    String repartidorNombre,
                    String inventarioNombre,
                    String tipoGarrafon,
                    String unidadMedida) {

        this.id = id;
        this.repartidorId = repartidorId;
        this.inventarioId = inventarioId;
        this.cantidad = cantidad;
        this.fechaHora = fechaHora;
        this.estado = estado;
        this.repartidorNombre = repartidorNombre;
        this.inventarioNombre = inventarioNombre;
        this.tipoGarrafon = tipoGarrafon;
        this.unidadMedida = unidadMedida;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getRepartidorId() {
        return repartidorId;
    }

    public void setRepartidorId(Long repartidorId) {
        this.repartidorId = repartidorId;
    }

    public Long getInventarioId() {
        return inventarioId;
    }

    public void setInventarioId(Long inventarioId) {
        this.inventarioId = inventarioId;
    }

    public Double getCantidad() {
        return cantidad;
    }

    public void setCantidad(Double cantidad) {
        this.cantidad = cantidad;
    }

    public LocalDateTime getFechaHora() {
        return fechaHora;
    }

    public void setFechaHora(LocalDateTime fechaHora) {
        this.fechaHora = fechaHora;
    }

    public String getEstado() {
        return estado;
    }

    public void setEstado(String estado) {
        this.estado = estado;
    }

    public String getRepartidorNombre() {
        return repartidorNombre;
    }

    public void setRepartidorNombre(String repartidorNombre) {
        this.repartidorNombre = repartidorNombre;
    }

    public String getInventarioNombre() {
        return inventarioNombre;
    }

    public void setInventarioNombre(String inventarioNombre) {
        this.inventarioNombre = inventarioNombre;
    }

    public String getTipoGarrafon() {
        return tipoGarrafon;
    }

    public void setTipoGarrafon(String tipoGarrafon) {
        this.tipoGarrafon = tipoGarrafon;
    }

    public String getUnidadMedida() {
        return unidadMedida;
    }

    public void setUnidadMedida(String unidadMedida) {
        this.unidadMedida = unidadMedida;
    }
}