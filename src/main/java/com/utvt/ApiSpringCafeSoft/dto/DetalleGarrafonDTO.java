package com.utvt.ApiSpringCafeSoft.dto;

/**
 * HU-015
 * Detalle por tipo de garrafón de una entrega.
 * Se regresa en la respuesta de las entregas para
 * que la app móvil pueda mostrar el desglose.
 */
public class DetalleGarrafonDTO {

    private Long inventarioId;

    private String inventarioNombre;

    private Double cantidad;

    public DetalleGarrafonDTO() {
    }

    public DetalleGarrafonDTO(Long inventarioId,
                              String inventarioNombre,
                              Double cantidad) {

        this.inventarioId = inventarioId;
        this.inventarioNombre = inventarioNombre;
        this.cantidad = cantidad;
    }

    public Long getInventarioId() {
        return inventarioId;
    }

    public void setInventarioId(Long inventarioId) {
        this.inventarioId = inventarioId;
    }

    public String getInventarioNombre() {
        return inventarioNombre;
    }

    public void setInventarioNombre(String inventarioNombre) {
        this.inventarioNombre = inventarioNombre;
    }

    public Double getCantidad() {
        return cantidad;
    }

    public void setCantidad(Double cantidad) {
        this.cantidad = cantidad;
    }
}
