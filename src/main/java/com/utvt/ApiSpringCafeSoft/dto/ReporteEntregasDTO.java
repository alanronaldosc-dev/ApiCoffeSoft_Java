package com.utvt.ApiSpringCafeSoft.dto;

import java.time.LocalDate;
import java.util.List;

public class ReporteEntregasDTO {

    private LocalDate fechaInicio;
    private LocalDate fechaFin;

    private Long repartidorId;
    private String repartidorNombre;

    private Integer totalEntregas;
    private Double totalGarrafonesVendidos;
    private Double totalEfectivoCobrado;
    private Double totalEnvasesRetornados;

    private Double promedioTiempoEntregaMinutos;

    private List<DetalleEntregaReporteDTO> entregas;

    public ReporteEntregasDTO() {
    }

    public LocalDate getFechaInicio() {
        return fechaInicio;
    }

    public void setFechaInicio(LocalDate fechaInicio) {
        this.fechaInicio = fechaInicio;
    }

    public LocalDate getFechaFin() {
        return fechaFin;
    }

    public void setFechaFin(LocalDate fechaFin) {
        this.fechaFin = fechaFin;
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

    public Integer getTotalEntregas() {
        return totalEntregas;
    }

    public void setTotalEntregas(Integer totalEntregas) {
        this.totalEntregas = totalEntregas;
    }

    public Double getTotalGarrafonesVendidos() {
        return totalGarrafonesVendidos;
    }

    public void setTotalGarrafonesVendidos(Double totalGarrafonesVendidos) {
        this.totalGarrafonesVendidos = totalGarrafonesVendidos;
    }

    public Double getTotalEfectivoCobrado() {
        return totalEfectivoCobrado;
    }

    public void setTotalEfectivoCobrado(Double totalEfectivoCobrado) {
        this.totalEfectivoCobrado = totalEfectivoCobrado;
    }

    public Double getTotalEnvasesRetornados() {
        return totalEnvasesRetornados;
    }

    public void setTotalEnvasesRetornados(Double totalEnvasesRetornados) {
        this.totalEnvasesRetornados = totalEnvasesRetornados;
    }

    public Double getPromedioTiempoEntregaMinutos() {
        return promedioTiempoEntregaMinutos;
    }

    public void setPromedioTiempoEntregaMinutos(Double promedioTiempoEntregaMinutos) {
        this.promedioTiempoEntregaMinutos = promedioTiempoEntregaMinutos;
    }

    public List<DetalleEntregaReporteDTO> getEntregas() {
        return entregas;
    }

    public void setEntregas(List<DetalleEntregaReporteDTO> entregas) {
        this.entregas = entregas;
    }
}