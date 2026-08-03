package com.utvt.ApiSpringCafeSoft.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import java.util.ArrayList;
import java.util.List;

@Schema(description = "DTO para el producto")
public class ProductoDTO {

    @Schema(description = "ID único del producto", example = "1")
    private Long id;

    @Schema(description = "Nombre del producto", example = "Café Latte", required = true)
    @NotBlank(message = "El nombre es obligatorio")
    @Size(max = 100, message = "El nombre no puede exceder los 100 caracteres")
    private String nombre;

    @Schema(description = "Precio del producto", example = "45.50", required = true)
    @NotNull(message = "El precio es obligatorio")
    @Min(value = 0, message = "El precio no puede ser negativo")
    private Double precio;

    @Schema(description = "Descripción del producto", example = "Delicioso café con leche y canela")
    @Size(max = 500, message = "La descripción no puede exceder los 500 caracteres")
    private String descripcion;

    @Schema(description = "Imagen del producto", example = "cafe-latte.jpg")
    private String imagen;

    @Schema(description = "Lista de insumos necesarios para el producto")
    @Valid
    private List<ProductoInsumoDTO> insumos = new ArrayList<>();

    // Constructor por defecto
    public ProductoDTO() {}

    // Constructor con parámetros
    public ProductoDTO(Long id, String nombre, Double precio, String descripcion, String imagen) {
        this.id = id;
        this.nombre = nombre;
        this.precio = precio;
        this.descripcion = descripcion;
        this.imagen = imagen;
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

    public Double getPrecio() {
        return precio;
    }

    public void setPrecio(Double precio) {
        this.precio = precio;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public void setDescripcion(String descripcion) {
        this.descripcion = descripcion;
    }

    public String getImagen() {
        return imagen;
    }

    public void setImagen(String imagen) {
        this.imagen = imagen;
    }

    public List<ProductoInsumoDTO> getInsumos() {
        return insumos;
    }

    public void setInsumos(List<ProductoInsumoDTO> insumos) {
        this.insumos = insumos;
    }
}