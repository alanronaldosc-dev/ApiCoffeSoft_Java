package com.utvt.ApiSpringCafeSoft.repository;

import com.utvt.ApiSpringCafeSoft.model.EntregaGarrafonDetalle;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface EntregaGarrafonDetalleRepository
        extends JpaRepository<EntregaGarrafonDetalle, Long> {

    List<EntregaGarrafonDetalle> findByEntregaPedidoId(Long entregaPedidoId);
}
