package com.utvt.ApiSpringCafeSoft.repository;

import com.utvt.ApiSpringCafeSoft.model.Lote;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;

public interface LoteRepository extends JpaRepository<Lote, Long> {

    List<Lote> findByInsumoId(Long insumoId);
    List<Lote> findByProveedorId(Long proveedorId);
    List<Lote> findByFechaCaducidadBefore(LocalDate fecha);
    List<Lote> findByTipoLote(String tipoLote);

    @Modifying
    @Query("DELETE FROM Lote l WHERE l.productoId = :productoId")
    void deleteByProductoId(@Param("productoId") Long productoId);

    @Query("SELECT l FROM Lote l WHERE l.insumo.id = :insumoId AND l.cantidad > 0 ORDER BY l.fechaCaducidad ASC, l.fechaEntrada DESC")
    List<Lote> findLotesDisponiblesByInsumoId(@Param("insumoId") Long insumoId);

    @Query("SELECT l FROM Lote l WHERE l.insumo.nombre = :nombre AND l.insumo.unidadMedida = :unidad AND l.cantidad > 0 ORDER BY l.fechaCaducidad ASC, l.fechaEntrada DESC")
    List<Lote> findLotesDisponiblesByNombreYUnidad(@Param("nombre") String nombre, @Param("unidad") String unidad);

    /**
     * Bloqueo pesimista de escritura para consumir lotes de forma
     * atómica en ventas concurrentes (HU-017).
     * Ordena por fecha de caducidad (FEFO) y fecha de entrada.
     */
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT l FROM Lote l WHERE l.insumo.nombre = :nombre AND l.insumo.unidadMedida = :unidad AND l.cantidad > 0 ORDER BY l.fechaCaducidad ASC, l.fechaEntrada DESC")
    List<Lote> findLotesDisponiblesByNombreYUnidadForUpdate(@Param("nombre") String nombre, @Param("unidad") String unidad);
}
