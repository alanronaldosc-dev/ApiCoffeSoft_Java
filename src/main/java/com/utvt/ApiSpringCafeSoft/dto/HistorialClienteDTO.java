package com.utvt.ApiSpringCafeSoft.dto;

import java.util.List;

public class HistorialClienteDTO {

    private Long clienteId;
    private String clienteNombre;
    private Integer totalPedidos;
    private Double promedioGarrafones;
    private Long frecuenciaPromedioDias;
    private Long diasDesdeUltimoPedido;
    private List<PedidoHistorialDTO> pedidos;

    public HistorialClienteDTO() {}

    public Long getClienteId() {
        return clienteId;
    }

    public void setClienteId(Long clienteId) {
        this.clienteId = clienteId;
    }

    public String getClienteNombre() {
        return clienteNombre;
    }

    public void setClienteNombre(String clienteNombre) {
        this.clienteNombre = clienteNombre;
    }

    public Integer getTotalPedidos() {
        return totalPedidos;
    }

    public void setTotalPedidos(Integer totalPedidos) {
        this.totalPedidos = totalPedidos;
    }

    public Double getPromedioGarrafones() {
        return promedioGarrafones;
    }

    public void setPromedioGarrafones(Double promedioGarrafones) {
        this.promedioGarrafones = promedioGarrafones;
    }

    public Long getFrecuenciaPromedioDias() {
        return frecuenciaPromedioDias;
    }

    public void setFrecuenciaPromedioDias(Long frecuenciaPromedioDias) {
        this.frecuenciaPromedioDias = frecuenciaPromedioDias;
    }

    public Long getDiasDesdeUltimoPedido() {
        return diasDesdeUltimoPedido;
    }

    public void setDiasDesdeUltimoPedido(Long diasDesdeUltimoPedido) {
        this.diasDesdeUltimoPedido = diasDesdeUltimoPedido;
    }

    public List<PedidoHistorialDTO> getPedidos() {
        return pedidos;
    }

    public void setPedidos(List<PedidoHistorialDTO> pedidos) {
        this.pedidos = pedidos;
    }
}