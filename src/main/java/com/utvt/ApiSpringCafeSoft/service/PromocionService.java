package com.utvt.ApiSpringCafeSoft.service;

import com.utvt.ApiSpringCafeSoft.dto.PromocionDTO;
import com.utvt.ApiSpringCafeSoft.model.Promocion;
import com.utvt.ApiSpringCafeSoft.model.Sucursal;
import com.utvt.ApiSpringCafeSoft.model.Usuario;
import com.utvt.ApiSpringCafeSoft.repository.PromocionRepository;
import com.utvt.ApiSpringCafeSoft.repository.SucursalRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.utvt.ApiSpringCafeSoft.model.Usuario;
import com.utvt.ApiSpringCafeSoft.repository.UsuarioRepository;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class PromocionService {

    @Autowired
    private PromocionRepository promocionRepository;

    @Autowired
    private UsuarioRepository usuarioRepository;


    // ── Mapeo entidad → DTO ──
    private PromocionDTO toDTO(Promocion p) {
        PromocionDTO dto = new PromocionDTO();
        dto.setId(p.getId());
        dto.setNombre(p.getNombre());
        dto.setDescripcion(p.getDescripcion());
        dto.setDescuentoPorcentaje(p.getDescuentoPorcentaje());
        dto.setFechaInicio(p.getFechaInicio());
        dto.setFechaFin(p.getFechaFin());
        dto.setActiva(p.getActiva());
        dto.setMinGarrafones(p.getMinGarrafones());
        dto.setAcumulaPuntos(p.getAcumulaPuntos());
        dto.setCreatedAt(p.getCreatedAt());
        // CA-2: se calcula en tiempo real
        dto.setVigente(p.isVigente());
        if (p.getSucursal() != null) {
            dto.setSucursalId(p.getSucursal().getId());
            dto.setSucursalNombre(p.getSucursal().getNombre());
        }
        return dto;
    }

    // ── Crear promoción ──
    @Transactional
public PromocionDTO crearPromocion(PromocionDTO dto) {
    // CA-1: validar coherencia de fechas
    if (dto.getFechaFin().isBefore(dto.getFechaInicio())) {
        throw new IllegalArgumentException(
                "La fecha de fin no puede ser anterior a la fecha de inicio.");
    }

    // Sucursal del usuario autenticado (JWT) — igual que en VentaService
    Authentication auth = SecurityContextHolder.getContext().getAuthentication();
    String email = auth.getName();
    Usuario usuarioAuth = usuarioRepository.findByEmail(email)
            .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));
    if (usuarioAuth.getSucursal() == null) {
        throw new IllegalArgumentException(
                "Tu cuenta no tiene una sucursal asignada. Contacta al administrador.");
    }

    Promocion promocion = new Promocion();
    promocion.setNombre(dto.getNombre());
    promocion.setDescripcion(dto.getDescripcion());
    promocion.setDescuentoPorcentaje(dto.getDescuentoPorcentaje());
    promocion.setFechaInicio(dto.getFechaInicio());
    promocion.setFechaFin(dto.getFechaFin());
    promocion.setActiva(true);
    promocion.setMinGarrafones(dto.getMinGarrafones() != null ? dto.getMinGarrafones() : 0);
    promocion.setAcumulaPuntos(dto.getAcumulaPuntos() != null ? dto.getAcumulaPuntos() : true);
    promocion.setCreatedAt(LocalDateTime.now());
    promocion.setSucursal(usuarioAuth.getSucursal());

    return toDTO(promocionRepository.save(promocion));
}

    // ── Listar todas ──
    public List<PromocionDTO> obtenerTodas() {
        return promocionRepository.findAll().stream()
                .map(this::toDTO).collect(Collectors.toList());
    }

    // ── Listar solo las vigentes (CA-2) ──
    public List<PromocionDTO> obtenerVigentes() {
        return promocionRepository.findVigentes(LocalDateTime.now()).stream()
                .map(this::toDTO).collect(Collectors.toList());
    }

    // ── Listar vigentes por sucursal (CA-2) ──
    public List<PromocionDTO> obtenerVigentesPorSucursal(Long sucursalId) {
        return promocionRepository
                .findVigentesBySucursal(sucursalId, LocalDateTime.now()).stream()
                .map(this::toDTO).collect(Collectors.toList());
    }

    // ── Obtener por ID ──
    public PromocionDTO obtenerPorId(Long id) {
        Promocion p = promocionRepository.findById(id)
                .orElseThrow(() -> new RuntimeException(
                        "Promoción no encontrada con ID: " + id));
        return toDTO(p);
    }

    // ── Actualizar ──
    @Transactional
    public PromocionDTO actualizarPromocion(Long id, PromocionDTO dto) {
        Promocion promocion = promocionRepository.findById(id)
                .orElseThrow(() -> new RuntimeException(
                        "Promoción no encontrada con ID: " + id));

        // CA-3: no se puede reactivar una promoción expirada cambiando solo 'activa'
        // Si el fin ya pasó, debe actualizar también la fecha
        if (Boolean.TRUE.equals(dto.getActiva())
                && dto.getFechaFin() == null
                && LocalDateTime.now().isAfter(promocion.getFechaFin())) {
            throw new IllegalArgumentException(
                    "La promoción está expirada. Actualiza la fecha de fin para reactivarla.");
        }

        if (dto.getFechaFin() != null && dto.getFechaInicio() != null
                && dto.getFechaFin().isBefore(dto.getFechaInicio())) {
            throw new IllegalArgumentException(
                    "La fecha de fin no puede ser anterior a la fecha de inicio.");
        }

        promocion.setNombre(dto.getNombre());
        promocion.setDescripcion(dto.getDescripcion());
        promocion.setDescuentoPorcentaje(dto.getDescuentoPorcentaje());
        promocion.setFechaInicio(dto.getFechaInicio());
        promocion.setFechaFin(dto.getFechaFin());
        if (dto.getActiva() != null) {
            promocion.setActiva(dto.getActiva());
        }
        if (dto.getMinGarrafones() != null) {
            promocion.setMinGarrafones(dto.getMinGarrafones());
        }
        if (dto.getAcumulaPuntos() != null) {
            promocion.setAcumulaPuntos(dto.getAcumulaPuntos());
        }

        return toDTO(promocionRepository.save(promocion));
    }

    // ── Desactivar manualmente (CA-3) ──
    @Transactional
    public PromocionDTO desactivarPromocion(Long id) {
        Promocion promocion = promocionRepository.findById(id)
                .orElseThrow(() -> new RuntimeException(
                        "Promoción no encontrada con ID: " + id));
        promocion.setActiva(false);
        return toDTO(promocionRepository.save(promocion));
    }

    // ── Eliminar ──
    @Transactional
    public void eliminarPromocion(Long id) {
        if (!promocionRepository.existsById(id)) {
            throw new RuntimeException("Promoción no encontrada con ID: " + id);
        }
        promocionRepository.deleteById(id);
    }

    // ── CA-3: validar si una promoción puede aplicarse ahora ──
    public PromocionDTO validarAplicacion(Long id) {
        Promocion p = promocionRepository.findById(id)
                .orElseThrow(() -> new RuntimeException(
                        "Promoción no encontrada con ID: " + id));

        if (!Boolean.TRUE.equals(p.getActiva())) {
            throw new IllegalStateException(
                    "La promoción está desactivada y no puede aplicarse.");
        }

        LocalDateTime ahora = LocalDateTime.now();
        if (ahora.isBefore(p.getFechaInicio())) {
            throw new IllegalStateException(
                    "La promoción aún no ha iniciado. Inicia el: "
                    + p.getFechaInicio());
        }
        if (ahora.isAfter(p.getFechaFin())) {
            throw new IllegalStateException(
                    "La promoción ha expirado. Venció el: " + p.getFechaFin());
        }

        return toDTO(p);
    }
}
