
package com.utvt.ApiSpringCafeSoft.dto;

import java.time.LocalDateTime;
import java.util.List;

public class PrediccionDemandaDTO {

    private LocalDateTime inicioPeriodoReferencia;
    private LocalDateTime finPeriodoReferencia;
    private LocalDateTime inicioPeriodoPrediccion;
    private LocalDateTime finPeriodoPrediccion;
    private List<InsumoPredichoDTO> insumos;

    public PrediccionDemandaDTO() {}

    public PrediccionDemandaDTO(
            LocalDateTime inicioPeriodoReferencia,
            LocalDateTime finPeriodoReferencia,
            LocalDateTime inicioPeriodoPrediccion,
            LocalDateTime finPeriodoPrediccion,
            List<InsumoPredichoDTO> insumos) {
        this.inicioPeriodoReferencia = inicioPeriodoReferencia;
        this.finPeriodoReferencia = finPeriodoReferencia;
        this.inicioPeriodoPrediccion = inicioPeriodoPrediccion;
        this.finPeriodoPrediccion = finPeriodoPrediccion;
        this.insumos = insumos;
    }

    public LocalDateTime getInicioPeriodoReferencia() {
        return inicioPeriodoReferencia;
    }

    public LocalDateTime getFinPeriodoReferencia() {
        return finPeriodoReferencia;
    }

    public LocalDateTime getInicioPeriodoPrediccion() {
        return inicioPeriodoPrediccion;
    }

    public LocalDateTime getFinPeriodoPrediccion() {
        return finPeriodoPrediccion;
    }

    public List<InsumoPredichoDTO> getInsumos() {
        return insumos;
    }

    public static class InsumoPredichoDTO {

        private Long id;
        private String nombre;
        private String unidadMedida;
        private Double stockActual;
        private Double consumoUltimos30Dias;
        private Double promedioDiario;
        private Double demandaSiguiente30Dias;
        private Double faltante;
        private String estadoStock;
        private Long sucursalId;
        private String sucursalNombre;

        public InsumoPredichoDTO() {}

        public InsumoPredichoDTO(
                Long id,
                String nombre,
                String unidadMedida,
                Double stockActual,
                Double consumoUltimos30Dias,
                Double promedioDiario,
                Double demandaSiguiente30Dias,
                Double faltante,
                String estadoStock,
                Long sucursalId,
                String sucursalNombre) {
            this.id = id;
            this.nombre = nombre;
            this.unidadMedida = unidadMedida;
            this.stockActual = stockActual;
            this.consumoUltimos30Dias = consumoUltimos30Dias;
            this.promedioDiario = promedioDiario;
            this.demandaSiguiente30Dias = demandaSiguiente30Dias;
            this.faltante = faltante;
            this.estadoStock = estadoStock;
            this.sucursalId = sucursalId;
            this.sucursalNombre = sucursalNombre;
        }

        public Long getId() { return id; }
        public String getNombre() { return nombre; }
        public String getUnidadMedida() { return unidadMedida; }
        public Double getStockActual() { return stockActual; }
        public Double getConsumoUltimos30Dias() { return consumoUltimos30Dias; }
        public Double getPromedioDiario() { return promedioDiario; }
        public Double getDemandaSiguiente30Dias() { return demandaSiguiente30Dias; }
        public Double getFaltante() { return faltante; }
        public String getEstadoStock() { return estadoStock; }
        public Long getSucursalId() { return sucursalId; }
        public String getSucursalNombre() { return sucursalNombre; }
    }
}
