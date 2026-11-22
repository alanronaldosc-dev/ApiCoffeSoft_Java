package com.utvt.ApiSpringCafeSoft.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.time.LocalDateTime;

/** HU-081: datos necesarios para registrar una acción de retención. */
public record NuevaAccionRetencionDTO(
        @NotBlank(message = "El tipo de acción es obligatorio") String tipo,
        @NotBlank(message = "El responsable es obligatorio") String responsable,
        @NotNull(message = "La fecha es obligatoria") LocalDateTime fecha,
        @Size(max = 500, message = "El detalle no puede exceder 500 caracteres") String detalle) {
}
