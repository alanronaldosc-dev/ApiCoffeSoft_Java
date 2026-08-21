package com.utvt.ApiSpringCafeSoft.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "DTO de Usuario para respuestas de la API")
public class UsuarioDTO {
    
    @Schema(description = "ID único del usuario", example = "1")
    private Long id;

    @Schema(description = "Estado de la cuenta", example = "true")
private Boolean activo;
    
    @Schema(description = "Nombre completo del usuario", example = "Alan")
    private String nombre;
    
    @Schema(description = "Email del usuario", example = "alan@gmail.com")
    private String email;
    
    @Schema(description = "Dirección del usuario", example = "calle Emiliano Zapata")
    private String direccion;
    
    @Schema(description = "Teléfono del usuario", example = "7203533170")
    private String telefono;
    @Schema(description = "Indica si el usuario puede acceder al sistema", example = "true")
private Boolean activo;
    
    @Schema(description = "Tipo de usuario: 0=Administrador, 1=Empleado, 2=Cliente", 
            example = "0", 
            allowableValues = {"0", "1", "2"})
    private Integer userTipo;
    
    
    // Constructor vacío
    public UsuarioDTO() {
    }
    
    // Constructor completo
    public UsuarioDTO(Long id, String nombre, String email, String direccion,
                  String telefono, Integer userTipo, Boolean activo) {
    this.id = id;
    this.nombre = nombre;
    this.email = email;
    this.direccion = direccion;
    this.telefono = telefono;
    this.userTipo = userTipo;
    this.activo = activo;
}

    public Boolean getActivo() {
    return activo;
}

public void setActivo(Boolean activo) {
    this.activo = activo;
}
    
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
