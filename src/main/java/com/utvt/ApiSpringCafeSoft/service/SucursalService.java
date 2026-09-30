package com.utvt.ApiSpringCafeSoft.service;

import com.utvt.ApiSpringCafeSoft.dto.SucursalDTO;
import com.utvt.ApiSpringCafeSoft.model.Sucursal;
import com.utvt.ApiSpringCafeSoft.repository.SucursalRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class SucursalService {

    @Autowired
    private SucursalRepository sucursalRepository;

    private SucursalDTO convertToDTO(Sucursal s) {
        return new SucursalDTO(s.getId(), s.getNombre(), s.getDireccion(),
                s.getTelefono(), s.getCiudad(), s.getActivo(), s.getCreatedAt());
    }

    @Transactional
    public SucursalDTO crearSucursal(SucursalDTO dto) {
        if (sucursalRepository.existsByNombre(dto.getNombre())) {
            throw new RuntimeException("Ya existe una sucursal con ese nombre");
        }
        Sucursal s = new Sucursal();
        s.setNombre(dto.getNombre());
        s.setDireccion(dto.getDireccion());
        s.setTelefono(dto.getTelefono());
        s.setCiudad(dto.getCiudad());
        s.setActivo(true);
        return convertToDTO(sucursalRepository.save(s));
    }

    public List<SucursalDTO> obtenerTodas() {
        return sucursalRepository.findAll().stream()
                .map(this::convertToDTO).collect(Collectors.toList());
    }

    public List<SucursalDTO> obtenerActivas() {
        return sucursalRepository.findByActivo(true).stream()
                .map(this::convertToDTO).collect(Collectors.toList());
    }

    public SucursalDTO obtenerPorId(Long id) {
        return convertToDTO(sucursalRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Sucursal no encontrada con ID: " + id)));
    }

    @Transactional
    public SucursalDTO actualizarSucursal(Long id, SucursalDTO dto) {
        Sucursal s = sucursalRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Sucursal no encontrada con ID: " + id));
        if (dto.getNombre() != null) s.setNombre(dto.getNombre());
        if (dto.getDireccion() != null) s.setDireccion(dto.getDireccion());
        if (dto.getTelefono() != null) s.setTelefono(dto.getTelefono());
        if (dto.getCiudad() != null) s.setCiudad(dto.getCiudad());
        if (dto.getActivo() != null) s.setActivo(dto.getActivo());
        return convertToDTO(sucursalRepository.save(s));
    }

    @Transactional
    public void eliminarSucursal(Long id) {
        if (!sucursalRepository.existsById(id)) {
            throw new RuntimeException("Sucursal no encontrada con ID: " + id);
        }
        sucursalRepository.deleteById(id);
    }
}
