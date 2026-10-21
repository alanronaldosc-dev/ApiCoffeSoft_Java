package com.utvt.ApiSpringCafeSoft.dto;

import java.time.LocalDate;
import java.time.LocalDateTime;

/** HU-016: resultado de un cierre de turno. */
public class LiquidacionDTO {
    private Long id;
    private Long repartidorId;
    private String repartidorNombre;
    private LocalDate fechaOperacion;
    private LocalDateTime fechaCierre;
    private Double cargaInicial;
    private Double garrafonesEntregados;
    private Double garrafonesNoVendidosDevueltos;
    private Double totalEfectivo;
    private Double totalTransferencias;
    private Double efectivoEntregado;
    private Double diferenciaGarrafones;
    private Double diferenciaEfectivo;
    private String estado;
    private String observaciones;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Long getRepartidorId() { return repartidorId; }
    public void setRepartidorId(Long repartidorId) { this.repartidorId = repartidorId; }
    public String getRepartidorNombre() { return repartidorNombre; }
    public void setRepartidorNombre(String repartidorNombre) { this.repartidorNombre = repartidorNombre; }
    public LocalDate getFechaOperacion() { return fechaOperacion; }
    public void setFechaOperacion(LocalDate fechaOperacion) { this.fechaOperacion = fechaOperacion; }
    public LocalDateTime getFechaCierre() { return fechaCierre; }
    public void setFechaCierre(LocalDateTime fechaCierre) { this.fechaCierre = fechaCierre; }
    public Double getCargaInicial() { return cargaInicial; }
    public void setCargaInicial(Double cargaInicial) { this.cargaInicial = cargaInicial; }
    public Double getGarrafonesEntregados() { return garrafonesEntregados; }
    public void setGarrafonesEntregados(Double garrafonesEntregados) { this.garrafonesEntregados = garrafonesEntregados; }
    public Double getGarrafonesNoVendidosDevueltos() { return garrafonesNoVendidosDevueltos; }
    public void setGarrafonesNoVendidosDevueltos(Double value) { this.garrafonesNoVendidosDevueltos = value; }
    public Double getTotalEfectivo() { return totalEfectivo; }
    public void setTotalEfectivo(Double totalEfectivo) { this.totalEfectivo = totalEfectivo; }
    public Double getTotalTransferencias() { return totalTransferencias; }
    public void setTotalTransferencias(Double totalTransferencias) { this.totalTransferencias = totalTransferencias; }
    public Double getEfectivoEntregado() { return efectivoEntregado; }
    public void setEfectivoEntregado(Double efectivoEntregado) { this.efectivoEntregado = efectivoEntregado; }
    public Double getDiferenciaGarrafones() { return diferenciaGarrafones; }
    public void setDiferenciaGarrafones(Double diferenciaGarrafones) { this.diferenciaGarrafones = diferenciaGarrafones; }
    public Double getDiferenciaEfectivo() { return diferenciaEfectivo; }
    public void setDiferenciaEfectivo(Double diferenciaEfectivo) { this.diferenciaEfectivo = diferenciaEfectivo; }
    public String getEstado() { return estado; }
    public void setEstado(String estado) { this.estado = estado; }
    public String getObservaciones() { return observaciones; }
    public void setObservaciones(String observaciones) { this.observaciones = observaciones; }
}
