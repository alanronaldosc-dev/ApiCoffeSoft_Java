package com.utvt.ApiSpringCafeSoft.controller;

import com.utvt.ApiSpringCafeSoft.dto.UsuarioDTO;
import com.utvt.ApiSpringCafeSoft.model.Usuario;
import com.utvt.ApiSpringCafeSoft.service.UsuarioService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/usuarios")
@CrossOrigin(origins = "*")
@Tag(name = "👤 Usuarios", description = "API para la gestión completa de usuarios del sistema CoffeeSoft")
public class UsuarioController {
    
    @Autowired
    private UsuarioService usuarioService;

    // ============================================
    // 📝 1. CREAR USUARIO - POST
    // ============================================
    
    @Operation(
        summary = "📝 Crear un nuevo usuario",
        description = """
            Registra un nuevo usuario en el sistema con todos sus datos.
            
            ### 🔐 Seguridad:
            - La contraseña se encripta automáticamente con BCrypt
            - El email debe ser de Gmail o Hotmail
            - El teléfono debe tener exactamente 10 dígitos
            
            ### 📋 Validaciones:
            - Nombre: mínimo 2 caracteres, máximo 100
            - Email: formato válido y dominio permitido
            - Contraseña: mínimo 8 caracteres
            - Teléfono: exactamente 10 dígitos numéricos
            - Tipo de usuario: 0, 1 o 2
            """
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "201", 
            description = "✅ Usuario creado exitosamente",
            content = @Content(
                mediaType = "application/json",
                examples = @ExampleObject(
                    name = "Respuesta exitosa",
                    value = """
                        {
                            "mensaje": "Usuario creado exitosamente",
                            "usuario": {
                                "id": 1,
                                "nombre": "Alan Hernández",
                                "email": "alan@gmail.com",
                                "direccion": "Calle Emiliano Zapata #45",
                                "telefono": "7203533170",
                                "userTipo": 0
                            }
                        }
                        """
                )
            )
        ),
        @ApiResponse(
            responseCode = "400", 
            description = "❌ Datos inválidos o email ya registrado",
            content = @Content(
                examples = @ExampleObject(
                    value = """
                        {
                            "error": "El email ya está registrado"
                        }
                        """
                )
            )
        ),
        @ApiResponse(responseCode = "500", description = "❌ Error interno del servidor")
    })
    @PostMapping
    public ResponseEntity<Map<String, Object>> crearUsuario(
            @Valid @RequestBody 
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                description = "📦 Datos del usuario a crear (todos los campos son obligatorios)",
                required = true,
                content = @Content(
                    mediaType = "application/json",
                    examples = {
                        @ExampleObject(
                            name = "👑 Usuario Administrador",
                            summary = "Crear un administrador",
                            value = """
                                {
                                    "nombre": "Alan Hernández",
                                    "email": "alan@gmail.com",
                                    "password": "admin123456",
                                    "direccion": "Calle Emiliano Zapata #45, Colonia Centro",
                                    "telefono": "7203533170",
                                    "userTipo": 0
                                }
                                """
                        ),
                        @ExampleObject(
                            name = "👔 Usuario Empleado",
                            summary = "Crear un empleado",
                            value = """
                                {
                                    "nombre": "María González",
                                    "email": "maria@gmail.com",
                                    "password": "empleado123",
                                    "direccion": "Avenida Morelos #123, Colonia Reforma",
                                    "telefono": "7221234567",
                                    "userTipo": 1
                                }
                                """
                        ),
                        @ExampleObject(
                            name = "👤 Usuario Cliente",
                            summary = "Crear un cliente",
                            value = """
                                {
                                    "nombre": "Carlos López",
                                    "email": "carlos@gmail.com",
                                    "password": "cliente12345",
                                    "direccion": "Calle Hidalgo #78, Colonia Juárez",
                                    "telefono": "7339876543",
                                    "userTipo": 2
                                }
                                """
                        )
                    }
                )
            ) 
            Usuario usuario) {
        try {
            Usuario nuevoUsuario = usuarioService.crearUsuario(usuario);
            Map<String, Object> response = new HashMap<>();
            response.put("mensaje", "✅ Usuario creado exitosamente");
            response.put("usuario", usuarioService.obtenerUsuarioPorId(nuevoUsuario.getId()));
            return new ResponseEntity<>(response, HttpStatus.CREATED);
        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("error", "❌ " + e.getMessage());
            return new ResponseEntity<>(errorResponse, HttpStatus.BAD_REQUEST);
        }
    }

    // ============================================
    // 📋 2. OBTENER TODOS LOS USUARIOS - GET
    // ============================================
    
    @Operation(
        summary = "📋 Obtener todos los usuarios",
        description = """
            Retorna una lista con todos los usuarios registrados en el sistema.
            
            ### 📊 La respuesta incluye:
            - Todos los usuarios con sus datos completos
            - Ordenados por ID (ascendente)
            - Sin información de contraseñas (campo excluido)
            """
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200", 
            description = "✅ Lista de usuarios obtenida exitosamente",
            content = @Content(
                mediaType = "application/json",
                examples = @ExampleObject(
                    value = """
                        [
                            {
                                "id": 1,
                                "nombre": "Alan Hernández",
                                "email": "alan@gmail.com",
                                "direccion": "Calle Emiliano Zapata #45",
                                "telefono": "7203533170",
                                "userTipo": 0
                            },
                            {
                                "id": 2,
                                "nombre": "María González",
                                "email": "maria@gmail.com",
                                "direccion": "Avenida Morelos #123",
                                "telefono": "7221234567",
                                "userTipo": 1
                            }
                        ]
                        """
                )
            )
        ),
        @ApiResponse(responseCode = "500", description = "❌ Error interno del servidor")
    })
    @GetMapping
    public ResponseEntity<List<UsuarioDTO>> obtenerTodosLosUsuarios() {
        List<UsuarioDTO> usuarios = usuarioService.obtenerTodosLosUsuarios();
        return new ResponseEntity<>(usuarios, HttpStatus.OK);
    }

    // ============================================
    // 🔍 3. OBTENER USUARIO POR ID - GET
    // ============================================
    
    @Operation(
        summary = "🔍 Obtener usuario por ID",
        description = """
            Retorna los datos completos de un usuario específico usando su ID único.
            
            ### 🔑 Parámetros:
            - **id**: ID numérico del usuario (generado automáticamente)
            
            ### 💡 Ejemplo de uso:
            - `GET /api/usuarios/1` → Obtiene el usuario con ID 1
            """
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200", 
            description = "✅ Usuario encontrado",
            content = @Content(
                examples = @ExampleObject(
                    value = """
                        {
                            "mensaje": "Usuario encontrado",
                            "usuario": {
                                "id": 1,
                                "nombre": "Alan Hernández",
                                "email": "alan@gmail.com",
                                "direccion": "Calle Emiliano Zapata #45",
                                "telefono": "7203533170",
                                "userTipo": 0
                            }
                        }
                        """
                )
            )
        ),
        @ApiResponse(
            responseCode = "404", 
            description = "❌ Usuario no encontrado",
            content = @Content(
                examples = @ExampleObject(
                    value = """
                        {
                            "error": "Usuario no encontrado con ID: 99"
                        }
                        """
                )
            )
        )
    })
    @GetMapping("/{id}")
    public ResponseEntity<Map<String, Object>> obtenerUsuarioPorId(
            @Parameter(
                description = "🆔 ID único del usuario (número entero)", 
                example = "1", 
                required = true,
                schema = @Schema(type = "integer", minimum = "1")
            )
            @PathVariable Long id) {
        Optional<UsuarioDTO> usuarioOpt = usuarioService.obtenerUsuarioPorId(id);
        
        if (usuarioOpt.isPresent()) {
            Map<String, Object> response = new HashMap<>();
            response.put("mensaje", "✅ Usuario encontrado");
            response.put("usuario", usuarioOpt.get());
            return new ResponseEntity<>(response, HttpStatus.OK);
        } else {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("error", "❌ Usuario no encontrado con ID: " + id);
            return new ResponseEntity<>(errorResponse, HttpStatus.NOT_FOUND);
        }
    }

    // ============================================
    // 📧 4. OBTENER USUARIO POR EMAIL - GET
    // ============================================
    
    @Operation(
        summary = "📧 Obtener usuario por email",
        description = """
            Retorna los datos de un usuario específico usando su email.
            
            ### 📧 Requisitos del email:
            - Debe ser de Gmail (@gmail.com) o Hotmail (@hotmail.com)
            - No distingue entre mayúsculas y minúsculas
            - Debe estar registrado en el sistema
            
            ### 💡 Ejemplo de uso:
            - `GET /api/usuarios/email/alan@gmail.com`
            """
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200", 
            description = "✅ Usuario encontrado",
            content = @Content(
                examples = @ExampleObject(
                    value = """
                        {
                            "mensaje": "Usuario encontrado",
                            "usuario": {
                                "id": 1,
                                "nombre": "Alan Hernández",
                                "email": "alan@gmail.com",
                                "direccion": "Calle Emiliano Zapata #45",
                                "telefono": "7203533170",
                                "userTipo": 0
                            }
                        }
                        """
                )
            )
        ),
        @ApiResponse(
            responseCode = "404", 
            description = "❌ Usuario no encontrado",
            content = @Content(
                examples = @ExampleObject(
                    value = """
                        {
                            "error": "Usuario no encontrado con email: inexistente@gmail.com"
                        }
                        """
                )
            )
        )
    })
    @GetMapping("/email/{email}")
    public ResponseEntity<Map<String, Object>> obtenerUsuarioPorEmail(
            @Parameter(
                description = "📧 Email del usuario (debe ser Gmail o Hotmail)", 
                example = "alan@gmail.com", 
                required = true,
                schema = @Schema(type = "string", format = "email", pattern = "^[A-Za-z0-9+_.-]+@(gmail\\.com|hotmail\\.com)$")
            )
            @PathVariable String email) {
        Optional<UsuarioDTO> usuarioOpt = usuarioService.obtenerUsuarioPorEmail(email);
        
        if (usuarioOpt.isPresent()) {
            Map<String, Object> response = new HashMap<>();
            response.put("mensaje", "✅ Usuario encontrado");
            response.put("usuario", usuarioOpt.get());
            return new ResponseEntity<>(response, HttpStatus.OK);
        } else {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("error", "❌ Usuario no encontrado con email: " + email);
            return new ResponseEntity<>(errorResponse, HttpStatus.NOT_FOUND);
        }
    }

    // ============================================
    // 👥 5. OBTENER POR TIPO - GET
    // ============================================
    
    @Operation(
        summary = "👥 Obtener usuarios por tipo",
        description = """
            Retorna todos los usuarios filtrados por su tipo de rol.
            
            ### 👤 Tipos de usuario:
            - **0**: Administrador (acceso total)
            - **1**: Empleado (acceso limitado)
            - **2**: Cliente (acceso básico)
            
            ### 💡 Ejemplo de uso:
            - `GET /api/usuarios/tipo/0` → Obtiene todos los administradores
            """
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200", 
            description = "✅ Usuarios encontrados",
            content = @Content(
                examples = @ExampleObject(
                    value = """
                        {
                            "mensaje": "Usuarios encontrados por tipo: 0",
                            "cantidad": 2,
                            "usuarios": [
                                {
                                    "id": 1,
                                    "nombre": "Alan Hernández",
                                    "email": "alan@gmail.com",
                                    "direccion": "Calle Emiliano Zapata #45",
                                    "telefono": "7203533170",
                                    "userTipo": 0
                                }
                            ]
                        }
                        """
                )
            )
        ),
        @ApiResponse(
            responseCode = "200", 
            description = "⚠️ No se encontraron usuarios con ese tipo",
            content = @Content(
                examples = @ExampleObject(
                    value = """
                        {
                            "mensaje": "Usuarios encontrados por tipo: 3",
                            "cantidad": 0,
                            "usuarios": []
                        }
                        """
                )
            )
        )
    })
    @GetMapping("/tipo/{userTipo}")
    public ResponseEntity<Map<String, Object>> obtenerUsuariosPorTipo(
            @Parameter(
                description = "👤 Tipo de usuario: 0=Admin, 1=Empleado, 2=Cliente", 
                example = "0", 
                required = true,
                schema = @Schema(type = "integer", allowableValues = {"0", "1", "2"})
            )
            @PathVariable Integer userTipo) {
        List<UsuarioDTO> usuarios = usuarioService.obtenerUsuariosPorTipo(userTipo);
        
        Map<String, Object> response = new HashMap<>();
        response.put("mensaje", "✅ Usuarios encontrados por tipo: " + userTipo);
        response.put("cantidad", usuarios.size());
        response.put("usuarios", usuarios);
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    // ============================================
    // 🔎 6. BUSCAR POR NOMBRE - GET
    // ============================================
    
    @Operation(
        summary = "🔎 Buscar usuarios por nombre",
        description = """
            Retorna todos los usuarios cuyo nombre contenga la palabra buscada.
            
            ### 🔍 Características:
            - Búsqueda parcial (contiene la palabra)
            - No distingue entre mayúsculas y minúsculas
            - Útil para autocompletado y búsquedas rápidas
            
            ### 💡 Ejemplos:
            - `GET /api/usuarios/buscar?nombre=Al` → Encuentra "Alan", "Alejandro", etc.
            - `GET /api/usuarios/buscar?nombre=maria` → Encuentra "María", "Mariam", etc.
            """
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200", 
            description = "✅ Resultados de búsqueda",
            content = @Content(
                examples = @ExampleObject(
                    value = """
                        {
                            "mensaje": "Resultados de búsqueda para: Alan",
                            "cantidad": 1,
                            "usuarios": [
                                {
                                    "id": 1,
                                    "nombre": "Alan Hernández",
                                    "email": "alan@gmail.com",
                                    "direccion": "Calle Emiliano Zapata #45",
                                    "telefono": "7203533170",
                                    "userTipo": 0
                                }
                            ]
                        }
                        """
                )
            )
        )
    })
    @GetMapping("/buscar")
    public ResponseEntity<Map<String, Object>> buscarUsuariosPorNombre(
            @Parameter(
                description = "🔤 Nombre o parte del nombre a buscar (mínimo 2 caracteres)", 
                example = "Alan", 
                required = true,
                schema = @Schema(type = "string", minLength = 2)
            )
            @RequestParam String nombre) {
        List<UsuarioDTO> usuarios = usuarioService.buscarUsuariosPorNombre(nombre);
        
        Map<String, Object> response = new HashMap<>();
        response.put("mensaje", "✅ Resultados de búsqueda para: " + nombre);
        response.put("cantidad", usuarios.size());
        response.put("usuarios", usuarios);
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    // ============================================
    // ✏️ 7. ACTUALIZAR USUARIO - PUT
    // ============================================
    
    @Operation(
        summary = "✏️ Actualizar usuario",
        description = """
            Actualiza los datos de un usuario existente.
            
            ### 📝 Características:
            - Solo se actualizan los campos enviados
            - Los campos no enviados mantienen su valor actual
            - La contraseña se encripta automáticamente si se envía
            - Validación de email único (no puede usarse en otro usuario)
            
            ### ⚠️ Nota:
            - Para actualizar la contraseña, enviar el campo 'password'
            - El ID no se puede modificar
            """
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200", 
            description = "✅ Usuario actualizado exitosamente",
            content = @Content(
                examples = @ExampleObject(
                    value = """
                        {
                            "mensaje": "Usuario actualizado exitosamente",
                            "usuario": {
                                "id": 1,
                                "nombre": "Alan Hernández Actualizado",
                                "email": "alan.actualizado@gmail.com",
                                "direccion": "Nueva dirección #456",
                                "telefono": "7203533171",
                                "userTipo": 0
                            }
                        }
                        """
                )
            )
        ),
        @ApiResponse(
            responseCode = "400", 
            description = "❌ Datos inválidos",
            content = @Content(
                examples = @ExampleObject(
                    value = """
                        {
                            "error": "El email ya está registrado por otro usuario"
                        }
                        """
                )
            )
        ),
        @ApiResponse(
            responseCode = "404", 
            description = "❌ Usuario no encontrado",
            content = @Content(
                examples = @ExampleObject(
                    value = """
                        {
                            "error": "Usuario no encontrado con ID: 99"
                        }
                        """
                )
            )
        )
    })
    @PutMapping("/{id}")
    public ResponseEntity<Map<String, Object>> actualizarUsuario(
            @Parameter(
                description = "🆔 ID del usuario a actualizar", 
                example = "1", 
                required = true
            )
            @PathVariable Long id,
            @Valid @RequestBody 
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                description = "📦 Datos a actualizar (solo los campos que se quieren modificar)",
                required = true,
                content = @Content(
                    examples = {
                        @ExampleObject(
                            name = "Actualizar nombre y email",
                            value = """
                                {
                                    "nombre": "Alan Hernández Actualizado",
                                    "email": "alan.nuevo@gmail.com"
                                }
                                """
                        ),
                        @ExampleObject(
                            name = "Actualizar contraseña",
                            value = """
                                {
                                    "password": "nuevaPassword123"
                                }
                                """
                        ),
                        @ExampleObject(
                            name = "Actualizar todos los campos",
                            value = """
                                {
                                    "nombre": "Alan Hernández Completo",
                                    "email": "alan.completo@gmail.com",
                                    "password": "nuevaPassword456",
                                    "direccion": "Calle Principal #789",
                                    "telefono": "7203533199",
                                    "userTipo": 1
                                }
                                """
                        )
                    }
                )
            ) 
            Usuario usuarioActualizado) {
        try {
            Usuario usuarioActualizadoObj = usuarioService.actualizarUsuario(id, usuarioActualizado);
            
            Map<String, Object> response = new HashMap<>();
            response.put("mensaje", "✅ Usuario actualizado exitosamente");
            response.put("usuario", usuarioService.obtenerUsuarioPorId(usuarioActualizadoObj.getId()));
            return new ResponseEntity<>(response, HttpStatus.OK);
        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("error", "❌ " + e.getMessage());
            return new ResponseEntity<>(errorResponse, HttpStatus.BAD_REQUEST);
        }
    }

    // ============================================
// 🔒 10. CAMBIAR ESTADO DE USUARIO (ACTIVO/INACTIVO) - PUT
// ============================================

@Operation(summary = "🔒 Cambiar estado del usuario (activo/inactivo)")
@PutMapping("/{id}/estado")
public ResponseEntity<Map<String, Object>> cambiarEstadoUsuario(
        @PathVariable Long id,
        @RequestBody Map<String, Boolean> body) {
    try {
        boolean activo = body.get("activo");
        usuarioService.cambiarEstadoUsuario(id, activo);
        Map<String, Object> response = new HashMap<>();
        response.put("mensaje", "✅ Estado actualizado correctamente");
        response.put("id", id);
        response.put("activo", activo);
        return new ResponseEntity<>(response, HttpStatus.OK);
    } catch (Exception e) {
        Map<String, Object> errorResponse = new HashMap<>();
        errorResponse.put("error", "❌ " + e.getMessage());
        return new ResponseEntity<>(errorResponse, HttpStatus.BAD_REQUEST);
    }
}


    // ============================================
    // 🗑️ 8. ELIMINAR USUARIO - DELETE
    // ============================================
    
    @Operation(
        summary = "🗑️ Eliminar usuario",
        description = """
            Elimina un usuario del sistema por su ID.
            
            ### ⚠️ Advertencia:
            - Esta acción **no se puede deshacer**
            - Se eliminan todos los datos del usuario
            - Los IDs no se reutilizan (permanecen como históricos)
            
            ### 💡 Recomendación:
            - En sistemas productivos, considerar un borrado lógico (activo/inactivo)
            - Esta implementación es un borrado físico definitivo
            """
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200", 
            description = "✅ Usuario eliminado exitosamente",
            content = @Content(
                examples = @ExampleObject(
                    value = """
                        {
                            "mensaje": "Usuario eliminado exitosamente",
                            "id": 1
                        }
                        """
                )
            )
        ),
        @ApiResponse(
            responseCode = "404", 
            description = "❌ Usuario no encontrado",
            content = @Content(
                examples = @ExampleObject(
                    value = """
                        {
                            "error": "Usuario no encontrado con ID: 99"
                        }
                        """
                )
            )
        )
    })
    @DeleteMapping("/{id}")
    public ResponseEntity<Map<String, Object>> eliminarUsuario(
            @Parameter(
                description = "🆔 ID del usuario a eliminar", 
                example = "1", 
                required = true
            )
            @PathVariable Long id) {
        try {
            usuarioService.eliminarUsuario(id);
            
            Map<String, Object> response = new HashMap<>();
            response.put("mensaje", "🗑️ Usuario eliminado exitosamente");
            response.put("id", id);
            return new ResponseEntity<>(response, HttpStatus.OK);
        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("error", "❌ " + e.getMessage());
            return new ResponseEntity<>(errorResponse, HttpStatus.NOT_FOUND);
        }
    }


    // ============================================
// 📱 9. ACTUALIZAR PUSH TOKEN - PUT
// ============================================

@Operation(summary = "📱 Actualizar push token del usuario")
@PutMapping("/{id}/push-token")
public ResponseEntity<Map<String, Object>> actualizarPushToken(
        @PathVariable Long id,
        @RequestBody Map<String, String> body) {
    try {
        String pushToken = body.get("pushToken");
        usuarioService.actualizarPushToken(id, pushToken);
        Map<String, Object> response = new HashMap<>();
        response.put("mensaje", "✅ Push token actualizado");
        return new ResponseEntity<>(response, HttpStatus.OK);
    } catch (Exception e) {
        Map<String, Object> errorResponse = new HashMap<>();
        errorResponse.put("error", "❌ " + e.getMessage());
        return new ResponseEntity<>(errorResponse, HttpStatus.BAD_REQUEST);
    }
}

// ============================================
// 📱 10. OBTENER PUSH TOKENS DE EMPLEADOS - GET
// ============================================

@Operation(summary = "📱 Obtener push tokens de todos los empleados activos")
@GetMapping("/empleados/push-tokens")
public ResponseEntity<Map<String, Object>> getPushTokensEmpleados() {
    List<String> tokens = usuarioService.getPushTokensEmpleados();
    Map<String, Object> response = new HashMap<>();
    response.put("tokens", tokens);
    return new ResponseEntity<>(response, HttpStatus.OK);
}


@Operation(summary = "Iniciar sesión")
@PostMapping("/login")
public ResponseEntity<Map<String, Object>> iniciarSesion(
        @RequestBody Map<String, String> body) {

    Map<String, Object> response = new HashMap<>();

    try {
        String email = body.get("email");
        String password = body.get("password");

        if (email == null || email.trim().isEmpty()
                || password == null || password.trim().isEmpty()) {

            response.put("error", "Correo y contraseña son obligatorios");

            return new ResponseEntity<>(
                    response,
                    HttpStatus.BAD_REQUEST
            );
        }

        UsuarioDTO usuario = usuarioService.iniciarSesion(
                email.trim().toLowerCase(),
                password
        );

        response.put("mensaje", "Inicio de sesión exitoso");
        response.put("usuario", usuario);

        return new ResponseEntity<>(
                response,
                HttpStatus.OK
        );

    } catch (RuntimeException e) {

        response.put("error", e.getMessage());

        return new ResponseEntity<>(
                response,
                HttpStatus.UNAUTHORIZED
        );
    }
}
    
}
