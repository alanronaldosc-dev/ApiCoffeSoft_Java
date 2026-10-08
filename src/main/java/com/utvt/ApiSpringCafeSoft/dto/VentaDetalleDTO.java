package com.utvt.ApiSpringCafeSoft.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;

@Schema(description = "DTO para el detalle de venta")
public class VentaDetalleDTO {

    @Schema(description = "ID del detalle", example = "1")
    private Long id;

    @Schema(description = "ID del producto", example = "1", required = true)
    @NotNull(message = "El ID del producto es obligatorio")
    private Long productoId;

    @Schema(description = "Nombre del producto (solo lectura)", example = "Té Chai Latte")
    private String productoNombre;

    @Schema(description = "Cantidad del producto", example = "2", required = true)
    @NotNull(message = "La cantidad es obligatoria")
    @Min(value = 1, message = "La cantidad debe ser al menos 1")
    private Integer cantidad;

    @Schema(description = "Precio unitario del producto", example = "45.50")
    private Double precioUnitario;

    @Schema(description = "Subtotal del detalle", example = "91.00")
    private Double subtotal;

    // Constructor por defecto
    public VentaDetalleDTO() {}

    // Constructor con parámetros
    public VentaDetalleDTO(Long productoId, Integer cantidad, Double precioUnitario) {
        this.productoId = productoId;
        this.cantidad = cantidad;
        this.precioUnitario = precioUnitario;
        this.subtotal = cantidad * precioUnitario;
    }

    // Getters y Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getProductoId() {
        return productoId;
    }

    public void setProductoId(Long productoId) {
        this.productoId = productoId;
    }

    public String getProductoNombre() {
        return productoNombre;
    }

    public void setProductoNombre(String productoNombre) {
        this.productoNombre = productoNombre;
    }

    public Integer getCantidad() {
        return cantidad;
    }

    public void setCantidad(Integer cantidad) {
        this.cantidad = cantidad;
    }

    public Double getPrecioUnitario() {
        return precioUnitario;
    }

    public void setPrecioUnitario(Double precioUnitario) {
        this.precioUnitario = precioUnitario;
    }

    public Double getSubtotal() {
        return subtotal;
    }

    public void setSubtotal(Double subtotal) {
        this.subtotal = subtotal;
    }
}