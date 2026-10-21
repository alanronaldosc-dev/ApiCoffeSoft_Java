package com.utvt.ApiSpringCafeSoft.controller;

import com.utvt.ApiSpringCafeSoft.model.Auditoria;
import com.utvt.ApiSpringCafeSoft.service.AuditoriaService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/auditoria")
@CrossOrigin(origins = "*")
public class AuditoriaController {

    @Autowired
    private AuditoriaService auditoriaService;

    /**
     * Registro de actividad reciente: quién hizo qué y cuándo.
     * Visible para ADMIN, EMPLEADO y REPARTIDOR (ver SecurityConfig).
     */
    @GetMapping
    public ResponseEntity<List<Auditoria>> obtenerRecientes() {
        return ResponseEntity.ok(auditoriaService.obtenerRecientes());
    }
}
