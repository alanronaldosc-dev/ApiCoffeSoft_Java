package com.utvt.ApiSpringCafeSoft.dto;

public class DashboardStockDTO {

    private Double llenosEnPlanta;
    private Double enTransito;
    private Double vaciosEnPlanta;
    private Double stockMinimo;
    private boolean alertaStockBajo;

    public DashboardStockDTO() {
    }

    public DashboardStockDTO(
            Double llenosEnPlanta,
            Double enTransito,
            Double vaciosEnPlanta,
            Double stockMinimo,
            boolean alertaStockBajo) {

        this.llenosEnPlanta = llenosEnPlanta;
        this.enTransito = enTransito;
        this.vaciosEnPlanta = vaciosEnPlanta;
        this.stockMinimo = stockMinimo;
        this.alertaStockBajo = alertaStockBajo;
    }

    public Double getLlenosEnPlanta() {
        return llenosEnPlanta;
    }

    public void setLlenosEnPlanta(Double llenosEnPlanta) {
        this.llenosEnPlanta = llenosEnPlanta;
    }

    public Double getEnTransito() {
        return enTransito;
    }

    public void setEnTransito(Double enTransito) {
        this.enTransito = enTransito;
    }

    public Double getVaciosEnPlanta() {
        return vaciosEnPlanta;
    }

    public void setVaciosEnPlanta(Double vaciosEnPlanta) {
        this.vaciosEnPlanta = vaciosEnPlanta;
    }

    public Double getStockMinimo() {
        return stockMinimo;
    }

    public void setStockMinimo(Double stockMinimo) {
        this.stockMinimo = stockMinimo;
    }

    public boolean isAlertaStockBajo() {
        return alertaStockBajo;
    }

    public void setAlertaStockBajo(boolean alertaStockBajo) {
        this.alertaStockBajo = alertaStockBajo;
    }
}