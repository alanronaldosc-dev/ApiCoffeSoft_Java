package com.utvt.ApiSpringCafeSoft.controller;

import com.utvt.ApiSpringCafeSoft.model.Cliente;
import com.utvt.ApiSpringCafeSoft.service.ClienteService;

import jakarta.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/clientes")
@CrossOrigin(origins = "*")
public class ClienteController {

    @Autowired
    private ClienteService clienteService;

    // ============================================
    // HU-011 - REGISTRAR CLIENTE
    // ============================================

    @PostMapping
    public ResponseEntity<Cliente> crearCliente(
            @Valid @RequestBody Cliente cliente) {

        return ResponseEntity.ok(
                clienteService.crearCliente(cliente)
        );
    }

    // ============================================
    // HU-011 - CATÁLOGO DE CLIENTES
    // ============================================

    @GetMapping
    public ResponseEntity<List<Cliente>> obtenerClientes() {

        return ResponseEntity.ok(
                clienteService.obtenerClientes()
        );
    }

    // Obtener cliente por ID
    @GetMapping("/{id}")
    public ResponseEntity<Cliente> obtenerCliente(@PathVariable Long id) {

        return ResponseEntity.ok(
                clienteService.obtenerClientePorId(id)
        );
    }

    // ============================================
    // HU-011 - ACTUALIZAR CLIENTE
    // ============================================

    @PutMapping("/{id}")
    public ResponseEntity<Cliente> actualizarCliente(
            @PathVariable Long id,
            @Valid @RequestBody Cliente cliente) {

        return ResponseEntity.ok(
                clienteService.actualizarCliente(id, cliente)
        );
    }

    // ============================================
    // HU-011 - DAR DE BAJA
    // ============================================

    @PutMapping("/{id}/baja")
    public ResponseEntity<Cliente> darDeBaja(@PathVariable Long id) {

        return ResponseEntity.ok(
                clienteService.darDeBaja(id)
        );
    }

    // ============================================
    // HU-011 - DAR DE ALTA
    // ============================================

    @PutMapping("/{id}/alta")
    public ResponseEntity<Cliente> darDeAlta(@PathVariable Long id) {

        return ResponseEntity.ok(
                clienteService.darDeAlta(id)
        );
    }

    // ============================================
    // HU-011 - ELIMINAR CLIENTE
    // ============================================

    @DeleteMapping("/{id}")
    public ResponseEntity<Map<String, String>> eliminarCliente(@PathVariable Long id) {

        clienteService.eliminarCliente(id);

        return ResponseEntity.ok(
                Map.of(
                    "mensaje",
                    "Cliente eliminado correctamente"
                )
        );
    }
}
