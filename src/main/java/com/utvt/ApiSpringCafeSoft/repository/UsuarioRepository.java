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

    // Buscar por token de recuperación
    Optional<Usuario> findByResetToken(String resetToken);
    
    // Buscar por tipo de usuario
    List<Usuario> findByUserTipo(Integer userTipo);
    
    // Buscar por nombre (contiene, ignorando mayúsculas/minúsculas)
    List<Usuario> findByNombreContainingIgnoreCase(String nombre);

    @Repository
public interface UsuarioRepository extends JpaRepository<Usuario, Long> {

    Optional<Usuario> findByEmail(String email);

    Optional<Usuario> findByResetToken(String resetToken);

    List<Usuario> findByUserTipo(Integer userTipo);

    List<Usuario> findByUserTipoAndActivo(Integer userTipo, Boolean activo);

    List<Usuario> findByNombreContainingIgnoreCase(String nombre);

    boolean existsByEmail(String email);
}
    
    // Verificar si existe por email
    boolean existsByEmail(String email);



    

}
