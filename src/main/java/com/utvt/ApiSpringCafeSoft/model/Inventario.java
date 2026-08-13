package com.utvt.ApiSpringCafeSoft.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;

@Entity
@Table(name = "inventario")
public class Inventario {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "El nombre es obligatorio")
    @Size(
        max = 100,
        message = "El nombre no puede exceder los 100 caracteres"
    )
    @Column(nullable = false, length = 100)
    private String nombre;

    @NotBlank(message = "El tipo es obligatorio")
    @Size(
        max = 50,
        message = "El tipo no puede exceder los 50 caracteres"
    )
    @Column(nullable = false, length = 50)
    private String tipo;

    @NotNull(message = "La cantidad es obligatoria")
    @DecimalMin(
        value = "0.0",
        inclusive = true,
        message = "La cantidad no puede ser negativa"
    )
    @Column(nullable = false)
    private Double cantidad;

    @NotBlank(message = "La unidad de medida es obligatoria")
    @Pattern(
        regexp = "^(litros|kilogramos|piezas)$",
        message = "La unidad debe ser: litros, kilogramos o piezas"
    )
    @Column(nullable = false, length = 20)
    private String unidadMedida;

    @NotNull(message = "La cantidad mínima es obligatoria")
    @DecimalMin(
        value = "0.0",
        inclusive = true,
        message = "La cantidad mínima no puede ser negativa"
    )
    @Column(nullable = false)
    private Double cantidadMinima;

    @Column(name = "caducidad")
    private String caducidad;

    @Size(
        max = 100,
        message = "El proveedor no puede exceder los 100 caracteres"
    )
    @Column(length = 100)
    private String proveedor;

    @NotNull(message = "El precio unitario es obligatorio")
    @DecimalMin(
        value = "0.0",
        inclusive = true,
        message = "El precio unitario no puede ser negativo"
    )
    @Column(name = "precio_unitario", nullable = false)
    private Double precioUnitario;

    // Constructor por defecto
    public Inventario() {
    }

    // Constructor
    public Inventario(
            String nombre,
            String tipo,
            Double cantidad,
            String unidadMedida,
            Double cantidadMinima,
            String caducidad,
            String proveedor,
            Double precioUnitario) {

        this.nombre = nombre;
        this.tipo = tipo;
        this.cantidad = cantidad;
        this.unidadMedida = unidadMedida;
        this.cantidadMinima = cantidadMinima;
        this.caducidad = caducidad;
        this.proveedor = proveedor;
        this.precioUnitario = precioUnitario;
    }

    // Getters y Setters

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public String getTipo() {
        return tipo;
    }

    public void setTipo(String tipo) {
        this.tipo = tipo;
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

    public Double getCantidadMinima() {
        return cantidadMinima;
    }

    public void setCantidadMinima(Double cantidadMinima) {
        this.cantidadMinima = cantidadMinima;
    }

    public String getCaducidad() {
        return caducidad;
    }

    public void setCaducidad(String caducidad) {
        this.caducidad = caducidad;
    }

    public String getProveedor() {
        return proveedor;
    }

    public void setProveedor(String proveedor) {
        this.proveedor = proveedor;
    }

    public Double getPrecioUnitario() {
        return precioUnitario;
    }

    public void setPrecioUnitario(Double precioUnitario) {
        this.precioUnitario = precioUnitario;
    }
}