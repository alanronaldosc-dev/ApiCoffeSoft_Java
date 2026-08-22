package com.utvt.ApiSpringCafeSoft.controller;

import com.utvt.ApiSpringCafeSoft.dto.LoteDTO;
import com.utvt.ApiSpringCafeSoft.service.LoteService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/lotes")
@Tag(name = "Lotes", description = "API para registro de lotes de insumos")
public class LoteController {

    @Autowired
    private LoteService loteService;

    @Operation(summary = "Registrar un nuevo lote")
    @PostMapping
    public ResponseEntity<LoteDTO> registrarLote(@Valid @RequestBody LoteDTO loteDTO) {
        return new ResponseEntity<>(loteService.registrarLote(loteDTO), HttpStatus.CREATED);
    }

    @Operation(summary = "Obtener todos los lotes")
    @GetMapping
    public ResponseEntity<List<LoteDTO>> obtenerTodos() {
        return ResponseEntity.ok(loteService.obtenerTodos());
    }

    @Operation(summary = "Obtener lotes por insumo")
    @GetMapping("/insumo/{insumoId}")
    public ResponseEntity<List<LoteDTO>> obtenerPorInsumo(@PathVariable Long insumoId) {
        return ResponseEntity.ok(loteService.obtenerPorInsumo(insumoId));
    }

    @Operation(summary = "Eliminar un lote")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminarLote(@PathVariable Long id) {
        loteService.eliminarLote(id);
        return ResponseEntity.noContent().build();
    }
    @Operation(summary = "Obtener lotes por proveedor")
@GetMapping("/proveedor/{proveedorId}")
public ResponseEntity<List<LoteDTO>> obtenerPorProveedor(@PathVariable Long proveedorId) {
    return ResponseEntity.ok(loteService.obtenerPorProveedor(proveedorId));
}

}