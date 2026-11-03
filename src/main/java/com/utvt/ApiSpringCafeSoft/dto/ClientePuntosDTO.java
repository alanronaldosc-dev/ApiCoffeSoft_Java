package com.utvt.ApiSpringCafeSoft.dto;

public class ClientePuntosDTO {

    private Long id;
    private String nombre;
    private Boolean activo;
    private Integer saldoPuntos;

    public ClientePuntosDTO(Long id, String nombre, Boolean activo, Integer saldoPuntos) {
        this.id = id;
        this.nombre = nombre;
        this.activo = activo;
        this.saldoPuntos = saldoPuntos;
    }

    public Long getId() {
        return id;
    }

    public String getNombre() {
        return nombre;
    }

    public Boolean getActivo() {
        return activo;
    }

    public Integer getSaldoPuntos() {
        return saldoPuntos;
    }
}
