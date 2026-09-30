package com.utvt.ApiSpringCafeSoft.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;

@Entity
@Table(name = "insumos")
public class Insumo {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull(message = "El nombre es obligatorio")
    @Size(max = 100, message = "El nombre no puede exceder los 100 caracteres")
    @Column(nullable = false, length = 100)
    private String nombre;

    @NotNull(message = "El tipo es obligatorio")
    @Column(nullable = false, length = 100)
    private String tipo;

    @NotBlank(message = "La unidad de medida es obligatoria")
    @Pattern(regexp = "^(litros|kilogramos|piezas)$", message = "La unidad debe ser: litros, kilogramos o piezas")
    @Column(name = "unidad_medida", nullable = false, length = 20)
    private String unidadMedida;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "proveedor_id")
    private Proveedor proveedor;

    @NotNull(message = "El precio es obligatorio")
    @Min(value = 0, message = "El precio no puede ser negativo")
    @Column(nullable = false)
    private Double precio;

    // ── NUEVO: relación con sucursal ──
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "sucursal_id", nullable = false)
    private Sucursal sucursal;

    public Insumo() {}

    public Insumo(String nombre, String tipo, String unidadMedida, Proveedor proveedor, Double precio) {
        this.nombre = nombre;
        this.tipo = tipo;
        this.unidadMedida = unidadMedida;
        this.proveedor = proveedor;
        this.precio = precio;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }
    public String getTipo() { return tipo; }
    public void setTipo(String tipo) { this.tipo = tipo; }
    public String getUnidadMedida() { return unidadMedida; }
    public void setUnidadMedida(String unidadMedida) { this.unidadMedida = unidadMedida; }
    public Proveedor getProveedor() { return proveedor; }
    public void setProveedor(Proveedor proveedor) { this.proveedor = proveedor; }
    public Double getPrecio() { return precio; }
    public void setPrecio(Double precio) { this.precio = precio; }

    // ── NUEVO getter/setter ──
    public Sucursal getSucursal() { return sucursal; }
    public void setSucursal(Sucursal sucursal) { this.sucursal = sucursal; }

    public String getProveedorNombre() {
        return proveedor != null ? proveedor.getNombreEmpresa() : null;
    }
}
