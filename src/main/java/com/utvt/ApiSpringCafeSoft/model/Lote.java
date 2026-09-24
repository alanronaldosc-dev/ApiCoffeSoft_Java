package com.utvt.ApiSpringCafeSoft.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import java.time.LocalDate;

@Entity
@Table(name = "lotes")
public class Lote {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // nullable: puede ser null cuando el lote es de producción de producto
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "insumo_id", nullable = true)
    private Insumo insumo;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "proveedor_id")
    private Proveedor proveedor;

    @NotNull(message = "La cantidad es obligatoria")
    @DecimalMin(value = "0.001", message = "La cantidad debe ser mayor a 0")
    @Column(nullable = false)
    private Double cantidad;

    @NotNull(message = "La fecha de caducidad es obligatoria")
    @Column(name = "fecha_caducidad", nullable = false)
    private LocalDate fechaCaducidad;

    @Column(name = "fecha_entrada", nullable = false)
    private LocalDate fechaEntrada = LocalDate.now();

    @Size(max = 255)
    @Column(length = 255)
    private String observaciones;

    /** Referencia al producto cuando el lote es de producción */
    @Column(name = "producto_id")
    private Long productoId;

    /** Nombre del producto producido (desnormalizado para consultas fáciles) */
    @Column(name = "producto_nombre", length = 100)
    private String productoNombre;

    /** Tipo de lote: "insumo" o "produccion" */
    @Column(name = "tipo_lote", length = 20, nullable = true)
    private String tipoLote = "insumo";

    public Lote() {}

    // Constructor para lotes de insumo (existente)
    public Lote(Insumo insumo, Proveedor proveedor, Double cantidad,
                LocalDate fechaCaducidad, String observaciones) {
        this.insumo = insumo;
        this.proveedor = proveedor;
        this.cantidad = cantidad;
        this.fechaCaducidad = fechaCaducidad;
        this.observaciones = observaciones;
        this.fechaEntrada = LocalDate.now();
        this.tipoLote = "insumo";
    }

    // Constructor para lotes de producción (nuevo)
    public Lote(Long productoId, String productoNombre, Double cantidad,
                LocalDate fechaCaducidad, String observaciones) {
        this.productoId = productoId;
        this.productoNombre = productoNombre;
        this.cantidad = cantidad;
        this.fechaCaducidad = fechaCaducidad;
        this.observaciones = observaciones;
        this.fechaEntrada = LocalDate.now();
        this.tipoLote = "produccion";
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Insumo getInsumo() { return insumo; }
    public void setInsumo(Insumo insumo) { this.insumo = insumo; }
    public Proveedor getProveedor() { return proveedor; }
    public void setProveedor(Proveedor proveedor) { this.proveedor = proveedor; }
    public Double getCantidad() { return cantidad; }
    public void setCantidad(Double cantidad) { this.cantidad = cantidad; }
    public LocalDate getFechaCaducidad() { return fechaCaducidad; }
    public void setFechaCaducidad(LocalDate fechaCaducidad) { this.fechaCaducidad = fechaCaducidad; }
    public LocalDate getFechaEntrada() { return fechaEntrada; }
    public void setFechaEntrada(LocalDate fechaEntrada) { this.fechaEntrada = fechaEntrada; }
    public String getObservaciones() { return observaciones; }
    public void setObservaciones(String observaciones) { this.observaciones = observaciones; }
    public Long getProductoId() { return productoId; }
    public void setProductoId(Long productoId) { this.productoId = productoId; }
    public String getProductoNombre() { return productoNombre; }
    public void setProductoNombre(String productoNombre) { this.productoNombre = productoNombre; }
    public String getTipoLote() { return tipoLote; }
    public void setTipoLote(String tipoLote) { this.tipoLote = tipoLote; }
}
