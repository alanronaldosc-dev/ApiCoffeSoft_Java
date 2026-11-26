package com.utvt.ApiSpringCafeSoft.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDateTime;
import java.util.List;

@Schema(description = "Ranking predictivo de los productos más y menos vendidos")
@JsonIgnoreProperties(ignoreUnknown = true)
public class PrediccionProductosDTO {

    @Schema(description = "Días hacia adelante que cubre la estimación", example = "7")
    private int horizonteDias;

    @Schema(description = "Días de ventas usados como historial", example = "28")
    private int historialDias;

    @Schema(description = "Indica que los valores son estimados y no ventas reales")
    private boolean esEstimacion;

    @Schema(description = "Método usado para calcular la estimación")
    private String metodo;

    @Schema(description = "Nombre de la sucursal consultada", example = "Bonafont")
    private String sucursalNombre;

    @Schema(description = "Fecha y hora en que se generó la predicción")
    private LocalDateTime generadoEn;

    @Schema(description = "Cinco productos con mayor demanda estimada")
    private List<Producto> masVendidos;

    @Schema(description = "Cinco productos con menor demanda estimada")
    private List<Producto> menosVendidos;

    public PrediccionProductosDTO() {
    }

    public int getHorizonteDias() {
        return horizonteDias;
    }

    public void setHorizonteDias(int horizonteDias) {
        this.horizonteDias = horizonteDias;
    }

    public int getHistorialDias() {
        return historialDias;
    }

    public void setHistorialDias(int historialDias) {
        this.historialDias = historialDias;
    }

    public boolean isEsEstimacion() {
        return esEstimacion;
    }

    public void setEsEstimacion(boolean esEstimacion) {
        this.esEstimacion = esEstimacion;
    }

    public String getMetodo() {
        return metodo;
    }

    public void setMetodo(String metodo) {
        this.metodo = metodo;
    }

    public String getSucursalNombre() {
        return sucursalNombre;
    }

    public void setSucursalNombre(String sucursalNombre) {
        this.sucursalNombre = sucursalNombre;
    }

    public LocalDateTime getGeneradoEn() {
        return generadoEn;
    }

    public void setGeneradoEn(LocalDateTime generadoEn) {
        this.generadoEn = generadoEn;
    }

    public List<Producto> getMasVendidos() {
        return masVendidos;
    }

    public void setMasVendidos(List<Producto> masVendidos) {
        this.masVendidos = masVendidos;
    }

    public List<Producto> getMenosVendidos() {
        return menosVendidos;
    }

    public void setMenosVendidos(List<Producto> menosVendidos) {
        this.menosVendidos = menosVendidos;
    }

    @Schema(description = "Producto dentro del ranking predictivo")
    public static class Producto {

        @Schema(description = "ID del producto", example = "12")
        private Long productoId;

        @Schema(description = "Nombre del producto", example = "Garrafón 20L")
        private String nombre;

        @Schema(description = "Unidades vendidas en el historial", example = "84")
        private long unidadesHistoricas;

        @Schema(description = "Unidades estimadas para el horizonte", example = "24")
        private long unidadesEstimadas;

        public Producto() {
        }

        public Producto(Long productoId, String nombre, long unidadesHistoricas, long unidadesEstimadas) {
            this.productoId = productoId;
            this.nombre = nombre;
            this.unidadesHistoricas = unidadesHistoricas;
            this.unidadesEstimadas = unidadesEstimadas;
        }

        public Long getProductoId() {
            return productoId;
        }

        public void setProductoId(Long productoId) {
            this.productoId = productoId;
        }

        public String getNombre() {
            return nombre;
        }

        public void setNombre(String nombre) {
            this.nombre = nombre;
        }

        public long getUnidadesHistoricas() {
            return unidadesHistoricas;
        }

        public void setUnidadesHistoricas(long unidadesHistoricas) {
            this.unidadesHistoricas = unidadesHistoricas;
        }

        public long getUnidadesEstimadas() {
            return unidadesEstimadas;
        }

        public void setUnidadesEstimadas(long unidadesEstimadas) {
            this.unidadesEstimadas = unidadesEstimadas;
        }
    }
}
