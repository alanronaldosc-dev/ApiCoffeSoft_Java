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

        Ruta ruta = new Ruta();

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
                                "Ruta no encontrada con ID: " + id
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

        // No modificar una ruta que ya inició recorrido
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
                nombre != null &&
                !nombre.trim().isEmpty()
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

        for (
                Cliente cliente :
                clientesAnteriores
        ) {

            cliente.setRuta(null);
            cliente.setOrdenEnRuta(null);

            clienteRepository.save(
                    cliente
            );
        }

        Ruta rutaActualizada =
                rutaRepository.save(ruta);

        // ========================================
        // ASIGNAR NUEVO ORDEN
        // ========================================

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
    public void eliminarRuta(Long id) {

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

        for (Cliente cliente : clientes) {

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
    // ASIGNAR CLIENTES A LA RUTA
    // ============================================

    private void asignarClientes(
            Ruta ruta,
            List<Long> clienteIds) {

        if (
                clienteIds == null ||
                clienteIds.isEmpty()
        ) {

            return;
        }

        int posicion = 1;

        for (Long clienteId : clienteIds) {

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
    // ACTIVAR RUTA
    // ============================================

    /**
     * Activa la ruta del repartidor.
     *
     * La carga ya no se vincula a la ruta:
     * el repartidor puede llevar varias
     * cargas (una por tipo de garrafón)
     * y las acepta directamente en
     * "Mis cargas".
     */
    @Transactional
    public Ruta desactivarRuta(Long rutaId) {
        Ruta ruta = obtenerPorId(rutaId);
        ruta.setActiva(false);
        ruta.setEstado("CREADA");
        return rutaRepository.save(ruta);
    }

    @Transactional
    public Ruta activarRuta(
            Long rutaId) {

        Ruta ruta =
                obtenerPorId(
                        rutaId
                );

        // ========================================
        // VALIDAR REPARTIDOR DE LA RUTA
        // ========================================

        if (
                ruta.getRepartidor() == null
        ) {

            throw new RuntimeException(
                    "La ruta debe tener un repartidor asignado"
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
    // HU-015
    // CLIENTES DE UNA RUTA
    // ============================================

    /**
     * Obtiene los clientes asignados a una
     * ruta en el orden establecido por
     * el encargado.
     */
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

    /**
     * Muestra cargas que todavía pueden vincularse
     * con una ruta.
     *
     * SIN ASIGNAR = carga sin repartidor.
     * PENDIENTE   = carga asignada pero no aceptada.
     */
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
    // HU-015
    // CLIENTES DE LA RUTA ACTIVA
    // ============================================

    /**
     * Obtiene los clientes de la ruta activa
     * del repartidor y respeta el orden
     * establecido por el encargado.
     */
    public List<Cliente>
    clientesRutaActivaDelRepartidor(
            Long repartidorId) {

        if (repartidorId == null) {

            throw new RuntimeException(
                    "El repartidor es obligatorio"
            );
        }

        // ========================================
        // VALIDAR QUE EXISTA EL REPARTIDOR
        // ========================================

        obtenerRepartidor(
                repartidorId
        );

        // ========================================
        // BUSCAR RUTA ACTIVA
        // ========================================

        List<Ruta> rutasActivas =
                rutaRepository
                        .findByRepartidorId(
                                repartidorId
                        )
                        .stream()
                        .filter(
                                ruta ->
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

        /*
         * Normalmente solo debe existir una ruta
         * activa por repartidor.
         */
        Ruta ruta =
                rutasActivas.get(0);

        System.out.println(
            "RUTA ACTIVA encontrada: id=" + ruta.getId()
            + " nombre=" + ruta.getNombre()
        );

        List<Cliente> clientesEncontrados = clienteRepository
                .findByRutaId(ruta.getId())
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
            "Clientes encontrados para ruta " + ruta.getId()
            + ": " + clientesEncontrados.size()
        );

        return clientesEncontrados;
    }

    // ============================================
    // RUTA EN TRÁNSITO
    // ============================================

    /**
     * Se ejecuta cuando el repartidor acepta
     * la carga.
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
                        .filter(
                                ruta ->
                                        ruta.getCarga() != null
                                        &&
                                        cargaId.equals(
                                                ruta.getCarga()
                                                        .getId()
                                        )
                        )
                        .toList();

        for (Ruta ruta : rutas) {

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