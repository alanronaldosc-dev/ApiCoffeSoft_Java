package com.utvt.ApiSpringCafeSoft.controller;

import com.utvt.ApiSpringCafeSoft.dto.InsumoDTO;
import com.utvt.ApiSpringCafeSoft.service.InsumoService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@CrossOrigin(origins = "*")
@RestController
@RequestMapping("/api/insumos")
@Tag(name = "Insumos", description = "API para la gestión de insumos")
public class InsumoController {

    @Autowired
    private InsumoService insumoService;

    @Operation(summary = "Crear un nuevo insumo")
    @PostMapping
    public ResponseEntity<InsumoDTO> crearInsumo(@Valid @RequestBody InsumoDTO insumoDTO) {
        return new ResponseEntity<>(insumoService.crearInsumo(insumoDTO), HttpStatus.CREATED);
    }

    @Operation(summary = "Obtener todos los insumos")
    @GetMapping
    public ResponseEntity<List<InsumoDTO>> obtenerTodos() {
        return ResponseEntity.ok(insumoService.obtenerTodos());
    }

    @Operation(summary = "Obtener insumo por ID")
    @GetMapping("/{id}")
    public ResponseEntity<InsumoDTO> obtenerPorId(@PathVariable Long id) {
        return ResponseEntity.ok(insumoService.obtenerPorId(id));
    }

    @Operation(summary = "Actualizar un insumo")
    @PutMapping("/{id}")
    public ResponseEntity<InsumoDTO> actualizarInsumo(@PathVariable Long id,
            @Valid @RequestBody InsumoDTO insumoDTO) {
        return ResponseEntity.ok(insumoService.actualizarInsumo(id, insumoDTO));
    }

    @Operation(summary = "Eliminar un insumo")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminarInsumo(@PathVariable Long id) {
        insumoService.eliminarInsumo(id);
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "Buscar insumos por nombre")
    @GetMapping("/buscar")
    public ResponseEntity<List<InsumoDTO>> buscarPorNombre(@RequestParam String nombre) {
        return ResponseEntity.ok(insumoService.buscarPorNombre(nombre));
    }
}
