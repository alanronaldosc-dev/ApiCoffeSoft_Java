package com.utvt.ApiSpringCafeSoft.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;

@Entity
@Table(name = "rutas")
public class Ruta {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_ruta")
    private Long id;

    @NotBlank(message = "El nombre de la ruta es obligatorio")
    @Column(nullable = false)
    private String nombre;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "repartidor_id")
    private Usuario repartidor;

    // Días en que se hace la ruta, ej. "Lunes,Jueves"
    @Column(name = "dias_reparto")
    private String diasReparto;

    // Estado: CREADA / ACTIVA
    private String estado = "CREADA";

    private Boolean activa = false;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "carga_id")
    private Carga carga;

    public Ruta() {}

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }
    public Usuario getRepartidor() { return repartidor; }
    public void setRepartidor(Usuario repartidor) { this.repartidor = repartidor; }
    public String getDiasReparto() { return diasReparto; }
    public void setDiasReparto(String diasReparto) { this.diasReparto = diasReparto; }
    public String getEstado() { return estado; }
    public void setEstado(String estado) { this.estado = estado; }
    public Boolean getActiva() { return activa; }
    public void setActiva(Boolean activa) { this.activa = activa; }
    public Carga getCarga() { return carga; }
    public void setCarga(Carga carga) { this.carga = carga; }
}
