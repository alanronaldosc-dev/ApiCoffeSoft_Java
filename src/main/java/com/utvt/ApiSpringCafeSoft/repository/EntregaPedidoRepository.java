package com.utvt.ApiSpringCafeSoft.repository;

import com.utvt.ApiSpringCafeSoft.model.EntregaPedido;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;

public interface EntregaPedidoRepository extends JpaRepository<EntregaPedido, Long> {

    List<EntregaPedido> findByRepartidorIdAndFechaBetweenOrderByFechaAsc(
        Long repartidorId,
        LocalDateTime inicio,
        LocalDateTime fin
    );
}
