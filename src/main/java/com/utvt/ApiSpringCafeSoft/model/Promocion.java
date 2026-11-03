package com.utvt.ApiSpringCafeSoft.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "promociones")
public class Promocion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank
    @Size(max = 100)
    @Column(nullable = false, length = 100)
    private String nombre;

    @Size(max = 500)
    @Column(length = 500)
    private String descripcion;

    @NotNull
    @DecimalMin("0.01")
    @DecimalMax("100.00")
    @Digits(integer = 3, fraction = 2)
    @Column(name = "descuento_porcentaje", nullable = false, precision = 5, scale = 2)
    private BigDecimal descuentoPorcentaje;

    @NotNull
    @Column(name = "fecha_inicio", nullable = false)
    private LocalDateTime fechaInicio;

    @NotNull
    @Column(name = "fecha_fin", nullable = false)
    private LocalDateTime fechaFin;

    @NotNull
    @Min(0)
    @Column(name = "min_garrafones", nullable = false)
    private Integer minGarrafones = 0;

    @NotNull
    @Column(name = "acumula_puntos", nullable = false)
    private Boolean acumulaPuntos = true;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = "segmento_objetivo", nullable = false, length = 32)
    private SegmentoCliente segmentoObjetivo = SegmentoCliente.TODOS;

    @NotNull
    @Column(nullable = false)
    private Boolean activa = true;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @OneToMany(mappedBy = "promocion")
    private List<Venta> ventas = new ArrayList<>();

    @PrePersist
    protected void onCreate() {
        if (createdAt == null) {
            createdAt = LocalDateTime.now(ZoneId.systemDefault());
        }
    }

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

    public String getDescripcion() {
        return descripcion;
    }

    public void setDescripcion(String descripcion) {
        this.descripcion = descripcion;
    }

    public BigDecimal getDescuentoPorcentaje() {
        return descuentoPorcentaje;
    }

    public void setDescuentoPorcentaje(BigDecimal descuentoPorcentaje) {
        this.descuentoPorcentaje = descuentoPorcentaje;
    }

    public LocalDateTime getFechaInicio() {
        return fechaInicio;
    }

    public void setFechaInicio(LocalDateTime fechaInicio) {
        this.fechaInicio = fechaInicio;
    }

    public LocalDateTime getFechaFin() {
        return fechaFin;
    }

    public void setFechaFin(LocalDateTime fechaFin) {
        this.fechaFin = fechaFin;
    }

    public Integer getMinGarrafones() {
        return minGarrafones;
    }

    public void setMinGarrafones(Integer minGarrafones) {
        this.minGarrafones = minGarrafones;
    }

    public Boolean getAcumulaPuntos() {
        return acumulaPuntos;
    }

    public void setAcumulaPuntos(Boolean acumulaPuntos) {
        this.acumulaPuntos = acumulaPuntos;
    }

    public SegmentoCliente getSegmentoObjetivo() {
        return segmentoObjetivo;
    }

    public void setSegmentoObjetivo(SegmentoCliente segmentoObjetivo) {
        this.segmentoObjetivo = segmentoObjetivo;
    }

    public Boolean getActiva() {
        return activa;
    }

    public void setActiva(Boolean activa) {
        this.activa = activa;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}
