package com.utvt.ApiSpringCafeSoft.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Cliente de una ruta sin imágenes ni relaciones profundas")
public class ClienteRutaDTO {

    private Long id;
    private String nombre;
    private String domicilio;
    private String linkGoogleMaps;
    private String diasReparto;
    private Double precioPorGarrafon;
    private Integer ordenEnRuta;
    private ProductoResumen garrafonPreferencia;
    private RutaResumen ruta;

    public ClienteRutaDTO() {
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

    public String getDomicilio() {
        return domicilio;
    }

    public void setDomicilio(String domicilio) {
        this.domicilio = domicilio;
    }

    public String getLinkGoogleMaps() {
        return linkGoogleMaps;
    }

    public void setLinkGoogleMaps(String linkGoogleMaps) {
        this.linkGoogleMaps = linkGoogleMaps;
    }

    public String getDiasReparto() {
        return diasReparto;
    }

    public void setDiasReparto(String diasReparto) {
        this.diasReparto = diasReparto;
    }

    public Double getPrecioPorGarrafon() {
        return precioPorGarrafon;
    }

    public void setPrecioPorGarrafon(Double precioPorGarrafon) {
        this.precioPorGarrafon = precioPorGarrafon;
    }

    public Integer getOrdenEnRuta() {
        return ordenEnRuta;
    }

    public void setOrdenEnRuta(Integer ordenEnRuta) {
        this.ordenEnRuta = ordenEnRuta;
    }

    public ProductoResumen getGarrafonPreferencia() {
        return garrafonPreferencia;
    }

    public void setGarrafonPreferencia(ProductoResumen garrafonPreferencia) {
        this.garrafonPreferencia = garrafonPreferencia;
    }

    public RutaResumen getRuta() {
        return ruta;
    }

    public void setRuta(RutaResumen ruta) {
        this.ruta = ruta;
    }

    @Schema(description = "Datos mínimos del garrafón preferido")
    public static class ProductoResumen {

        private Long id;
        private String nombre;

        public ProductoResumen() {
        }

        public ProductoResumen(Long id, String nombre) {
            this.id = id;
            this.nombre = nombre;
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
    }

    @Schema(description = "Identificador de la ruta asignada")
    public static class RutaResumen {

        private Long id;

        public RutaResumen() {
        }

        public RutaResumen(Long id) {
            this.id = id;
        }

        public Long getId() {
            return id;
        }

        public void setId(Long id) {
            this.id = id;
        }
    }
}
