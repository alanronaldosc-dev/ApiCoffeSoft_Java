package com.utvt.ApiSpringCafeSoft.repository;

import com.utvt.ApiSpringCafeSoft.model.Cliente;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ClienteRepository
        extends JpaRepository<Cliente, Long> {

    List<Cliente> findByActivoTrue();

    List<Cliente> findByRutaId(Long rutaId);

    List<Cliente> findByRutaIsNull();
}
