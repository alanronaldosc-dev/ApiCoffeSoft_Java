package com.utvt.ApiSpringCafeSoft.dto;

import java.time.LocalDateTime;

/** HU-081: acción de retención persistida en el log de auditoría existente. */
public record AccionRetencionDTO(Long id, Long clienteId, String tipo, String responsable,
        LocalDateTime fecha, String detalle, boolean atendida, LocalDateTime fechaAtencion) {
}
