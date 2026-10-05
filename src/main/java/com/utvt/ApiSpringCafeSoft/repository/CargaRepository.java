package com.utvt.ApiSpringCafeSoft.repository;

import com.utvt.ApiSpringCafeSoft.model.Carga;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import jakarta.persistence.LockModeType;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface CargaRepository extends JpaRepository<Carga, Long> {

    List<Carga> findByRepartidorIdOrderByFechaHoraDesc(Long repartidorId);

    List<Carga> findByEstadoOrderByFechaHoraDesc(String estado);

    List<Carga> findByRepartidorIdAndEstadoOrderByFechaHoraDesc(
            Long repartidorId,
            String estado
    );


    List<Carga> findByRepartidorIdAndEstadoOrderByFechaHoraAsc(
            Long repartidorId,
            String estado
    );

    List<Carga> findByRepartidorIdAndFechaHoraBetweenOrderByFechaHoraAsc(
            Long repartidorId,
            LocalDateTime inicio,
            LocalDateTime fin
    );

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    Optional<Carga> findWithLockById(Long id);

    List<Carga> findByRepartidorIsNullAndEstadoOrderByFechaHoraDesc(String estado);

    @Modifying
    @Query("DELETE FROM Carga c WHERE c.inventario.id = :inventarioId")
    void deleteByInventarioId(@Param("inventarioId") Long inventarioId);
}