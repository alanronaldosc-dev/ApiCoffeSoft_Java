package com.utvt.ApiSpringCafeSoft.service;

import com.utvt.ApiSpringCafeSoft.dto.CategoriaDTO;
import com.utvt.ApiSpringCafeSoft.model.Categoria;
import com.utvt.ApiSpringCafeSoft.repository.CategoriaRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class CategoriaService {

    @Autowired
    private CategoriaRepository categoriaRepository;

    private CategoriaDTO convertToDTO(Categoria categoria) {
        String createdAt = categoria.getCreatedAt() != null
            ? categoria.getCreatedAt().toString()
            : null;
        return new CategoriaDTO(
            categoria.getId(),
            categoria.getNombre(),
            categoria.getDescripcion(),
            categoria.getActivo(),
            createdAt
        );
    }

    public List<CategoriaDTO> obtenerTodas() {
        return categoriaRepository.findAll().stream()
            .map(this::convertToDTO)
            .collect(Collectors.toList());
    }

    public List<CategoriaDTO> obtenerActivas() {
        return categoriaRepository.findByActivoTrue().stream()
            .map(this::convertToDTO)
            .collect(Collectors.toList());
    }

    public CategoriaDTO obtenerPorId(Long id) {
        Categoria categoria = categoriaRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Categoría no encontrada con ID: " + id));
        return convertToDTO(categoria);
    }

    @Transactional
    public CategoriaDTO crear(CategoriaDTO dto) {
        if (categoriaRepository.existsByNombreIgnoreCase(dto.getNombre())) {
            throw new RuntimeException("Ya existe una categoría con ese nombre");
        }
        Categoria categoria = new Categoria();
        categoria.setNombre(dto.getNombre());
        categoria.setDescripcion(dto.getDescripcion());
        categoria.setActivo(dto.getActivo() != null ? dto.getActivo() : true);
        return convertToDTO(categoriaRepository.save(categoria));
    }

    @Transactional
    public CategoriaDTO actualizar(Long id, CategoriaDTO dto) {
        Categoria categoria = categoriaRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Categoría no encontrada con ID: " + id));

        // Validar nombre único excluyendo la propia categoría
        categoriaRepository.findByNombreIgnoreCase(dto.getNombre())
            .ifPresent(existing -> {
                if (!existing.getId().equals(id)) {
                    throw new RuntimeException("Ya existe una categoría con ese nombre");
                }
            });

        categoria.setNombre(dto.getNombre());
        categoria.setDescripcion(dto.getDescripcion());
        if (dto.getActivo() != null) {
            categoria.setActivo(dto.getActivo());
        }
        return convertToDTO(categoriaRepository.save(categoria));
    }

    @Transactional
    public void eliminar(Long id) {
        if (!categoriaRepository.existsById(id)) {
            throw new RuntimeException("Categoría no encontrada con ID: " + id);
        }
        categoriaRepository.deleteById(id);
    }
}
