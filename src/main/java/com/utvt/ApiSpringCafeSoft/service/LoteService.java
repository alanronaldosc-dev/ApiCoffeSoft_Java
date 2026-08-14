package com.utvt.ApiSpringCafeSoft.service;

import com.utvt.ApiSpringCafeSoft.dto.LoteDTO;
import com.utvt.ApiSpringCafeSoft.model.Insumo;
import com.utvt.ApiSpringCafeSoft.model.Inventario;
import com.utvt.ApiSpringCafeSoft.model.Lote;
import com.utvt.ApiSpringCafeSoft.repository.InsumoRepository;
import com.utvt.ApiSpringCafeSoft.repository.InventarioRepository;
import com.utvt.ApiSpringCafeSoft.repository.LoteRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;


import java.util.List;
import java.util.stream.Collectors;

@Service
public class LoteService {

    @Autowired
    private LoteRepository loteRepository;

    @Autowired
    private InsumoRepository insumoRepository;

    @Autowired
    private InventarioRepository inventarioRepository;


    private LoteDTO convertToDTO(Lote lote) {
        return new LoteDTO(
            lote.getId(),
            lote.getInsumo().getId(),
            lote.getInsumo().getNombre(),
            lote.getInsumo().getUnidadMedida(),
            lote.getCantidad(),
            lote.getFechaCaducidad(),
            lote.getFechaEntrada(),
            lote.getObservaciones()
        );
    }

@Transactional
public LoteDTO registrarLote(LoteDTO dto) {
    Insumo insumo = insumoRepository.findById(dto.getInsumoId())
        .orElseThrow(() -> new RuntimeException("Insumo no encontrado con ID: " + dto.getInsumoId()));

    // Guardar el lote
    Lote lote = new Lote(insumo, dto.getCantidad(), dto.getFechaCaducidad(), dto.getObservaciones());
    loteRepository.save(lote);

    // Buscar si ya existe en inventario por nombre y unidad
    List<Inventario> existentes = inventarioRepository.findByNombreContainingIgnoreCase(insumo.getNombre());
    Inventario inventario = existentes.stream()
        .filter(i -> i.getUnidadMedida().equals(insumo.getUnidadMedida()))
        .findFirst()
        .orElse(null);

    if (inventario != null) {
        // Ya existe, sumar cantidad
        inventario.setCantidad(inventario.getCantidad() + dto.getCantidad());
        inventarioRepository.save(inventario);
    } else {
        // Crear nuevo registro en inventario
        Inventario nuevo = new Inventario(
            insumo.getNombre(),
            insumo.getTipo(),
            dto.getCantidad(),
            insumo.getUnidadMedida(),
            0.0,                          // cantidadMinima
            dto.getFechaCaducidad().toString(),
            insumo.getProveedor(),
            insumo.getPrecio()
        );
        inventarioRepository.save(nuevo);
    }

    return convertToDTO(lote);
}

public List<LoteDTO> obtenerTodos() {
    return loteRepository.findAll().stream()
        .map(this::convertToDTO)
        .collect(Collectors.toList());
}

public List<LoteDTO> obtenerPorInsumo(Long insumoId) {
    return loteRepository.findByInsumoId(insumoId).stream()
        .map(this::convertToDTO)
        .collect(Collectors.toList());
}



    @Transactional
    public void eliminarLote(Long id) {
        if (!loteRepository.existsById(id))
            throw new RuntimeException("Lote no encontrado con ID: " + id);
        loteRepository.deleteById(id);
    }
}
