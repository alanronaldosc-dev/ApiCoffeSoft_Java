package com.utvt.ApiSpringCafeSoft.service;

import com.utvt.ApiSpringCafeSoft.model.Carga;
import com.utvt.ApiSpringCafeSoft.model.Cliente;
import com.utvt.ApiSpringCafeSoft.model.Ruta;
import com.utvt.ApiSpringCafeSoft.model.Usuario;

import com.utvt.ApiSpringCafeSoft.repository.CargaRepository;
import com.utvt.ApiSpringCafeSoft.repository.ClienteRepository;
import com.utvt.ApiSpringCafeSoft.repository.RutaRepository;
import com.utvt.ApiSpringCafeSoft.repository.UsuarioRepository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Comparator;
import java.util.List;

@Service
public class RutaService {

    @Autowired
    private RutaRepository rutaRepository;

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private ClienteRepository clienteRepository;

    @Autowired
    private CargaRepository cargaRepository;

    // ============================================
    // CREAR RUTA
    // ============================================

    @Transactional
    public Ruta crearRuta(
            String nombre,
            Long repartidorId,
            String diasReparto,
            List<Long> clienteIds) {

        if (nombre == null || nombre.trim().isEmpty()) {
            throw new RuntimeException(
                    "El nombre de la ruta es obligatorio"
            );
        }

        Usuario repartidor =
                obtenerRepartidor(repartidorId);

        Ruta ruta =
                new Ruta();

        ruta.setNombre(
                nombre.trim()
        );

        ruta.setRepartidor(
                repartidor
        );

        ruta.setDiasReparto(
                diasReparto
        );

        ruta.setEstado(
                "CREADA"
        );

        ruta.setActiva(
                false
        );

        Ruta rutaGuardada =
                rutaRepository.save(ruta);

        asignarClientes(
                rutaGuardada,
                clienteIds
        );

        return rutaGuardada;
    }

    // ============================================
    // OBTENER TODAS LAS RUTAS
    // ============================================

    public List<Ruta> obtenerRutas() {

        return rutaRepository.findAll();
    }

    // ============================================
    // OBTENER RUTA POR ID
    // ============================================

    public Ruta obtenerPorId(Long id) {

        return rutaRepository
                .findById(id)
                .orElseThrow(() ->
                        new RuntimeException(
                                "Ruta no encontrada con ID: "
                                        + id
                        )
                );
    }

    // ============================================
    // ACTUALIZAR RUTA
    // ============================================

    @Transactional
    public Ruta actualizarRuta(
            Long id,
            String nombre,
            Long repartidorId,
            String diasReparto,
            List<Long> clienteIds) {

        Ruta ruta =
                obtenerPorId(id);

        if (
                "EN_TRANSITO".equalsIgnoreCase(
                        ruta.getEstado()
                )
        ) {

            throw new RuntimeException(
                    "No se puede modificar una ruta que ya está en tránsito"
            );
        }

        if (
                nombre != null
                        && !nombre.trim().isEmpty()
        ) {

            ruta.setNombre(
                    nombre.trim()
            );
        }

        if (diasReparto != null) {

            ruta.setDiasReparto(
                    diasReparto
            );
        }

        if (repartidorId != null) {

            Usuario repartidor =
                    obtenerRepartidor(
                            repartidorId
                    );

            ruta.setRepartidor(
                    repartidor
            );
        }

        // ========================================
        // DESASIGNAR CLIENTES ANTERIORES
        // ========================================

        List<Cliente> clientesAnteriores =
                clienteRepository
                        .findByRutaId(id);

        for (Cliente cliente :
                clientesAnteriores) {

            cliente.setRuta(null);
            cliente.setOrdenEnRuta(null);

            clienteRepository.save(
                    cliente
            );
        }

        Ruta rutaActualizada =
                rutaRepository.save(ruta);

        asignarClientes(
                rutaActualizada,
                clienteIds
        );

        return rutaActualizada;
    }

    // ============================================
    // ELIMINAR RUTA
    // ============================================

    @Transactional
    public void eliminarRuta(
            Long id) {

        Ruta ruta =
                obtenerPorId(id);

        if (
                "EN_TRANSITO".equalsIgnoreCase(
                        ruta.getEstado()
                )
        ) {

            throw new RuntimeException(
                    "No se puede eliminar una ruta que está en tránsito"
            );
        }

        List<Cliente> clientes =
                clienteRepository
                        .findByRutaId(id);

        for (Cliente cliente :
                clientes) {

            cliente.setRuta(null);
            cliente.setOrdenEnRuta(null);

            clienteRepository.save(
                    cliente
            );
        }

        rutaRepository.delete(
                ruta
        );
    }

    // ============================================
    // ASIGNAR CLIENTES
    // ============================================

    private void asignarClientes(
            Ruta ruta,
            List<Long> clienteIds) {

        if (
                clienteIds == null
                        || clienteIds.isEmpty()
        ) {

            return;
        }

        int posicion =
                1;

        for (Long clienteId :
                clienteIds) {

            Cliente cliente =
                    clienteRepository
                            .findById(clienteId)
                            .orElseThrow(() ->
                                    new RuntimeException(
                                            "Cliente no encontrado con ID: "
                                                    + clienteId
                                    )
                            );

            cliente.setRuta(
                    ruta
            );

            cliente.setOrdenEnRuta(
                    posicion
            );

            clienteRepository.save(
                    cliente
            );

            posicion++;
        }
    }

    // ============================================
    // TERMINAR RUTA ACTIVA
    // ============================================

    @Transactional
    public Ruta terminarRutaActivaDelRepartidor(
            Long repartidorId) {

        if (repartidorId == null) {

            throw new RuntimeException(
                    "El repartidor es obligatorio"
            );
        }

        obtenerRepartidor(
                repartidorId
        );

        List<Ruta> rutasActivas =
                rutaRepository
                        .findByRepartidorId(
                                repartidorId
                        )
                        .stream()
                        .filter(ruta ->
                                Boolean.TRUE.equals(
                                        ruta.getActiva()
                                )
                        )
                        .toList();

        if (rutasActivas.isEmpty()) {

            throw new RuntimeException(
                    "No hay una ruta activa para este repartidor"
            );
        }

        /*
         * Debe existir solamente una ruta activa.
         * Si existen datos antiguos inconsistentes,
         * se toma la ruta más reciente.
         */
        Ruta ruta =
                rutasActivas
                        .stream()
                        .max(
                                Comparator.comparing(
                                        Ruta::getId
                                )
                        )
                        .orElseThrow();

        ruta.setActiva(
                false
        );

        ruta.setEstado(
                "TERMINADA"
        );

        return rutaRepository.save(
                ruta
        );
    }

    // ============================================
    // DESACTIVAR RUTA
    // ============================================

    @Transactional
    public Ruta desactivarRuta(
            Long rutaId) {

        Ruta ruta =
                obtenerPorId(
                        rutaId
                );

        ruta.setActiva(
                false
        );

        ruta.setEstado(
                "CREADA"
        );

        return rutaRepository.save(
                ruta
        );
    }

    // ============================================
    // ACTIVAR RUTA
    // ============================================

    @Transactional
    public Ruta activarRuta(
            Long rutaId) {

        Ruta ruta =
                obtenerPorId(
                        rutaId
                );

        // ========================================
        // VALIDAR REPARTIDOR
        // ========================================

        if (ruta.getRepartidor() == null) {

            throw new RuntimeException(
                    "La ruta debe tener un repartidor asignado"
            );
        }

        Long repartidorId =
                ruta.getRepartidor()
                        .getId();

        obtenerRepartidor(
                repartidorId
        );

        // ========================================
        // VALIDAR QUE NO EXISTA OTRA RUTA ACTIVA
        // ========================================

        boolean otraRutaActiva =
                rutaRepository
                        .findByRepartidorId(
                                repartidorId
                        )
                        .stream()
                        .anyMatch(r ->
                                !r.getId().equals(
                                        ruta.getId()
                                )
                                        && Boolean.TRUE.equals(
                                        r.getActiva()
                                )
                        );

        if (otraRutaActiva) {

            throw new RuntimeException(
                    "El repartidor ya tiene una ruta activa"
            );
        }

        // ========================================
        // VALIDAR CLIENTES
        // ========================================

        List<Cliente> clientesRuta =
                clienteRepository
                        .findByRutaId(
                                ruta.getId()
                        );

        if (clientesRuta.isEmpty()) {

            throw new RuntimeException(
                    "La ruta debe tener al menos un cliente asignado"
            );
        }

        // ========================================
        // VINCULAR CARGAS A ESTA RUTA
        // ========================================

        List<Carga> cargasDelRepartidor =
                cargaRepository
                        .findByRepartidorIdOrderByFechaHoraDesc(
                                repartidorId
                        );

        for (Carga carga :
                cargasDelRepartidor) {

            String estado =
                    carga.getEstado();

            if (estado == null) {
                continue;
            }

            boolean disponibleParaRuta =
                    "PENDIENTE".equalsIgnoreCase(
                            estado
                    )
                            ||
                    "CARGA EN TRÁNSITO".equalsIgnoreCase(
                            estado
                    );

            /*
             * Solamente tomamos cargas que todavía
             * no pertenezcan a otra ruta.
             */
            if (
                    disponibleParaRuta
                            && carga.getRuta() == null
            ) {

                carga.setRuta(
                        ruta
                );

                cargaRepository.save(
                        carga
                );
            }
        }

        // ========================================
        // VALIDAR QUE HAYA CARGAS
        // ========================================

        List<Carga> cargasRuta =
                cargaRepository
                        .findByRutaIdOrderByFechaHoraAsc(
                                ruta.getId()
                        );

        if (cargasRuta.isEmpty()) {

            throw new RuntimeException(
                    "No hay cargas disponibles para esta ruta"
            );
        }

        // ========================================
        // ACTIVAR RUTA
        // ========================================

        ruta.setActiva(
                true
        );

        ruta.setEstado(
                "ACTIVA"
        );

        return rutaRepository.save(
                ruta
        );
    }

    // ============================================
    // CLIENTES DE UNA RUTA
    // ============================================

    public List<Cliente> obtenerClientesRuta(
            Long rutaId) {

        Ruta ruta =
                obtenerPorId(
                        rutaId
                );

        return clienteRepository
                .findByRutaId(
                        ruta.getId()
                )
                .stream()
                .sorted(
                        Comparator.comparing(
                                Cliente::getOrdenEnRuta,
                                Comparator.nullsLast(
                                        Comparator.naturalOrder()
                                )
                        )
                )
                .toList();
    }

    // ============================================
    // CARGAS DISPONIBLES
    // ============================================

    public List<Carga> cargasDisponibles() {

        return cargaRepository
                .findAll()
                .stream()
                .filter(carga -> {

                    String estado =
                            carga.getEstado();

                    if (estado == null) {
                        return false;
                    }

                    return
                            "SIN ASIGNAR"
                                    .equalsIgnoreCase(
                                            estado
                                    )
                                    ||
                            "PENDIENTE"
                                    .equalsIgnoreCase(
                                            estado
                                    );

                })
                .sorted(
                        Comparator.comparing(
                                Carga::getFechaHora,
                                Comparator.nullsLast(
                                        Comparator.naturalOrder()
                                )
                        ).reversed()
                )
                .toList();
    }

    // ============================================
    // CLIENTES DE RUTA ACTIVA
    // ============================================

    public List<Cliente>
    clientesRutaActivaDelRepartidor(
            Long repartidorId) {

        if (repartidorId == null) {

            throw new RuntimeException(
                    "El repartidor es obligatorio"
            );
        }

        obtenerRepartidor(
                repartidorId
        );

        List<Ruta> rutasActivas =
                rutaRepository
                        .findByRepartidorId(
                                repartidorId
                        )
                        .stream()
                        .filter(ruta ->
                                Boolean.TRUE.equals(
                                        ruta.getActiva()
                                )
                        )
                        .toList();

        if (rutasActivas.isEmpty()) {

            throw new RuntimeException(
                    "No hay ruta activa para este repartidor"
            );
        }

        Ruta ruta =
                rutasActivas
                        .stream()
                        .max(
                                Comparator.comparing(
                                        Ruta::getId
                                )
                        )
                        .orElseThrow();

        System.out.println(
                "RUTA ACTIVA encontrada: id="
                        + ruta.getId()
                        + " nombre="
                        + ruta.getNombre()
        );

        List<Cliente> clientesEncontrados =
                clienteRepository
                        .findByRutaId(
                                ruta.getId()
                        )
                        .stream()
                        .sorted(
                                Comparator.comparing(
                                        Cliente::getOrdenEnRuta,
                                        Comparator.nullsLast(
                                                Comparator.naturalOrder()
                                        )
                                )
                        )
                        .toList();

        System.out.println(
                "Clientes encontrados para ruta "
                        + ruta.getId()
                        + ": "
                        + clientesEncontrados.size()
        );

        return clientesEncontrados;
    }

    // ============================================
    // RUTA EN TRÁNSITO
    // ============================================

    /**
     * Mantiene compatibilidad con
     * comportamiento anterior.
     */
    @Transactional
    public void marcarRutaEnTransito(
            Long cargaId) {

        if (cargaId == null) {
            return;
        }

        List<Ruta> rutas =
                rutaRepository
                        .findAll()
                        .stream()
                        .filter(ruta ->
                                ruta.getCarga() != null
                                        &&
                                        cargaId.equals(
                                                ruta.getCarga()
                                                        .getId()
                                        )
                        )
                        .toList();

        for (Ruta ruta :
                rutas) {

            ruta.setEstado(
                    "EN_TRANSITO"
            );

            ruta.setActiva(
                    true
            );

            rutaRepository.save(
                    ruta
            );
        }
    }

    // ============================================
    // OBTENER / VALIDAR REPARTIDOR
    // ============================================

    private Usuario obtenerRepartidor(
            Long repartidorId) {

        if (repartidorId == null) {

            throw new RuntimeException(
                    "El repartidor es obligatorio"
            );
        }

        Usuario repartidor =
                usuarioRepository
                        .findById(
                                repartidorId
                        )
                        .orElseThrow(() ->
                                new RuntimeException(
                                        "Repartidor no encontrado con ID: "
                                                + repartidorId
                                )
                        );

        if (
                repartidor.getUserTipo() == null
                        ||
                repartidor.getUserTipo() != 4
        ) {

            throw new RuntimeException(
                    "El usuario seleccionado no tiene el rol de Repartidor"
            );
        }

        if (
                Boolean.FALSE.equals(
                        repartidor.getActivo()
                )
        ) {

            throw new RuntimeException(
                    "El repartidor seleccionado está inactivo"
            );
        }

        return repartidor;
    }
}