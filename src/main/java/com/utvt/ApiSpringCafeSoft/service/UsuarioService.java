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

@Service
public class UsuarioService {
    
    @Autowired
    private UsuarioRepository usuarioRepository;
    
    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
    
    // CRUD - Create
    public Usuario crearUsuario(Usuario usuario) {
        // Validar que el email no exista
        if (usuarioRepository.existsByEmail(usuario.getEmail())) {
            throw new RuntimeException("El email ya está registrado");
        }
        
        // Encriptar la contraseña
        usuario.setPassword(passwordEncoder.encode(usuario.getPassword()));

        
        return usuarioRepository.save(usuario);
    }
    
    // CRUD - Read (Todos)
    public List<UsuarioDTO> obtenerTodosLosUsuarios() {
        return usuarioRepository.findAll().stream()
                .map(this::convertirADTO)
                .collect(Collectors.toList());
    }
    
    // CRUD - Read (Por ID)
    public Optional<UsuarioDTO> obtenerUsuarioPorId(Long id) {
        return usuarioRepository.findById(id)
                .map(this::convertirADTO);
    }
    
    // CRUD - Read (Por Email)
    public Optional<UsuarioDTO> obtenerUsuarioPorEmail(String email) {
        return usuarioRepository.findByEmail(email)
                .map(this::convertirADTO);
    }
    
    // CRUD - Read (Por Tipo de Usuario)
    public List<UsuarioDTO> obtenerUsuariosPorTipo(Integer userTipo) {
        return usuarioRepository.findByUserTipo(userTipo).stream()
                .map(this::convertirADTO)
                .collect(Collectors.toList());
    }
    
    // CRUD - Read (Por Nombre - Búsqueda)
    public List<UsuarioDTO> buscarUsuariosPorNombre(String nombre) {
        return usuarioRepository.findByNombreContainingIgnoreCase(nombre).stream()
                .map(this::convertirADTO)
                .collect(Collectors.toList());
    }
    
    // CRUD - Update
    public Usuario actualizarUsuario(Long id, Usuario usuarioActualizado) {
        Optional<Usuario> usuarioExistenteOpt = usuarioRepository.findById(id);
        
        if (usuarioExistenteOpt.isEmpty()) {
            throw new RuntimeException("Usuario no encontrado con ID: " + id);
        }
        
        Usuario usuarioExistente = usuarioExistenteOpt.get();
        
        // Actualizar solo los campos que no sean nulos
        if (usuarioActualizado.getNombre() != null) {
            usuarioExistente.setNombre(usuarioActualizado.getNombre());
        }
        
        if (usuarioActualizado.getEmail() != null) {
            // Verificar que el nuevo email no esté en uso por otro usuario
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
            usuarioExistente.setUserTipo(usuarioActualizado.getUserTipo());
        }
        
        
        return usuarioRepository.save(usuarioExistente);
    }
    
    // CRUD - Delete
    public void eliminarUsuario(Long id) {
        if (!usuarioRepository.existsById(id)) {
            throw new RuntimeException("Usuario no encontrado con ID: " + id);
        }
        usuarioRepository.deleteById(id);
    }
    
    
    
    // Método auxiliar para convertir Usuario a UsuarioDTO
    private UsuarioDTO convertirADTO(Usuario usuario) {
        return new UsuarioDTO(
                usuario.getId(),
                usuario.getNombre(),
                usuario.getEmail(),
                usuario.getDireccion(),
                usuario.getTelefono(),
                usuario.getUserTipo()
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
        return usuarioRepository.findByUserTipo(1).stream()
                .map(u -> u.getPushToken())
                .filter(token -> token != null && !token.isEmpty())
                .collect(Collectors.toList());
    }

    public UsuarioDTO iniciarSesion(String email, String password) {

    Usuario usuario = usuarioRepository.findByEmail(email)
            .orElseThrow(() -> new RuntimeException("Correo o contraseña incorrectos"));

    boolean passwordCorrecta = passwordEncoder.matches(
            password,
            usuario.getPassword()
    );

    if (!passwordCorrecta) {
        throw new RuntimeException("Correo o contraseña incorrectos");
    }

    return convertirADTO(usuario);
}
public void restablecerPassword(String email, String codigo, String nuevaPassword) {
    // 1. Buscar usuario por email
    Usuario usuario = usuarioRepository.findByEmail(email)
            .orElseThrow(() -> new RuntimeException("Usuario no encontrado con email: " + email));

    // 2. Validar que el código sea correcto y no esté expirado
    // Aquí deberías tener una tabla o campo en Usuario que guarde el código y su fecha de expiración.
    if (usuario.getResetToken() == null || !usuario.getResetToken().equals(codigo)) {
        throw new RuntimeException("Código inválido");
    }
    if (usuario.getResetTokenExpiresAt().isBefore(LocalDateTime.now())) {
        throw new RuntimeException("Código expirado");
    }

    // 3. Encriptar la nueva contraseña con BCrypt
    usuario.setPassword(passwordEncoder.encode(nuevaPassword));

    // 4. Limpiar el token para que no se reutilice
    usuario.setResetToken(null);
    usuario.setResetTokenExpiresAt(null);

    // 5. Guardar el usuario actualizado
    usuarioRepository.save(usuario);
}


}
