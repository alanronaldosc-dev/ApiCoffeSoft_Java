package com.utvt.ApiSpringCafeSoft.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;

@Schema(description = "DTO para la relación producto-insumo")
public class ProductoInsumoDTO {

    @Schema(description = "ID de la relación", example = "1")
    private Long id;

    @Schema(description = "ID del insumo", example = "1", required = true)
    @NotNull(message = "El ID del insumo es obligatorio")
    private Long insumoId;

    @Schema(description = "Nombre del insumo (solo lectura)", example = "Café Arábica")
    private String insumoNombre;

    @Schema(description = "Cantidad del insumo necesaria", example = "2.5", required = true)
    @NotNull(message = "La cantidad es obligatoria")
    @DecimalMin(value = "0.001", message = "La cantidad debe ser mayor a 0")
    private Double cantidad;

    @Schema(description = "Unidad de medida", example = "kilogramos", required = true, 
            allowableValues = {"litros", "kilogramos", "piezas"})
    @NotNull(message = "La unidad de medida es obligatoria")
    @Pattern(regexp = "^(litros|kilogramos|piezas)$", message = "La unidad debe ser: litros, kilogramos o piezas")
    private String unidadMedida;

    // Constructor por defecto
    public ProductoInsumoDTO() {}

    // Constructor con parámetros
    public ProductoInsumoDTO(Long insumoId, Double cantidad, String unidadMedida) {
        this.insumoId = insumoId;
        this.cantidad = cantidad;
        this.unidadMedida = unidadMedida;
    }

    // Getters y Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getInsumoId() {
        return insumoId;
    }

    public void setInsumoId(Long insumoId) {
        this.insumoId = insumoId;
    }

    public String getInsumoNombre() {
        return insumoNombre;
    }

    public void setInsumoNombre(String insumoNombre) {
        this.insumoNombre = insumoNombre;
    }

    public Double getCantidad() {
        return cantidad;
    }

    public void setCantidad(Double cantidad) {
        this.cantidad = cantidad;
    }

    public String getUnidadMedida() {
        return unidadMedida;
    }

    public void setUnidadMedida(String unidadMedida) {
        this.unidadMedida = unidadMedida;
    }
}