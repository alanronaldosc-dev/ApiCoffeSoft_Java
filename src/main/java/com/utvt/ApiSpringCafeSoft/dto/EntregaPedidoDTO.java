package com.utvt.ApiSpringCafeSoft.dto;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/** HU-015: respuesta de una entrega o incidencia de reparto. */
public class EntregaPedidoDTO {
    private Long id;
    private Long ventaId;
    private String folio;
    private Long repartidorId;
    private String repartidorNombre;
    private Double garrafonesEntregados;
    private Double envasesVaciosRecibidos;

    /** HU-015: desglose de garrafones vendidos por tipo. */
    private List<DetalleGarrafonDTO> garrafonesDetalle = new ArrayList<>();

    /** HU-015: desglose de envases vacíos recibidos por tipo. */
    private List<DetalleGarrafonDTO> envasesVaciosDetalle = new ArrayList<>();

    private String metodoCobro;
    private Double montoCobrado;
    private String resultado;
    private LocalDateTime fecha;
    private String observaciones;
    private Long clienteId;
private String clienteNombre;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Long getVentaId() { return ventaId; }
    public void setVentaId(Long ventaId) { this.ventaId = ventaId; }
    public String getFolio() { return folio; }
    public void setFolio(String folio) { this.folio = folio; }
    public Long getRepartidorId() { return repartidorId; }
    public void setRepartidorId(Long repartidorId) { this.repartidorId = repartidorId; }
    public String getRepartidorNombre() { return repartidorNombre; }
    public void setRepartidorNombre(String repartidorNombre) { this.repartidorNombre = repartidorNombre; }
    public Double getGarrafonesEntregados() { return garrafonesEntregados; }
    public void setGarrafonesEntregados(Double garrafonesEntregados) { this.garrafonesEntregados = garrafonesEntregados; }
    public Double getEnvasesVaciosRecibidos() { return envasesVaciosRecibidos; }
    public void setEnvasesVaciosRecibidos(Double envasesVaciosRecibidos) { this.envasesVaciosRecibidos = envasesVaciosRecibidos; }
    public List<DetalleGarrafonDTO> getGarrafonesDetalle() { return garrafonesDetalle; }
    public void setGarrafonesDetalle(List<DetalleGarrafonDTO> garrafonesDetalle) { this.garrafonesDetalle = garrafonesDetalle; }
    public List<DetalleGarrafonDTO> getEnvasesVaciosDetalle() { return envasesVaciosDetalle; }
    public void setEnvasesVaciosDetalle(List<DetalleGarrafonDTO> envasesVaciosDetalle) { this.envasesVaciosDetalle = envasesVaciosDetalle; }
    public String getMetodoCobro() { return metodoCobro; }
    public void setMetodoCobro(String metodoCobro) { this.metodoCobro = metodoCobro; }
    public Double getMontoCobrado() { return montoCobrado; }
    public void setMontoCobrado(Double montoCobrado) { this.montoCobrado = montoCobrado; }
    public String getResultado() { return resultado; }
    public void setResultado(String resultado) { this.resultado = resultado; }
    public LocalDateTime getFecha() { return fecha; }
    public void setFecha(LocalDateTime fecha) { this.fecha = fecha; }
    public String getObservaciones() { return observaciones; }
    public void setObservaciones(String observaciones) { this.observaciones = observaciones; }

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
}