package com.utvt.ApiSpringCafeSoft.controller;

import com.utvt.ApiSpringCafeSoft.dto.ClienteSegmentoDTO;
import com.utvt.ApiSpringCafeSoft.dto.SegmentosResumenDTO;
import com.utvt.ApiSpringCafeSoft.service.SegmentacionClienteService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/clientes/segmentos")
public class SegmentacionClienteController {

    private final SegmentacionClienteService segmentacionClienteService;

    public SegmentacionClienteController(
            SegmentacionClienteService segmentacionClienteService) {
        this.segmentacionClienteService = segmentacionClienteService;
    }

    @GetMapping
    public List<ClienteSegmentoDTO> obtenerClientes() {
        return segmentacionClienteService.obtenerClientesSegmentados();
    }

    @GetMapping("/resumen")
    public SegmentosResumenDTO obtenerResumen() {
        return segmentacionClienteService.obtenerResumen();
    }
}
