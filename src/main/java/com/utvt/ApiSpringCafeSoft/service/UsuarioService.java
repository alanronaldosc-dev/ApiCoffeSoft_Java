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


    // ============================================
    // ACTIVIDAD 07 - SEGURIDAD
    // PROTECCIÓN DE CREDENCIALES CON BCRYPT
    // ============================================
    //
    // BCrypt se utiliza para cifrar las contraseñas
    // antes de guardarlas y para verificarlas
    // durante el inicio de sesión.
    //
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
            "proveedores",
            "cargas",
            "liquidaciones"
    );


    private static final List<String> PERMISOS_USUARIO = Arrays.asList(
            "productos",
            "pedidos",
            "ventas",
            "carrito",
            "cargas",
            "liquidaciones"
    );


    // ============================================
    // HU-011: REGISTRAR USUARIO
    // Formulario de alta
    // ============================================
    //
    // Crea un nuevo perfil de administrador,
    // empleado, cliente, personalizado o repartidor.
    //
    public Usuario crearUsuario(Usuario usuario) {

        if (usuarioRepository.existsByEmail(usuario.getEmail())) {

            throw new RuntimeException(
                    "El email ya está registrado"
            );
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
                    new ArrayList<>(
                            TODOS_LOS_PERMISOS
                    )
            );
        }


        /*
         * ========================================
         * USUARIO / EMPLEADO
         * ========================================
         */
        else if (usuario.getUserTipo() == 1) {

            usuario.setPermisos(
                    new ArrayList<>(
                            PERMISOS_USUARIO
                    )
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

            if (
                    usuario.getPermisos() == null ||
                    usuario.getPermisos().isEmpty()
            ) {

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
                            .filter(
                                    TODOS_LOS_PERMISOS::contains
                            )
                            .distinct()
                            .collect(
                                    Collectors.toList()
                            );


            if (permisosValidos.isEmpty()) {

                throw new RuntimeException(
                        "Los permisos seleccionados no son válidos"
                );
            }


            usuario.setPermisos(
                    permisosValidos
            );
        }


        /*
         * ========================================
         * REPARTIDOR
         * ========================================
         */
        else if (usuario.getUserTipo() == 4) {

            // Repartidor sin permisos administrativos
            usuario.setPermisos(
                    new ArrayList<>()
            );
        }


        /*
         * Tipo de usuario inválido.
         */
        else {

            throw new RuntimeException(
                    "El tipo de usuario no es válido"
            );
        }


        // ============================================
        // ACTIVIDAD 07 - SEGURIDAD
        // MECANISMO 2: PROTECCIÓN DE CREDENCIALES
        // ============================================
        //
        // La contraseña NO se guarda directamente
        // como texto normal en la base de datos.
        //
        // BCrypt transforma la contraseña antes
        // de almacenarla.
        //
        usuario.setPassword(
                passwordEncoder.encode(
                        usuario.getPassword()
                )
        );


        return usuarioRepository.save(
                usuario
        );
    }


    // ============================================
    // HU-011: CONSULTAR TODOS LOS USUARIOS
    // ============================================
    //
    // Permite al administrador ver la lista
    // completa de perfiles.
    //
    public List<UsuarioDTO> obtenerTodosLosUsuarios() {

        return usuarioRepository
                .findAll()
                .stream()
                .map(this::convertirADTO)
                .collect(
                        Collectors.toList()
                );
    }


    // ============================================
    // HU-011: CONSULTAR USUARIO POR ID
    // ============================================
    public Optional<UsuarioDTO> obtenerUsuarioPorId(Long id) {

        return usuarioRepository
                .findById(id)
                .map(this::convertirADTO);
    }


    // ============================================
    // HU-011: CONSULTAR USUARIO POR EMAIL
    // ============================================
    public Optional<UsuarioDTO> obtenerUsuarioPorEmail(String email) {

        return usuarioRepository
                .findByEmail(email)
                .map(this::convertirADTO);
    }


    // ============================================
    // HU-011: FILTRAR USUARIOS POR TIPO
    // ============================================
    //
    // 0 = Administrador
    // 1 = Empleado
    // 2 = Cliente
    // 3 = Personalizado
    // 4 = Repartidor
    //
    public List<UsuarioDTO> obtenerUsuariosPorTipo(
            Integer userTipo
    ) {

        return usuarioRepository
                .findByUserTipo(userTipo)
                .stream()
                .map(this::convertirADTO)
                .collect(
                        Collectors.toList()
                );
    }


    // ============================================
    // HU-011: BUSCAR USUARIO POR NOMBRE
    // ============================================
    public List<UsuarioDTO> buscarUsuariosPorNombre(
            String nombre
    ) {

        return usuarioRepository
                .findByNombreContainingIgnoreCase(nombre)
                .stream()
                .map(this::convertirADTO)
                .collect(
                        Collectors.toList()
                );
    }


    // ============================================
    // HU-011: OBTENER EMPLEADOS
    // ============================================
    //
    // Obtiene todos los usuarios con
    // userTipo = 1.
    //
    public List<UsuarioDTO> obtenerEmpleados() {

        return usuarioRepository
                .findByUserTipo(1)
                .stream()
                .map(this::convertirADTO)
                .collect(
                        Collectors.toList()
                );
    }


    // ============================================
    // HU-011: CAMBIAR ESTADO DE EMPLEADO
    // ============================================
    public UsuarioDTO cambiarEstadoEmpleado(
            Long id,
            Boolean activo
    ) {

        Usuario usuario =
                usuarioRepository
                        .findById(id)
                        .orElseThrow(
                                () ->
                                        new RuntimeException(
                                                "Usuario no encontrado con ID: " + id
                                        )
                        );


        if (usuario.getUserTipo() != 1) {

            throw new RuntimeException(
                    "El usuario seleccionado no es un empleado"
            );
        }


        usuario.setActivo(
                activo
        );


        Usuario usuarioGuardado =
                usuarioRepository.save(
                        usuario
                );


        return convertirADTO(
                usuarioGuardado
        );
    }


    // ============================================
    // HU-011: ACTUALIZAR PERFIL DE USUARIO
    // ============================================
    //
    // Permite editar los datos básicos de
    // cualquier perfil:
    //
    // nombre
    // email
    // contraseña
    // dirección
    // teléfono
    // tipo de usuario
    // permisos
    //
    public Usuario actualizarUsuario(
            Long id,
            Usuario usuarioActualizado
    ) {


        Optional<Usuario> usuarioExistenteOpt =
                usuarioRepository.findById(
                        id
                );


        if (usuarioExistenteOpt.isEmpty()) {

            throw new RuntimeException(
                    "Usuario no encontrado con ID: " + id
            );
        }


        Usuario usuarioExistente =
                usuarioExistenteOpt.get();


        /*
         * ========================================
         * NOMBRE
         * ========================================
         */
        if (usuarioActualizado.getNombre() != null) {

            usuarioExistente.setNombre(
                    usuarioActualizado.getNombre()
            );
        }


        /*
         * ========================================
         * EMAIL
         * ========================================
         */
        if (usuarioActualizado.getEmail() != null) {

            Optional<Usuario> usuarioConEmail =
                    usuarioRepository.findByEmail(
                            usuarioActualizado.getEmail()
                    );


            if (
                    usuarioConEmail.isPresent()
                    &&
                    !usuarioConEmail
                            .get()
                            .getId()
                            .equals(id)
            ) {

                throw new RuntimeException(
                        "El email ya está registrado por otro usuario"
                );
            }


            usuarioExistente.setEmail(
                    usuarioActualizado.getEmail()
            );
        }


        /*
         * ========================================
         * CONTRASEÑA
         * ========================================
         */
        if (
                usuarioActualizado.getPassword() != null
                &&
                !usuarioActualizado
                        .getPassword()
                        .isEmpty()
        ) {


            // ============================================
            // ACTIVIDAD 07 - SEGURIDAD
            // PROTECCIÓN AL ACTUALIZAR CONTRASEÑA
            // ============================================
            //
            // Si el usuario cambia su contraseña,
            // la nueva contraseña también se cifra
            // utilizando BCrypt.
            //
            usuarioExistente.setPassword(
                    passwordEncoder.encode(
                            usuarioActualizado.getPassword()
                    )
            );
        }


        /*
         * ========================================
         * DIRECCIÓN
         * ========================================
         */
        if (usuarioActualizado.getDireccion() != null) {

            usuarioExistente.setDireccion(
                    usuarioActualizado.getDireccion()
            );
        }


        /*
         * ========================================
         * TELÉFONO
         * ========================================
         */
        if (usuarioActualizado.getTelefono() != null) {

            usuarioExistente.setTelefono(
                    usuarioActualizado.getTelefono()
            );
        }


        /*
         * ========================================
         * HU-015
         * ACTUALIZAR TIPO Y PERMISOS
         * ========================================
         */
        if (usuarioActualizado.getUserTipo() != null) {


            Integer nuevoTipo =
                    usuarioActualizado.getUserTipo();


            if (
                    nuevoTipo < 0 ||
                    nuevoTipo > 4
            ) {

                throw new RuntimeException(
                        "El tipo de usuario no es válido"
                );
            }


            usuarioExistente.setUserTipo(
                    nuevoTipo
            );


            /*
             * ========================================
             * ADMINISTRADOR
             * ========================================
             */
            if (nuevoTipo == 0) {

                usuarioExistente.setPermisos(
                        new ArrayList<>(
                                TODOS_LOS_PERMISOS
                        )
                );
            }


            /*
             * ========================================
             * USUARIO / EMPLEADO
             * ========================================
             */
            else if (nuevoTipo == 1) {

                usuarioExistente.setPermisos(
                        new ArrayList<>(
                                PERMISOS_USUARIO
                        )
                );
            }


            /*
             * ========================================
             * CLIENTE
             * ========================================
             */
            else if (nuevoTipo == 2) {

                usuarioExistente.setPermisos(
                        new ArrayList<>()
                );
            }


            /*
             * ========================================
             * PERSONALIZADO
             * ========================================
             */
            else if (nuevoTipo == 3) {

                if (
                        usuarioActualizado.getPermisos() == null
                        ||
                        usuarioActualizado
                                .getPermisos()
                                .isEmpty()
                ) {

                    throw new RuntimeException(
                            "Debe seleccionar al menos un permiso"
                    );
                }


                List<String> permisosValidos =
                        usuarioActualizado
                                .getPermisos()
                                .stream()
                                .filter(
                                        TODOS_LOS_PERMISOS::contains
                                )
                                .distinct()
                                .collect(
                                        Collectors.toList()
                                );


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
         * ========================================
         * ACTUALIZAR SOLO PERMISOS
         * ========================================
         *
         * Si solamente se actualizaron permisos
         * sin modificar userTipo.
         */
        else if (
                usuarioActualizado.getPermisos() != null
        ) {

            List<String> permisosValidos =
                    usuarioActualizado
                            .getPermisos()
                            .stream()
                            .filter(
                                    TODOS_LOS_PERMISOS::contains
                            )
                            .distinct()
                            .collect(
                                    Collectors.toList()
                            );


            usuarioExistente.setPermisos(
                    permisosValidos
            );
        }


        return usuarioRepository.save(
                usuarioExistente
        );
    }


    // ============================================
    // HU-011: ELIMINAR USUARIO
    // ============================================
    public void eliminarUsuario(Long id) {

        if (!usuarioRepository.existsById(id)) {

            throw new RuntimeException(
                    "Usuario no encontrado con ID: " + id
            );
        }


        usuarioRepository.deleteById(
                id
        );
    }


    // ============================================
    // ACTIVIDAD 07 - SEGURIDAD
    // PROTECCIÓN DE DATOS SENSIBLES
    // ============================================
    //
    // Se utiliza UsuarioDTO para enviar únicamente
    // los datos necesarios hacia la aplicación móvil.
    //
    // La contraseña NO se incluye en la respuesta
    // de la API.
    //
    private UsuarioDTO convertirADTO(
            Usuario usuario
    ) {

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


    // ============================================
    // ACTUALIZAR PUSH TOKEN
    // ============================================
    public void actualizarPushToken(
            Long id,
            String pushToken
    ) {

        Usuario usuario =
                usuarioRepository
                        .findById(id)
                        .orElseThrow(
                                () ->
                                        new RuntimeException(
                                                "Usuario no encontrado con ID: " + id
                                        )
                        );


        usuario.setPushToken(
                pushToken
        );


        usuarioRepository.save(
                usuario
        );
    }


    // ============================================
    // OBTENER PUSH TOKENS DE EMPLEADOS
    // ============================================
    //
    // userTipo = 1
    //
    public List<String> getPushTokensEmpleados() {

        return usuarioRepository
                .findByUserTipo(1)
                .stream()
                .map(
                        Usuario::getPushToken
                )
                .filter(
                        token ->
                                token != null
                                &&
                                !token.isEmpty()
                )
                .collect(
                        Collectors.toList()
                );
    }


    // ============================================
    // HU-011: INICIO DE SESIÓN
    // ============================================
    //
    // ACTIVIDAD 07 - SEGURIDAD
    // VERIFICACIÓN SEGURA DE CREDENCIALES
    //
    public UsuarioDTO iniciarSesion(
            String email,
            String password
    ) {


        // ============================================
        // SEGURIDAD:
        // BUSCAR USUARIO POR CORREO
        // ============================================
        //
        // Se utiliza el mismo mensaje tanto si el
        // correo no existe como si la contraseña
        // es incorrecta.
        //
        // Esto evita revelar información sobre
        // qué usuarios existen en el sistema.
        //
        Usuario usuario =
                usuarioRepository
                        .findByEmail(email)
                        .orElseThrow(
                                () ->
                                        new RuntimeException(
                                                "Correo o contraseña incorrectos"
                                        )
                        );


        // ============================================
        // ACTIVIDAD 07 - SEGURIDAD
        // COMPARACIÓN SEGURA CON BCRYPT
        // ============================================
        //
        // La contraseña escrita por el usuario
        // NO se compara directamente.
        //
        // BCrypt compara la contraseña ingresada
        // con el hash almacenado en la base
        // de datos.
        //
        boolean passwordCorrecta =
                passwordEncoder.matches(
                        password,
                        usuario.getPassword()
                );


        // ============================================
        // MANEJO SEGURO DEL ERROR
        // ============================================
        //
        // Si la contraseña es incorrecta se muestra
        // un mensaje genérico.
        //
        // No se indica específicamente si falló
        // el correo o la contraseña.
        //
        if (!passwordCorrecta) {

            throw new RuntimeException(
                    "Correo o contraseña incorrectos"
            );
        }


        // ============================================
        // VERIFICAR ESTADO DE LA CUENTA
        // ============================================
        if (
                Boolean.FALSE.equals(
                        usuario.getActivo()
                )
        ) {

            throw new RuntimeException(
                    "Esta cuenta está inactiva. No es posible iniciar sesión."
            );
        }


        // ============================================
        // ACTIVIDAD 07 - SEGURIDAD
        // RESPUESTA SIN CONTRASEÑA
        // ============================================
        //
        // El método convertirADTO devuelve solamente
        // los datos necesarios del usuario y evita
        // enviar la contraseña a la aplicación móvil.
        //
        return convertirADTO(
                usuario
        );
    }
}