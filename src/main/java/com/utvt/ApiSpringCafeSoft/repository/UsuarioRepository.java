package com.utvt.ApiSpringCafeSoft.repository;

import com.utvt.ApiSpringCafeSoft.model.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * HU-011: Repositorio de acceso a datos para la gestión de perfiles de usuario.
 * Provee consultas para buscar por email, tipo y nombre, necesarias para
 * mantener la plantilla del personal operativa y actualizada.
 */
@Repository
public interface UsuarioRepository extends JpaRepository<Usuario, Long> {

    // HU-011: Buscar un perfil por su email (usado en login y validación de duplicados)
    Optional<Usuario> findByEmail(String email);

    // HU-011: Filtrar usuarios por tipo (0=Admin, 1=Empleado, 2=Cliente)
    List<Usuario> findByUserTipo(Integer userTipo);

    // HU-011: Búsqueda parcial por nombre para localizar empleados/clientes rápidamente
    List<Usuario> findByNombreContainingIgnoreCase(String nombre);

    // HU-011: Verificar si ya existe un perfil con ese email antes de registrarlo
    boolean existsByEmail(String email);

    // HU-011: Filtrar empleados activos o inactivos para mostrar en la tabla de la plantilla
    List<Usuario> findByUserTipoAndActivo(Integer userTipo, Boolean activo);

}