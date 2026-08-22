package com.utvt.ApiSpringCafeSoft.service;

import com.utvt.ApiSpringCafeSoft.dto.UsuarioDTO;
import com.utvt.ApiSpringCafeSoft.model.Usuario;
import com.utvt.ApiSpringCafeSoft.repository.UsuarioRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;


@Service
public class UsuarioService {
    
    @Autowired
    private UsuarioRepository usuarioRepository;
    
    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

     /*
     * ============================================
     * HU-015 - PERMISOS DEL SISTEMA
     * ============================================
     */
    private static final List<String> TODOS_LOS_PERMISOS = Arrays.asList(
        "crearProducto", "ventas", "pedidos", "productos", "usuarios",
        "reportes", "carrito", "registro", "insumos", "lotes", "categorias", "proveedores"
    );

    private static final List<String> PERMISOS_USUARIO = Arrays.asList(
        "productos", "pedidos", "ventas", "carrito"
    );

    
    // ============================================
    // HU-011: REGISTRAR USUARIO (Formulario de alta)
    // Crea un nuevo perfil de administrador, empleado o cliente
    // ============================================
    public Usuario crearUsuario(Usuario usuario) {

        if (usuarioRepository.existsByEmail(usuario.getEmail())) {
            throw new RuntimeException("El email ya está registrado");
        }

        /*
         * Si no viene tipo de usuario,
         * se crea como Usuario normal.
         */
        if (usuario.getUserTipo() == null) {
            usuario.setUserTipo(1);
        }

        /*
         * ========================================
         * ADMINISTRADOR
         * ========================================
         */
        if (usuario.getUserTipo() == 0) {

            usuario.setPermisos(
                    new ArrayList<>(TODOS_LOS_PERMISOS)
            );
        }

        /*
         * ========================================
         * USUARIO / EMPLEADO
         * ========================================
         */
        else if (usuario.getUserTipo() == 1) {

            usuario.setPermisos(
                    new ArrayList<>(PERMISOS_USUARIO)
            );
        }

        /*
         * ========================================
         * CLIENTE
         * ========================================
         */
        else if (usuario.getUserTipo() == 2) {

            /*
             * Los clientes no necesitan permisos
             * administrativos.
             */
            usuario.setPermisos(
                    new ArrayList<>()
            );
        }

        /*
         * ========================================
         * PERSONALIZADO - HU-015
         * ========================================
         */
        else if (usuario.getUserTipo() == 3) {

            if (usuario.getPermisos() == null ||
                    usuario.getPermisos().isEmpty()) {

                throw new RuntimeException(
                        "Debe seleccionar al menos un permiso para el usuario personalizado"
                );
            }

            /*
             * Solamente permitimos permisos
             * reconocidos por el sistema.
             */
            List<String> permisosValidos =
                    usuario.getPermisos()
                            .stream()
                            .filter(TODOS_LOS_PERMISOS::contains)
                            .distinct()
                            .collect(Collectors.toList());

            if (permisosValidos.isEmpty()) {
                throw new RuntimeException(
                        "Los permisos seleccionados no son válidos"
                );
            }

            usuario.setPermisos(permisosValidos);
        }

        /*
         * Tipo de usuario inválido.
         */
        else {
            throw new RuntimeException(
                    "El tipo de usuario no es válido"
            );
        }

        /*
         * Encriptar contraseña.
         */
        usuario.setPassword(
                passwordEncoder.encode(
                        usuario.getPassword()
                )
        );

        return usuarioRepository.save(usuario);
    }
    
    // ============================================
    // HU-011: CONSULTAR TODOS LOS USUARIOS
    // Permite al administrador ver la lista completa de perfiles
    // ============================================
    public List<UsuarioDTO> obtenerTodosLosUsuarios() {
        return usuarioRepository.findAll().stream()
                .map(this::convertirADTO)
                .collect(Collectors.toList());
    }
    
    // HU-011: Consultar perfil de un usuario por su ID
    public Optional<UsuarioDTO> obtenerUsuarioPorId(Long id) {
        return usuarioRepository.findById(id)
                .map(this::convertirADTO);
    }
    
    // HU-011: Consultar perfil de un usuario por su email
    public Optional<UsuarioDTO> obtenerUsuarioPorEmail(String email) {
        return usuarioRepository.findByEmail(email)
                .map(this::convertirADTO);
    }
    
    // HU-011: Filtrar perfiles por tipo (0=Admin, 1=Empleado, 2=Cliente)
    public List<UsuarioDTO> obtenerUsuariosPorTipo(Integer userTipo) {
        return usuarioRepository.findByUserTipo(userTipo).stream()
                .map(this::convertirADTO)
                .collect(Collectors.toList());
    }
    
    // HU-011: Buscar perfiles por nombre (búsqueda parcial, sin importar mayúsculas)
    public List<UsuarioDTO> buscarUsuariosPorNombre(String nombre) {
        return usuarioRepository.findByNombreContainingIgnoreCase(nombre).stream()
                .map(this::convertirADTO)
                .collect(Collectors.toList());
    }
    // HU-011: Obtener la lista de empleados (userTipo=1) para la tabla visible del administrador
    public List<UsuarioDTO> obtenerEmpleados() {
    return usuarioRepository.findByUserTipo(1).stream()
            .map(this::convertirADTO)
            .collect(Collectors.toList());
}

    // HU-011: Activar o desactivar un empleado para mantener la plantilla actualizada
    public UsuarioDTO cambiarEstadoEmpleado(Long id, Boolean activo) {

    Usuario usuario = usuarioRepository.findById(id)
            .orElseThrow(() ->
                    new RuntimeException("Usuario no encontrado con ID: " + id));

    if (usuario.getUserTipo() != 1) {
        throw new RuntimeException("El usuario seleccionado no es un empleado");
    }

    usuario.setActivo(activo);

    Usuario usuarioGuardado = usuarioRepository.save(usuario);

    return convertirADTO(usuarioGuardado);
}
    
    // ============================================
    // HU-011: ACTUALIZAR PERFIL DE USUARIO
    // Permite editar los datos básicos de cualquier perfil (nombre, email,
    // contraseña, dirección, teléfono y tipo), cumpliendo el criterio de
    // "formulario único para dar de alta y editar datos básicos del empleado"
    // ============================================
    public Usuario actualizarUsuario(
            Long id,
            Usuario usuarioActualizado
    ) {

        Optional<Usuario> usuarioExistenteOpt =
                usuarioRepository.findById(id);

        if (usuarioExistenteOpt.isEmpty()) {

            throw new RuntimeException(
                    "Usuario no encontrado con ID: " + id
            );
        }

        Usuario usuarioExistente =
                usuarioExistenteOpt.get();

        /*
         * Nombre
         */
        if (usuarioActualizado.getNombre() != null) {

            usuarioExistente.setNombre(
                    usuarioActualizado.getNombre()
            );
        }

        /*
         * Email
         */
        if (usuarioActualizado.getEmail() != null) {

            Optional<Usuario> usuarioConEmail =
                    usuarioRepository.findByEmail(
                            usuarioActualizado.getEmail()
                    );

            if (usuarioConEmail.isPresent()
                    && !usuarioConEmail.get()
                    .getId()
                    .equals(id)) {

                throw new RuntimeException(
                        "El email ya está registrado por otro usuario"
                );
            }

            usuarioExistente.setEmail(
                    usuarioActualizado.getEmail()
            );
        }

        /*
         * Contraseña
         */
        if (usuarioActualizado.getPassword() != null
                && !usuarioActualizado
                .getPassword()
                .isEmpty()) {

            usuarioExistente.setPassword(
                    passwordEncoder.encode(
                            usuarioActualizado.getPassword()
                    )
            );
        }

        /*
         * Dirección
         */
        if (usuarioActualizado.getDireccion() != null) {

            usuarioExistente.setDireccion(
                    usuarioActualizado.getDireccion()
            );
        }

        /*
         * Teléfono
         */
        if (usuarioActualizado.getTelefono() != null) {

            usuarioExistente.setTelefono(
                    usuarioActualizado.getTelefono()
            );
        }

        /*
         * ========================================
         * HU-015
         * Actualizar tipo y permisos
         * ========================================
         */
        if (usuarioActualizado.getUserTipo() != null) {

            Integer nuevoTipo =
                    usuarioActualizado.getUserTipo();

            if (nuevoTipo < 0 || nuevoTipo > 3) {

                throw new RuntimeException(
                        "El tipo de usuario no es válido"
                );
            }

            usuarioExistente.setUserTipo(nuevoTipo);

            /*
             * Administrador
             */
            if (nuevoTipo == 0) {

                usuarioExistente.setPermisos(
                        new ArrayList<>(
                                TODOS_LOS_PERMISOS
                        )
                );
            }

            /*
             * Usuario
             */
            else if (nuevoTipo == 1) {

                usuarioExistente.setPermisos(
                        new ArrayList<>(
                                PERMISOS_USUARIO
                        )
                );
            }

            /*
             * Cliente
             */
            else if (nuevoTipo == 2) {

                usuarioExistente.setPermisos(
                        new ArrayList<>()
                );
            }

            /*
             * Personalizado
             */
            else if (nuevoTipo == 3) {

                if (usuarioActualizado.getPermisos() == null
                        || usuarioActualizado
                        .getPermisos()
                        .isEmpty()) {

                    throw new RuntimeException(
                            "Debe seleccionar al menos un permiso"
                    );
                }

                List<String> permisosValidos =
                        usuarioActualizado
                                .getPermisos()
                                .stream()
                                .filter(TODOS_LOS_PERMISOS::contains)
                                .distinct()
                                .collect(Collectors.toList());

                if (permisosValidos.isEmpty()) {

                    throw new RuntimeException(
                            "Los permisos seleccionados no son válidos"
                    );
                }

                usuarioExistente.setPermisos(
                        permisosValidos
                );
            }
        }

        /*
         * Si solamente se actualizaron permisos
         * sin modificar userTipo.
         */
        else if (usuarioActualizado.getPermisos() != null) {

            List<String> permisosValidos =
                    usuarioActualizado
                            .getPermisos()
                            .stream()
                            .filter(TODOS_LOS_PERMISOS::contains)
                            .distinct()
                            .collect(Collectors.toList());

            usuarioExistente.setPermisos(
                    permisosValidos
            );
        }

        return usuarioRepository.save(
                usuarioExistente
        );
    }

    
    // HU-011: Eliminar un perfil del sistema (borrado físico)
    public void eliminarUsuario(Long id) {
        if (!usuarioRepository.existsById(id)) {
            throw new RuntimeException("Usuario no encontrado con ID: " + id);
        }
        usuarioRepository.deleteById(id);
    }
    
    
    
    // HU-011: Convierte la entidad Usuario a DTO para no exponer la contraseña en las respuestas
    private UsuarioDTO convertirADTO(Usuario usuario) {
        return new UsuarioDTO(
            usuario.getId(),
            usuario.getNombre(),
            usuario.getEmail(),
            usuario.getDireccion(),
            usuario.getTelefono(),
            usuario.getUserTipo(),
            usuario.getActivo(),       // tu campo activo
            usuario.getPermisos()      // nuevo campo permisos
        );
    }



    // Actualizar push token
    public void actualizarPushToken(Long id, String pushToken) {
        Usuario usuario = usuarioRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado con ID: " + id));
        usuario.setPushToken(pushToken);
        usuarioRepository.save(usuario);
    }

    // Obtener push tokens de todos los empleados (userTipo = 1)
    public List<String> getPushTokensEmpleados() {

        return usuarioRepository
                .findByUserTipo(1)
                .stream()
                .map(Usuario::getPushToken)
                .filter(token ->
                        token != null &&
                        !token.isEmpty()
                )
                .collect(Collectors.toList());
    }

    // HU-011: Inicio de sesión — valida credenciales y verifica que la cuenta esté activa
    public UsuarioDTO iniciarSesion(String email, String password) {

    Usuario usuario = usuarioRepository.findByEmail(email)
            .orElseThrow(() ->
                    new RuntimeException("Correo o contraseña incorrectos"));

    boolean passwordCorrecta = passwordEncoder.matches(
            password,
            usuario.getPassword()
    );

    if (!passwordCorrecta) {
        throw new RuntimeException("Correo o contraseña incorrectos");
    }

    // Verificar si la cuenta está activa
    if (Boolean.FALSE.equals(usuario.getActivo())) {
        throw new RuntimeException(
                "Esta cuenta está inactiva. No es posible iniciar sesión."
        );
    }

    return convertirADTO(usuario);
}
}