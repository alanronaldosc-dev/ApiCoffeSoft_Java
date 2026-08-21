package com.utvt.ApiSpringCafeSoft.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.ArrayList;
import java.util.List;

@Schema(description = "DTO de Usuario para respuestas de la API")
public class UsuarioDTO {

    @Schema(
        description = "ID único del usuario",
        example = "1"
    )
    private Long id;

    @Schema(
        description = "Nombre completo del usuario",
        example = "Alan"
    )
    private String nombre;

    @Schema(
        description = "Email del usuario",
        example = "alan@gmail.com"
    )
    private String email;

    @Schema(
        description = "Dirección del usuario",
        example = "calle Emiliano Zapata"
    )
    private String direccion;

    @Schema(
        description = "Teléfono del usuario",
        example = "7203533170"
    )
    private String telefono;

    @Schema(
        description = "Tipo de usuario: 0=Administrador, 1=Usuario, 2=Cliente, 3=Personalizado",
        example = "3",
        allowableValues = {"0", "1", "2", "3"}
    )
    private Integer userTipo;

    @Schema(
        description = "Lista de permisos específicos del usuario",
        example = "[\"productos\", \"ventas\", \"insumos\"]"
    )
    private List<String> permisos = new ArrayList<>();

    public UsuarioDTO() {
    }

    public UsuarioDTO(
            Long id,
            String nombre,
            String email,
            String direccion,
            String telefono,
            Integer userTipo,
            List<String> permisos
    ) {
        this.id = id;
        this.nombre = nombre;
        this.email = email;
        this.direccion = direccion;
        this.telefono = telefono;
        this.userTipo = userTipo;
        this.permisos = permisos != null
                ? new ArrayList<>(permisos)
                : new ArrayList<>();
    }

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

    public List<String> getPermisos() {
        return permisos;
    }

    public void setPermisos(List<String> permisos) {
        this.permisos = permisos != null
                ? new ArrayList<>(permisos)
                : new ArrayList<>();
    }
}