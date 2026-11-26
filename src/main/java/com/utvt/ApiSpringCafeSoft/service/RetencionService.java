package com.utvt.ApiSpringCafeSoft.service;

import com.utvt.ApiSpringCafeSoft.dto.AccionRetencionDTO;
import com.utvt.ApiSpringCafeSoft.dto.NuevaAccionRetencionDTO;
import com.utvt.ApiSpringCafeSoft.dto.ResumenAccionesDTO;
import com.utvt.ApiSpringCafeSoft.model.Auditoria;
import com.utvt.ApiSpringCafeSoft.repository.AuditoriaRepository;
import com.utvt.ApiSpringCafeSoft.repository.ClienteRepository;

import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * HU-081: las acciones de retención se guardan en el log de auditoría ya
 * existente. No se crea ninguna tabla ni entidad nueva.
 */
@Service
public class RetencionService {

    private static final String ENTIDAD = "CLIENTE";
    private static final String ATENDIDA = "ATENDIDA";
    private static final String MARCADOR = "accionId=";
    private static final List<String> TIPOS = List.of("LLAMADA", "VISITA", "PROMOCION");

    private final AuditoriaRepository auditoriaRepository;
    private final ClienteRepository clienteRepository;

    public RetencionService(AuditoriaRepository auditoriaRepository,
            ClienteRepository clienteRepository) {
        this.auditoriaRepository = auditoriaRepository;
        this.clienteRepository = clienteRepository;
    }

    private void verificarCliente(Long clienteId) {
        if (!clienteRepository.existsById(clienteId)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Cliente no encontrado");
        }
    }

    private String limitar(String texto) {
        if (texto == null) return null;
        return texto.length() > 500 ? texto.substring(0, 500) : texto;
    }

    private Long idMarcado(String detalle) {
        if (detalle == null || !detalle.startsWith(MARCADOR)) return null;
        try {
            return Long.valueOf(detalle.substring(MARCADOR.length()).trim());
        } catch (NumberFormatException e) {
            return null;
        }
    }

    private String usuarioActual() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.getPrincipal() != null
                && !"anonymousUser".equals(auth.getPrincipal())) {
            return auth.getName();
        }
        return "ADMIN";
    }

    @Transactional
    public AccionRetencionDTO registrar(Long clienteId, NuevaAccionRetencionDTO solicitud) {
        verificarCliente(clienteId);
        String tipo = solicitud.tipo() == null ? "" : solicitud.tipo().trim().toUpperCase(Locale.ROOT);
        if (!TIPOS.contains(tipo)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Tipo de acción no válido. Use LLAMADA, VISITA o PROMOCION");
        }

        String responsable = solicitud.responsable().trim();
        Auditoria registro = new Auditoria();
        registro.setUsuario(responsable);
        registro.setAccion(tipo);
        registro.setEntidad(ENTIDAD);
        registro.setEntidadId(clienteId);
        registro.setDetalle(limitar(solicitud.detalle()));
        registro.setFecha(solicitud.fecha());
        Auditoria guardado = auditoriaRepository.save(registro);

        return new AccionRetencionDTO(guardado.getId(), clienteId, tipo, responsable,
                guardado.getFecha(), guardado.getDetalle(), false, null);
    }

    @Transactional(readOnly = true)
    public List<AccionRetencionDTO> listar(Long clienteId) {
        verificarCliente(clienteId);
        List<Auditoria> acciones = auditoriaRepository
                .findByEntidadAndEntidadIdAndAccionInOrderByFechaDesc(ENTIDAD, clienteId, TIPOS);
        Map<Long, LocalDateTime> atenciones = fechasAtencion(
                auditoriaRepository.findByEntidadAndEntidadIdAndAccionOrderByFechaDesc(
                        ENTIDAD, clienteId, ATENDIDA));

        return acciones.stream().map(a -> new AccionRetencionDTO(
                a.getId(), clienteId, a.getAccion(), a.getUsuario(), a.getFecha(),
                a.getDetalle(), atenciones.containsKey(a.getId()), atenciones.get(a.getId())))
                .collect(Collectors.toList());
    }

    @Transactional
    public AccionRetencionDTO atender(Long clienteId, Long accionId) {
        verificarCliente(clienteId);
        Auditoria accion = auditoriaRepository
                .findByIdAndEntidadAndEntidadIdAndAccionIn(accionId, ENTIDAD, clienteId, TIPOS)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                        "Acción de retención no encontrada"));

        if (fechasAtencion(auditoriaRepository
                .findByEntidadAndEntidadIdAndAccionOrderByFechaDesc(ENTIDAD, clienteId, ATENDIDA))
                .containsKey(accionId)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "La acción ya está atendida");
        }

        Auditoria registro = new Auditoria();
        registro.setUsuario(usuarioActual());
        registro.setAccion(ATENDIDA);
        registro.setEntidad(ENTIDAD);
        registro.setEntidadId(clienteId);
        registro.setDetalle(MARCADOR + accionId);
        registro.setFecha(LocalDateTime.now());
        Auditoria guardado = auditoriaRepository.save(registro);

        return new AccionRetencionDTO(accion.getId(), clienteId, accion.getAccion(),
                accion.getUsuario(), accion.getFecha(), accion.getDetalle(), true, guardado.getFecha());
    }

    @Transactional(readOnly = true)
    public List<ResumenAccionesDTO> resumen() {
        List<Auditoria> atenciones = auditoriaRepository.findByEntidadAndAccion(ENTIDAD, ATENDIDA);
        Map<Long, LocalDateTime> atendidasPorAccion = fechasAtencion(atenciones);
        Map<Long, LocalDateTime> ultimaPorCliente = new HashMap<>();
        for (Auditoria a : atenciones) {
            ultimaPorCliente.merge(a.getEntidadId(), a.getFecha(),
                    (x, y) -> x.isAfter(y) ? x : y);
        }

        Map<Long, List<Auditoria>> accionesPorCliente = auditoriaRepository
                .findByEntidadAndAccionIn(ENTIDAD, TIPOS).stream()
                .collect(Collectors.groupingBy(Auditoria::getEntidadId));

        List<ResumenAccionesDTO> resultado = new ArrayList<>();
        for (Map.Entry<Long, List<Auditoria>> entry : accionesPorCliente.entrySet()) {
            long total = entry.getValue().size();
            long atendidas = entry.getValue().stream()
                    .filter(a -> atendidasPorAccion.containsKey(a.getId())).count();
            resultado.add(new ResumenAccionesDTO(entry.getKey(), total - atendidas, atendidas,
                    ultimaPorCliente.get(entry.getKey())));
        }
        return resultado;
    }

    private Map<Long, LocalDateTime> fechasAtencion(List<Auditoria> atenciones) {
        Map<Long, LocalDateTime> mapa = new HashMap<>();
        Set<Long> vistos = new HashSet<>();
        for (Auditoria a : atenciones) {
            Long id = idMarcado(a.getDetalle());
            if (id != null && vistos.add(id)) mapa.put(id, a.getFecha());
        }
        return mapa;
    }
}
