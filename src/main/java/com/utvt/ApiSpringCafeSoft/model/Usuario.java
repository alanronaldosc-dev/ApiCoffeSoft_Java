package com.utvt.ApiSpringCafeSoft.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;


/**
 * HU-011: Registro, consulta y actualización de perfiles de usuario.
 * Esta entidad representa a los usuarios del sistema: administrador (0),
 * empleado (1) y cliente (2), manteniendo la plantilla del personal operativa.
 *
 * HU-015: Gestión de roles personalizados y permisos específicos
 * para puestos especiales dentro del sistema.
 */
@Entity
@Table(name = "usuarios")
public class Usuario {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_usuario")
    private Long id;

    // HU-011: Datos básicos del perfil del usuario (nombre, email, contraseña, dirección, teléfono)
    @NotBlank(message = "El nombre es obligatorio")
    @Column(nullable = false)
    private String nombre;

    @NotBlank(message = "El email es obligatorio")
    @Email(message = "El email debe ser válido")
    @Column(unique = true, nullable = false)
    private String email;

    @NotBlank(message = "La contraseña es obligatoria")
    @Column(nullable = false)
    private String password;

    @Column(nullable = false)
    private String direccion;

    @Column(nullable = false)
    private String telefono;


    /**
     * HU-011: Tipo de usuario para diferenciar los roles del personal operativo.
     * Tipos de usuario:
     * 0 = Administrador
     * 1 = Usuario / Empleado
     * 2 = Cliente
     * 3 = Personalizado (HU-015)
     */
    @Column(name = "user_tipo", nullable = false)
    private Integer userTipo;

    @Column(name = "push_token")
    private String pushToken;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    // HU-011: Campo para activar/desactivar una cuenta de usuario en la plantilla operativa
    @Column(nullable = false)
    private Boolean activo = true;

    public Usuario() {}

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }

    public String getDireccion() { return direccion; }
    public void setDireccion(String direccion) { this.direccion = direccion; }

    public String getTelefono() { return telefono; }
    public void setTelefono(String telefono) { this.telefono = telefono; }

    public Integer getUserTipo() { return userTipo; }
    public void setUserTipo(Integer userTipo) { this.userTipo = userTipo; }

    public String getPushToken() { return pushToken; }
    public void setPushToken(String pushToken) { this.pushToken = pushToken; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    /** HU-015
     *
     * Permisos específicos asignados a un usuario
     * con rol personalizado.
     */
    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "usuario_permisos",
            joinColumns = @JoinColumn(name = "id_usuario"))
    @Column(name = "permiso")
    private List<String> permisos = new ArrayList<>();


    public Boolean getActivo() {
        return activo;
    }

    public void setActivo(Boolean activo) {
        this.activo = activo;
    }

    public List<String> getPermisos() {
        return permisos;
    }

    public void setPermisos(List<String> permisos) {
        this.permisos = permisos;
    }

}