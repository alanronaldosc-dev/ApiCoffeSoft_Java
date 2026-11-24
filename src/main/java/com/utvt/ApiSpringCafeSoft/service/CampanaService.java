package com.utvt.ApiSpringCafeSoft.service;

import com.utvt.ApiSpringCafeSoft.dto.CampanaDTO;
import com.utvt.ApiSpringCafeSoft.model.Campana;
import com.utvt.ApiSpringCafeSoft.model.Promocion;
import com.utvt.ApiSpringCafeSoft.model.Sucursal;
import com.utvt.ApiSpringCafeSoft.model.Usuario;
import com.utvt.ApiSpringCafeSoft.repository.CampanaRepository;
import com.utvt.ApiSpringCafeSoft.repository.PromocionRepository;
import com.utvt.ApiSpringCafeSoft.repository.UsuarioRepository;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * HU-037: Servicio de campañas por segmento.
 *
 * Responsabilidades:
 * - Crear campañas asociadas a un segmento y una promoción (CA-1)
 * - Activar automáticamente campañas al llegar su fechaInicio (CA-2)
 * - Desactivar automáticamente campañas al pasar su fechaFin (CA-3)
 */
@Service
public class CampanaService {

    private static final Set<String> SEGMENTOS_VALIDOS =
            Set.of("ACTIVO", "POTENCIAL", "DORMIDO", "POCO_FRECUENTE");

    private final CampanaRepository campanaRepository;
    private final PromocionRepository promocionRepository;
    private final UsuarioRepository usuarioRepository;

    public CampanaService(CampanaRepository campanaRepository,
                          PromocionRepository promocionRepository,
                          UsuarioRepository usuarioRepository) {
        this.campanaRepository = campanaRepository;
        this.promocionRepository = promocionRepository;
        this.usuarioRepository = usuarioRepository;
    }

    // ── Mapeo entidad → DTO ──
    private CampanaDTO toDTO(Campana c) {
        CampanaDTO dto = new CampanaDTO();
        dto.setId(c.getId());
        dto.setNombre(c.getNombre());
        dto.setDescripcion(c.getDescripcion());
        dto.setSegmento(c.getSegmento());
        dto.setFechaInicio(c.getFechaInicio());
        dto.setFechaFin(c.getFechaFin());
        dto.setActiva(c.getActiva());
        dto.setVigente(c.isVigente());
        dto.setCreatedAt(c.getCreatedAt());
        if (c.getPromocion() != null) {
            dto.setPromocionId(c.getPromocion().getId());
            dto.setPromocionNombre(c.getPromocion().getNombre());
            dto.setPromocionDescuento(c.getPromocion().getDescuentoPorcentaje());
        }
        if (c.getSucursal() != null) {
            dto.setSucursalId(c.getSucursal().getId());
            dto.setSucursalNombre(c.getSucursal().getNombre());
        }
        return dto;
    }

    // ── CA-1: Crear campaña con segmento, promoción y periodo ──
    @Transactional
    public CampanaDTO crearCampana(CampanaDTO dto) {
        // Validar segmento
        if (!SEGMENTOS_VALIDOS.contains(dto.getSegmento())) {
            throw new IllegalArgumentException(
                    "Segmento inválido. Valores permitidos: " + SEGMENTOS_VALIDOS);
        }

        // Validar coherencia de fechas
        if (dto.getFechaFin().isBefore(dto.getFechaInicio())) {
            throw new IllegalArgumentException(
                    "La fecha de fin no puede ser anterior a la fecha de inicio.");
        }

        // Obtener promoción
        Promocion promocion = promocionRepository.findById(dto.getPromocionId())
                .orElseThrow(() -> new RuntimeException(
                        "Promoción no encontrada con ID: " + dto.getPromocionId()));

        // Sucursal del usuario autenticado
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        Usuario usuario = usuarioRepository.findByEmail(auth.getName())
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));
        if (usuario.getSucursal() == null) {
            throw new IllegalArgumentException(
                    "Tu cuenta no tiene una sucursal asignada.");
        }

        Campana campana = new Campana();
        campana.setNombre(dto.getNombre());
        campana.setDescripcion(dto.getDescripcion());
        campana.setSegmento(dto.getSegmento());
        campana.setPromocion(promocion);
        campana.setFechaInicio(dto.getFechaInicio());
        campana.setFechaFin(dto.getFechaFin());
        campana.setSucursal(usuario.getSucursal());

        // CA-2: Si la fecha de inicio ya llegó, activar inmediatamente
        LocalDateTime ahora = LocalDateTime.now();
        boolean activarAhora = !ahora.isBefore(dto.getFechaInicio())
                && !ahora.isAfter(dto.getFechaFin());
        campana.setActiva(activarAhora);

        return toDTO(campanaRepository.save(campana));
    }

    // ── Obtener todas ──
    public List<CampanaDTO> obtenerTodas() {
        return campanaRepository.findAll().stream()
                .map(this::toDTO).collect(Collectors.toList());
    }

    // ── CA-2: Obtener campañas vigentes en este momento ──
    public List<CampanaDTO> obtenerVigentes() {
        return campanaRepository.findVigentes(LocalDateTime.now()).stream()
                .map(this::toDTO).collect(Collectors.toList());
    }

    // ── CA-2: Obtener campañas vigentes por segmento ──
    public List<CampanaDTO> obtenerVigentesPorSegmento(String segmento) {
        if (!SEGMENTOS_VALIDOS.contains(segmento.toUpperCase())) {
            throw new IllegalArgumentException("Segmento inválido: " + segmento);
        }
        return campanaRepository
                .findVigentesPorSegmento(segmento.toUpperCase(), LocalDateTime.now())
                .stream().map(this::toDTO).collect(Collectors.toList());
    }

    // ── Obtener por segmento (todas, no solo vigentes) ──
    public List<CampanaDTO> obtenerPorSegmento(String segmento) {
        return campanaRepository.findBySegmento(segmento.toUpperCase()).stream()
                .map(this::toDTO).collect(Collectors.toList());
    }

    // ── Obtener por ID ──
    public CampanaDTO obtenerPorId(Long id) {
        Campana c = campanaRepository.findById(id)
                .orElseThrow(() -> new RuntimeException(
                        "Campaña no encontrada con ID: " + id));
        return toDTO(c);
    }

    // ── Actualizar ──
    @Transactional
    public CampanaDTO actualizarCampana(Long id, CampanaDTO dto) {
        Campana campana = campanaRepository.findById(id)
                .orElseThrow(() -> new RuntimeException(
                        "Campaña no encontrada con ID: " + id));

        if (dto.getFechaFin() != null && dto.getFechaInicio() != null
                && dto.getFechaFin().isBefore(dto.getFechaInicio())) {
            throw new IllegalArgumentException(
                    "La fecha de fin no puede ser anterior a la fecha de inicio.");
        }

        if (dto.getSegmento() != null
                && !SEGMENTOS_VALIDOS.contains(dto.getSegmento())) {
            throw new IllegalArgumentException("Segmento inválido: " + dto.getSegmento());
        }

        campana.setNombre(dto.getNombre());
        campana.setDescripcion(dto.getDescripcion());
        if (dto.getSegmento() != null) campana.setSegmento(dto.getSegmento());
        if (dto.getFechaInicio() != null) campana.setFechaInicio(dto.getFechaInicio());
        if (dto.getFechaFin() != null) campana.setFechaFin(dto.getFechaFin());

        if (dto.getPromocionId() != null) {
            Promocion promocion = promocionRepository.findById(dto.getPromocionId())
                    .orElseThrow(() -> new RuntimeException(
                            "Promoción no encontrada con ID: " + dto.getPromocionId()));
            campana.setPromocion(promocion);
        }

        return toDTO(campanaRepository.save(campana));
    }

    // ── CA-3: Desactivar manualmente ──
    @Transactional
    public CampanaDTO desactivarCampana(Long id) {
        Campana campana = campanaRepository.findById(id)
                .orElseThrow(() -> new RuntimeException(
                        "Campaña no encontrada con ID: " + id));
        campana.setActiva(false);
        return toDTO(campanaRepository.save(campana));
    }

    // ── Eliminar ──
    @Transactional
    public void eliminarCampana(Long id) {
        if (!campanaRepository.existsById(id)) {
            throw new RuntimeException("Campaña no encontrada con ID: " + id);
        }
        campanaRepository.deleteById(id);
    }

    // ── CA-1 + CA-2: Activar campañas cuyo periodo comenzó ──
    @Transactional
    public int activarPendientes() {
        return campanaRepository.activarPendientes(LocalDateTime.now());
    }

    // ── CA-3: Desactivar campañas cuyo periodo expiró automáticamente ──
    @Transactional
    public int desactivarExpiradas() {
        return campanaRepository.desactivarExpiradas(LocalDateTime.now());
    }

    /**
     * HU-037 CA-3: Scheduler que se ejecuta cada minuto.
     * Activa campañas pendientes y desactiva las expiradas sin intervención manual.
     */
    @Scheduled(fixedRate = 60_000)
    @Transactional
    public void sincronizarEstados() {
        int activadas  = campanaRepository.activarPendientes(LocalDateTime.now());
        int desactivadas = campanaRepository.desactivarExpiradas(LocalDateTime.now());
        if (activadas > 0 || desactivadas > 0) {
            System.out.printf(
                "[CampanaService] Scheduler: %d activadas, %d desactivadas%n",
                activadas, desactivadas);
        }
    }
}
