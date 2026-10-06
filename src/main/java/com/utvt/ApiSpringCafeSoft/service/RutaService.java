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

    public Ruta crearRuta(String nombre, Long repartidorId, String diasReparto, List<Long> clienteIds) {

        if (nombre == null || nombre.trim().isEmpty()) {
            throw new RuntimeException("El nombre de la ruta es obligatorio");
        }

        Usuario repartidor = null;
        if (repartidorId != null) {
            repartidor = usuarioRepository.findById(repartidorId)
                    .orElseThrow(() -> new RuntimeException("Repartidor no encontrado"));
            if (repartidor.getUserTipo() == null || repartidor.getUserTipo() != 4) {
                throw new RuntimeException("El usuario no tiene el rol de Repartidor");
            }
        }

        Ruta ruta = new Ruta();
        ruta.setNombre(nombre);
        ruta.setRepartidor(repartidor);
        ruta.setDiasReparto(diasReparto);
        ruta.setEstado("CREADA");
        ruta.setActiva(false);

        Ruta guardada = rutaRepository.save(ruta);

        asignarClientes(guardada, clienteIds);

        return guardada;
    }

    public List<Ruta> obtenerRutas() {
        return rutaRepository.findAll();
    }

    public Ruta obtenerPorId(Long id) {
        return rutaRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Ruta no encontrada"));
    }

    public Ruta actualizarRuta(Long id, String nombre, Long repartidorId, String diasReparto, List<Long> clienteIds) {

        Ruta ruta = obtenerPorId(id);

        if (nombre != null && !nombre.trim().isEmpty()) {
            ruta.setNombre(nombre);
        }

        if (diasReparto != null) {
            ruta.setDiasReparto(diasReparto);
        }

        if (repartidorId != null) {
            Usuario repartidor = usuarioRepository.findById(repartidorId)
                    .orElseThrow(() -> new RuntimeException("Repartidor no encontrado"));
            ruta.setRepartidor(repartidor);
        }

        // Desasignar clientes que ya no pertenecen
        List<Cliente> anteriores = clienteRepository.findByRutaId(id);
        for (Cliente c : anteriores) {
            c.setRuta(null);
            c.setOrdenEnRuta(null);
            clienteRepository.save(c);
        }

        Ruta actualizada = rutaRepository.save(ruta);

        asignarClientes(actualizada, clienteIds);

        return actualizada;
    }

    public void eliminarRuta(Long id) {
        Ruta ruta = obtenerPorId(id);

        List<Cliente> anteriores = clienteRepository.findByRutaId(id);
        for (Cliente c : anteriores) {
            c.setRuta(null);
            c.setOrdenEnRuta(null);
            clienteRepository.save(c);
        }

        rutaRepository.deleteById(id);
    }

    private void asignarClientes(Ruta ruta, List<Long> clienteIds) {
        if (clienteIds == null) return;
        int posicion = 1;
        for (Long clienteId : clienteIds) {
            Cliente cliente = clienteRepository.findById(clienteId)
                    .orElseThrow(() -> new RuntimeException("Cliente no encontrado: " + clienteId));
            cliente.setRuta(ruta);
            cliente.setOrdenEnRuta(posicion++);
            clienteRepository.save(cliente);
        }
    }

    /**
     * Activar ruta: queda visible para el repartidor e incluye la carga seleccionada.
     */
    public Ruta activarRuta(Long id, Long cargaId) {

        Ruta ruta = obtenerPorId(id);

        if (cargaId == null) {
            throw new RuntimeException("La carga es obligatoria para activar la ruta");
        }

        Carga carga = cargaRepository.findById(cargaId)
                .orElseThrow(() -> new RuntimeException("Carga no encontrada"));

        if (carga.getRepartidor() != null) {
            throw new RuntimeException("La carga ya está asignada");
        }

        carga.setRepartidor(ruta.getRepartidor());
        carga.setEstado("PENDIENTE");
        cargaRepository.save(carga);

        ruta.setCarga(carga);
        ruta.setActiva(true);
        ruta.setEstado("ACTIVA");

        return rutaRepository.save(ruta);
    }

    public List<Carga> cargasDisponibles() {
        return cargaRepository.findByRepartidorIsNullAndEstadoOrderByFechaHoraDesc("SIN ASIGNAR");
    }
}
