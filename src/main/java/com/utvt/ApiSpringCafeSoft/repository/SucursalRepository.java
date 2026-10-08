package com.utvt.ApiSpringCafeSoft.repository;

import com.utvt.ApiSpringCafeSoft.model.Sucursal;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface SucursalRepository extends JpaRepository<Sucursal, Long> {

    List<Sucursal> findByActivo(Boolean activo);
    Optional<Sucursal> findByNombre(String nombre);
    boolean existsByNombre(String nombre);
}
