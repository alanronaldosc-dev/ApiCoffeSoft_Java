package com.utvt.ApiSpringCafeSoft.repository;

import com.utvt.ApiSpringCafeSoft.model.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UsuarioRepository extends JpaRepository<Usuario, Long> {
    
    // Buscar por email
    Optional<Usuario> findByEmail(String email);
    
    // Buscar por tipo de usuario
    List<Usuario> findByUserTipo(Integer userTipo);
    
    // Buscar por nombre (contiene, ignorando mayúsculas/minúsculas)
    List<Usuario> findByNombreContainingIgnoreCase(String nombre);
    
    // Verificar si existe por email
    boolean existsByEmail(String email);

}