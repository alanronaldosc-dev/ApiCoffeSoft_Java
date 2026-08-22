package com.utvt.ApiSpringCafeSoft.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "usuarios")

/**
 * HU-015: Gestión de roles personalizados y permisos específicos
 * para puestos especiales dentro del sistema.
 */
public class Usuario {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_usuario")
    private Long id;

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

    /*
     * Tipos de usuario:
     * 0 = Administrador
     * 1 = Usuario / Empleado
     * 2 = Cliente
     * 3 = Personalizado
     */
    @Column(name = "user_tipo", nullable = false)
    private Integer userTipo;

    @Column(name = "push_token")
    private String pushToken;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(nullable = false)
    private Boolean activo = true;

    /*
     * HU-015
     * Permisos específicos asignados a un usuario
     * con rol personalizado.
     */
    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(
        name = "usuario_permisos",
        joinColumns = @JoinColumn(name = "id_usuario")
    )
    @Column(name = "permiso")
    private List<String> permisos = new ArrayList<>();

    public Usuario() {}

    // Getters y Setters
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

    public Boolean getActivo() { return activo; }
    public void setActivo(Boolean activo) { this.activo = activo; }

    public List<String> getPermisos() { return permisos; }
    public void setPermisos(List<String> permisos) { this.permisos = permisos; }
}