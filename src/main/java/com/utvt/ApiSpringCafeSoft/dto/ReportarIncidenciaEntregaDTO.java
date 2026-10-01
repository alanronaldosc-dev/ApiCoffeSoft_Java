package com.utvt.ApiSpringCafeSoft.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;

/** HU-015: reporte cuando la entrega no puede completarse. */
public class ReportarIncidenciaEntregaDTO {

    @NotNull(message = "El repartidor es obligatorio")
    private Long repartidorId;

    @NotBlank(message = "El motivo es obligatorio")
    @Pattern(regexp = "^(CLIENTE_AUSENTE|SIN_ENVASES)$", message = "Motivo no válido")
    private String motivo;

    private String observaciones;

    public Long getRepartidorId() { return repartidorId; }
    public void setRepartidorId(Long repartidorId) { this.repartidorId = repartidorId; }

    public String getMotivo() { return motivo; }
    public void setMotivo(String motivo) { this.motivo = motivo; }

    public String getObservaciones() { return observaciones; }
    public void setObservaciones(String observaciones) { this.observaciones = observaciones; }
}
