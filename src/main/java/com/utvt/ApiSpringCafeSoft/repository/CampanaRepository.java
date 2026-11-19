package com.utvt.ApiSpringCafeSoft.repository;

import com.utvt.ApiSpringCafeSoft.model.Campana;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

/**
 * HU-037: Repositorio de Campañas con soporte para
 * activación y desactivación automática por periodo.
 */
@Repository
public interface CampanaRepository extends JpaRepository<Campana, Long> {

    List<Campana> findBySegmento(String segmento);

    List<Campana> findByActiva(Boolean activa);

    List<Campana> findBySucursalId(Long sucursalId);

    /**
     * HU-037 CA-2: Campañas cuyo periodo incluye el momento actual y están activas.
     * El administrador ve la campaña activa en la fecha de inicio.
     */
    @Query("SELECT c FROM Campana c WHERE c.activa = true " +
           "AND c.fechaInicio <= :ahora AND c.fechaFin >= :ahora")
    List<Campana> findVigentes(@Param("ahora") LocalDateTime ahora);

    /**
     * HU-037 CA-2: Vigentes filtradas por segmento.
     */
    @Query("SELECT c FROM Campana c WHERE c.activa = true " +
           "AND c.segmento = :segmento " +
           "AND c.fechaInicio <= :ahora AND c.fechaFin >= :ahora")
    List<Campana> findVigentesPorSegmento(
            @Param("segmento") String segmento,
            @Param("ahora") LocalDateTime ahora);

    /**
     * HU-037 CA-1: Campañas que deben activarse porque su periodo ya comenzó
     * pero aún no están marcadas como activas.
     */
    @Query("SELECT c FROM Campana c WHERE c.activa = false " +
           "AND c.fechaInicio <= :ahora AND c.fechaFin >= :ahora")
    List<Campana> findParaActivar(@Param("ahora") LocalDateTime ahora);

    /**
     * HU-037 CA-3: Campañas que deben desactivarse porque su periodo ya expiró.
     * El administrador no tiene que desactivar campañas manualmente.
     */
    @Query("SELECT c FROM Campana c WHERE c.activa = true " +
           "AND c.fechaFin < :ahora")
    List<Campana> findParaDesactivar(@Param("ahora") LocalDateTime ahora);

    /**
     * HU-037 CA-3: Desactiva en bloque todas las campañas expiradas.
     * Ejecutado por el scheduler automático.
     */
    @Modifying
    @Query("UPDATE Campana c SET c.activa = false WHERE c.activa = true AND c.fechaFin < :ahora")
    int desactivarExpiradas(@Param("ahora") LocalDateTime ahora);

    /**
     * HU-037 CA-1: Activa en bloque todas las campañas cuyo periodo ya comenzó.
     */
    @Modifying
    @Query("UPDATE Campana c SET c.activa = true " +
           "WHERE c.activa = false AND c.fechaInicio <= :ahora AND c.fechaFin >= :ahora")
    int activarPendientes(@Param("ahora") LocalDateTime ahora);
}
