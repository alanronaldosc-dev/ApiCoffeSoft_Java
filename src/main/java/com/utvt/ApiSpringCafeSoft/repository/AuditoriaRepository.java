package com.utvt.ApiSpringCafeSoft.repository;

import com.utvt.ApiSpringCafeSoft.model.Auditoria;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

@Repository
public interface AuditoriaRepository extends JpaRepository<Auditoria, Long> {

    // Últimas 50 acciones (para el panel de actividad visible al usuario)
    List<Auditoria> findTop50ByOrderByFechaDesc();

    List<Auditoria> findByEntidadAndEntidadIdAndAccionInOrderByFechaDesc(
            String entidad, Long entidadId, Collection<String> acciones);

    List<Auditoria> findByEntidadAndEntidadIdAndAccionOrderByFechaDesc(
            String entidad, Long entidadId, String accion);

    Optional<Auditoria> findByIdAndEntidadAndEntidadIdAndAccionIn(
            Long id, String entidad, Long entidadId, Collection<String> acciones);

    List<Auditoria> findByEntidadAndAccionIn(String entidad, Collection<String> acciones);

    List<Auditoria> findByEntidadAndAccion(String entidad, String accion);
}
