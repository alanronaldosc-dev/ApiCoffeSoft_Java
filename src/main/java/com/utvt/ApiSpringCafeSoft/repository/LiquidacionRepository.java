package com.utvt.ApiSpringCafeSoft.repository;

import com.utvt.ApiSpringCafeSoft.model.Liquidacion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface LiquidacionRepository extends JpaRepository<Liquidacion, Long> {
    Optional<Liquidacion> findByRepartidorIdAndFechaOperacion(Long repartidorId, LocalDate fechaOperacion);
    List<Liquidacion> findByRepartidorIdOrderByFechaOperacionDesc(Long repartidorId);
}
