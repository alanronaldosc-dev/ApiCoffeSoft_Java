package com.utvt.ApiSpringCafeSoft.service;

import com.utvt.ApiSpringCafeSoft.dto.DashboardStockDTO;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

/**
 * El archivo original de HU-010 estaba vacío. Se conserva el contrato del
 * controlador sin inventar una regla para distinguir envases llenos y vacíos.
 * Completar el cálculo antes de aprobar o desplegar esta HU.
 */
@Service
public class DashboardStockService {
    public DashboardStockDTO obtenerStockGarrafones() {
        throw new ResponseStatusException(HttpStatus.NOT_IMPLEMENTED,
                "La consulta de stock de garrafones todavía no está disponible");
    }
}
