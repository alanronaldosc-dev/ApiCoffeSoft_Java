
package com.utvt.ApiSpringCafeSoft.service;

import com.utvt.ApiSpringCafeSoft.dto.CargaDTO;
import com.utvt.ApiSpringCafeSoft.dto.ItemGarrafonDTO;

import com.utvt.ApiSpringCafeSoft.model.Carga;
import com.utvt.ApiSpringCafeSoft.model.Inventario;
import com.utvt.ApiSpringCafeSoft.model.Ruta;
import com.utvt.ApiSpringCafeSoft.model.Usuario;

import com.utvt.ApiSpringCafeSoft.repository.CargaRepository;
import com.utvt.ApiSpringCafeSoft.repository.InventarioRepository;
import com.utvt.ApiSpringCafeSoft.repository.RutaRepository;
import com.utvt.ApiSpringCafeSoft.repository.UsuarioRepository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class CargaService {

    @Autowired
    private CargaRepository cargaRepository;

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private InventarioRepository inventarioRepository;

    @Autowired
    private RutaRepository rutaRepository;

    // ============================================
    // HU-005: CREAR CARGA
    // ============================================

    @Transactional
    public CargaDTO crearCarga(CargaDTO dto) {

        if (dto.getCantidad() == null ||
                dto.getCantidad() <= 0) {

            throw new RuntimeException(
                    "La cantidad debe ser mayor a cero"
            );
        }

        // VALIDAR REPARTIDOR

        Usuario repartidor = null;

        if (dto.getRepartidorId() != null) {

            repartidor = usuarioRepository
                    .findById(dto.getRepartidorId())
                    .orElseThrow(() ->
                            new RuntimeException(
                                    "No se encontró el usuario con ID: "
                                            + dto.getRepartidorId()
                            )
                    );

            if (repartidor.getUserTipo() == null ||
                    repartidor.getUserTipo() != 4) {

                throw new RuntimeException(
                        "El usuario seleccionado no tiene el rol de Repartidor"
                );
            }
        }

        // VALIDAR INVENTARIO

        Inventario inventario = inventarioRepository
                .findById(dto.getInventarioId())
                .orElseThrow(() ->
                        new RuntimeException(
                                "No se encontró el inventario con ID: "
                                        + dto.getInventarioId()
                        )
                );

        // VALIDAR STOCK

        if (inventario.getCantidad() == null ||
                inventario.getCantidad() < dto.getCantidad()) {

            throw new RuntimeException(
                    "Stock insuficiente. Disponible: "
                            + inventario.getCantidad()
                            + ", solicitado: "
                            + dto.getCantidad()
            );
        }

        // DESCONTAR INVENTARIO

        inventario.setCantidad(
                inventario.getCantidad() - dto.getCantidad()
        );

        inventarioRepository.save(inventario);

        // CREAR CARGA

        Carga carga = new Carga();

        carga.setRepartidor(repartidor);
        carga.setInventario(inventario);
        carga.setCantidad(dto.getCantidad());
        carga.setCantidadDisponible(dto.getCantidad());
        carga.setFechaHora(LocalDateTime.now());

        // La ruta se asignará al aceptar
        // o activar la ruta correspondiente.

        carga.setRuta(null);

        carga.setEstado(
                repartidor != null
                        ? "PENDIENTE"
                        : "SIN ASIGNAR"
        );

        Carga guardada = cargaRepository.save(carga);

        return convertirDTO(guardada);
    }

    // ============================================
    // HU-015: CREAR CARGAS MÚLTIPLES
    // ============================================

    @Transactional
    public List<CargaDTO> crearCargasMultiples(
            Long repartidorId,
            List<ItemGarrafonDTO> items) {

        if (repartidorId == null) {

            throw new RuntimeException(
                    "El repartidor es obligatorio"
            );
        }

        Usuario repartidor = usuarioRepository
                .findById(repartidorId)
                .orElseThrow(() ->
                        new RuntimeException(
                                "No se encontró el usuario con ID: "
                                        + repartidorId
                        )
                );

        if (repartidor.getUserTipo() == null ||
                repartidor.getUserTipo() != 4) {

            throw new RuntimeException(
                    "El usuario seleccionado no tiene el rol de Repartidor"
            );
        }

        if (items == null || items.isEmpty()) {

            throw new RuntimeException(
                    "Debe seleccionar al menos un garrafón"
            );
        }

        List<CargaDTO> creadas = new ArrayList<>();

        for (ItemGarrafonDTO item : items) {

            if (item.getInventarioId() == null ||
                    item.getCantidad() == null ||
                    item.getCantidad() <= 0) {

                throw new RuntimeException(
                        "Cada garrafón debe tener un inventario y una cantidad válida"
                );
            }

            CargaDTO dto = new CargaDTO();

            dto.setRepartidorId(repartidorId);
            dto.setInventarioId(item.getInventarioId());
            dto.setCantidad(item.getCantidad());

            creadas.add(crearCarga(dto));
        }

        return creadas;
    }

    // ============================================
    // OBTENER TODAS LAS CARGAS
    // ============================================

    public List<CargaDTO> obtenerTodas() {

        return cargaRepository
                .findAll()
                .stream()
                .map(this::convertirDTO)
                .collect(Collectors.toList());
    }

    // ============================================
    // OBTENER CARGA POR ID
    // ============================================

    public CargaDTO obtenerPorId(Long id) {

        Carga carga = cargaRepository
                .findById(id)
                .orElseThrow(() ->
                        new RuntimeException(
                                "Carga no encontrada con ID: " + id
                        )
                );

        return convertirDTO(carga);
    }

    // ============================================
    // OBTENER CARGAS POR REPARTIDOR
    // ============================================

    public List<CargaDTO> obtenerCargasPorRepartidor(
            Long repartidorId) {

        Usuario repartidor = usuarioRepository
                .findById(repartidorId)
                .orElseThrow(() ->
                        new RuntimeException(
                                "Repartidor no encontrado con ID: "
                                        + repartidorId
                        )
                );

        if (repartidor.getUserTipo() == null ||
                repartidor.getUserTipo() != 4) {

            throw new RuntimeException(
                    "El usuario indicado no es un Repartidor"
            );
        }

        return cargaRepository
                .findByRepartidorIdOrderByFechaHoraDesc(
                        repartidorId
                )
                .stream()
                .map(this::convertirDTO)
                .collect(Collectors.toList());
    }

    // ============================================
    // OBTENER CARGAS PENDIENTES
    // ============================================

    public List<CargaDTO> obtenerCargasPendientes(
            Long repartidorId) {

        return cargaRepository
                .findByRepartidorIdAndEstadoOrderByFechaHoraDesc(
                        repartidorId,
                        "PENDIENTE"
                )
                .stream()
                .map(this::convertirDTO)
                .collect(Collectors.toList());
    }

    // ============================================
    // HU-006: ACEPTAR CARGA SEGURA
    // ============================================

    @Transactional
    public CargaDTO aceptarCarga(
            Long id,
            String correoAutenticado) {

        // ========================================
        // 1. VALIDAR IDENTIDAD
        // ========================================

        if (correoAutenticado == null ||
                correoAutenticado.isBlank()) {

            throw new ResponseStatusException(
                    HttpStatus.UNAUTHORIZED,
                    "Usuario no autenticado"
            );
        }

        // ========================================
        // 2. OBTENER CARGA CON BLOQUEO
        // ========================================

        Carga carga = cargaRepository
                .findWithLockById(id)
                .orElseThrow(() ->
                        new ResponseStatusException(
                                HttpStatus.NOT_FOUND,
                                "Carga no encontrada"
                        )
                );

        // ========================================
        // 3. VALIDAR REPARTIDOR ASIGNADO
        // ========================================

        Usuario repartidor = carga.getRepartidor();

        if (repartidor == null ||
                repartidor.getUserTipo() == null ||
                repartidor.getUserTipo() != 4) {

            throw new ResponseStatusException(
                    HttpStatus.FORBIDDEN,
                    "La carga no tiene un repartidor válido"
            );
        }

        // ========================================
        // 4. EVITAR ACEPTACIÓN DE CARGAS AJENAS
        // ========================================

        // IMPORTANTE:
        // Si Usuario.java usa getEmail(),
        // reemplaza getCorreo() por getEmail().

        String correoRepartidor = repartidor.getEmail();

        if (correoRepartidor == null ||
                !correoRepartidor.equalsIgnoreCase(
                        correoAutenticado.trim()
                )) {

            throw new ResponseStatusException(
                    HttpStatus.FORBIDDEN,
                    "No tienes permiso para aceptar esta carga"
            );
        }

        Long repartidorId = repartidor.getId();

        // ========================================
        // 5. EVITAR ACEPTACIONES REPETIDAS
        // ========================================

        if ("CARGA EN TRÁNSITO".equalsIgnoreCase(
                carga.getEstado())) {

            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "Esta carga ya fue aceptada"
            );
        }

        if (!"PENDIENTE".equalsIgnoreCase(
                carga.getEstado())) {

            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "La carga no está pendiente"
            );
        }

        // ========================================
        // 6. BUSCAR RUTA ACTIVA
        // ========================================

        Ruta rutaActiva = rutaRepository
                .findByRepartidorId(repartidorId)
                .stream()
                .filter(ruta ->
                        Boolean.TRUE.equals(
                                ruta.getActiva()
                        )
                )
                .max(
                        Comparator.comparing(
                                Ruta::getId
                        )
                )
                .orElseThrow(() ->
                        new ResponseStatusException(
                                HttpStatus.CONFLICT,
                                "El repartidor no tiene una ruta activa"
                        )
                );

        // ========================================
        // 7. VALIDAR RELACIÓN CON RUTA
        // ========================================

        if (carga.getRuta() == null) {

            carga.setRuta(rutaActiva);

        } else if (!carga.getRuta()
                .getId()
                .equals(rutaActiva.getId())) {

            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "La carga ya pertenece a otra ruta"
            );
        }

        // ========================================
        // 8. INICIALIZAR CANTIDAD DISPONIBLE
        // ========================================

        if (carga.getCantidadDisponible() == null) {

            carga.setCantidadDisponible(
                    carga.getCantidad()
            );
        }

        // ========================================
        // 9. CAMBIAR ESTADO A EN TRÁNSITO
        // ========================================

        carga.setEstado("CARGA EN TRÁNSITO");

        Carga guardada =
                cargaRepository.save(carga);

        return convertirDTO(guardada);
    }

    // ============================================
    // CONVERTIR ENTIDAD A DTO
    // ============================================

    private CargaDTO convertirDTO(Carga carga) {

        String repartidorNombre = null;
        String inventarioNombre = null;
        String tipoGarrafon = null;
        String unidadMedida = null;

        if (carga.getRepartidor() != null) {

            repartidorNombre = carga
                    .getRepartidor()
                    .getNombre();
        }

        if (carga.getInventario() != null) {

            inventarioNombre = carga
                    .getInventario()
                    .getNombre();

            tipoGarrafon = carga
                    .getInventario()
                    .getTipo();

            unidadMedida = carga
                    .getInventario()
                    .getUnidadMedida();
        }

        return new CargaDTO(
                carga.getId(),

                carga.getRepartidor() != null
                        ? carga.getRepartidor().getId()
                        : null,

                carga.getInventario() != null
                        ? carga.getInventario().getId()
                        : null,

                carga.getCantidad(),

                carga.getCantidadDisponible() != null
                        ? carga.getCantidadDisponible()
                        : carga.getCantidad(),

                carga.getFechaHora(),
                carga.getEstado(),
                repartidorNombre,
                inventarioNombre,
                tipoGarrafon,
                unidadMedida
        );
    }
}
