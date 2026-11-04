
package com.utvt.ApiSpringCafeSoft.service;

import com.utvt.ApiSpringCafeSoft.dto.PromocionDTO;
import com.utvt.ApiSpringCafeSoft.model.Promocion;
import com.utvt.ApiSpringCafeSoft.model.Usuario;
import com.utvt.ApiSpringCafeSoft.repository.PromocionRepository;
import com.utvt.ApiSpringCafeSoft.repository.UsuarioRepository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

@Service
public class PromocionService {

    @Autowired
    private PromocionRepository promocionRepository;

    @Autowired
    private UsuarioRepository usuarioRepository;

    // =====================================================
    // HU-039: CALCULAR ESTADO AUTOMÁTICAMENTE
    // =====================================================

    private String calcularEstado(Promocion promocion) {

        if (!Boolean.TRUE.equals(promocion.getActiva())) {
            return "DESACTIVADA";
        }

        LocalDateTime ahora = LocalDateTime.now();

        if (ahora.isBefore(promocion.getFechaInicio())) {
            return "PROGRAMADA";
        }

        if (ahora.isAfter(promocion.getFechaFin())) {
            return "VENCIDA";
        }

        return "VIGENTE";
    }

    // =====================================================
    // CONVERTIR ENTIDAD A DTO
    // =====================================================

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

        // HU-039
        String estado = calcularEstado(p);
        dto.setEstado(estado);
        dto.setVigente("VIGENTE".equals(estado));

        if (p.getSucursal() != null) {
            dto.setSucursalId(p.getSucursal().getId());
            dto.setSucursalNombre(p.getSucursal().getNombre());
        }

        return dto;
    }

    // =====================================================
    // CREAR PROMOCIÓN
    // =====================================================

    @Transactional
    public PromocionDTO crearPromocion(PromocionDTO dto) {

        if (dto.getFechaInicio() == null ||
            dto.getFechaFin() == null) {

            throw new IllegalArgumentException(
                "Las fechas de inicio y fin son obligatorias."
            );
        }

        if (dto.getFechaFin().isBefore(dto.getFechaInicio())) {
            throw new IllegalArgumentException(
                "La fecha de fin no puede ser anterior a la fecha de inicio."
            );
        }

        Authentication auth =
            SecurityContextHolder.getContext().getAuthentication();

        if (auth == null || !auth.isAuthenticated()) {
            throw new IllegalStateException(
                "No existe una sesión autenticada."
            );
        }

        String email = auth.getName();

        Usuario usuarioAuth = usuarioRepository.findByEmail(email)
            .orElseThrow(() ->
                new IllegalStateException("Usuario no encontrado.")
            );

        if (usuarioAuth.getSucursal() == null) {
            throw new IllegalArgumentException(
                "Tu cuenta no tiene una sucursal asignada."
            );
        }

        Promocion promocion = new Promocion();

        promocion.setNombre(dto.getNombre());
        promocion.setDescripcion(dto.getDescripcion());
        promocion.setDescuentoPorcentaje(
            dto.getDescuentoPorcentaje()
        );
        promocion.setFechaInicio(dto.getFechaInicio());
        promocion.setFechaFin(dto.getFechaFin());
        promocion.setActiva(true);

        promocion.setMinGarrafones(
            dto.getMinGarrafones() != null
                ? dto.getMinGarrafones()
                : 0
        );

        promocion.setAcumulaPuntos(
            dto.getAcumulaPuntos() != null
                ? dto.getAcumulaPuntos()
                : true
        );

        promocion.setCreatedAt(LocalDateTime.now());
        promocion.setSucursal(usuarioAuth.getSucursal());

        return toDTO(promocionRepository.save(promocion));
    }

    // =====================================================
    // CONSULTAR TODAS LAS PROMOCIONES
    // =====================================================

    @Transactional(readOnly = true)
    public List<PromocionDTO> obtenerTodas() {

        return promocionRepository.findAll()
            .stream()
            .map(this::toDTO)
            .collect(Collectors.toList());
    }

    // =====================================================
    // HU-039: FILTRAR PROMOCIONES POR ESTADO
    // =====================================================

    @Transactional(readOnly = true)
    public List<PromocionDTO> obtenerPorEstado(String estado) {

        if (estado == null ||
            estado.isBlank() ||
            estado.equalsIgnoreCase("TODAS")) {

            return obtenerTodas();
        }

        String estadoNormalizado =
            estado.trim().toUpperCase(Locale.ROOT);

        List<String> estadosPermitidos = List.of(
            "VIGENTE",
            "VENCIDA",
            "PROGRAMADA",
            "DESACTIVADA"
        );

        if (!estadosPermitidos.contains(estadoNormalizado)) {
            throw new IllegalArgumentException(
                "Estado no válido. Utiliza VIGENTE, VENCIDA, " +
                "PROGRAMADA o DESACTIVADA."
            );
        }

        return promocionRepository.findAll()
            .stream()
            .filter(p ->
                calcularEstado(p).equals(estadoNormalizado)
            )
            .map(this::toDTO)
            .collect(Collectors.toList());
    }

    // =====================================================
    // CONSULTAR PROMOCIONES VIGENTES
    // =====================================================

    @Transactional(readOnly = true)
    public List<PromocionDTO> obtenerVigentes() {

        return promocionRepository
            .findVigentes(LocalDateTime.now())
            .stream()
            .map(this::toDTO)
            .collect(Collectors.toList());
    }

    // =====================================================
    // CONSULTAR VIGENTES POR SUCURSAL
    // =====================================================

    @Transactional(readOnly = true)
    public List<PromocionDTO> obtenerVigentesPorSucursal(
        Long sucursalId
    ) {

        return promocionRepository
            .findVigentesBySucursal(
                sucursalId,
                LocalDateTime.now()
            )
            .stream()
            .map(this::toDTO)
            .collect(Collectors.toList());
    }

    // =====================================================
    // CONSULTAR PROMOCIÓN POR ID
    // =====================================================

    @Transactional(readOnly = true)
    public PromocionDTO obtenerPorId(Long id) {

        Promocion promocion = promocionRepository.findById(id)
            .orElseThrow(() ->
                new IllegalArgumentException(
                    "Promoción no encontrada con ID: " + id
                )
            );

        return toDTO(promocion);
    }

    // =====================================================
    // ACTUALIZAR PROMOCIÓN
    // =====================================================

    @Transactional
    public PromocionDTO actualizarPromocion(
        Long id,
        PromocionDTO dto
    ) {

        Promocion promocion = promocionRepository.findById(id)
            .orElseThrow(() ->
                new IllegalArgumentException(
                    "Promoción no encontrada con ID: " + id
                )
            );

        LocalDateTime fechaInicio =
            dto.getFechaInicio() != null
                ? dto.getFechaInicio()
                : promocion.getFechaInicio();

        LocalDateTime fechaFin =
            dto.getFechaFin() != null
                ? dto.getFechaFin()
                : promocion.getFechaFin();

        if (fechaFin.isBefore(fechaInicio)) {
            throw new IllegalArgumentException(
                "La fecha de fin no puede ser anterior a la fecha de inicio."
            );
        }

        if (Boolean.TRUE.equals(dto.getActiva()) &&
            !Boolean.TRUE.equals(promocion.getActiva()) &&
            LocalDateTime.now().isAfter(fechaFin)) {

            throw new IllegalArgumentException(
                "La promoción está vencida. " +
                "Actualiza su fecha de fin para reactivarla."
            );
        }

        promocion.setNombre(dto.getNombre());
        promocion.setDescripcion(dto.getDescripcion());

        promocion.setDescuentoPorcentaje(
            dto.getDescuentoPorcentaje()
        );

        promocion.setFechaInicio(fechaInicio);
        promocion.setFechaFin(fechaFin);

        if (dto.getActiva() != null) {
            promocion.setActiva(dto.getActiva());
        }

        if (dto.getMinGarrafones() != null) {
            promocion.setMinGarrafones(
                dto.getMinGarrafones()
            );
        }

        if (dto.getAcumulaPuntos() != null) {
            promocion.setAcumulaPuntos(
                dto.getAcumulaPuntos()
            );
        }

        return toDTO(promocionRepository.save(promocion));
    }

    // =====================================================
    // DESACTIVAR PROMOCIÓN
    // =====================================================

    @Transactional
    public PromocionDTO desactivarPromocion(Long id) {

        Promocion promocion = promocionRepository.findById(id)
            .orElseThrow(() ->
                new IllegalArgumentException(
                    "Promoción no encontrada con ID: " + id
                )
            );

        promocion.setActiva(false);

        return toDTO(promocionRepository.save(promocion));
    }

    // =====================================================
    // ELIMINAR PROMOCIÓN
    // =====================================================

    @Transactional
    public void eliminarPromocion(Long id) {

        if (!promocionRepository.existsById(id)) {
            throw new IllegalArgumentException(
                "Promoción no encontrada con ID: " + id
            );
        }

        promocionRepository.deleteById(id);
    }

    // =====================================================
    // VALIDAR SI LA PROMOCIÓN SE PUEDE APLICAR
    // =====================================================

    @Transactional(readOnly = true)
    public PromocionDTO validarAplicacion(Long id) {

        Promocion promocion = promocionRepository.findById(id)
            .orElseThrow(() ->
                new IllegalArgumentException(
                    "Promoción no encontrada con ID: " + id
                )
            );

        String estado = calcularEstado(promocion);

        switch (estado) {

            case "DESACTIVADA":
                throw new IllegalStateException(
                    "La promoción está desactivada y no puede aplicarse."
                );

            case "PROGRAMADA":
                throw new IllegalStateException(
                    "La promoción aún no ha iniciado."
                );

            case "VENCIDA":
                throw new IllegalStateException(
                    "La promoción ha vencido."
                );

            default:
                return toDTO(promocion);
        }
    }
}
