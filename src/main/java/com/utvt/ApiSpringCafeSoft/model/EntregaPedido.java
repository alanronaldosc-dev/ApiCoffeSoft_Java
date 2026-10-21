package com.utvt.ApiSpringCafeSoft.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

/**
 * HU-015: registra la confirmación de una entrega realizada por un repartidor.
 */
@Entity
@Table(name = "entregas_pedido")
public class EntregaPedido {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.EAGER, optional = false)
    @JoinColumn(name = "venta_id", nullable = false)
    private Venta venta;

    @ManyToOne(fetch = FetchType.EAGER, optional = false)
    @JoinColumn(name = "repartidor_id", nullable = false)
    private Usuario repartidor;

    @Column(name = "garrafones_entregados", nullable = false)
    private Double garrafonesEntregados = 0.0;

    @Column(name = "envases_vacios_recibidos", nullable = false)
    private Double envasesVaciosRecibidos = 0.0;

    @Column(name = "metodo_cobro", length = 20)
    private String metodoCobro;

    @Column(name = "monto_cobrado", nullable = false)
    private Double montoCobrado = 0.0;

    @Column(nullable = false, length = 30)
    private String resultado;

    @Column(nullable = false)
    private LocalDateTime fecha;

    @Column(length = 500)
    private String observaciones;

    public EntregaPedido() {}

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Venta getVenta() { return venta; }
    public void setVenta(Venta venta) { this.venta = venta; }

    public Usuario getRepartidor() { return repartidor; }
    public void setRepartidor(Usuario repartidor) { this.repartidor = repartidor; }

    public Double getGarrafonesEntregados() { return garrafonesEntregados; }
    public void setGarrafonesEntregados(Double garrafonesEntregados) { this.garrafonesEntregados = garrafonesEntregados; }

    public Double getEnvasesVaciosRecibidos() { return envasesVaciosRecibidos; }
    public void setEnvasesVaciosRecibidos(Double envasesVaciosRecibidos) { this.envasesVaciosRecibidos = envasesVaciosRecibidos; }

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
}