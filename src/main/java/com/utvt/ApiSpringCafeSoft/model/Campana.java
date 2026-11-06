package com.utvt.ApiSpringCafeSoft.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import java.time.LocalDateTime;

/**
 * HU-037: Campaña de marketing dirigida a un segmento de clientes.
 * Se asocia a una Promoción existente y se activa/desactiva
 * automáticamente según su periodo.
 *
 * Segmentos válidos: ACTIVO, POTENCIAL, DORMIDO, POCO_FRECUENTE
 */
@Entity
@Table(name = "campanas")
public class Campana {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "El nombre es obligatorio")
    @Size(max = 100)
    @Column(nullable = false, length = 100)
    private String nombre;

    @Size(max = 255)
    @Column
    private String descripcion;

    /**
     * Segmento al que aplica la campaña.
     * Valores válidos: ACTIVO, POTENCIAL, DORMIDO, POCO_FRECUENTE
     */
    @NotBlank(message = "El segmento es obligatorio")
    @Pattern(
        regexp = "^(ACTIVO|POTENCIAL|DORMIDO|POCO_FRECUENTE)$",
        message = "El segmento debe ser: ACTIVO, POTENCIAL, DORMIDO o POCO_FRECUENTE"
    )
    @Column(nullable = false, length = 20)
    private String segmento;

    /**
     * Promoción que se aplica durante la campaña.
     * La campaña hereda el descuento y reglas de la promoción.
     */
    @NotNull(message = "La promoción es obligatoria")
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "promocion_id", nullable = false)
    private Promocion promocion;

    /**
     * Inicio del periodo activo de la campaña.
     */
    @NotNull(message = "La fecha de inicio es obligatoria")
    @Column(name = "fecha_inicio", nullable = false)
    private LocalDateTime fechaInicio;

    /**
     * Fin del periodo activo de la campaña.
     */
    @NotNull(message = "La fecha de fin es obligatoria")
    @Column(name = "fecha_fin", nullable = false)
    private LocalDateTime fechaFin;

    /**
     * Estado de activación. Se gestiona automáticamente:
     * - true: dentro del periodo fechaInicio–fechaFin
     * - false: fuera del periodo o desactivada manualmente
     */
    @Column(nullable = false)
    private Boolean activa = false;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "sucursal_id", nullable = false)
    private Sucursal sucursal;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @PrePersist
    public void prePersist() {
        if (createdAt == null) createdAt = LocalDateTime.now();
    }

    // ── Método de dominio: vigencia calculada en tiempo real ──
    @Transient
    public boolean isVigente() {
        if (!Boolean.TRUE.equals(activa)) return false;
        LocalDateTime ahora = LocalDateTime.now();
        return !ahora.isBefore(fechaInicio) && !ahora.isAfter(fechaFin);
    }

    public Campana() {}

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }
    public String getDescripcion() { return descripcion; }
    public void setDescripcion(String descripcion) { this.descripcion = descripcion; }
    public String getSegmento() { return segmento; }
    public void setSegmento(String segmento) { this.segmento = segmento; }
    public Promocion getPromocion() { return promocion; }
    public void setPromocion(Promocion promocion) { this.promocion = promocion; }
    public LocalDateTime getFechaInicio() { return fechaInicio; }
    public void setFechaInicio(LocalDateTime fechaInicio) { this.fechaInicio = fechaInicio; }
    public LocalDateTime getFechaFin() { return fechaFin; }
    public void setFechaFin(LocalDateTime fechaFin) { this.fechaFin = fechaFin; }
    public Boolean getActiva() { return activa; }
    public void setActiva(Boolean activa) { this.activa = activa; }
    public Sucursal getSucursal() { return sucursal; }
    public void setSucursal(Sucursal sucursal) { this.sucursal = sucursal; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}
