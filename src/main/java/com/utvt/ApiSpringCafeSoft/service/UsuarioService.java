// ============================================
// HU-008 - VALIDACION DE INICIO DE SESION
// Verifica las credenciales del usuario antes
// de permitir el acceso al sistema.
// ============================================

package com.utvt.ApiSpringCafeSoft.service;

import com.utvt.ApiSpringCafeSoft.dto.UsuarioDTO;
import com.utvt.ApiSpringCafeSoft.model.Usuario;
import com.utvt.ApiSpringCafeSoft.repository.UsuarioRepository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class UsuarioService {

    @Autowired
    private UsuarioRepository usuarioRepository;

    private final BCryptPasswordEncoder passwordEncoder =
            new BCryptPasswordEncoder();

    /*
     * ============================================
     * HU-015 - PERMISOS DEL SISTEMA
     * ============================================
     */

    private static final List<String> TODOS_LOS_PERMISOS = Arrays.asList(
            "crearProducto",
            "ventas",
            "pedidos",
            "productos",
            "usuarios",
            "reportes",
            "carrito",
            "registro",
            "insumos",
            "lotes",
            "categorias",
            "proveedores"
    );

    /*
     * Permisos predeterminados para usuarios normales.
     */
    private static final List<String> PERMISOS_USUARIO = Arrays.asList(
            "productos",
            "pedidos",
            "ventas",
            "carrito"
    );

    /*
     * ============================================
     * CREAR USUARIO
     * ============================================
     */
    public Usuario crearUsuario(Usuario usuario) {

        if (usuarioRepository.existsByEmail(usuario.getEmail())) {
            throw new RuntimeException("El email ya está registrado");
        }

        // Encriptar la contraseña
        usuario.setPassword(
            passwordEncoder.encode(usuario.getPassword())
        );

        usuario.setActivo(true);

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

        return usuarioRepository.save(usuario);
    }

    /*
     * ============================================
     * OBTENER TODOS
     * ============================================
     */
    public List<UsuarioDTO> obtenerTodosLosUsuarios() {
        return usuarioRepository.findAll()
                .stream()
                .map(this::convertirADTO)
                .collect(Collectors.toList());
    }

    /*
     * ============================================
     * OBTENER POR ID
     * ============================================
     */
    public Optional<UsuarioDTO> obtenerUsuarioPorId(Long id) {
        return usuarioRepository.findById(id)
                .map(this::convertirADTO);
    }

    /*
     * ============================================
     * OBTENER POR EMAIL
     * ============================================
     */
    public Optional<UsuarioDTO> obtenerUsuarioPorEmail(String email) {
        return usuarioRepository.findByEmail(email)
                .map(this::convertirADTO);
    }

    /*
     * ============================================
     * OBTENER POR TIPO
     * ============================================
     */
    public List<UsuarioDTO> obtenerUsuariosPorTipo(Integer userTipo) {
        return usuarioRepository
                .findByUserTipo(userTipo)
                .stream()
                .map(this::convertirADTO)
                .collect(Collectors.toList());
    }

    /*
     * ============================================
     * BUSCAR POR NOMBRE
     * ============================================
     */
    public List<UsuarioDTO> buscarUsuariosPorNombre(String nombre) {
        return usuarioRepository
                .findByNombreContainingIgnoreCase(nombre)
                .stream()
                .map(this::convertirADTO)
                .collect(Collectors.toList());
    }

    /*
     * ============================================
     * OBTENER EMPLEADOS
     * ============================================
     */
    public List<UsuarioDTO> obtenerEmpleados() {
        return usuarioRepository.findByUserTipo(1).stream()
                .map(this::convertirADTO)
                .collect(Collectors.toList());
    }

    /*
     * ============================================
     * CAMBIAR ESTADO DE EMPLEADO
     * ============================================
     */
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

    /*
     * ============================================
     * ACTUALIZAR USUARIO
     * ============================================
     */
    public Usuario actualizarUsuario(Long id, Usuario usuarioActualizado) {
        Optional<Usuario> usuarioExistenteOpt = usuarioRepository.findById(id);
        
        if (usuarioExistenteOpt.isEmpty()) {
            throw new RuntimeException(
                    "Usuario no encontrado con ID: " + id
            );
        }

        Usuario usuarioExistente = usuarioExistenteOpt.get();

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
         * Estado activo
         */
        if (usuarioActualizado.getActivo() != null) {
            usuarioExistente.setActivo(usuarioActualizado.getActivo());
        }

        /*
         * ========================================
         * HU-015
         * Actualizar tipo y permisos
         * ========================================
         */
        if (usuarioActualizado.getUserTipo() != null) {
            Integer nuevoTipo = usuarioActualizado.getUserTipo();

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
                        new ArrayList<>(TODOS_LOS_PERMISOS)
                );
            }

            /*
             * Usuario
             */
            else if (nuevoTipo == 1) {
                usuarioExistente.setPermisos(
                        new ArrayList<>(PERMISOS_USUARIO)
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

        return usuarioRepository.save(usuarioExistente);
    }

    /*
     * ============================================
     * ELIMINAR
     * ============================================
     */
    public void eliminarUsuario(Long id) {
        if (!usuarioRepository.existsById(id)) {
            throw new RuntimeException(
                    "Usuario no encontrado con ID: " + id
            );
        }
        usuarioRepository.deleteById(id);
    }

    /*
     * ============================================
     * CONVERTIR A DTO
     * ============================================
     */
    private UsuarioDTO convertirADTO(Usuario usuario) {
        // Convertir a DTO con todos los campos
        return new UsuarioDTO(
            usuario.getId(),
            usuario.getNombre(),
            usuario.getEmail(),
            usuario.getDireccion(),
            usuario.getTelefono(),
            usuario.getUserTipo(),
            usuario.getActivo(),
            usuario.getPermisos()
        );
    }

    /*
     * ============================================
     * PUSH TOKEN
     * ============================================
     */
    public void actualizarPushToken(Long id, String pushToken) {
        Usuario usuario = usuarioRepository
                .findById(id)
                .orElseThrow(
                        () -> new RuntimeException(
                                "Usuario no encontrado con ID: " + id
                        )
                );

        usuario.setPushToken(pushToken);
        usuarioRepository.save(usuario);
    }

    /*
     * ============================================
     * PUSH TOKENS DE EMPLEADOS
     * ============================================
     */
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

    /*
     * ============================================
     * LOGIN - HU-008
     * ============================================
     */
    public UsuarioDTO iniciarSesion(String email, String password) {
        Usuario usuario = usuarioRepository.findByEmail(email)
                .orElseThrow(() ->
                        new RuntimeException("Correo o contraseña incorrectos"));

        boolean passwordCorrecta = passwordEncoder.matches(
                password,
                usuario.getPassword()
        );

        if (!passwordCorrecta) {
            throw new RuntimeException(
                    "Correo o contraseña incorrectos"
            );
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