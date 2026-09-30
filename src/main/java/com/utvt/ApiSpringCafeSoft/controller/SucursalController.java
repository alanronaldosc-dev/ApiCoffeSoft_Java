package com.utvt.ApiSpringCafeSoft.controller;

import com.utvt.ApiSpringCafeSoft.dto.SucursalDTO;
import com.utvt.ApiSpringCafeSoft.service.SucursalService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@CrossOrigin(origins = "*")
@RestController
@RequestMapping("/api/sucursales")
@Tag(name = "Sucursales", description = "API para la gestión de sucursales")
public class SucursalController {

    @Autowired
    private SucursalService sucursalService;

    @Operation(summary = "Crear una nueva sucursal")
    @PostMapping
    public ResponseEntity<Map<String, Object>> crearSucursal(@Valid @RequestBody SucursalDTO dto) {
        Map<String, Object> response = new HashMap<>();
        try {
            response.put("mensaje", "Sucursal creada exitosamente");
            response.put("sucursal", sucursalService.crearSucursal(dto));
            return new ResponseEntity<>(response, HttpStatus.CREATED);
        } catch (Exception e) {
            response.put("error", e.getMessage());
            return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
        }
    }

    @Operation(summary = "Obtener todas las sucursales")
    @GetMapping
    public ResponseEntity<List<SucursalDTO>> obtenerTodas() {
        return ResponseEntity.ok(sucursalService.obtenerTodas());
    }

    @Operation(summary = "Obtener solo sucursales activas")
    @GetMapping("/activas")
    public ResponseEntity<List<SucursalDTO>> obtenerActivas() {
        return ResponseEntity.ok(sucursalService.obtenerActivas());
    }

    @Operation(summary = "Obtener sucursal por ID")
    @GetMapping("/{id}")
    public ResponseEntity<Map<String, Object>> obtenerPorId(@PathVariable Long id) {
        Map<String, Object> response = new HashMap<>();
        try {
            response.put("sucursal", sucursalService.obtenerPorId(id));
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("error", e.getMessage());
            return new ResponseEntity<>(response, HttpStatus.NOT_FOUND);
        }
    }

    @Operation(summary = "Actualizar sucursal")
    @PutMapping("/{id}")
    public ResponseEntity<Map<String, Object>> actualizar(@PathVariable Long id,
                                                           @RequestBody SucursalDTO dto) {
        Map<String, Object> response = new HashMap<>();
        try {
            response.put("mensaje", "Sucursal actualizada exitosamente");
            response.put("sucursal", sucursalService.actualizarSucursal(id, dto));
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("error", e.getMessage());
            return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
        }
    }

    @Operation(summary = "Eliminar sucursal")
    @DeleteMapping("/{id}")
    public ResponseEntity<Map<String, Object>> eliminar(@PathVariable Long id) {
        Map<String, Object> response = new HashMap<>();
        try {
            sucursalService.eliminarSucursal(id);
            response.put("mensaje", "Sucursal eliminada exitosamente");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("error", e.getMessage());
            return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
        }
    }
}
