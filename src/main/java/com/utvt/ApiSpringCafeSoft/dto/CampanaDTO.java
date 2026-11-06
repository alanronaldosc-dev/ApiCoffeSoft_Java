package com.utvt.ApiSpringCafeSoft.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;
import java.time.LocalDateTime;

/**
 * HU-037: DTO para creación, actualización y respuesta de Campañas.
 */
@Schema(description = "Campaña de marketing dirigida a un segmento de clientes")
public class CampanaDTO {

    @Schema(description = "ID de la campaña", accessMode = Schema.AccessMode.READ_ONLY)
    private Long id;

    @NotBlank(message = "El nombre es obligatorio")
    @Size(max = 100)
    @Schema(description = "Nombre de la campaña", example = "Campaña Clientes Dormidos Noviembre")
    private String nombre;

    @Schema(description = "Descripción opcional", example = "Descuento especial para reactivar clientes")
    private String descripcion;

    @NotBlank(message = "El segmento es obligatorio")
    @Pattern(
        regexp = "^(ACTIVO|POTENCIAL|DORMIDO|POCO_FRECUENTE)$",
        message = "El segmento debe ser: ACTIVO, POTENCIAL, DORMIDO o POCO_FRECUENTE"
    )
    @Schema(description = "Segmento objetivo", example = "DORMIDO",
            allowableValues = {"ACTIVO", "POTENCIAL", "DORMIDO", "POCO_FRECUENTE"})
    private String segmento;

    @NotNull(message = "El ID de la promoción es obligatorio")
    @Schema(description = "ID de la promoción asociada", example = "1")
    private Long promocionId;

    @Schema(description = "Nombre de la promoción", accessMode = Schema.AccessMode.READ_ONLY)
    private String promocionNombre;

    @Schema(description = "Descuento de la promoción (%)", accessMode = Schema.AccessMode.READ_ONLY)
    private Double promocionDescuento;

    @NotNull(message = "La fecha de inicio es obligatoria")
    @Schema(description = "Inicio del periodo activo", example = "2026-11-01T00:00:00")
    private LocalDateTime fechaInicio;

    @NotNull(message = "La fecha de fin es obligatoria")
    @Schema(description = "Fin del periodo activo", example = "2026-11-30T23:59:59")
    private LocalDateTime fechaFin;

    @Schema(description = "¿Campaña activa?", accessMode = Schema.AccessMode.READ_ONLY)
    private Boolean activa;

    @Schema(description = "¿Campaña vigente en este momento?", accessMode = Schema.AccessMode.READ_ONLY)
    private Boolean vigente;

    @Schema(description = "ID de la sucursal", accessMode = Schema.AccessMode.READ_ONLY)
    private Long sucursalId;

    @Schema(description = "Nombre de la sucursal", accessMode = Schema.AccessMode.READ_ONLY)
    private String sucursalNombre;

    @Schema(description = "Fecha de creación", accessMode = Schema.AccessMode.READ_ONLY)
    private LocalDateTime createdAt;

    public CampanaDTO() {}

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }
    public String getDescripcion() { return descripcion; }
    public void setDescripcion(String descripcion) { this.descripcion = descripcion; }
    public String getSegmento() { return segmento; }
    public void setSegmento(String segmento) { this.segmento = segmento; }
    public Long getPromocionId() { return promocionId; }
    public void setPromocionId(Long promocionId) { this.promocionId = promocionId; }
    public String getPromocionNombre() { return promocionNombre; }
    public void setPromocionNombre(String promocionNombre) { this.promocionNombre = promocionNombre; }
    public Double getPromocionDescuento() { return promocionDescuento; }
    public void setPromocionDescuento(Double promocionDescuento) { this.promocionDescuento = promocionDescuento; }
    public LocalDateTime getFechaInicio() { return fechaInicio; }
    public void setFechaInicio(LocalDateTime fechaInicio) { this.fechaInicio = fechaInicio; }
    public LocalDateTime getFechaFin() { return fechaFin; }
    public void setFechaFin(LocalDateTime fechaFin) { this.fechaFin = fechaFin; }
    public Boolean getActiva() { return activa; }
    public void setActiva(Boolean activa) { this.activa = activa; }
    public Boolean getVigente() { return vigente; }
    public void setVigente(Boolean vigente) { this.vigente = vigente; }
    public Long getSucursalId() { return sucursalId; }
    public void setSucursalId(Long sucursalId) { this.sucursalId = sucursalId; }
    public String getSucursalNombre() { return sucursalNombre; }
    public void setSucursalNombre(String sucursalNombre) { this.sucursalNombre = sucursalNombre; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}
