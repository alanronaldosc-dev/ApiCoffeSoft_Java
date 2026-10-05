package com.utvt.ApiSpringCafeSoft.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "mermas_garrafon")
public class MermaGarrafon {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Carga sobre la cual ocurrió la merma.
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "carga_id", nullable = false)
    private Carga carga;

    // Repartidor que tenía los garrafones asignados.
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "repartidor_id", nullable = false)
    private Usuario repartidor;

    // Inventario/tipo de garrafón relacionado.
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "inventario_id", nullable = false)
    private Inventario inventario;

    @Column(nullable = false)
    private Double cantidad;

    @Column(nullable = false, length = 50)
    private String causa;

    @Column(length = 255)
    private String observaciones;

    @Column(name = "fecha_hora", nullable = false)
    private LocalDateTime fechaHora;

    public MermaGarrafon() {
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Carga getCarga() {
        return carga;
    }

    public void setCarga(Carga carga) {
        this.carga = carga;
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

    public String getCausa() {
        return causa;
    }

    public void setCausa(String causa) {
        this.causa = causa;
    }

    public String getObservaciones() {
        return observaciones;
    }

    public void setObservaciones(String observaciones) {
        this.observaciones = observaciones;
    }

    public LocalDateTime getFechaHora() {
        return fechaHora;
    }

    public void setFechaHora(LocalDateTime fechaHora) {
        this.fechaHora = fechaHora;
    }
}