package com.utvt.ApiSpringCafeSoft.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import io.swagger.v3.oas.annotations.media.Schema;

/** HU-023: cotización de puntos sin modificar el saldo del cliente. */
@Schema(description = "Cotización de canje de puntos, sin consumir el saldo")
public class CotizacionCanjeDTO {

    @Schema(example = "100", description = "Puntos que el cliente desea canjear")
    @NotNull
    @Min(1)
    private Integer puntosSolicitados;

    @Schema(example = "150.00", description = "Total de compra después de impuestos")
    @NotNull
    @DecimalMin("0.01")
    private Double totalCompra;

    @Schema(accessMode = Schema.AccessMode.READ_ONLY, example = "100")
    private Integer puntosCanjeados;

    @Schema(accessMode = Schema.AccessMode.READ_ONLY, example = "1.00")
    private Double descuento;

    public Integer getPuntosSolicitados() {
        return puntosSolicitados;
    }

    public void setPuntosSolicitados(Integer puntosSolicitados) {
        this.puntosSolicitados = puntosSolicitados;
    }

    public Double getTotalCompra() {
        return totalCompra;
    }

    public void setTotalCompra(Double totalCompra) {
        this.totalCompra = totalCompra;
    }

    public Integer getPuntosCanjeados() {
        return puntosCanjeados;
    }

    public void setPuntosCanjeados(Integer puntosCanjeados) {
        this.puntosCanjeados = puntosCanjeados;
    }

    public Double getDescuento() {
        return descuento;
    }

    public void setDescuento(Double descuento) {
        this.descuento = descuento;
    }
}
