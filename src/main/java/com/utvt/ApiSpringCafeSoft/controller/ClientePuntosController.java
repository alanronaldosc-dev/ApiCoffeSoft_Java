package com.utvt.ApiSpringCafeSoft.controller;

import com.utvt.ApiSpringCafeSoft.dto.ClientePuntosDTO;
import com.utvt.ApiSpringCafeSoft.dto.CotizacionCanjeDTO;
import com.utvt.ApiSpringCafeSoft.dto.CotizacionCanjeRequestDTO;
import com.utvt.ApiSpringCafeSoft.dto.PuntosResumenDTO;
import com.utvt.ApiSpringCafeSoft.service.PuntosService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/clientes")
public class ClientePuntosController {

    private final PuntosService puntosService;

    public ClientePuntosController(PuntosService puntosService) {
        this.puntosService = puntosService;
    }

    @GetMapping
    public List<ClientePuntosDTO> listarClientes() {
        return puntosService.listarClientesActivos();
    }

    @GetMapping("/{clienteId}/puntos")
    public PuntosResumenDTO obtenerSaldo(@PathVariable Long clienteId) {
        return puntosService.obtenerResumen(clienteId);
    }

    @PostMapping("/{clienteId}/beneficios/cotizaciones")
    public CotizacionCanjeDTO cotizarCanje(
            @PathVariable Long clienteId,
            @Valid @RequestBody CotizacionCanjeRequestDTO solicitud) {
        return puntosService.cotizar(clienteId, solicitud);
    }
}
