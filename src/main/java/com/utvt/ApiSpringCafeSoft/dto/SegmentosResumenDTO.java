package com.utvt.ApiSpringCafeSoft.dto;

import com.utvt.ApiSpringCafeSoft.model.SegmentoCliente;

import java.util.Map;

public record SegmentosResumenDTO(long total, Map<SegmentoCliente, Long> segmentos) {
}
