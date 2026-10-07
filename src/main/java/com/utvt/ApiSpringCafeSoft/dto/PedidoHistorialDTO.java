package com.utvt.ApiSpringCafeSoft.dto;

import java.time.LocalDateTime;

public class PedidoHistorialDTO {

    private Long entregaId;
    private Long ventaId;
    private LocalDateTime fecha;
    private Double garrafones;
    private Double montoCobrado;
    private String metodoCobro;
    private String resultado;

    public PedidoHistorialDTO() {}

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

    public LocalDateTime getFecha() {
        return fecha;
    }

    public void setFecha(LocalDateTime fecha) {
        this.fecha = fecha;
    }

    public Double getGarrafones() {
        return garrafones;
    }

    public void setGarrafones(Double garrafones) {
        this.garrafones = garrafones;
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