package com.utvt.ApiSpringCafeSoft.repository;

import com.utvt.ApiSpringCafeSoft.model.Venta;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface VentaRepository extends JpaRepository<Venta, Long> {

    Venta findByFolio(String folio);
    List<Venta> findByUsuarioId(Long usuarioId);
    List<Venta> findByFechaBetween(LocalDateTime inicio, LocalDateTime fin);
    List<Venta> findByMetodoPago(String metodoPago);
    List<Venta> findByEstadoPedidoOrderByFechaAsc(String estadoPedido);

    @Query("SELECT MAX(v.folio) FROM Venta v")
    String findLastFolio();

    @Query("SELECT COUNT(v) FROM Venta v WHERE DATE(v.fecha) = DATE(:fecha)")
    Long countVentasByFecha(@Param("fecha") LocalDateTime fecha);

    @Query("SELECT SUM(v.total) FROM Venta v WHERE DATE(v.fecha) = DATE(:fecha)")
    Double sumTotalByFecha(@Param("fecha") LocalDateTime fecha);

    @Query("SELECT v.metodoPago, COUNT(v), SUM(v.total) FROM Venta v " +
           "WHERE v.fecha BETWEEN :inicio AND :fin GROUP BY v.metodoPago")
    List<Object[]> getVentasPorMetodoPago(@Param("inicio") LocalDateTime inicio,
                                          @Param("fin") LocalDateTime fin);

    // ── NUEVO: filtrar por sucursal ──
    List<Venta> findBySucursalId(Long sucursalId);

    @Query("SELECT SUM(v.total) FROM Venta v WHERE v.sucursal.id = :sucursalId AND DATE(v.fecha) = DATE(:fecha)")
    Double sumTotalBySucursalAndFecha(@Param("sucursalId") Long sucursalId, @Param("fecha") LocalDateTime fecha);
}
