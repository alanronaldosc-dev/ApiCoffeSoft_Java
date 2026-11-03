package com.utvt.ApiSpringCafeSoft.dto;

public class MovimientoPuntosDTO {

    private final Long ventaId;
    private final Integer puntos;

    public MovimientoPuntosDTO(Long ventaId, Integer puntos) {
        this.ventaId = ventaId;
        this.puntos = puntos;
    }

    public Long getVentaId() {
        return ventaId;
    }

    public Integer getPuntos() {
        return puntos;
    }
}
