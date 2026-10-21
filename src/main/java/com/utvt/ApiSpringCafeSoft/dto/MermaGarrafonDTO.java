package com.utvt.ApiSpringCafeSoft.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.time.LocalDateTime;

public class MermaGarrafonDTO {

    private Long id;

    @NotNull(message = "La carga es obligatoria")
    private Long cargaId;

    private Long repartidorId;
    private String repartidorNombre;

    private Long inventarioId;
    private String inventarioNombre;
    private String tipoGarrafon;

    @NotNull(message = "La cantidad es obligatoria")
    @Positive(message = "La cantidad debe ser mayor a cero")
    private Double cantidad;

    @NotBlank(message = "La causa es obligatoria")
    private String causa;

    private String observaciones;

    private LocalDateTime fechaHora;

    public MermaGarrafonDTO() {
    }

    public MermaGarrafonDTO(
            Long id,
            Long cargaId,
            Long repartidorId,
            String repartidorNombre,
            Long inventarioId,
            String inventarioNombre,
            String tipoGarrafon,
            Double cantidad,
            String causa,
            String observaciones,
            LocalDateTime fechaHora) {

        this.id = id;
        this.cargaId = cargaId;
        this.repartidorId = repartidorId;
        this.repartidorNombre = repartidorNombre;
        this.inventarioId = inventarioId;
        this.inventarioNombre = inventarioNombre;
        this.tipoGarrafon = tipoGarrafon;
        this.cantidad = cantidad;
        this.causa = causa;
        this.observaciones = observaciones;
        this.fechaHora = fechaHora;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getCargaId() {
        return cargaId;
    }

    public void setCargaId(Long cargaId) {
        this.cargaId = cargaId;
    }

    public Long getRepartidorId() {
        return repartidorId;
    }

    public void setRepartidorId(Long repartidorId) {
        this.repartidorId = repartidorId;
    }

    public String getRepartidorNombre() {
        return repartidorNombre;
    }

    public void setRepartidorNombre(String repartidorNombre) {
        this.repartidorNombre = repartidorNombre;
    }

    public Long getInventarioId() {
        return inventarioId;
    }

    public void setInventarioId(Long inventarioId) {
        this.inventarioId = inventarioId;
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

    public Double getCantidad() {
        return cantidad;
    }

    public void setCantidad(Double cantidad) {
        this.cantidad = cantidad;
    }

    public String getCausa() {
        return causa;
    }

    public void setCausa(String causa) {
        this.causa = causa;
    }

    public String getObservaciones() {
        return observaciones;
    }

    public void setObservaciones(String observaciones) {
        this.observaciones = observaciones;
    }

    public LocalDateTime getFechaHora() {
        return fechaHora;
    }

    public void setFechaHora(LocalDateTime fechaHora) {
        this.fechaHora = fechaHora;
    }
}