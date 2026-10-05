package com.utvt.ApiSpringCafeSoft.dto;

import java.time.LocalDate;

/** HU-016: resumen previo al cierre de turno. */
public class ResumenLiquidacionDTO {
    private Long repartidorId;
    private String repartidorNombre;
    private LocalDate fecha;
    private Double cargaInicial;
    private Double garrafonesEntregados;
    private Double garrafonesPendientes;
    private Double envasesVaciosRecibidos;
    private Double totalEfectivo;
    private Double totalTransferencias;
    private Double totalCobrado;
    private Integer entregasRealizadas;

    public Long getRepartidorId() { return repartidorId; }
    public void setRepartidorId(Long repartidorId) { this.repartidorId = repartidorId; }
    public String getRepartidorNombre() { return repartidorNombre; }
    public void setRepartidorNombre(String repartidorNombre) { this.repartidorNombre = repartidorNombre; }
    public LocalDate getFecha() { return fecha; }
    public void setFecha(LocalDate fecha) { this.fecha = fecha; }
    public Double getCargaInicial() { return cargaInicial; }
    public void setCargaInicial(Double cargaInicial) { this.cargaInicial = cargaInicial; }
    public Double getGarrafonesEntregados() { return garrafonesEntregados; }
    public void setGarrafonesEntregados(Double garrafonesEntregados) { this.garrafonesEntregados = garrafonesEntregados; }
    public Double getGarrafonesPendientes() { return garrafonesPendientes; }
    public void setGarrafonesPendientes(Double garrafonesPendientes) { this.garrafonesPendientes = garrafonesPendientes; }
    public Double getEnvasesVaciosRecibidos() { return envasesVaciosRecibidos; }
    public void setEnvasesVaciosRecibidos(Double envasesVaciosRecibidos) { this.envasesVaciosRecibidos = envasesVaciosRecibidos; }
    public Double getTotalEfectivo() { return totalEfectivo; }
    public void setTotalEfectivo(Double totalEfectivo) { this.totalEfectivo = totalEfectivo; }
    public Double getTotalTransferencias() { return totalTransferencias; }
    public void setTotalTransferencias(Double totalTransferencias) { this.totalTransferencias = totalTransferencias; }
    public Double getTotalCobrado() { return totalCobrado; }
    public void setTotalCobrado(Double totalCobrado) { this.totalCobrado = totalCobrado; }
    public Integer getEntregasRealizadas() { return entregasRealizadas; }
    public void setEntregasRealizadas(Integer entregasRealizadas) { this.entregasRealizadas = entregasRealizadas; }
}
