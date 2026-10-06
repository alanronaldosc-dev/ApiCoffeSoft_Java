package com.utvt.ApiSpringCafeSoft.repository;

import com.utvt.ApiSpringCafeSoft.model.EntregaPedido;
import org.springframework.data.jpa.repository.JpaRepository;
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
}