package com.utvt.ApiSpringCafeSoft.controller;

import com.utvt.ApiSpringCafeSoft.service.AnaliticaProductosService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.time.LocalDate;
import java.util.*;

@RestController
@RequestMapping("/api/analitica")
public class AnaliticaProductosController {
    private final AnaliticaProductosService servicio;
    public AnaliticaProductosController(AnaliticaProductosService servicio) { this.servicio=servicio; }
    @GetMapping("/combinaciones")
    public ResponseEntity<?> combinaciones(@RequestParam(required=false) LocalDate inicio, @RequestParam(required=false) LocalDate fin,
                                           @RequestParam(defaultValue="2") int minSupport,@RequestParam(defaultValue="10") int limite) {
        try { return ResponseEntity.ok(servicio.combinaciones(inicio,fin,minSupport,limite)); }
        catch (IllegalArgumentException e) { return ResponseEntity.badRequest().body(Map.of("error",e.getMessage())); }
    }
    @GetMapping("/recomendaciones/{clienteId}")
    public ResponseEntity<?> recomendaciones(@PathVariable Long clienteId,@RequestParam(defaultValue="5") int limite) {
        try { return ResponseEntity.ok(servicio.recomendaciones(clienteId,limite)); }
        catch (IllegalArgumentException e) { return ResponseEntity.badRequest().body(Map.of("error",e.getMessage())); }
    }
}
