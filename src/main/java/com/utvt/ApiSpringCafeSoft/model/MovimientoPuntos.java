package com.utvt.ApiSpringCafeSoft.model;
import jakarta.persistence.*;
import java.time.LocalDateTime;
@Entity
@Table(name="movimientos_puntos", uniqueConstraints=@UniqueConstraint(columnNames="venta_id"))
public class MovimientoPuntos {
 @Id @GeneratedValue(strategy=GenerationType.IDENTITY) private Long id;
 @ManyToOne(optional=false) @JoinColumn(name="cliente_id",nullable=false) private Cliente cliente;
 @OneToOne(optional=false) @JoinColumn(name="venta_id",nullable=false,unique=true) private Venta venta;
 @Column(nullable=false) private Integer puntos;
 @Column(nullable=false) private LocalDateTime fecha=LocalDateTime.now();
 public Long getId(){return id;}
 public Cliente getCliente(){return cliente;}
 public void setCliente(Cliente cliente){this.cliente=cliente;}
 public Venta getVenta(){return venta;}
 public void setVenta(Venta venta){this.venta=venta;}
 public Integer getPuntos(){return puntos;}
 public void setPuntos(Integer puntos){this.puntos=puntos;}
 public LocalDateTime getFecha(){return fecha;}
}
