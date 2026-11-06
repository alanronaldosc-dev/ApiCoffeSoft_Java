
package com.utvt.ApiSpringCafeSoft.repository;

import com.utvt.ApiSpringCafeSoft.model.Promocion;

import jakarta.persistence.LockModeType;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface PromocionRepository
        extends JpaRepository<Promocion, Long> {

    List<Promocion> findBySucursalId(Long sucursalId);

    List<Promocion> findByActiva(Boolean activa);

    // HU-039: promociones vigentes.
    @Query(
        "SELECT p FROM Promocion p " +
        "WHERE p.activa = true " +
        "AND p.fechaInicio <= :ahora " +
        "AND p.fechaFin >= :ahora"
    )
    List<Promocion> findVigentes(
        @Param("ahora") LocalDateTime ahora
    );

    // HU-039: promociones vigentes por sucursal.
    @Query(
        "SELECT p FROM Promocion p " +
        "WHERE p.activa = true " +
        "AND p.sucursal.id = :sucursalId " +
        "AND p.fechaInicio <= :ahora " +
        "AND p.fechaFin >= :ahora"
    )
    List<Promocion> findVigentesBySucursal(
        @Param("sucursalId") Long sucursalId,
        @Param("ahora") LocalDateTime ahora
    );

    // HU-040: bloqueo pesimista para cambios concurrentes.
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query(
        "SELECT p FROM Promocion p WHERE p.id = :id"
    )
    Optional<Promocion> findByIdForUpdate(
        @Param("id") Long id
    );
}
