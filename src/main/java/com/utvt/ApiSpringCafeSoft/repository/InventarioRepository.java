package com.utvt.ApiSpringCafeSoft.repository;

import com.utvt.ApiSpringCafeSoft.model.Inventario;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface InventarioRepository extends JpaRepository<Inventario, Long> {

    List<Inventario> findByNombreContainingIgnoreCase(String nombre);
    List<Inventario> findByTipoContainingIgnoreCase(String tipo);
    List<Inventario> findByProveedorContainingIgnoreCase(String proveedor);
    List<Inventario> findByCantidadLessThanEqual(Double cantidadMinima);
    List<Inventario> findByUnidadMedida(String unidadMedida);
    List<Inventario> findByCaducidadBefore(String fecha);
    List<Inventario> findByCaducidadAfter(String fecha);

    @Query("SELECT i FROM Inventario i WHERE i.cantidad <= i.cantidadMinima")
    List<Inventario> findLowStockItems();

    @Query("SELECT i FROM Inventario i WHERE i.cantidad < i.cantidadMinima * 0.5")
    List<Inventario> findCriticalStockItems();

    @Query("SELECT i FROM Inventario i WHERE i.precioUnitario BETWEEN :precioMin AND :precioMax")
    List<Inventario> findByPrecioRange(@Param("precioMin") Double precioMin, @Param("precioMax") Double precioMax);

    @Query("SELECT i.tipo, COUNT(i) FROM Inventario i GROUP BY i.tipo")
    List<Object[]> countByTipo();

    List<Inventario> findByCantidadLessThan(Double cantidad);
}
