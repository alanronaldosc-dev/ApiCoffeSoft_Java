package com.utvt.ApiSpringCafeSoft.service;

import com.utvt.ApiSpringCafeSoft.dto.InsumoDTO;
import com.utvt.ApiSpringCafeSoft.model.Insumo;
import com.utvt.ApiSpringCafeSoft.model.Proveedor;
import com.utvt.ApiSpringCafeSoft.repository.InsumoRepository;
import com.utvt.ApiSpringCafeSoft.repository.ProveedorRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class InsumoService {

    @Autowired
    private InsumoRepository insumoRepository;

    @Autowired
    private ProveedorRepository proveedorRepository;

    private InsumoDTO convertToDTO(Insumo insumo) {
        Long proveedorId = null;
        String proveedorNombre = null;
        if (insumo.getProveedor() != null) {
            proveedorId = insumo.getProveedor().getId();
            proveedorNombre = insumo.getProveedor().getNombreEmpresa();
        }
        return new InsumoDTO(
            insumo.getId(),
            insumo.getNombre(),
            insumo.getTipo(),
            insumo.getUnidadMedida(),
            proveedorId,
            proveedorNombre,
            insumo.getPrecio()
        );
    }

    private Proveedor resolverProveedor(Long proveedorId) {
        if (proveedorId == null) return null;
        return proveedorRepository.findById(proveedorId)
            .orElseThrow(() -> new RuntimeException("Proveedor no encontrado con ID: " + proveedorId));
    }

    @Transactional
    public InsumoDTO crearInsumo(InsumoDTO dto) {
        Insumo insumo = new Insumo();
        insumo.setNombre(dto.getNombre());
        insumo.setTipo(dto.getTipo());
        insumo.setUnidadMedida(dto.getUnidadMedida());
        insumo.setPrecio(dto.getPrecio());
        insumo.setProveedor(resolverProveedor(dto.getProveedorId()));
        return convertToDTO(insumoRepository.save(insumo));
    }

    public List<InsumoDTO> obtenerTodos() {
        return insumoRepository.findAll().stream().map(this::convertToDTO).collect(Collectors.toList());
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
        existing.setPrecio(dto.getPrecio());
        existing.setProveedor(resolverProveedor(dto.getProveedorId()));
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
            .map(this::convertToDTO).collect(Collectors.toList());
    }
}