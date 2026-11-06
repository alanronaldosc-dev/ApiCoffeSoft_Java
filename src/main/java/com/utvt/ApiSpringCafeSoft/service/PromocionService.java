
package com.utvt.ApiSpringCafeSoft.service;

import com.utvt.ApiSpringCafeSoft.dto.PromocionDTO;
import com.utvt.ApiSpringCafeSoft.model.Promocion;
import com.utvt.ApiSpringCafeSoft.model.Usuario;
import com.utvt.ApiSpringCafeSoft.repository.PromocionRepository;
import com.utvt.ApiSpringCafeSoft.repository.UsuarioRepository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

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
    // HU-039: ESTADO AUTOMATICO
    // =====================================================

    private String calcularEstado(Promocion p) {

        if (!Boolean.TRUE.equals(p.getActiva())) {
            return "DESACTIVADA";
        }

        LocalDateTime ahora = LocalDateTime.now();

        if (ahora.isBefore(p.getFechaInicio())) {
            return "PROGRAMADA";
        }

        if (ahora.isAfter(p.getFechaFin())) {
            return "VENCIDA";
        }

        return "VIGENTE";
    }

    // =====================================================
    // HU-040: OBTENER ADMINISTRADOR AUTENTICADO
    // =====================================================

    private Usuario obtenerAdministrador() {

        Authentication auth = SecurityContextHolder
            .getContext()
            .getAuthentication();

        if (auth == null ||
            !auth.isAuthenticated() ||
            "anonymousUser".equals(auth.getPrincipal())) {

            throw new ResponseStatusException(
                HttpStatus.UNAUTHORIZED,
                "Debes iniciar sesion."
            );
        }

        boolean tieneRolAdmin = auth.getAuthorities()
            .stream()
            .anyMatch(a ->
                "ROLE_ADMIN".equals(a.getAuthority())
            );

        if (!tieneRolAdmin) {
            throw new ResponseStatusException(
                HttpStatus.FORBIDDEN,
                "Solo un administrador puede realizar esta accion."
            );
        }

        Usuario usuario = usuarioRepository
            .findByEmail(auth.getName())
            .orElseThrow(() ->
                new ResponseStatusException(
                    HttpStatus.UNAUTHORIZED,
                    "Usuario autenticado no encontrado."
                )
            );

        if (!Integer.valueOf(0).equals(usuario.getUserTipo())
            || !Boolean.TRUE.equals(usuario.getActivo())) {

            throw new ResponseStatusException(
                HttpStatus.FORBIDDEN,
                "El usuario no es un administrador activo."
            );
        }

        return usuario;
    }

    // =====================================================
    // BUSCAR PROMOCION
    // =====================================================

    private Promocion buscarPromocion(Long id) {

        return promocionRepository.findById(id)
            .orElseThrow(() ->
                new ResponseStatusException(
                    HttpStatus.NOT_FOUND,
                    "Promocion no encontrada con ID: " + id
                )
            );
    }

    // =====================================================
    // VALIDAR FECHAS
    // =====================================================

    private void validarFechas(
        LocalDateTime inicio,
        LocalDateTime fin
    ) {

        if (inicio == null || fin == null) {
            throw new ResponseStatusException(
                HttpStatus.BAD_REQUEST,
                "Las fechas de inicio y fin son obligatorias."
            );
        }

        if (!fin.isAfter(inicio)) {
            throw new ResponseStatusException(
                HttpStatus.BAD_REQUEST,
                "La fecha de fin debe ser posterior al inicio."
            );
        }
    }

    // =====================================================
    // CONVERTIR ENTIDAD A DTO
    // =====================================================

    private PromocionDTO toDTO(Promocion p) {

        PromocionDTO dto = new PromocionDTO();

        dto.setId(p.getId());
        dto.setNombre(p.getNombre());
        dto.setDescripcion(p.getDescripcion());
        dto.setDescuentoPorcentaje(
            p.getDescuentoPorcentaje()
        );
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
            dto.setSucursalNombre(
                p.getSucursal().getNombre()
            );
        }

        // HU-040
        dto.setFechaDesactivacion(
            p.getFechaDesactivacion()
        );

        if (p.getDesactivadaPor() != null) {
            Usuario admin = p.getDesactivadaPor();

            dto.setDesactivadaPorId(admin.getId());
            dto.setDesactivadaPorNombre(admin.getNombre());
            dto.setDesactivadaPorEmail(admin.getEmail());
        }

        return dto;
    }

    // =====================================================
    // CREAR PROMOCION
    // =====================================================

    @Transactional
    public PromocionDTO crearPromocion(PromocionDTO dto) {

        Usuario administrador = obtenerAdministrador();

        validarFechas(
            dto.getFechaInicio(),
            dto.getFechaFin()
        );

        if (administrador.getSucursal() == null) {
            throw new ResponseStatusException(
                HttpStatus.BAD_REQUEST,
                "El administrador no tiene sucursal asignada."
            );
        }

        Promocion p = new Promocion();

        p.setNombre(dto.getNombre());
        p.setDescripcion(dto.getDescripcion());
        p.setDescuentoPorcentaje(
            dto.getDescuentoPorcentaje()
        );
        p.setFechaInicio(dto.getFechaInicio());
        p.setFechaFin(dto.getFechaFin());
        p.setActiva(true);

        p.setMinGarrafones(
            dto.getMinGarrafones() != null
                ? dto.getMinGarrafones()
                : 0
        );

        p.setAcumulaPuntos(
            dto.getAcumulaPuntos() != null
                ? dto.getAcumulaPuntos()
                : true
        );

        p.setCreatedAt(LocalDateTime.now());
        p.setSucursal(administrador.getSucursal());

        // Una promocion nueva no tiene auditoria.
        p.setFechaDesactivacion(null);
        p.setDesactivadaPor(null);

        return toDTO(promocionRepository.save(p));
    }

    // =====================================================
    // CONSULTAR TODAS
    // =====================================================

    @Transactional(readOnly = true)
    public List<PromocionDTO> obtenerTodas() {

        return promocionRepository.findAll()
            .stream()
            .map(this::toDTO)
            .collect(Collectors.toList());
    }

    // =====================================================
    // HU-039: FILTRAR POR ESTADO
    // =====================================================

    @Transactional(readOnly = true)
    public List<PromocionDTO> obtenerPorEstado(
        String estado
    ) {

        String filtro = estado == null
            ? "TODAS"
            : estado.trim().toUpperCase(Locale.ROOT);

        if (filtro.isBlank() || "TODAS".equals(filtro)) {
            return obtenerTodas();
        }

        List<String> permitidos = List.of(
            "VIGENTE",
            "VENCIDA",
            "PROGRAMADA",
            "DESACTIVADA"
        );

        if (!permitidos.contains(filtro)) {
            throw new ResponseStatusException(
                HttpStatus.BAD_REQUEST,
                "Estado invalido. Usa VIGENTE, VENCIDA, " +
                "PROGRAMADA o DESACTIVADA."
            );
        }

        return promocionRepository.findAll()
            .stream()
            .filter(p -> calcularEstado(p).equals(filtro))
            .map(this::toDTO)
            .collect(Collectors.toList());
    }

    // =====================================================
    // CONSULTAR VIGENTES
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
    // CONSULTAR POR ID
    // =====================================================

    @Transactional(readOnly = true)
    public PromocionDTO obtenerPorId(Long id) {

        return toDTO(buscarPromocion(id));
    }

    // =====================================================
    // HU-040: ACTUALIZAR SIN PERMITIR REACTIVACION
    // =====================================================

    @Transactional
    public PromocionDTO actualizarPromocion(
        Long id,
        PromocionDTO dto
    ) {

        obtenerAdministrador();

        Promocion p = promocionRepository.findByIdForUpdate(id)
    .orElseThrow(() ->
        new ResponseStatusException(
            HttpStatus.NOT_FOUND,
            "Promocion no encontrada con ID: " + id
        )
    );

        // No se permite modificar una promocion
        // que ya fue desactivada manualmente.
        if (!Boolean.TRUE.equals(p.getActiva())) {
            throw new ResponseStatusException(
                HttpStatus.CONFLICT,
                "La promocion esta desactivada y " +
                "no puede editarse ni reactivarse."
            );
        }

        // La desactivacion solo puede hacerse
        // mediante el endpoint dedicado.
        if (Boolean.FALSE.equals(dto.getActiva())) {
            throw new ResponseStatusException(
                HttpStatus.BAD_REQUEST,
                "Utiliza el endpoint de desactivacion."
            );
        }

        LocalDateTime inicio = dto.getFechaInicio() != null
            ? dto.getFechaInicio()
            : p.getFechaInicio();

        LocalDateTime fin = dto.getFechaFin() != null
            ? dto.getFechaFin()
            : p.getFechaFin();

        validarFechas(inicio, fin);

        p.setNombre(dto.getNombre());
        p.setDescripcion(dto.getDescripcion());
        p.setDescuentoPorcentaje(
            dto.getDescuentoPorcentaje()
        );
        p.setFechaInicio(inicio);
        p.setFechaFin(fin);

        if (dto.getMinGarrafones() != null) {
            p.setMinGarrafones(dto.getMinGarrafones());
        }

        if (dto.getAcumulaPuntos() != null) {
            p.setAcumulaPuntos(dto.getAcumulaPuntos());
        }

        // No modificar activa ni los campos de auditoria.
        return toDTO(promocionRepository.save(p));
    }

    // =====================================================
    // HU-040: DESACTIVACION ANTICIPADA CON AUDITORIA
    // =====================================================

    @Transactional
    public PromocionDTO desactivarPromocion(Long id) {

        Usuario administrador = obtenerAdministrador();

        Promocion p = promocionRepository.findByIdForUpdate(id)
    .orElseThrow(() ->
        new ResponseStatusException(
            HttpStatus.NOT_FOUND,
            "Promocion no encontrada con ID: " + id
        )
    );

        if (!Boolean.TRUE.equals(p.getActiva())) {
            throw new ResponseStatusException(
                HttpStatus.CONFLICT,
                "La promocion ya esta desactivada."
            );
        }

        LocalDateTime ahora = LocalDateTime.now();

        // HU-040: desactivar antes del vencimiento.
        if (!ahora.isBefore(p.getFechaFin())) {
            throw new ResponseStatusException(
                HttpStatus.CONFLICT,
                "La promocion ya vencio. " +
                "No es posible una desactivacion anticipada."
            );
        }

        p.setActiva(false);
        p.setFechaDesactivacion(ahora);
        p.setDesactivadaPor(administrador);

        return toDTO(promocionRepository.save(p));
    }

    // =====================================================
    // ELIMINAR PROMOCION
    // =====================================================

    @Transactional
    public void eliminarPromocion(Long id) {

        obtenerAdministrador();

        Promocion p = promocionRepository.findByIdForUpdate(id)
    .orElseThrow(() ->
        new ResponseStatusException(
            HttpStatus.NOT_FOUND,
            "Promocion no encontrada con ID: " + id
        )
    );

        // HU-040: conservar la auditoria
        // de promociones desactivadas.
        if (p.getFechaDesactivacion() != null) {
            throw new ResponseStatusException(
                HttpStatus.CONFLICT,
                "No se puede eliminar una promocion " +
                "desactivada porque contiene auditoria."
            );
        }

        promocionRepository.delete(p);
    }

    // =====================================================
    // VALIDAR APLICACION DE PROMOCION
    // =====================================================

    @Transactional(readOnly = true)
    public PromocionDTO validarAplicacion(Long id) {

        Promocion p = buscarPromocion(id);

        String estado = calcularEstado(p);

        switch (estado) {

            case "DESACTIVADA":
                throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "La promocion esta desactivada " +
                    "y no puede aplicarse."
                );

            case "PROGRAMADA":
                throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "La promocion aun no ha iniciado."
                );

            case "VENCIDA":
                throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "La promocion ha vencido."
                );

            default:
                return toDTO(p);
        }
    }
}
