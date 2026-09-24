package com.utvt.ApiSpringCafeSoft.repository;

import com.utvt.ApiSpringCafeSoft.model.Carga;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.stereotype.Repository;

import jakarta.persistence.LockModeType;

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

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    Optional<Carga> findWithLockById(Long id);
}