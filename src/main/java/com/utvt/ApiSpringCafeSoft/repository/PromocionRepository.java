package com.utvt.ApiSpringCafeSoft.repository;

import com.utvt.ApiSpringCafeSoft.model.Promocion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface PromocionRepository extends JpaRepository<Promocion, Long> {

    List<Promocion> findBySucursalId(Long sucursalId);

    List<Promocion> findByActiva(Boolean activa);

    // CA-2: promociones cuyo periodo incluye el momento actual
    @Query("SELECT p FROM Promocion p WHERE p.activa = true " +
           "AND p.fechaInicio <= :ahora AND p.fechaFin >= :ahora")
    List<Promocion> findVigentes(@Param("ahora") LocalDateTime ahora);

    // CA-2 filtrado por sucursal
    @Query("SELECT p FROM Promocion p WHERE p.activa = true " +
           "AND p.sucursal.id = :sucursalId " +
           "AND p.fechaInicio <= :ahora AND p.fechaFin >= :ahora")
    List<Promocion> findVigentesBySucursal(
            @Param("sucursalId") Long sucursalId,
            @Param("ahora") LocalDateTime ahora);
}
