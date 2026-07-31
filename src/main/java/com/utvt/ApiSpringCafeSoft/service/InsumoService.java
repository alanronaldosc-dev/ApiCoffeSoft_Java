package com.utvt.ApiSpringCafeSoft.service;

import com.utvt.ApiSpringCafeSoft.dto.InsumoDTO;
import com.utvt.ApiSpringCafeSoft.model.Insumo;
import com.utvt.ApiSpringCafeSoft.repository.InsumoRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class InsumoService {

    @Autowired
    private InsumoRepository insumoRepository;

    private InsumoDTO convertToDTO(Insumo insumo) {
        return new InsumoDTO(
            insumo.getId(),
            insumo.getNombre(),
            insumo.getTipo(),
            insumo.getUnidadMedida(),
            insumo.getProveedor(),
            insumo.getPrecio()
        );
    }

    private Insumo convertToEntity(InsumoDTO dto) {
        Insumo insumo = new Insumo();
        insumo.setId(dto.getId());
        insumo.setNombre(dto.getNombre());
        insumo.setTipo(dto.getTipo());
        insumo.setUnidadMedida(dto.getUnidadMedida());
        insumo.setProveedor(dto.getProveedor());
        insumo.setPrecio(dto.getPrecio());
        return insumo;
    }

    @Transactional
    public InsumoDTO crearInsumo(InsumoDTO dto) {
        return convertToDTO(insumoRepository.save(convertToEntity(dto)));
    }

    public List<InsumoDTO> obtenerTodos() {
        return insumoRepository.findAll().stream()
            .map(this::convertToDTO)
            .collect(Collectors.toList());
    }

    public InsumoDTO obtenerPorId(Long id) {
        Insumo insumo = insumoRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Insumo no encontrado con ID: " + id));
        return convertToDTO(insumo);
    }

    @Transactional
    public InsumoDTO actualizarInsumo(Long id, InsumoDTO dto) {
        Insumo existing = insumoRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Insumo no encontrado con ID: " + id));
        existing.setNombre(dto.getNombre());
        existing.setTipo(dto.getTipo());
        existing.setUnidadMedida(dto.getUnidadMedida());
        existing.setProveedor(dto.getProveedor());
        existing.setPrecio(dto.getPrecio());
        return convertToDTO(insumoRepository.save(existing));
    }

    @Transactional
    public void eliminarInsumo(Long id) {
        if (!insumoRepository.existsById(id))
            throw new RuntimeException("Insumo no encontrado con ID: " + id);
        insumoRepository.deleteById(id);
    }

    public List<InsumoDTO> buscarPorNombre(String nombre) {
        return insumoRepository.findByNombreContainingIgnoreCase(nombre).stream()
            .map(this::convertToDTO)
            .collect(Collectors.toList());
    }
}
