package com.utvt.ApiSpringCafeSoft.dto;

import jakarta.validation.constraints.*;
import java.time.LocalDate;

public class LoteDTO {

    private Long id;

    @NotNull(message = "El insumo es obligatorio")
    private Long insumoId;

    private String insumoNombre;
    private String insumoUnidad;

    private Long proveedorId;       // nuevo
    private String proveedorNombre; // nuevo

    @NotNull(message = "La cantidad es obligatoria")
    @DecimalMin(value = "0.001")
    private Double cantidad;

    @NotNull(message = "La fecha de caducidad es obligatoria")
    private LocalDate fechaCaducidad;

    private LocalDate fechaEntrada;

    @Size(max = 255)
    private String observaciones;

    public LoteDTO() {}

    public LoteDTO(Long id, Long insumoId, String insumoNombre, String insumoUnidad,
                   Long proveedorId, String proveedorNombre,
                   Double cantidad, LocalDate fechaCaducidad, LocalDate fechaEntrada, String observaciones) {
        this.id = id;
        this.insumoId = insumoId;
        this.insumoNombre = insumoNombre;
        this.insumoUnidad = insumoUnidad;
        this.proveedorId = proveedorId;
        this.proveedorNombre = proveedorNombre;
        this.cantidad = cantidad;
        this.fechaCaducidad = fechaCaducidad;
        this.fechaEntrada = fechaEntrada;
        this.observaciones = observaciones;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Long getInsumoId() { return insumoId; }
    public void setInsumoId(Long insumoId) { this.insumoId = insumoId; }
    public String getInsumoNombre() { return insumoNombre; }
    public void setInsumoNombre(String insumoNombre) { this.insumoNombre = insumoNombre; }
    public String getInsumoUnidad() { return insumoUnidad; }
    public void setInsumoUnidad(String insumoUnidad) { this.insumoUnidad = insumoUnidad; }
    public Long getProveedorId() { return proveedorId; }
    public void setProveedorId(Long proveedorId) { this.proveedorId = proveedorId; }
    public String getProveedorNombre() { return proveedorNombre; }
    public void setProveedorNombre(String proveedorNombre) { this.proveedorNombre = proveedorNombre; }
    public Double getCantidad() { return cantidad; }
    public void setCantidad(Double cantidad) { this.cantidad = cantidad; }
    public LocalDate getFechaCaducidad() { return fechaCaducidad; }
    public void setFechaCaducidad(LocalDate fechaCaducidad) { this.fechaCaducidad = fechaCaducidad; }
    public LocalDate getFechaEntrada() { return fechaEntrada; }
    public void setFechaEntrada(LocalDate fechaEntrada) { this.fechaEntrada = fechaEntrada; }
    public String getObservaciones() { return observaciones; }
    public void setObservaciones(String observaciones) { this.observaciones = observaciones; }
}