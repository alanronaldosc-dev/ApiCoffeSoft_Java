package com.utvt.ApiSpringCafeSoft.model;

import jakarta.persistence.*;

/**
 * HU-015
 * Detalle por tipo de garrafón de una entrega del repartidor.
 *
 * Registra tanto los garrafones llenos vendidos
 * (tipoMovimiento = VENDIDO) como los envases
 * vacíos recibidos (tipoMovimiento = DEVUELTO).
 */
@Entity
@Table(name = "entregas_garrafon_detalle")
public class EntregaGarrafonDetalle {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.EAGER, optional = false)
    @JoinColumn(name = "entrega_pedido_id", nullable = false)
    private EntregaPedido entregaPedido;

    /**
     * Tipo de garrafón (inventario) relacionado.
     */
    @ManyToOne(fetch = FetchType.EAGER, optional = false)
    @JoinColumn(name = "inventario_id", nullable = false)
    private Inventario inventario;

    /**
     * VENDIDO  = garrafón lleno entregado.
     * DEVUELTO = envase vacío recibido.
     */
    @Column(name = "tipo_movimiento", nullable = false, length = 20)
    private String tipoMovimiento;

    @Column(nullable = false)
    private Double cantidad;

    public EntregaGarrafonDetalle() {
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public EntregaPedido getEntregaPedido() {
        return entregaPedido;
    }

    public void setEntregaPedido(EntregaPedido entregaPedido) {
        this.entregaPedido = entregaPedido;
    }

    public Inventario getInventario() {
        return inventario;
    }

    public void setInventario(Inventario inventario) {
        this.inventario = inventario;
    }

    public String getTipoMovimiento() {
        return tipoMovimiento;
    }

    public void setTipoMovimiento(String tipoMovimiento) {
        this.tipoMovimiento = tipoMovimiento;
    }

    public Double getCantidad() {
        return cantidad;
    }

    public void setCantidad(Double cantidad) {
        this.cantidad = cantidad;
    }
}
