package com.utvt.ApiSpringCafeSoft.repository;

import com.utvt.ApiSpringCafeSoft.model.Producto;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ProductoRepository extends JpaRepository<Producto, Long> {

    List<Producto> findByNombreContainingIgnoreCase(String nombre);

    List<Producto> findByPrecioBetween(Double precioMin, Double precioMax);

    List<Producto> findByPrecioLessThan(Double precio);

    List<Producto> findByPrecioGreaterThan(Double precio);

    Long countByNombreContainingIgnoreCase(String nombre);

    @Query("SELECT p FROM Producto p JOIN p.insumos pi WHERE pi.insumo.id = :insumoId")
    List<Producto> findProductosByInsumoId(@Param("insumoId") Long insumoId);

    @Query("SELECT p FROM Producto p LEFT JOIN FETCH p.insumos pi LEFT JOIN FETCH pi.insumo")
    List<Producto> findAllWithInsumos();

    @Query("SELECT p FROM Producto p LEFT JOIN FETCH p.insumos pi LEFT JOIN FETCH pi.insumo WHERE p.id = :id")
    Producto findByIdWithInsumos(@Param("id") Long id);

    @Query("SELECT p FROM Producto p LEFT JOIN FETCH p.insumos pi LEFT JOIN FETCH pi.insumo WHERE p.categoria.id = :categoriaId")
    List<Producto> findByCategoriaId(@Param("categoriaId") Long categoriaId);

    // ── Filtrar por sucursal ──
    @Query("SELECT p FROM Producto p LEFT JOIN FETCH p.insumos pi LEFT JOIN FETCH pi.insumo WHERE p.sucursal.id = :sucursalId")
    List<Producto> findBySucursalId(@Param("sucursalId") Long sucursalId);

}
