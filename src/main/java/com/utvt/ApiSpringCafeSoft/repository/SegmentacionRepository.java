
package com.utvt.ApiSpringCafeSoft.repository;

import com.utvt.ApiSpringCafeSoft.model.Venta;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;

/**
 * HU-035 y HU-036.
 * Obtiene la actividad de compras asociadas
 * a clientes de ruta.
 */
public interface SegmentacionRepository
        extends JpaRepository<Venta, Long> {

    @Query("""
        SELECT v.clienteRutaId, COUNT(v), MAX(v.fecha)
        FROM Venta v
        WHERE v.clienteRutaId IS NOT NULL
        GROUP BY v.clienteRutaId
        """)
    List<Object[]> actividadPorCliente();

    /**
     * HU-081: compras, primera/última fecha y compras recientes por cliente.
     * Las ventas canceladas (o dadas de baja lógicamente) no cuentan.
     */
    @Query("""
        SELECT v.clienteRutaId, COUNT(v), MIN(v.fecha), MAX(v.fecha),
               SUM(CASE WHEN v.fecha >= :desde THEN 1 ELSE 0 END)
        FROM Venta v
        WHERE v.clienteRutaId IS NOT NULL
          AND (v.estadoPedido IS NULL OR UPPER(v.estadoPedido) <> 'CANCELADO')
        GROUP BY v.clienteRutaId
        """)
    List<Object[]> actividadRiesgoPorCliente(@Param("desde") LocalDateTime desde);

    /**
     * HU-060: obtiene las ventas de clientes para detectar anomalías.
     * Excluye ventas canceladas y ordena por cliente y fecha.
     */
    @Query("""
        SELECT v.clienteRutaId, v.fecha, v.total
        FROM Venta v
        WHERE v.clienteRutaId IS NOT NULL
          AND (v.estadoPedido IS NULL OR UPPER(v.estadoPedido) <> 'CANCELADO')
        ORDER BY v.clienteRutaId ASC, v.fecha ASC
        """)
    List<Object[]> ventasParaDeteccionAnomalias();
}
