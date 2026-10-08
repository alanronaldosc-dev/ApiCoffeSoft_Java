package com.utvt.ApiSpringCafeSoft.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;

@Entity
@Table(name = "proveedores")
public class Proveedor {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_proveedor")
    private Long id;

    @NotBlank(message = "El nombre de la empresa es obligatorio")
    @Column(name = "nombre_empresa", nullable = false)
    private String nombreEmpresa;

    @NotBlank(message = "El contacto es obligatorio")
    @Column(nullable = false)
    private String contacto;

    @NotBlank(message = "El teléfono es obligatorio")
    @Column(nullable = false)
    private String telefono;

    @Column(name = "insumo_principal")
    private String insumoPrincipal;

    @Column(nullable = false)
    private String direccion;

    @Column(nullable = false)
    private Boolean activo = true;

    public Proveedor() {
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getNombreEmpresa() {
        return nombreEmpresa;
    }

    public void setNombreEmpresa(String nombreEmpresa) {
        this.nombreEmpresa = nombreEmpresa;
    }

    public String getContacto() {
        return contacto;
    }

    public void setContacto(String contacto) {
        this.contacto = contacto;
    }

    public String getTelefono() {
        return telefono;
    }

    public void setTelefono(String telefono) {
        this.telefono = telefono;
    }

    public String getInsumoPrincipal() {
        return insumoPrincipal;
    }

    public void setInsumoPrincipal(String insumoPrincipal) {
        this.insumoPrincipal = insumoPrincipal;
    }

    public String getDireccion() {
        return direccion;
    }

    public void setDireccion(String direccion) {
        this.direccion = direccion;
    }

    public Boolean getActivo() {
        return activo;
    }

    public void setActivo(Boolean activo) {
        this.activo = activo;
    }
}