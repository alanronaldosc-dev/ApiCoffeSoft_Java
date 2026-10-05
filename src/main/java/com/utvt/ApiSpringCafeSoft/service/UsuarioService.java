package com.utvt.ApiSpringCafeSoft.service;

import com.utvt.ApiSpringCafeSoft.dto.UsuarioDTO;
import com.utvt.ApiSpringCafeSoft.model.Sucursal;
import com.utvt.ApiSpringCafeSoft.model.Usuario;
import com.utvt.ApiSpringCafeSoft.repository.SucursalRepository;
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

    @Autowired
    private SucursalRepository sucursalRepository;

    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    private static final List<String> TODOS_LOS_PERMISOS = Arrays.asList(
        "crearProducto", "ventas", "pedidos", "productos", "usuarios",
        "reportes", "carrito", "registro", "insumos", "lotes", "categorias", "proveedores"
    );

    private static final List<String> PERMISOS_USUARIO = Arrays.asList(
        "productos", "pedidos", "ventas", "carrito"
    );

    // HU-011: REGISTRAR USUARIO
    public Usuario crearUsuario(Usuario usuario) {

        if (usuarioRepository.existsByEmail(usuario.getEmail())) {
            throw new RuntimeException("El email ya está registrado");
        }

        if (usuario.getUserTipo() == null) {
            usuario.setUserTipo(1);
        }

        if (usuario.getUserTipo() == 0) {
            usuario.setPermisos(new ArrayList<>(TODOS_LOS_PERMISOS));
        } else if (usuario.getUserTipo() == 1) {
            usuario.setPermisos(new ArrayList<>(PERMISOS_USUARIO));
        } else if (usuario.getUserTipo() == 2) {
            usuario.setPermisos(new ArrayList<>());
        } else if (usuario.getUserTipo() == 3) {
            if (usuario.getPermisos() == null || usuario.getPermisos().isEmpty()) {
                throw new RuntimeException("Debe seleccionar al menos un permiso para el usuario personalizado");
            }
            List<String> permisosValidos = usuario.getPermisos().stream()
                    .filter(TODOS_LOS_PERMISOS::contains)
                    .distinct()
                    .collect(Collectors.toList());
            if (permisosValidos.isEmpty()) {
                throw new RuntimeException("Los permisos seleccionados no son válidos");
            }
            usuario.setPermisos(permisosValidos);
        } else if (usuario.getUserTipo() == 4) {
            usuario.setPermisos(new ArrayList<>());
        } else {
            throw new RuntimeException("El tipo de usuario no es válido");
        }

        usuario.setPassword(passwordEncoder.encode(usuario.getPassword()));

        // Sucursal obligatoria
        if (usuario.getSucursal() == null || usuario.getSucursal().getId() == null) {
            throw new RuntimeException("Debe seleccionar una sucursal para el usuario");
        }
        Sucursal sucursal = sucursalRepository.findById(usuario.getSucursal().getId())
                .orElseThrow(() -> new RuntimeException("Sucursal no encontrada con ID: " + usuario.getSucursal().getId()));
        usuario.setSucursal(sucursal);

        return usuarioRepository.save(usuario);
    }

    // HU-011: CONSULTAR TODOS LOS USUARIOS
    public List<UsuarioDTO> obtenerTodosLosUsuarios() {
        return usuarioRepository.findAll().stream()
                .map(this::convertirADTO)
                .collect(Collectors.toList());
    }

    // HU-011: Consultar por ID
    public Optional<UsuarioDTO> obtenerUsuarioPorId(Long id) {
        return usuarioRepository.findById(id).map(this::convertirADTO);
    }

    // HU-011: Consultar por email
    public Optional<UsuarioDTO> obtenerUsuarioPorEmail(String email) {
        return usuarioRepository.findByEmail(email).map(this::convertirADTO);
    }

    // HU-011: Filtrar por tipo
    public List<UsuarioDTO> obtenerUsuariosPorTipo(Integer userTipo) {
        return usuarioRepository.findByUserTipo(userTipo).stream()
                .map(this::convertirADTO)
                .collect(Collectors.toList());
    }

    // HU-011: Buscar por nombre
    public List<UsuarioDTO> buscarUsuariosPorNombre(String nombre) {
        return usuarioRepository.findByNombreContainingIgnoreCase(nombre).stream()
                .map(this::convertirADTO)
                .collect(Collectors.toList());
    }

    // HU-011: Obtener empleados
    public List<UsuarioDTO> obtenerEmpleados() {
        return usuarioRepository.findByUserTipo(1).stream()
                .map(this::convertirADTO)
                .collect(Collectors.toList());
    }

    // HU-011: Activar / desactivar empleado
    public UsuarioDTO cambiarEstadoEmpleado(Long id, Boolean activo) {
        Usuario usuario = usuarioRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado con ID: " + id));
        if (usuario.getUserTipo() != 1) {
            throw new RuntimeException("El usuario seleccionado no es un empleado");
        }
        usuario.setActivo(activo);
        return convertirADTO(usuarioRepository.save(usuario));
    }

    // HU-011: ACTUALIZAR PERFIL
    public Usuario actualizarUsuario(Long id, Usuario usuarioActualizado) {

        Optional<Usuario> usuarioExistenteOpt = usuarioRepository.findById(id);
        if (usuarioExistenteOpt.isEmpty()) {
            throw new RuntimeException("Usuario no encontrado con ID: " + id);
        }

        Usuario usuarioExistente = usuarioExistenteOpt.get();

        if (usuarioActualizado.getNombre() != null) {
            usuarioExistente.setNombre(usuarioActualizado.getNombre());
        }

        if (usuarioActualizado.getEmail() != null) {
            Optional<Usuario> usuarioConEmail = usuarioRepository.findByEmail(usuarioActualizado.getEmail());
            if (usuarioConEmail.isPresent() && !usuarioConEmail.get().getId().equals(id)) {
                throw new RuntimeException("El email ya está registrado por otro usuario");
            }
            usuarioExistente.setEmail(usuarioActualizado.getEmail());
        }

        if (usuarioActualizado.getPassword() != null && !usuarioActualizado.getPassword().isEmpty()) {
            usuarioExistente.setPassword(passwordEncoder.encode(usuarioActualizado.getPassword()));
        }

        if (usuarioActualizado.getDireccion() != null) {
            usuarioExistente.setDireccion(usuarioActualizado.getDireccion());
        }

        if (usuarioActualizado.getTelefono() != null) {
            usuarioExistente.setTelefono(usuarioActualizado.getTelefono());
        }

        if (usuarioActualizado.getUserTipo() != null) {
            Integer nuevoTipo = usuarioActualizado.getUserTipo();
            if (nuevoTipo < 0 || nuevoTipo > 4) {
                throw new RuntimeException("El tipo de usuario no es válido");
            }
            usuarioExistente.setUserTipo(nuevoTipo);

            if (nuevoTipo == 0) {
                usuarioExistente.setPermisos(new ArrayList<>(TODOS_LOS_PERMISOS));
            } else if (nuevoTipo == 1) {
                usuarioExistente.setPermisos(new ArrayList<>(PERMISOS_USUARIO));
            } else if (nuevoTipo == 2) {
                usuarioExistente.setPermisos(new ArrayList<>());
            } else if (nuevoTipo == 3) {
                if (usuarioActualizado.getPermisos() == null || usuarioActualizado.getPermisos().isEmpty()) {
                    throw new RuntimeException("Debe seleccionar al menos un permiso");
                }
                List<String> permisosValidos = usuarioActualizado.getPermisos().stream()
                        .filter(TODOS_LOS_PERMISOS::contains)
                        .distinct()
                        .collect(Collectors.toList());
                if (permisosValidos.isEmpty()) {
                    throw new RuntimeException("Los permisos seleccionados no son válidos");
                }
                usuarioExistente.setPermisos(permisosValidos);
            }
        } else if (usuarioActualizado.getPermisos() != null) {
            List<String> permisosValidos = usuarioActualizado.getPermisos().stream()
                    .filter(TODOS_LOS_PERMISOS::contains)
                    .distinct()
                    .collect(Collectors.toList());
            usuarioExistente.setPermisos(permisosValidos);
        }

        // Actualizar sucursal si viene en la petición
        if (usuarioActualizado.getSucursal() != null && usuarioActualizado.getSucursal().getId() != null) {
            Sucursal sucursal = sucursalRepository.findById(usuarioActualizado.getSucursal().getId())
                    .orElseThrow(() -> new RuntimeException("Sucursal no encontrada con ID: " + usuarioActualizado.getSucursal().getId()));
            usuarioExistente.setSucursal(sucursal);
        }

        return usuarioRepository.save(usuarioExistente);
    }

    // HU-011: Eliminar usuario
    public void eliminarUsuario(Long id) {
        if (!usuarioRepository.existsById(id)) {
            throw new RuntimeException("Usuario no encontrado con ID: " + id);
        }
        usuarioRepository.deleteById(id);
    }

    // HU-011: Convertir entidad a DTO
    private UsuarioDTO convertirADTO(Usuario usuario) {
        return new UsuarioDTO(
                usuario.getId(),
                usuario.getNombre(),
                usuario.getEmail(),
                usuario.getDireccion(),
                usuario.getTelefono(),
                usuario.getUserTipo(),
                usuario.getActivo(),
                usuario.getPermisos(),
                usuario.getSucursal() != null ? usuario.getSucursal().getId() : null,
                usuario.getSucursal() != null ? usuario.getSucursal().getNombre() : null
        );
    }

    // Actualizar push token
    public void actualizarPushToken(Long id, String pushToken) {
        Usuario usuario = usuarioRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado con ID: " + id));
        usuario.setPushToken(pushToken);
        usuarioRepository.save(usuario);
    }

    // Obtener push tokens de empleados
    public List<String> getPushTokensEmpleados() {
        return usuarioRepository.findByUserTipo(1).stream()
                .map(Usuario::getPushToken)
                .filter(token -> token != null && !token.isEmpty())
                .collect(Collectors.toList());
    }

    // HU-011: Inicio de sesión
    public UsuarioDTO iniciarSesion(String email, String password) {
        Usuario usuario = usuarioRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Correo o contraseña incorrectos"));

        boolean passwordCorrecta = passwordEncoder.matches(password, usuario.getPassword());
        if (!passwordCorrecta) {
            throw new RuntimeException("Correo o contraseña incorrectos");
        }

        if (Boolean.FALSE.equals(usuario.getActivo())) {
            throw new RuntimeException("Esta cuenta está inactiva. No es posible iniciar sesión.");
        }

        return convertirADTO(usuario);
    }

    // Obtener usuarios por sucursal
    public List<UsuarioDTO> obtenerUsuariosPorSucursal(Long sucursalId) {
        return usuarioRepository.findBySucursalId(sucursalId).stream()
                .map(this::convertirADTO)
                .collect(Collectors.toList());
    }
}
