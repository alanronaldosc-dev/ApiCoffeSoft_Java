package com.utvt.ApiSpringCafeSoft.repository;

import com.utvt.ApiSpringCafeSoft.model.Producto;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ProductoRepository extends JpaRepository<Producto, Long> {

    // Buscar productos por nombre (contiene)
    List<Producto> findByNombreContainingIgnoreCase(String nombre);

    // Buscar productos por rango de precio
    List<Producto> findByPrecioBetween(Double precioMin, Double precioMax);

    // Buscar productos por precio menor a
    List<Producto> findByPrecioLessThan(Double precio);

    // Buscar productos por precio mayor a
    List<Producto> findByPrecioGreaterThan(Double precio);

    // Contar productos por nombre
    Long countByNombreContainingIgnoreCase(String nombre);

    // Buscar productos que contienen un insumo específico
    @Query("SELECT p FROM Producto p JOIN p.insumos pi WHERE pi.insumo.id = :insumoId")
    List<Producto> findProductosByInsumoId(@Param("insumoId") Long insumoId);

    // Buscar productos con sus insumos (evita N+1)
    @Query("SELECT p FROM Producto p LEFT JOIN FETCH p.insumos pi LEFT JOIN FETCH pi.insumo")
    List<Producto> findAllWithInsumos();

    // Buscar producto por ID con sus insumos
    @Query("SELECT p FROM Producto p LEFT JOIN FETCH p.insumos pi LEFT JOIN FETCH pi.insumo WHERE p.id = :id")
    Producto findByIdWithInsumos(@Param("id") Long id);
}