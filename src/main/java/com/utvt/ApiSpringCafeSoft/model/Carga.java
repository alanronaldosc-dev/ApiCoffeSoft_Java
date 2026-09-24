package com.utvt.ApiSpringCafeSoft.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDateTime;

@Entity
@Table(name = "cargas")
public class Carga {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * Repartidor responsable de la carga.
     * userTipo = 4 corresponde a Repartidor.
     */
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "repartidor_id", nullable = false)
    private Usuario repartidor;

    /**
     * Registro de inventario del cual se toman los garrafones.
     */
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "inventario_id", nullable = false)
    private Inventario inventario;

    /**
     * Cantidad de garrafones asignados.
     */
    @NotNull(message = "La cantidad es obligatoria")
    @DecimalMin(value = "0.01", message = "La cantidad debe ser mayor a 0")
    @Column(nullable = false)
    private Double cantidad;

    /**
     * Fecha y hora en que se registra la carga.
     */
    @Column(name = "fecha_hora", nullable = false)
    private LocalDateTime fechaHora;

    /**
     * Estados posibles:
     * PENDIENTE
     * CARGA EN TRÁNSITO
     */
    @Column(nullable = false, length = 30)
    private String estado;

    public Carga() {
    }

    public Carga(Usuario repartidor,
                 Inventario inventario,
                 Double cantidad,
                 LocalDateTime fechaHora,
                 String estado) {

        this.repartidor = repartidor;
        this.inventario = inventario;
        this.cantidad = cantidad;
        this.fechaHora = fechaHora;
        this.estado = estado;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Usuario getRepartidor() {
        return repartidor;
    }

    public void setRepartidor(Usuario repartidor) {
        this.repartidor = repartidor;
    }

    public Inventario getInventario() {
        return inventario;
    }

    public void setInventario(Inventario inventario) {
        this.inventario = inventario;
    }

    public Double getCantidad() {
        return cantidad;
    }

    public void setCantidad(Double cantidad) {
        this.cantidad = cantidad;
    }

    public LocalDateTime getFechaHora() {
        return fechaHora;
    }

    public void setFechaHora(LocalDateTime fechaHora) {
        this.fechaHora = fechaHora;
    }

    public String getEstado() {
        return estado;
    }

    public void setEstado(String estado) {
        this.estado = estado;
    }
}