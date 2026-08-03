package com.utvt.ApiSpringCafeSoft.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Schema(description = "DTO para la venta")
public class VentaDTO {

    @Schema(description = "ID de la venta", example = "1")
    private Long id;

    @Schema(description = "Folio de la venta", example = "V-0001")
    private String folio;

    @Schema(description = "Fecha de la venta", example = "2026-06-22T10:30:00")
    private LocalDateTime fecha;

    @Schema(description = "Subtotal de la venta", example = "150.00")
    private Double subtotal;

    @Schema(description = "Impuestos de la venta", example = "24.00")
    private Double impuestos;

    @Schema(description = "Descuento aplicado", example = "10.00")
    private Double descuento;

    @Schema(description = "Total de la venta", example = "164.00")
    private Double total;

    @Schema(description = "Método de pago (efectivo o tarjeta)", example = "efectivo", required = true)
    @NotBlank(message = "El método de pago es obligatorio")
    @Pattern(regexp = "^(efectivo|tarjeta)$", message = "El método de pago debe ser: efectivo o tarjeta")
    private String metodoPago;

    @Schema(description = "ID del usuario que realiza la venta", example = "1", required = true)
    @NotNull(message = "El ID del usuario es obligatorio")
    private Long usuarioId;

    @Schema(description = "Nombre del usuario (solo lectura)")
    private String usuarioNombre;

    @Schema(description = "Observaciones de la venta", example = "Venta con descuento especial")
    private String observaciones;

    @Schema(description = "Lista de productos vendidos", required = true)
    @Valid
    @NotEmpty(message = "Debe haber al menos un producto")
    private List<VentaDetalleDTO> detalles = new ArrayList<>();

    @Schema(description = "Fecha de creación", example = "2026-06-22T10:30:00")
    private LocalDateTime createdAt;

    @Schema(description = "Monto en efectivo recibido (solo si método es efectivo)", example = "200.00")
    private Double montoEfectivo;

    @Schema(description = "Cambio a devolver (solo si método es efectivo)", example = "36.00")
    private Double cambio;


    // Constructor por defecto
    public VentaDTO() {}

    // Getters y Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getFolio() {
        return folio;
    }

    public void setFolio(String folio) {
        this.folio = folio;
    }

    public LocalDateTime getFecha() {
        return fecha;
    }

    public void setFecha(LocalDateTime fecha) {
        this.fecha = fecha;
    }

    public Double getSubtotal() {
        return subtotal;
    }

    public void setSubtotal(Double subtotal) {
        this.subtotal = subtotal;
    }

    public Double getImpuestos() {
        return impuestos;
    }

    public void setImpuestos(Double impuestos) {
        this.impuestos = impuestos;
    }

    public Double getDescuento() {
        return descuento;
    }

    public void setDescuento(Double descuento) {
        this.descuento = descuento;
    }

    public Double getTotal() {
        return total;
    }

    public void setTotal(Double total) {
        this.total = total;
    }

    public String getMetodoPago() {
        return metodoPago;
    }

    public void setMetodoPago(String metodoPago) {
        this.metodoPago = metodoPago;
    }

    public Long getUsuarioId() {
        return usuarioId;
    }

    public void setUsuarioId(Long usuarioId) {
        this.usuarioId = usuarioId;
    }

    public String getUsuarioNombre() {
        return usuarioNombre;
    }

    public void setUsuarioNombre(String usuarioNombre) {
        this.usuarioNombre = usuarioNombre;
    }

    public String getObservaciones() {
        return observaciones;
    }

    public void setObservaciones(String observaciones) {
        this.observaciones = observaciones;
    }

    public List<VentaDetalleDTO> getDetalles() {
        return detalles;
    }

    public void setDetalles(List<VentaDetalleDTO> detalles) {
        this.detalles = detalles;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public Double getMontoEfectivo() { 
        return montoEfectivo; 
    }
    
    public void setMontoEfectivo(Double montoEfectivo) { 
        this.montoEfectivo = montoEfectivo; 
    }

    public Double getCambio() { 
        return cambio; 
    }

    public void setCambio(Double cambio) { 
        this.cambio = cambio; 
    }

}