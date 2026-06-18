package com.utvt.ApiSpringCafeSoft.model;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "usuarios")
@Schema(description = "Modelo de Usuario para el sistema CoffeeSoft")
public class Usuario {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_usuario")
    @Schema(description = "ID único del usuario", example = "1", accessMode = Schema.AccessMode.READ_ONLY)
    private Long id;
    
    @NotBlank(message = "El nombre es obligatorio")
    @Size(min = 2, max = 100, message = "El nombre debe tener entre 2 y 100 caracteres")
    @Column(name = "nombre", nullable = false, length = 255)
    @Schema(description = "Nombre completo del usuario", example = "Alan", requiredMode = Schema.RequiredMode.REQUIRED)
    private String nombre;
    
    @NotBlank(message = "El email es obligatorio")
    @Email(message = "El email debe ser válido")
    @Pattern(
        regexp = "^[A-Za-z0-9+_.-]+@(gmail\\.com|hotmail\\.com)$",
        message = "El email debe ser de Gmail o Hotmail"
    )
    @Column(name = "email", nullable = false, unique = true, length = 255)
    @Schema(description = "Email del usuario (debe ser Gmail o Hotmail)", 
            example = "alan@gmail.com", 
            requiredMode = Schema.RequiredMode.REQUIRED)
    private String email;
    
    @NotBlank(message = "La contraseña es obligatoria")
    @Size(min = 8, message = "La contraseña debe tener al menos 8 caracteres")
    @Column(name = "password", nullable = false, length = 255)
    @Schema(description = "Contraseña del usuario (mínimo 8 caracteres)", 
            example = "********", 
            requiredMode = Schema.RequiredMode.REQUIRED,
            minLength = 8)
    private String password;
    
    @NotBlank(message = "La dirección es obligatoria")
    @Column(name = "direccion", nullable = false, length = 255)
    @Schema(description = "Dirección del usuario", example = "calle Emiliano Zapata", requiredMode = Schema.RequiredMode.REQUIRED)
    private String direccion;
    
    @NotBlank(message = "El teléfono es obligatorio")
    @Pattern(
        regexp = "^\\d{10}$",
        message = "El teléfono debe tener exactamente 10 dígitos"
    )
    @Column(name = "telefono", nullable = false, length = 255)
    @Schema(description = "Teléfono del usuario (10 dígitos)", 
            example = "7203533170", 
            requiredMode = Schema.RequiredMode.REQUIRED,
            pattern = "^\\d{10}$")
    private String telefono;
    
    @NotNull(message = "El tipo de usuario es obligatorio")
    @Min(value = 0, message = "El tipo debe ser 0, 1 o 2")
    @Max(value = 2, message = "El tipo debe ser 0, 1 o 2")
    @Column(name = "user_tipo", nullable = false)
    @Schema(description = "Tipo de usuario: 0=Administrador, 1=Empleado, 2=Cliente", 
            example = "0", 
            allowableValues = {"0", "1", "2"},
            requiredMode = Schema.RequiredMode.REQUIRED)
    private Integer userTipo;
    
    
    
    // Getters y Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getDireccion() {
        return direccion;
    }

    public void setDireccion(String direccion) {
        this.direccion = direccion;
    }

    public String getTelefono() {
        return telefono;
    }

    public void setTelefono(String telefono) {
        this.telefono = telefono;
    }

    public Integer getUserTipo() {
        return userTipo;
    }

    public void setUserTipo(Integer userTipo) {
        this.userTipo = userTipo;
    }

    
}