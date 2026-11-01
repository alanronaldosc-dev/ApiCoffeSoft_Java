package com.utvt.ApiSpringCafeSoft.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;
import java.time.LocalDateTime;

@Schema(description = "Promoción con vigencia controlada por fecha de inicio y fin")
public class PromocionDTO {

    @Schema(description = "ID de la promoción", example = "1")
    private Long id;

    @NotBlank(message = "El nombre es obligatorio")
    @Size(max = 100)
    @Schema(description = "Nombre de la promoción", example = "Descuento de temporada")
    private String nombre;

    @Schema(description = "Descripción opcional", example = "20% en todos los garrafones")
    private String descripcion;

    @NotNull(message = "El descuento es obligatorio")
    @DecimalMin("0.01")
    @DecimalMax("100.0")
    @Schema(description = "Porcentaje de descuento", example = "20.0")
    private Double descuentoPorcentaje;

    // CA-1: ambas fechas son obligatorias
    @NotNull(message = "La fecha de inicio es obligatoria")
    @Schema(description = "Fecha y hora de inicio", example = "2026-11-01T00:00:00")
    private LocalDateTime fechaInicio;

    @NotNull(message = "La fecha de fin es obligatoria")
    @Schema(description = "Fecha y hora de fin", example = "2026-11-30T23:59:59")
    private LocalDateTime fechaFin;

    @Schema(description = "¿La promoción está activa?", example = "true")
    private Boolean activa;

    @Schema(description = "Cantidad mínima de garrafones para aplicar", example = "10")
    private Integer minGarrafones = 0;

    @Schema(description = "¿Acumula puntos al vender con esta promoción?", example = "true")
    private Boolean acumulaPuntos = true;

    // CA-2: calculado en el backend, solo lectura
    @Schema(description = "¿La promoción está dentro de su periodo vigente?", example = "true")
    private Boolean vigente;

    @Schema(description = "ID de la sucursal", example = "1")
    private Long sucursalId;

    @Schema(description = "Nombre de la sucursal", example = "Sucursal Centro")
    private String sucursalNombre;

    @Schema(description = "Fecha de creación")
    private LocalDateTime createdAt;

    public PromocionDTO() {}

    // ── Getters / Setters ──

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }

    public String getDescripcion() { return descripcion; }
    public void setDescripcion(String descripcion) { this.descripcion = descripcion; }

    public Double getDescuentoPorcentaje() { return descuentoPorcentaje; }
    public void setDescuentoPorcentaje(Double v) { this.descuentoPorcentaje = v; }

    public LocalDateTime getFechaInicio() { return fechaInicio; }
    public void setFechaInicio(LocalDateTime fechaInicio) { this.fechaInicio = fechaInicio; }

    public LocalDateTime getFechaFin() { return fechaFin; }
    public void setFechaFin(LocalDateTime fechaFin) { this.fechaFin = fechaFin; }

    public Boolean getActiva() { return activa; }
    public void setActiva(Boolean activa) { this.activa = activa; }

    public Integer getMinGarrafones() { return minGarrafones; }
    public void setMinGarrafones(Integer minGarrafones) { this.minGarrafones = minGarrafones; }

    public Boolean getAcumulaPuntos() { return acumulaPuntos; }
    public void setAcumulaPuntos(Boolean acumulaPuntos) { this.acumulaPuntos = acumulaPuntos; }

    public Boolean getVigente() { return vigente; }
    public void setVigente(Boolean vigente) { this.vigente = vigente; }

    public Long getSucursalId() { return sucursalId; }
    public void setSucursalId(Long sucursalId) { this.sucursalId = sucursalId; }

    public String getSucursalNombre() { return sucursalNombre; }
    public void setSucursalNombre(String sucursalNombre) { this.sucursalNombre = sucursalNombre; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}
