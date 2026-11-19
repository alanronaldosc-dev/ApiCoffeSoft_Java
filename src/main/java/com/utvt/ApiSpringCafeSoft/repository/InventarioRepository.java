package com.utvt.ApiSpringCafeSoft.repository;

import com.utvt.ApiSpringCafeSoft.model.Inventario;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface InventarioRepository extends JpaRepository<Inventario, Long> {

    List<Inventario> findByNombreContainingIgnoreCase(String nombre);
    List<Inventario> findByTipoContainingIgnoreCase(String tipo);
    List<Inventario> findByProveedorContainingIgnoreCase(String proveedor);
    List<Inventario> findByCantidadLessThanEqual(Double cantidadMinima);
    List<Inventario> findByUnidadMedida(String unidadMedida);
    List<Inventario> findByCaducidadBefore(String fecha);
    List<Inventario> findByCaducidadAfter(String fecha);
    List<Inventario> findByTipo(String tipo);

    java.util.Optional<Inventario> findByProductoId(Long productoId);
    
    @Query("SELECT i FROM Inventario i WHERE i.cantidad <= i.cantidadMinima")
    List<Inventario> findLowStockItems();

    @Query("SELECT i FROM Inventario i WHERE i.cantidad < i.cantidadMinima * 0.5")
    List<Inventario> findCriticalStockItems();

    /**
     * Bloqueo pesimista de escritura para evitar race conditions
     * durante el descuento de inventario en ventas concurrentes.
     * Garantiza que solo una transacción a la vez puede leer y
     * modificar el mismo registro de inventario (HU-017).
     */
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT i FROM Inventario i WHERE i.id = :id")
    Optional<Inventario> findByIdForUpdate(@Param("id") Long id);

    @Query("SELECT i FROM Inventario i WHERE i.precioUnitario BETWEEN :precioMin AND :precioMax")
    List<Inventario> findByPrecioRange(@Param("precioMin") Double precioMin, @Param("precioMax") Double precioMax);

    @Query("SELECT i.tipo, COUNT(i) FROM Inventario i GROUP BY i.tipo")
    List<Object[]> countByTipo();

    List<Inventario> findByCantidadLessThan(Double cantidad);

    List<Inventario> findBySucursalId(Long sucursalId);
    List<Inventario> findBySucursalIdAndTipo(Long sucursalId, String tipo);
    java.util.Optional<Inventario> findByProductoIdAndSucursalId(Long productoId, Long sucursalId);

    @Query("SELECT i FROM Inventario i WHERE i.sucursal.id = :sucursalId AND i.cantidad <= i.cantidadMinima")
    List<Inventario> findLowStockBySucursal(@Param("sucursalId") Long sucursalId);
}
