package com.utvt.ApiSpringCafeSoft.controller;

import com.utvt.ApiSpringCafeSoft.dto.PromocionDTO;
import com.utvt.ApiSpringCafeSoft.service.PromocionService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Validated
@RestController
@RequestMapping("/api/promociones")
public class PromocionController {

    private final PromocionService promocionService;

    public PromocionController(PromocionService promocionService) {
        this.promocionService = promocionService;
    }

    @GetMapping
    public List<PromocionDTO> listar() {
        return promocionService.listar();
    }

    @GetMapping("/aplicables")
    public List<PromocionDTO> listarAplicables(
            @RequestParam Long clienteId,
            @RequestParam(defaultValue = "0") @Min(0) Integer cantidadGarrafones) {
        return promocionService.listarAplicables(clienteId, cantidadGarrafones);
    }

    @PostMapping
    public ResponseEntity<PromocionDTO> crear(@Valid @RequestBody PromocionDTO solicitud) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(promocionService.crear(solicitud));
    }

    @PutMapping("/{id}")
    public PromocionDTO actualizar(
            @PathVariable Long id,
            @Valid @RequestBody PromocionDTO solicitud) {
        return promocionService.actualizar(id, solicitud);
    }

    @PutMapping("/{id}/desactivar")
    public ResponseEntity<Void> desactivar(@PathVariable Long id) {
        promocionService.desactivar(id);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminar(@PathVariable Long id) {
        promocionService.eliminar(id);
        return ResponseEntity.noContent().build();
    }
}
