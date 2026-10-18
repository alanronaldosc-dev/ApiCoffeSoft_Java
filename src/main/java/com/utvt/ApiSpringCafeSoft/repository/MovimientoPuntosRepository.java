package com.utvt.ApiSpringCafeSoft.repository;
import com.utvt.ApiSpringCafeSoft.model.MovimientoPuntos;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.List;
import java.util.Optional;
public interface MovimientoPuntosRepository extends JpaRepository<MovimientoPuntos,Long> {
 @Query("select coalesce(sum(m.puntos),0) from MovimientoPuntos m where m.cliente.id=:clienteId")
 Long saldo(@Param("clienteId") Long clienteId);
 List<MovimientoPuntos> findByClienteIdOrderByFechaDesc(Long clienteId);
 Optional<MovimientoPuntos> findByVentaId(Long ventaId);
}
