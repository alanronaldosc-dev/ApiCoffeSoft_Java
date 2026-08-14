package com.utvt.ApiSpringCafeSoft.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;

@Schema(description = "DTO para el inventario de insumos")
public class InventarioDTO {

    @Schema(description = "ID único del inventario", example = "1")
    private Long id;

    @Schema(description = "Nombre del insumo", example = "Café Arabica")
    @NotBlank(message = "El nombre es obligatorio")
    @Size(max = 100, message = "El nombre no puede exceder los 100 caracteres")
    private String nombre;

    @Schema(description = "Tipo de insumo", example = "Grano")
    @NotBlank(message = "El tipo es obligatorio")
    @Size(max = 50, message = "El tipo no puede exceder los 50 caracteres")
    private String tipo;

    @Schema(description = "Cantidad disponible", example = "10.5")
    @NotNull(message = "La cantidad es obligatoria")
    @Min(value = 0, message = "La cantidad no puede ser negativa")
    private Double cantidad;

    @Schema(description = "Unidad de medida", example = "kilogramos", allowableValues = {"litros", "kilogramos", "piezas"})
    @NotBlank(message = "La unidad de medida es obligatoria")
    @Pattern(regexp = "^(litros|kilogramos|piezas)$", message = "La unidad debe ser: litros, kilogramos o piezas")
    private String unidadMedida;

    @Schema(description = "Cantidad mínima permitida", example = "2.0")
    @NotNull(message = "La cantidad mínima es obligatoria")
    @Min(value = 0, message = "La cantidad mínima no puede ser negativa")
    private Double cantidadMinima;

    @Schema(description = "Fecha de caducidad", example = "2026-12-31")
    private String caducidad;

    @Schema(description = "Nombre del proveedor", example = "Proveedor Local S.A.")
    @Size(max = 100, message = "El proveedor no puede exceder los 100 caracteres")
    private String proveedor;

    @Schema(description = "Precio unitario", example = "150.50")
    @NotNull(message = "El precio unitario es obligatorio")
    @Min(value = 0, message = "El precio unitario no puede ser negativo")
    private Double precioUnitario;

    public InventarioDTO() {}

    public InventarioDTO(Long id, String nombre, String tipo, Double cantidad, String unidadMedida,
                         Double cantidadMinima, String caducidad, String proveedor, Double precioUnitario) {
        this.id = id;
        this.nombre = nombre;
        this.tipo = tipo;
        this.cantidad = cantidad;
        this.unidadMedida = unidadMedida;
        this.cantidadMinima = cantidadMinima;
        this.caducidad = caducidad;
        this.proveedor = proveedor;
        this.precioUnitario = precioUnitario;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }
    public String getTipo() { return tipo; }
    public void setTipo(String tipo) { this.tipo = tipo; }
    public Double getCantidad() { return cantidad; }
    public void setCantidad(Double cantidad) { this.cantidad = cantidad; }
    public String getUnidadMedida() { return unidadMedida; }
    public void setUnidadMedida(String unidadMedida) { this.unidadMedida = unidadMedida; }
    public Double getCantidadMinima() { return cantidadMinima; }
    public void setCantidadMinima(Double cantidadMinima) { this.cantidadMinima = cantidadMinima; }
    public String getCaducidad() { return caducidad; }
    public void setCaducidad(String caducidad) { this.caducidad = caducidad; }
    public String getProveedor() { return proveedor; }
    public void setProveedor(String proveedor) { this.proveedor = proveedor; }
    public Double getPrecioUnitario() { return precioUnitario; }
    public void setPrecioUnitario(Double precioUnitario) { this.precioUnitario = precioUnitario; }
}
