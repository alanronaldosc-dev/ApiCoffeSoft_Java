package com.utvt.ApiSpringCafeSoft.model;


import jakarta.persistence.*;
import jakarta.validation.constraints.*;


@Entity
@Table(name = "insumos")
public class Insumo {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull(message = "El nombre es obligatorio")
    @Size(max = 20, message = "El nombre no puede exeder los 20 caracteres")
    @Column(nullable = false, length = 100)
    private String nombre;

    @NotNull(message = "El tipo es obligatorio")
    @Column(nullable = false, length = 100)
    private String tipo;

    @NotBlank(message = "La unidad de medida es obligatoria")
    @Pattern(regexp = "^(litros|kilogramos|piezas)$", message = "La unidad debe ser: litros, kilogramos o piezas")
    @Column(name = "unidad_medida", nullable = false, length = 20)
    private String unidadMedida;


    @NotNull(message = "El proveedor es obligatorio")
    @Column(nullable = false, length = 100)
    private String proveedor;

    @NotNull(message = "El precio es obligatorio")
    @Min(value = 0, message = "El precio no puede ser negativo")
    @Column(nullable = false)
    private Double precio;

    public Insumo() {}

    public Insumo(String nombre, String tipo, String unidadMedida,
        String proveedor, Double precio) {
            this.nombre = nombre;
            this.tipo = tipo;
            this.unidadMedida  = unidadMedida;
            this.proveedor = proveedor;
            this.precio = precio;
        }


    //Getters y Setters

    public Long getId() { 
        return id; 
    }
    public String getNombre() { 
        return nombre; 
    }
    public String getTipo() { 
        return tipo; 
    }
    public String getProveedor() { 
        return proveedor; 
    }
    public Double getPrecio() { 
        return precio; 
    }
    public String getUnidadMedida() { 
        return unidadMedida; 
    }

    public void setId(Long id) { 
        this.id = id; 
    }
    public void setNombre(String nombre) { 
        this.nombre = nombre; 
    }
    public void setTipo(String tipo) { 
        this.tipo = tipo; 
    }
    public void setUnidadMedida(String unidadMedida) { 
        this.unidadMedida = unidadMedida; 
    }
    public void setProveedor(String proveedor) 
    { this.proveedor = proveedor; 

    }
    public void setPrecio(Double precio) 
    { this.precio = precio; 

    }


}
