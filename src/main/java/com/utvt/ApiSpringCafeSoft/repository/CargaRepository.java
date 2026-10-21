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
public interface CargaRepository
        extends JpaRepository<Carga, Long> {

    // ============================================
    // CARGAS POR REPARTIDOR
    // ============================================

    List<Carga>
    findByRepartidorIdOrderByFechaHoraDesc(
            Long repartidorId
    );

    List<Carga>
    findByRepartidorIdAndEstadoOrderByFechaHoraDesc(
            Long repartidorId,
            String estado
    );

    List<Carga>
    findByRepartidorIdAndEstadoOrderByFechaHoraAsc(
            Long repartidorId,
            String estado
    );

    // ============================================
    // CARGAS POR ESTADO
    // ============================================

    List<Carga>
    findByEstadoOrderByFechaHoraDesc(
            String estado
    );

    // ============================================
    // HU-015
    // CARGAS POR TIPO DE GARRAFÓN
    // ============================================

    /**
     * Cargas activas del repartidor
     * para un tipo específico
     * de garrafón.
     */
    List<Carga>
    findByRepartidorIdAndEstadoAndInventarioIdOrderByFechaHoraAsc(
            Long repartidorId,
            String estado,
            Long inventarioId
    );

    // ============================================
    // CARGAS POR FECHA
    // ============================================

    List<Carga>
    findByRepartidorIdAndFechaHoraBetweenOrderByFechaHoraAsc(
            Long repartidorId,
            LocalDateTime inicio,
            LocalDateTime fin
    );

    // ============================================
    // CARGAS SIN REPARTIDOR
    // ============================================

    List<Carga>
    findByRepartidorIsNullAndEstadoOrderByFechaHoraDesc(
            String estado
    );

    // ============================================
    // HU-016
    // CARGAS POR RUTA
    // ============================================

    /**
     * Todas las cargas asociadas
     * a una ruta.
     */
    List<Carga>
    findByRutaIdOrderByFechaHoraAsc(
            Long rutaId
    );

    /**
     * Cargas de una ruta filtradas
     * por estado.
     */
    List<Carga>
    findByRutaIdAndEstadoOrderByFechaHoraAsc(
            Long rutaId,
            String estado
    );

    /**
     * Cargas de una ruta filtradas
     * por estado y tipo de garrafón.
     */
    List<Carga>
    findByRutaIdAndEstadoAndInventarioIdOrderByFechaHoraAsc(
            Long rutaId,
            String estado,
            Long inventarioId
    );

    // ============================================
    // BLOQUEO DE CARGA
    // ============================================

   @Lock(LockModeType.PESSIMISTIC_WRITE)
@Query("SELECT c FROM Carga c WHERE c.id = :id")
Optional<Carga> findWithLockById(
        @Param("id") Long id
);

    // ============================================
    // ELIMINAR POR INVENTARIO
    // ============================================

    @Modifying
    @Query(
        "DELETE FROM Carga c " +
        "WHERE c.inventario.id = :inventarioId"
    )
    void deleteByInventarioId(
            @Param("inventarioId")
            Long inventarioId
    );
}