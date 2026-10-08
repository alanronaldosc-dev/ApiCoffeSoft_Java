package com.utvt.ApiSpringCafeSoft.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;

/**
 * HU-015
 * Línea de garrafones por tipo de garrafón (inventario).
 *
 * Se utiliza tanto para los garrafones llenos que
 * se venden como para los envases vacíos que
 * el cliente devuelve.
 */
public class ItemGarrafonDTO {

    @NotNull(message = "El inventario es obligatorio")
    private Long inventarioId;

    @NotNull(message = "La cantidad es obligatoria")
    @DecimalMin(value = "0.01", message = "La cantidad debe ser mayor a cero")
    private Double cantidad;

    public ItemGarrafonDTO() {
    }

    public ItemGarrafonDTO(Long inventarioId, Double cantidad) {
        this.inventarioId = inventarioId;
        this.cantidad = cantidad;
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
}
