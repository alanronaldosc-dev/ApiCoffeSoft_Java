package com.utvt.ApiSpringCafeSoft.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Producto más vendido del periodo")
public class ProductoMasVendidoDTO {

    @Schema(description = "ID del producto", example = "12")
    private Long productoId;

    @Schema(description = "Nombre del producto", example = "Café Premium 250g")
    private String nombre;

    @Schema(description = "Unidades vendidas en el periodo", example = "84")
    private long unidadesVendidas;

    @Schema(description = "Monto recaudado por el producto en el periodo", example = "1680.00")
    private Double montoTotal;

    public ProductoMasVendidoDTO() {
    }

    public ProductoMasVendidoDTO(Long productoId, String nombre, long unidadesVendidas, Double montoTotal) {
        this.productoId = productoId;
        this.nombre = nombre;
        this.unidadesVendidas = unidadesVendidas;
        this.montoTotal = montoTotal;
    }

    public Long getProductoId() {
        return productoId;
    }

    public void setProductoId(Long productoId) {
        this.productoId = productoId;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public long getUnidadesVendidas() {
        return unidadesVendidas;
    }

    public void setUnidadesVendidas(long unidadesVendidas) {
        this.unidadesVendidas = unidadesVendidas;
    }

    public Double getMontoTotal() {
        return montoTotal;
    }

    public void setMontoTotal(Double montoTotal) {
        this.montoTotal = montoTotal;
    }
}
