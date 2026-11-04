
package com.utvt.ApiSpringCafeSoft.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;
import java.time.LocalDateTime;

@Schema(description = "Promocion con vigencia y auditoria")
public class PromocionDTO {

    private Long id;

    @NotBlank(message = "El nombre es obligatorio")
    @Size(max = 100)
    private String nombre;

    private String descripcion;

    @NotNull(message = "El descuento es obligatorio")
    @DecimalMin("0.01")
    @DecimalMax("100.0")
    private Double descuentoPorcentaje;

    @NotNull(message = "La fecha de inicio es obligatoria")
    private LocalDateTime fechaInicio;

    @NotNull(message = "La fecha de fin es obligatoria")
    private LocalDateTime fechaFin;

    private Boolean activa;
    private Integer minGarrafones = 0;
    private Boolean acumulaPuntos = true;

    // HU-039
    private Boolean vigente;
    private String estado;

    private Long sucursalId;
    private String sucursalNombre;
    private LocalDateTime createdAt;

    // HU-040: datos de auditoria (solo lectura)
    private LocalDateTime fechaDesactivacion;
    private Long desactivadaPorId;
    private String desactivadaPorNombre;
    private String desactivadaPorEmail;

    public PromocionDTO() {}

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public void setDescripcion(String descripcion) {
        this.descripcion = descripcion;
    }

    public Double getDescuentoPorcentaje() {
        return descuentoPorcentaje;
    }

    public void setDescuentoPorcentaje(Double descuentoPorcentaje) {
        this.descuentoPorcentaje = descuentoPorcentaje;
    }

    public LocalDateTime getFechaInicio() {
        return fechaInicio;
    }

    public void setFechaInicio(LocalDateTime fechaInicio) {
        this.fechaInicio = fechaInicio;
    }

    public LocalDateTime getFechaFin() {
        return fechaFin;
    }

    public void setFechaFin(LocalDateTime fechaFin) {
        this.fechaFin = fechaFin;
    }

    public Boolean getActiva() {
        return activa;
    }

    public void setActiva(Boolean activa) {
        this.activa = activa;
    }

    public Integer getMinGarrafones() {
        return minGarrafones;
    }

    public void setMinGarrafones(Integer minGarrafones) {
        this.minGarrafones = minGarrafones;
    }

    public Boolean getAcumulaPuntos() {
        return acumulaPuntos;
    }

    public void setAcumulaPuntos(Boolean acumulaPuntos) {
        this.acumulaPuntos = acumulaPuntos;
    }

    public Boolean getVigente() {
        return vigente;
    }

    public void setVigente(Boolean vigente) {
        this.vigente = vigente;
    }

    public String getEstado() {
        return estado;
    }

    public void setEstado(String estado) {
        this.estado = estado;
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

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getFechaDesactivacion() {
        return fechaDesactivacion;
    }

    public void setFechaDesactivacion(
        LocalDateTime fechaDesactivacion
    ) {
        this.fechaDesactivacion = fechaDesactivacion;
    }

    public Long getDesactivadaPorId() {
        return desactivadaPorId;
    }

    public void setDesactivadaPorId(Long desactivadaPorId) {
        this.desactivadaPorId = desactivadaPorId;
    }

    public String getDesactivadaPorNombre() {
        return desactivadaPorNombre;
    }

    public void setDesactivadaPorNombre(
        String desactivadaPorNombre
    ) {
        this.desactivadaPorNombre = desactivadaPorNombre;
    }

    public String getDesactivadaPorEmail() {
        return desactivadaPorEmail;
    }

    public void setDesactivadaPorEmail(
        String desactivadaPorEmail
    ) {
        this.desactivadaPorEmail = desactivadaPorEmail;
    }
}
