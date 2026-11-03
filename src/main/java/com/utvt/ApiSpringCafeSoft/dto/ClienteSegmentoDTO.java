package com.utvt.ApiSpringCafeSoft.dto;

import com.utvt.ApiSpringCafeSoft.model.SegmentoCliente;

import java.time.LocalDateTime;

public record ClienteSegmentoDTO(
        Long clienteId,
        String nombre,
        SegmentoCliente segmento,
        long compras,
        LocalDateTime ultimaCompra) {
}
