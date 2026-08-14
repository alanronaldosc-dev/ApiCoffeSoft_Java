package com.utvt.ApiSpringCafeSoft.controller;

import com.utvt.ApiSpringCafeSoft.model.Proveedor;
import com.utvt.ApiSpringCafeSoft.service.ProveedorService;

import jakarta.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/proveedores")
@CrossOrigin(origins = "*")
public class ProveedorController {

    @Autowired
    private ProveedorService proveedorService;

    // ============================================
    // HU-013 - REGISTRAR PROVEEDOR
    // ============================================

    @PostMapping
    public ResponseEntity<Proveedor> crearProveedor(
            @Valid @RequestBody Proveedor proveedor) {

        return ResponseEntity.ok(
                proveedorService.crearProveedor(proveedor)
        );
    }

    // ============================================
    // HU-013 - CATÁLOGO DE PROVEEDORES
    // ============================================

    @GetMapping
    public ResponseEntity<List<Proveedor>>
            obtenerProveedores() {

        return ResponseEntity.ok(
                proveedorService.obtenerProveedores()
        );
    }

    // Obtener proveedor por ID
    @GetMapping("/{id}")
    public ResponseEntity<Proveedor>
            obtenerProveedor(@PathVariable Long id) {

        return ResponseEntity.ok(
                proveedorService.obtenerProveedorPorId(id)
        );
    }

    // ============================================
    // HU-013 - DAR DE BAJA
    // ============================================

    @PutMapping("/{id}/baja")
    public ResponseEntity<Proveedor>
            darDeBaja(@PathVariable Long id) {

        return ResponseEntity.ok(
                proveedorService.darDeBaja(id)
        );
    }

    // ============================================
    // HU-013 - ELIMINAR PROVEEDOR
    // ============================================

    @DeleteMapping("/{id}")
    public ResponseEntity<Map<String, String>>
            eliminarProveedor(@PathVariable Long id) {

        proveedorService.eliminarProveedor(id);

        return ResponseEntity.ok(
                Map.of(
                    "mensaje",
                    "Proveedor eliminado correctamente"
                )
        );
    }
}