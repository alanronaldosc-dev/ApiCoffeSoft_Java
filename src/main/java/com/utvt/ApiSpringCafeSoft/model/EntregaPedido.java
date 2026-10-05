package com.utvt.ApiSpringCafeSoft.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

/**
 * HU-016: Registro de cada entrega realizada por un repartidor durante su turno.
 */
@Entity
@Table(name = "entrega_pedidos")
public class EntregaPedido {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "repartidor_id", nullable = false)
    private Usuario repartidor;

    @Column(name = "fecha", nullable = false)
    private LocalDateTime fecha;

    /** ENTREGADO, CANCELADO, PENDIENTE */
    @Column(name = "resultado", length = 30)
    private String resultado;

    @Column(name = "garrafones_entregados")
    private Double garrafonesEntregados;

    @Column(name = "envases_vacios_recibidos")
    private Double envasesVaciosRecibidos;

    /** EFECTIVO o TRANSFERENCIA */
    @Column(name = "metodo_cobro", length = 20)
    private String metodoCobro;

    @Column(name = "monto_cobrado")
    private Double montoCobrado;

    public EntregaPedido() {}

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Usuario getRepartidor() { return repartidor; }
    public void setRepartidor(Usuario repartidor) { this.repartidor = repartidor; }

    public LocalDateTime getFecha() { return fecha; }
    public void setFecha(LocalDateTime fecha) { this.fecha = fecha; }

    public String getResultado() { return resultado; }
    public void setResultado(String resultado) { this.resultado = resultado; }

    public Double getGarrafonesEntregados() { return garrafonesEntregados; }
    public void setGarrafonesEntregados(Double garrafonesEntregados) {
        this.garrafonesEntregados = garrafonesEntregados;
    }

    public Double getEnvasesVaciosRecibidos() { return envasesVaciosRecibidos; }
    public void setEnvasesVaciosRecibidos(Double envasesVaciosRecibidos) {
        this.envasesVaciosRecibidos = envasesVaciosRecibidos;
    }

    public String getMetodoCobro() { return metodoCobro; }
    public void setMetodoCobro(String metodoCobro) { this.metodoCobro = metodoCobro; }

    public Double getMontoCobrado() { return montoCobrado; }
    public void setMontoCobrado(Double montoCobrado) { this.montoCobrado = montoCobrado; }
}
