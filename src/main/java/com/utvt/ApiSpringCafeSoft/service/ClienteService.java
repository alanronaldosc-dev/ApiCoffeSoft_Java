package com.utvt.ApiSpringCafeSoft.service;

import com.utvt.ApiSpringCafeSoft.model.Cliente;
import com.utvt.ApiSpringCafeSoft.repository.ClienteRepository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ClienteService {

    @Autowired
    private ClienteRepository clienteRepository;

    // Registrar cliente
    public Cliente crearCliente(Cliente cliente) {

        cliente.setActivo(true);

        return clienteRepository.save(cliente);
    }

    // Catálogo de clientes (activos e inactivos)
    public List<Cliente> obtenerClientes() {

        return clienteRepository.findAll();
    }

    // Obtener por ID
    public Cliente obtenerClientePorId(Long id) {

        return clienteRepository.findById(id)
                .orElseThrow(
                    () -> new RuntimeException(
                        "Cliente no encontrado"
                    )
                );
    }

    // Actualizar cliente
    public Cliente actualizarCliente(Long id, Cliente clienteActualizado) {

        Cliente cliente = obtenerClientePorId(id);

        cliente.setNombre(clienteActualizado.getNombre());
        cliente.setDomicilio(clienteActualizado.getDomicilio());
        cliente.setLinkGoogleMaps(clienteActualizado.getLinkGoogleMaps());
        cliente.setDiasReparto(clienteActualizado.getDiasReparto());
        cliente.setFrecuencia(clienteActualizado.getFrecuencia());
        cliente.setPrecioPorGarrafon(clienteActualizado.getPrecioPorGarrafon());
        cliente.setGarrafonPreferencia(clienteActualizado.getGarrafonPreferencia());
        cliente.setFotografiaDomicilio(clienteActualizado.getFotografiaDomicilio());

        return clienteRepository.save(cliente);
    }

    // Dar de baja (pausa temporal / ya no requiere servicio)
    public Cliente darDeBaja(Long id) {

        Cliente cliente = obtenerClientePorId(id);

        cliente.setActivo(false);

        return clienteRepository.save(cliente);
    }

    // Dar de alta (reactivar cliente dado de baja)
    public Cliente darDeAlta(Long id) {

        Cliente cliente = obtenerClientePorId(id);

        cliente.setActivo(true);

        return clienteRepository.save(cliente);
    }

    // Eliminar definitivamente
    public void eliminarCliente(Long id) {

        if (!clienteRepository.existsById(id)) {
            throw new RuntimeException(
                "Cliente no encontrado"
            );
        }

        clienteRepository.deleteById(id);
    }
}
