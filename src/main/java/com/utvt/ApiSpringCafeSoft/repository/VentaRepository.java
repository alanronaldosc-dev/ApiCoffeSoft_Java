package com.utvt.ApiSpringCafeSoft.repository;

import com.utvt.ApiSpringCafeSoft.model.Venta;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;

public interface VentaRepository extends JpaRepository<Venta, Long> {

    boolean existsByPromocion_Id(Long promocionId);

    List<Venta> findByCliente_IdAndPuntosCanjeadosGreaterThanOrderByFechaDesc(
            Long clienteId,
            Integer puntosCanjeados);

    @Query("""
            SELECT v.cliente.id AS clienteId,
                   COUNT(v) AS compras,
                   MAX(v.fecha) AS ultimaCompra
            FROM Venta v
            WHERE v.cliente IS NOT NULL
            GROUP BY v.cliente.id
            """)
    List<ClienteCompraStatsProjection> findCustomerPurchaseStats();

    // Buscar por folio
    Venta findByFolio(String folio);

    // Buscar ventas por usuario (usando id_usuario)
    List<Venta> findByUsuarioId(Long usuarioId);

    // Buscar ventas por rango de fechas
    List<Venta> findByFechaBetween(LocalDateTime inicio, LocalDateTime fin);

    // Buscar ventas por método de pago
    List<Venta> findByMetodoPago(String metodoPago);

    List<Venta> findByEstadoPedidoOrderByFechaAsc(String estadoPedido);

    // Obtener el último folio
    @Query("SELECT MAX(v.folio) FROM Venta v")
    String findLastFolio();

    // Contar ventas por día
    @Query("SELECT COUNT(v) FROM Venta v WHERE DATE(v.fecha) = DATE(:fecha)")
    Long countVentasByFecha(@Param("fecha") LocalDateTime fecha);

    // Suma total de ventas por día
    @Query("SELECT SUM(v.total) FROM Venta v WHERE DATE(v.fecha) = DATE(:fecha)")
    Double sumTotalByFecha(@Param("fecha") LocalDateTime fecha);

    // Ventas por método de pago en un rango de fechas
    @Query("SELECT v.metodoPago, COUNT(v), SUM(v.total) FROM Venta v " +
           "WHERE v.fecha BETWEEN :inicio AND :fin GROUP BY v.metodoPago")
    List<Object[]> getVentasPorMetodoPago(@Param("inicio") LocalDateTime inicio, 
                                          @Param("fin") LocalDateTime fin);
}