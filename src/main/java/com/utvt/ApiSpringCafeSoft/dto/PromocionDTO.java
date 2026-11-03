package com.utvt.ApiSpringCafeSoft.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.utvt.ApiSpringCafeSoft.model.SegmentoCliente;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class PromocionDTO {

    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    private Long id;

    @NotBlank
    @Size(max = 100)
    private String nombre;

    @Size(max = 500)
    private String descripcion;

    @NotNull
    @DecimalMin("0.01")
    @DecimalMax("100.00")
    @Digits(integer = 3, fraction = 2)
    private BigDecimal descuentoPorcentaje;

    @NotNull
    private LocalDateTime fechaInicio;

    @NotNull
    private LocalDateTime fechaFin;

    @NotNull
    @Min(0)
    private Integer minGarrafones = 0;

    @NotNull
    private Boolean acumulaPuntos = true;

    @NotNull
    private SegmentoCliente segmentoObjetivo = SegmentoCliente.TODOS;

    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    private Boolean activa;

    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    private Boolean vigente;

    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    private String sucursalNombre = "Todas las sucursales";

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }
    public String getDescripcion() { return descripcion; }
    public void setDescripcion(String descripcion) { this.descripcion = descripcion; }
    public BigDecimal getDescuentoPorcentaje() { return descuentoPorcentaje; }
    public void setDescuentoPorcentaje(BigDecimal descuentoPorcentaje) {
        this.descuentoPorcentaje = descuentoPorcentaje;
    }
    public LocalDateTime getFechaInicio() { return fechaInicio; }
    public void setFechaInicio(LocalDateTime fechaInicio) { this.fechaInicio = fechaInicio; }
    public LocalDateTime getFechaFin() { return fechaFin; }
    public void setFechaFin(LocalDateTime fechaFin) { this.fechaFin = fechaFin; }
    public Integer getMinGarrafones() { return minGarrafones; }
    public void setMinGarrafones(Integer minGarrafones) { this.minGarrafones = minGarrafones; }
    public Boolean getAcumulaPuntos() { return acumulaPuntos; }
    public void setAcumulaPuntos(Boolean acumulaPuntos) { this.acumulaPuntos = acumulaPuntos; }
    public SegmentoCliente getSegmentoObjetivo() { return segmentoObjetivo; }
    public void setSegmentoObjetivo(SegmentoCliente segmentoObjetivo) {
        this.segmentoObjetivo = segmentoObjetivo;
    }
    public Boolean getActiva() { return activa; }
    public void setActiva(Boolean activa) { this.activa = activa; }
    public Boolean getVigente() { return vigente; }
    public void setVigente(Boolean vigente) { this.vigente = vigente; }
    public String getSucursalNombre() { return sucursalNombre; }
    public void setSucursalNombre(String sucursalNombre) { this.sucursalNombre = sucursalNombre; }
}
