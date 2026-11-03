package com.utvt.ApiSpringCafeSoft.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
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

    @Schema(description = "Puntos de fidelidad canjeados", example = "50")
    @NotNull
    @Min(0)
    private Integer puntosCanjeados = 0;

    @Schema(description = "Saldo de puntos después de la venta", example = "450")
    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    private Integer saldoPuntosRestante;

    @Schema(description = "Total de la venta", example = "164.00")
    private Double total;

    @Schema(description = "Método de pago (efectivo o tarjeta)", example = "efectivo",
            requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "El método de pago es obligatorio")
    @Pattern(regexp = "^(efectivo|tarjeta)$", message = "El método de pago debe ser: efectivo o tarjeta")
    private String metodoPago;

    @Schema(description = "ID del usuario que realiza la venta", example = "1",
            requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "El ID del usuario es obligatorio")
    private Long usuarioId;

    @Schema(description = "Nombre del usuario (solo lectura)")
    private String usuarioNombre;

    @Schema(description = "ID del cliente asociado a la venta", example = "12")
    private Long clienteId;

    @Schema(description = "ID de la promoción que se desea aplicar", example = "3")
    private Long promocionId;

    @Schema(description = "Observaciones de la venta", example = "Venta con descuento especial")
    private String observaciones;

    @Schema(description = "Lista de productos vendidos",
            requiredMode = Schema.RequiredMode.REQUIRED)
    @Valid
    @NotEmpty(message = "Debe haber al menos un producto")
    private List<VentaDetalleDTO> detalles = new ArrayList<>();

    @Schema(description = "Fecha de creación", example = "2026-06-22T10:30:00")
    private LocalDateTime createdAt;

    @Schema(description = "Monto en efectivo recibido (solo si método es efectivo)", example = "200.00")
    private Double montoEfectivo;

    @Schema(description = "Cambio a devolver (solo si método es efectivo)", example = "36.00")
    private Double cambio;

    @Schema(description = "Nombre de la persona para quien es el pedido")
private String nombreCliente;

@Schema(description = "Estado actual del pedido")
private String estadoPedido;


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

    public Long getClienteId() {
        return clienteId;
    }

    public void setClienteId(Long clienteId) {
        this.clienteId = clienteId;
    }

    public Long getPromocionId() {
        return promocionId;
    }

    public void setPromocionId(Long promocionId) {
        this.promocionId = promocionId;
    }

    public Integer getPuntosCanjeados() {
        return puntosCanjeados;
    }

    public void setPuntosCanjeados(Integer puntosCanjeados) {
        this.puntosCanjeados = puntosCanjeados;
    }

    public Integer getSaldoPuntosRestante() {
        return saldoPuntosRestante;
    }

    public void setSaldoPuntosRestante(Integer saldoPuntosRestante) {
        this.saldoPuntosRestante = saldoPuntosRestante;
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

    public String getNombreCliente() {
    return nombreCliente;
}

public void setNombreCliente(String nombreCliente) {
    this.nombreCliente = nombreCliente;
}

public String getEstadoPedido() {
    return estadoPedido;
}

public void setEstadoPedido(String estadoPedido) {
    this.estadoPedido = estadoPedido;
}

}