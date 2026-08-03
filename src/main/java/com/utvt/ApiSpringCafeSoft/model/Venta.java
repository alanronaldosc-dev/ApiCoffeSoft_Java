package com.utvt.ApiSpringCafeSoft.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "ventas")
public class Venta {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "El folio es obligatorio")
    @Size(max = 20, message = "El folio no puede exceder los 20 caracteres")
    @Column(nullable = false, unique = true, length = 20)
    private String folio;

    @NotNull(message = "La fecha es obligatoria")
    @Column(nullable = false)
    private LocalDateTime fecha;

    @NotNull(message = "El subtotal es obligatorio")
    @Min(value = 0, message = "El subtotal no puede ser negativo")
    @Column(nullable = false)
    private Double subtotal;

    @NotNull(message = "Los impuestos son obligatorios")
    @Min(value = 0, message = "Los impuestos no pueden ser negativos")
    @Column(nullable = false)
    private Double impuestos;

    @Min(value = 0, message = "El descuento no puede ser negativo")
    @Column(nullable = false)
    private Double descuento;

    @NotNull(message = "El total es obligatorio")
    @Min(value = 0, message = "El total no puede ser negativo")
    @Column(nullable = false)
    private Double total;

    @NotBlank(message = "El método de pago es obligatorio")
    @Pattern(regexp = "^(efectivo|tarjeta)$", message = "El método de pago debe ser: efectivo o tarjeta")
    @Column(name = "metodo_pago", nullable = false, length = 20)
    private String metodoPago;

    @NotNull(message = "El usuario es obligatorio")
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "usuario_id", referencedColumnName = "id_usuario", nullable = false)
    private Usuario usuario;

    @Size(max = 500, message = "Las observaciones no pueden exceder los 500 caracteres")
    @Column(length = 500)
    private String observaciones;

    @NotNull(message = "Los detalles de venta son obligatorios")
    @OneToMany(mappedBy = "venta", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.EAGER)
    private List<VentaDetalle> detalles = new ArrayList<>();

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "monto_efectivo")
    private Double montoEfectivo;

    @Column(name = "cambio")
    private Double cambio;

    public Double getMontoEfectivo() { return montoEfectivo; }
    public void setMontoEfectivo(Double montoEfectivo) { this.montoEfectivo = montoEfectivo; }

    public Double getCambio() { return cambio; }
    public void setCambio(Double cambio) { this.cambio = cambio; }


    // Constructor por defecto
    public Venta() {
        this.fecha = LocalDateTime.now();
        this.createdAt = LocalDateTime.now();
        this.descuento = 0.0;
    }

    // Constructor con parámetros básicos
    public Venta(String folio, Double subtotal, Double impuestos, Double descuento, 
                 Double total, String metodoPago, Usuario usuario) {
        this.folio = folio;
        this.fecha = LocalDateTime.now();
        this.subtotal = subtotal;
        this.impuestos = impuestos;
        this.descuento = descuento != null ? descuento : 0.0;
        this.total = total;
        this.metodoPago = metodoPago;
        this.usuario = usuario;
        this.createdAt = LocalDateTime.now();
    }

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

    public Usuario getUsuario() {
        return usuario;
    }

    public void setUsuario(Usuario usuario) {
        this.usuario = usuario;
    }

    public String getObservaciones() {
        return observaciones;
    }

    public void setObservaciones(String observaciones) {
        this.observaciones = observaciones;
    }

    public List<VentaDetalle> getDetalles() {
        return detalles;
    }

    public void setDetalles(List<VentaDetalle> detalles) {
        this.detalles = detalles;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    // Método helper para agregar detalle
    public void addDetalle(VentaDetalle detalle) {
        detalles.add(detalle);
        detalle.setVenta(this);
    }

    // Método helper para remover detalle
    public void removeDetalle(VentaDetalle detalle) {
        detalles.remove(detalle);
        detalle.setVenta(null);
    }
}