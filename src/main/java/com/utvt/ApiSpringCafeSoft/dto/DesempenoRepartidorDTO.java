package com.utvt.ApiSpringCafeSoft.dto;

/**
 * Resultado agregado del desempeño de un repartidor.
 * La consulta agrupa entregas confirmadas por repartidor.
 */
public class DesempenoRepartidorDTO {

    private Long repartidorId;
    private String nombre;
    private Long cantidadVentas;
    private Double montoTotal;
    private Integer posicion;

    public DesempenoRepartidorDTO() {
    }

    /** Constructor utilizado por la proyección JPQL del repositorio. */
    public DesempenoRepartidorDTO(
            Long repartidorId,
            String nombre,
            Long cantidadVentas,
            Double montoTotal) {
        this.repartidorId = repartidorId;
        this.nombre = nombre;
        this.cantidadVentas = cantidadVentas;
        this.montoTotal = montoTotal;
    }

    public Long getRepartidorId() {
        return repartidorId;
    }

    public void setRepartidorId(Long repartidorId) {
        this.repartidorId = repartidorId;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public Long getCantidadVentas() {
        return cantidadVentas;
    }

    public void setCantidadVentas(Long cantidadVentas) {
        this.cantidadVentas = cantidadVentas;
    }

    public Double getMontoTotal() {
        return montoTotal;
    }

    public void setMontoTotal(Double montoTotal) {
        this.montoTotal = montoTotal;
    }

    public Integer getPosicion() {
        return posicion;
    }

    public void setPosicion(Integer posicion) {
        this.posicion = posicion;
    }
}
