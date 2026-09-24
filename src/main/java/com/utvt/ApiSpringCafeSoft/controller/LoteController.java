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

    @Operation(summary = "Registrar un nuevo lote de insumo")
    @PostMapping
    public ResponseEntity<LoteDTO> registrarLote(@Valid @RequestBody LoteDTO loteDTO) {
        return new ResponseEntity<>(loteService.registrarLote(loteDTO), HttpStatus.CREATED);
    }

    @Operation(summary = "Producir unidades de un producto",
               description = "Descuenta insumos del inventario, suma al inventario de productos y registra un lote de producción")
    @PostMapping("/producir")
    public ResponseEntity<LoteDTO> producirProducto(@RequestBody ProducirRequest request) {
        LoteDTO resultado = loteService.producirProducto(
            request.getProductoId(),
            request.getCantidad(),
            request.getFechaCaducidad(),
            request.getObservaciones()
        );
        return new ResponseEntity<>(resultado, HttpStatus.CREATED);
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

    @Operation(summary = "Obtener lotes por proveedor")
    @GetMapping("/proveedor/{proveedorId}")
    public ResponseEntity<List<LoteDTO>> obtenerPorProveedor(@PathVariable Long proveedorId) {
        return ResponseEntity.ok(loteService.obtenerPorProveedor(proveedorId));
    }

    @Operation(summary = "Eliminar un lote")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminarLote(@PathVariable Long id) {
        loteService.eliminarLote(id);
        return ResponseEntity.noContent().build();
    }

    // ── DTO interno para la request de producción ──
    static class ProducirRequest {
        private Long productoId;
        private Double cantidad;
        private String fechaCaducidad; // formato "yyyy-MM-dd", puede ser null
        private String observaciones;

        public Long getProductoId() { return productoId; }
        public void setProductoId(Long productoId) { this.productoId = productoId; }
        public Double getCantidad() { return cantidad; }
        public void setCantidad(Double cantidad) { this.cantidad = cantidad; }
        public String getFechaCaducidad() { return fechaCaducidad; }
        public void setFechaCaducidad(String fechaCaducidad) { this.fechaCaducidad = fechaCaducidad; }
        public String getObservaciones() { return observaciones; }
        public void setObservaciones(String observaciones) { this.observaciones = observaciones; }
    }
}
