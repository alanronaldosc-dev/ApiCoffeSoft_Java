package com.utvt.ApiSpringCafeSoft.repository;

import com.utvt.ApiSpringCafeSoft.model.EntregaPedido;
import com.utvt.ApiSpringCafeSoft.dto.DesempenoRepartidorDTO;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface EntregaPedidoRepository extends JpaRepository<EntregaPedido, Long> {
    List<EntregaPedido> findByRepartidorIdAndFechaBetweenOrderByFechaAsc(
            Long repartidorId,
            LocalDateTime inicio,
            LocalDateTime fin
    );

    boolean existsByVentaIdAndResultado(Long ventaId, String resultado);

    /**
     * HU-049: agrega las entregas confirmadas por repartidor dentro de la
     * sucursal del administrador autenticado. Una fila de EntregaPedido
     * representa una entrega confirmada; las incidencias no se contabilizan.
     */
    @Query("SELECT new com.utvt.ApiSpringCafeSoft.dto.DesempenoRepartidorDTO(" +
            "e.repartidor.id, e.repartidor.nombre, COUNT(e.id), " +
            "COALESCE(SUM(e.montoCobrado), 0.0)) " +
            "FROM EntregaPedido e " +
            "WHERE e.resultado = 'ENTREGADO' " +
            "AND e.venta.estadoPedido = 'ENTREGADO' " +
            "AND e.repartidor.userTipo = 4 " +
            "AND e.repartidor.sucursal.id = :sucursalId " +
            "GROUP BY e.repartidor.id, e.repartidor.nombre")
    List<DesempenoRepartidorDTO> obtenerDesempenoPorSucursal(
            @Param("sucursalId") Long sucursalId);
}