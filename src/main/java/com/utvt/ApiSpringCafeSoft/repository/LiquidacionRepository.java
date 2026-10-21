package com.utvt.ApiSpringCafeSoft.repository;

import com.utvt.ApiSpringCafeSoft.model.Liquidacion;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface LiquidacionRepository
        extends JpaRepository<Liquidacion, Long> {

    /**
     * Método anterior.
     * Se mantiene temporalmente hasta
     * modificar LiquidacionService.
     */
    Optional<Liquidacion>
    findByRepartidorIdAndFechaOperacion(
            Long repartidorId,
            LocalDate fechaOperacion
    );

    /**
     * Historial de liquidaciones
     * de un repartidor.
     */
    List<Liquidacion>
    findByRepartidorIdOrderByFechaOperacionDesc(
            Long repartidorId
    );

    /**
     * NUEVO:
     * buscar liquidación por ruta.
     */
    Optional<Liquidacion>
    findByRutaId(
            Long rutaId
    );

    /**
     * NUEVO:
     * comprobar si una ruta
     * ya fue liquidada.
     */
    boolean existsByRutaId(
            Long rutaId
    );
}