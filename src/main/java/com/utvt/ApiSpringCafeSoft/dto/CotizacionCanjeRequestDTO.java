package com.utvt.ApiSpringCafeSoft.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

public class CotizacionCanjeRequestDTO {

    @NotNull
    @Min(1)
    private Integer puntosSolicitados;

    @NotNull
    @DecimalMin("0.00")
    private BigDecimal totalCompra;

    public Integer getPuntosSolicitados() {
        return puntosSolicitados;
    }

    public void setPuntosSolicitados(Integer puntosSolicitados) {
        this.puntosSolicitados = puntosSolicitados;
    }

    public BigDecimal getTotalCompra() {
        return totalCompra;
    }

    public void setTotalCompra(BigDecimal totalCompra) {
        this.totalCompra = totalCompra;
    }
}
