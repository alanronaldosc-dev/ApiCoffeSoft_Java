package com.utvt.ApiSpringCafeSoft.dto;

import java.util.ArrayList;
import java.util.List;

/** Respuesta del dashboard de desempeño de repartidores. */
public class DesempenoRepartidoresDTO {

    private Long sucursalId;
    private String sucursalNombre;
    private Long totalVentas;
    private Double montoTotal;
    private List<DesempenoRepartidorDTO> rankingVentas = new ArrayList<>();
    private List<DesempenoRepartidorDTO> rankingMonto = new ArrayList<>();

    public Long getSucursalId() {
        return sucursalId;
    }

    public void setSucursalId(Long sucursalId) {
        this.sucursalId = sucursalId;
    }

    public String getSucursalNombre() {
        return sucursalNombre;
    }

    public void setSucursalNombre(String sucursalNombre) {
        this.sucursalNombre = sucursalNombre;
    }

    public Long getTotalVentas() {
        return totalVentas;
    }

    public void setTotalVentas(Long totalVentas) {
        this.totalVentas = totalVentas;
    }

    public Double getMontoTotal() {
        return montoTotal;
    }

    public void setMontoTotal(Double montoTotal) {
        this.montoTotal = montoTotal;
    }

    public List<DesempenoRepartidorDTO> getRankingVentas() {
        return rankingVentas;
    }

    public void setRankingVentas(List<DesempenoRepartidorDTO> rankingVentas) {
        this.rankingVentas = rankingVentas;
    }

    public List<DesempenoRepartidorDTO> getRankingMonto() {
        return rankingMonto;
    }

    public void setRankingMonto(List<DesempenoRepartidorDTO> rankingMonto) {
        this.rankingMonto = rankingMonto;
    }
}
