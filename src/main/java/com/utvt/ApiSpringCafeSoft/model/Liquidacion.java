package com.utvt.ApiSpringCafeSoft.model;

import jakarta.persistence.*;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * HU-016: cierre de turno de un repartidor.
 */
@Entity
@Table(name = "liquidaciones")
public class Liquidacion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.EAGER, optional = false)
    @JoinColumn(name = "repartidor_id", nullable = false)
    private Usuario repartidor;

    @Column(name = "fecha_operacion", nullable = false)
    private LocalDate fechaOperacion;

    @Column(name = "fecha_cierre", nullable = false)
    private LocalDateTime fechaCierre;

    @Column(name = "carga_inicial", nullable = false)
    private Double cargaInicial;

    @Column(name = "garrafones_entregados", nullable = false)
    private Double garrafonesEntregados;

    @Column(name = "garrafones_devuelto", nullable = false)
    private Double garrafonesNoVendidosDevueltos;

    @Column(name = "total_efectivo", nullable = false)
    private Double totalEfectivo;

    @Column(name = "total_transferencias", nullable = false)
    private Double totalTransferencias;

    @Column(name = "efectivo_entregado", nullable = false)
    private Double efectivoEntregado;

    @Column(name = "diferencia_garrafones", nullable = false)
    private Double diferenciaGarrafones;

    @Column(name = "diferencia_efectivo", nullable = false)
    private Double diferenciaEfectivo;

    @Column(nullable = false, length = 30)
    private String estado;

    @Column(length = 500)
    private String observaciones;

    public Liquidacion() {}

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Usuario getRepartidor() { return repartidor; }
    public void setRepartidor(Usuario repartidor) { this.repartidor = repartidor; }
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
