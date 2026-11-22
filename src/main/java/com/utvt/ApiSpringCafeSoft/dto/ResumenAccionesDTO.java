package com.utvt.ApiSpringCafeSoft.dto;

import java.time.LocalDateTime;

/** HU-081: estado de las acciones de retención de un cliente. */
public record ResumenAccionesDTO(Long clienteId, long pendientes, long atendidas,
        LocalDateTime fechaUltimaAtencion) {
}
