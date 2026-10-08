package com.utvt.ApiSpringCafeSoft.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;

import java.util.ArrayList;
import java.util.List;

/**
 * HU-015
 * Permite registrar varias cargas (varios tipos de
 * garrafón) para un repartidor en una sola petición.
 */
public class RegistrarCargasMultipleDTO {

    @NotNull(message = "El repartidor es obligatorio")
    private Long repartidorId;

    @Valid
    private List<ItemGarrafonDTO> items = new ArrayList<>();

    public Long getRepartidorId() {
        return repartidorId;
    }

    public void setRepartidorId(Long repartidorId) {
        this.repartidorId = repartidorId;
    }

    public List<ItemGarrafonDTO> getItems() {
        return items;
    }

    public void setItems(List<ItemGarrafonDTO> items) {
        this.items = items;
    }
}
