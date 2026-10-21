package com.utvt.ApiSpringCafeSoft.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

@Entity
@Table(name = "clientes")
public class Cliente {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_cliente")
    private Long id;

    @NotBlank(message = "El nombre es obligatorio")
    @Column(nullable = false)
    private String nombre;

    @NotBlank(message = "El domicilio es obligatorio")
    @Column(nullable = false)
    private String domicilio;

    @Column(name = "link_google_maps")
    private String linkGoogleMaps;

    // Días fijos de reparto, ej. "Lunes,Jueves"
    @Column(name = "dias_reparto")
    private String diasReparto;

    // Frecuencia: diaria, semanal, quincenal, etc.
    private String frecuencia;

    @NotNull(message = "El precio por garrafón es obligatorio")
    @Min(value = 0, message = "El precio no puede ser negativo")
    @Column(name = "precio_por_garrafon", nullable = false)
    private Double precioPorGarrafon;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "garrafon_preferencia_id")
    private Producto garrafonPreferencia;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "ruta_id")
    private Ruta ruta;

    // Orden de entrega dentro de la ruta
    @Column(name = "orden_en_ruta")
    private Integer ordenEnRuta;

    @Column(name = "fotografia_domicilio", columnDefinition = "bytea")
    private byte[] fotografiaDomicilio;

    @Column(nullable = false)
    private Boolean activo = true;

    public Cliente() {
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

    public String getDomicilio() {
        return domicilio;
    }

    public void setDomicilio(String domicilio) {
        this.domicilio = domicilio;
    }

    public String getLinkGoogleMaps() {
        return linkGoogleMaps;
    }

    public void setLinkGoogleMaps(String linkGoogleMaps) {
        this.linkGoogleMaps = linkGoogleMaps;
    }

    public String getDiasReparto() {
        return diasReparto;
    }

    public void setDiasReparto(String diasReparto) {
        this.diasReparto = diasReparto;
    }

    public String getFrecuencia() {
        return frecuencia;
    }

    public void setFrecuencia(String frecuencia) {
        this.frecuencia = frecuencia;
    }

    public Double getPrecioPorGarrafon() {
        return precioPorGarrafon;
    }

    public void setPrecioPorGarrafon(Double precioPorGarrafon) {
        this.precioPorGarrafon = precioPorGarrafon;
    }

    public Producto getGarrafonPreferencia() {
        return garrafonPreferencia;
    }

    public void setGarrafonPreferencia(Producto garrafonPreferencia) {
        this.garrafonPreferencia = garrafonPreferencia;
    }

    public Ruta getRuta() {
        return ruta;
    }

    public void setRuta(Ruta ruta) {
        this.ruta = ruta;
    }

    public Integer getOrdenEnRuta() {
        return ordenEnRuta;
    }

    public void setOrdenEnRuta(Integer ordenEnRuta) {
        this.ordenEnRuta = ordenEnRuta;
    }

    public byte[] getFotografiaDomicilio() {
        return fotografiaDomicilio;
    }

    public void setFotografiaDomicilio(byte[] fotografiaDomicilio) {
        this.fotografiaDomicilio = fotografiaDomicilio;
    }

    public Boolean getActivo() {
        return activo;
    }

    public void setActivo(Boolean activo) {
        this.activo = activo;
    }
}
