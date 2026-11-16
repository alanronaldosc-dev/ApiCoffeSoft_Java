
package com.utvt.ApiSpringCafeSoft.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "promociones")
public class Promocion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "El nombre es obligatorio")
    @Size(max = 100, message = "El nombre no puede exceder los 100 caracteres")
    @Column(nullable = false, length = 100)
    private String nombre;

    @Size(max = 255)
    @Column
    private String descripcion;

    @NotNull(message = "El descuento es obligatorio")
    @DecimalMin(value = "0.01", message = "El descuento debe ser mayor a 0")
    @DecimalMax(value = "100.0", message = "El descuento no puede superar el 100%")
    @Column(nullable = false)
    private Double descuentoPorcentaje;

    @NotNull(message = "La fecha de inicio es obligatoria")
    @Column(name = "fecha_inicio", nullable = false)
    private LocalDateTime fechaInicio;

    @NotNull(message = "La fecha de fin es obligatoria")
    @Column(name = "fecha_fin", nullable = false)
    private LocalDateTime fechaFin;

    @Column(nullable = false)
    private Boolean activa = true;

    @Column(name = "minimo_garrafones")
    private Integer minGarrafones = 0;

    @Column(name = "acumula_puntos")
    private Boolean acumulaPuntos = true;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "sucursal_id", nullable = false)
    private Sucursal sucursal;

    // HU-040: fecha y hora de desactivación.
    @Column(name = "fecha_desactivacion")
    private LocalDateTime fechaDesactivacion;

    // HU-040: administrador que desactivó la promoción.
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "desactivada_por_usuario_id")
    private Usuario desactivadaPor;

    public Promocion() {}

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

    public Double getDescuentoPorcentaje() {
        return descuentoPorcentaje;
    }

    public void setDescuentoPorcentaje(Double descuentoPorcentaje) {
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

    public Boolean getActiva() {
        return activa;
    }

    public void setActiva(Boolean activa) {
        this.activa = activa;
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

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public Sucursal getSucursal() {
        return sucursal;
    }

    public void setSucursal(Sucursal sucursal) {
        this.sucursal = sucursal;
    }

    public LocalDateTime getFechaDesactivacion() {
        return fechaDesactivacion;
    }

    public void setFechaDesactivacion(LocalDateTime fechaDesactivacion) {
        this.fechaDesactivacion = fechaDesactivacion;
    }

    public Usuario getDesactivadaPor() {
        return desactivadaPor;
    }

    public void setDesactivadaPor(Usuario desactivadaPor) {
        this.desactivadaPor = desactivadaPor;
    }

    @Transient
    public boolean isVigente() {
        if (!Boolean.TRUE.equals(activa)) {
            return false;
        }

        if (fechaInicio == null || fechaFin == null) {
            return false;
        }

        LocalDateTime ahora = LocalDateTime.now();

        return !ahora.isBefore(fechaInicio)
                && !ahora.isAfter(fechaFin);
    }
}
