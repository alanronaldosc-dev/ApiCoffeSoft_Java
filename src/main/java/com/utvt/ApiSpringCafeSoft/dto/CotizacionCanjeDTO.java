package com.utvt.ApiSpringCafeSoft.dto;

import java.math.BigDecimal;

public class CotizacionCanjeDTO {

    private final Integer puntosCanjeados;
    private final BigDecimal descuento;

    public CotizacionCanjeDTO(Integer puntosCanjeados, BigDecimal descuento) {
        this.puntosCanjeados = puntosCanjeados;
        this.descuento = descuento;
    }

    public Integer getPuntosCanjeados() {
        return puntosCanjeados;
    }

    public BigDecimal getDescuento() {
        return descuento;
    }
}
