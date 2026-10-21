package com.utvt.ApiSpringCafeSoft.dto;

import java.time.LocalDateTime;

public class DetalleEntregaReporteDTO {

    private Long entregaId;
    private Long ventaId;

    private Long repartidorId;
    private String repartidorNombre;

    private LocalDateTime fecha;

    private Double garrafonesVendidos;
    private Double envasesRetornados;
    private Double montoCobrado;

    private String metodoCobro;
    private String resultado;

    public DetalleEntregaReporteDTO() {
    }

    public Long getEntregaId() {
        return entregaId;
    }

    public void setEntregaId(Long entregaId) {
        this.entregaId = entregaId;
    }

    public Long getVentaId() {
        return ventaId;
    }

    public void setVentaId(Long ventaId) {
        this.ventaId = ventaId;
    }

    public Long getRepartidorId() {
        return repartidorId;
    }

    public void setRepartidorId(Long repartidorId) {
        this.repartidorId = repartidorId;
    }

    public String getRepartidorNombre() {
        return repartidorNombre;
    }

    public void setRepartidorNombre(String repartidorNombre) {
        this.repartidorNombre = repartidorNombre;
    }

    public LocalDateTime getFecha() {
        return fecha;
    }

    public void setFecha(LocalDateTime fecha) {
        this.fecha = fecha;
    }

    public Double getGarrafonesVendidos() {
        return garrafonesVendidos;
    }

    public void setGarrafonesVendidos(Double garrafonesVendidos) {
        this.garrafonesVendidos = garrafonesVendidos;
    }

    public Double getEnvasesRetornados() {
        return envasesRetornados;
    }

    public void setEnvasesRetornados(Double envasesRetornados) {
        this.envasesRetornados = envasesRetornados;
    }

    public Double getMontoCobrado() {
        return montoCobrado;
    }

    public void setMontoCobrado(Double montoCobrado) {
        this.montoCobrado = montoCobrado;
    }

    public String getMetodoCobro() {
        return metodoCobro;
    }

    public void setMetodoCobro(String metodoCobro) {
        this.metodoCobro = metodoCobro;
    }

    public String getResultado() {
        return resultado;
    }

    public void setResultado(String resultado) {
        this.resultado = resultado;
    }
}