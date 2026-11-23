package com.utvt.ApiSpringCafeSoft.dto;

import java.time.LocalDateTime;

public class RutaAsignadaDTO {

    private Long id;
    private String nombre;
    private String diasReparto;
    private LocalDateTime fechaAsignacion;

    public RutaAsignadaDTO() {
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

    public String getDiasReparto() {
        return diasReparto;
    }

    public void setDiasReparto(String diasReparto) {
        this.diasReparto = diasReparto;
    }

    public LocalDateTime getFechaAsignacion() {
        return fechaAsignacion;
    }

    public void setFechaAsignacion(LocalDateTime fechaAsignacion) {
        this.fechaAsignacion = fechaAsignacion;
    }
}
