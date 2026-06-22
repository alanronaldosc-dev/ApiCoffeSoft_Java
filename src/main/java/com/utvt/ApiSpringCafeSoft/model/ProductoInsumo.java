package com.utvt.ApiSpringCafeSoft.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import jakarta.validation.constraints.*;

@Entity
@Table(name = "producto_insumo")
public class ProductoInsumo {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "producto_id", nullable = false)
    @JsonIgnore
    private Producto producto;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "insumo_id", nullable = false)
    private Inventario insumo;

    @NotNull(message = "La cantidad es obligatoria")
    @DecimalMin(value = "0.001", message = "La cantidad debe ser mayor a 0")
    @Column(nullable = false)
    private Double cantidad;

    @NotNull(message = "La unidad de medida es obligatoria")
    @Pattern(regexp = "^(litros|kilogramos|piezas)$", message = "La unidad debe ser: litros, kilogramos o piezas")
    @Column(name = "unidad_medida", nullable = false, length = 20)
    private String unidadMedida;

    // Constructor por defecto
    public ProductoInsumo() {}

    // Constructor con parámetros
    public ProductoInsumo(Inventario insumo, Double cantidad, String unidadMedida) {
        this.insumo = insumo;
        this.cantidad = cantidad;
        this.unidadMedida = unidadMedida;
    }

    // Getters y Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Producto getProducto() {
        return producto;
    }

    public void setProducto(Producto producto) {
        this.producto = producto;
    }

    public Inventario getInsumo() {
        return insumo;
    }

    public void setInsumo(Inventario insumo) {
        this.insumo = insumo;
    }

    public Double getCantidad() {
        return cantidad;
    }

    public void setCantidad(Double cantidad) {
        this.cantidad = cantidad;
    }

    public String getUnidadMedida() {
        return unidadMedida;
    }

    public void setUnidadMedida(String unidadMedida) {
        this.unidadMedida = unidadMedida;
    }
}