package com.utvt.ApiSpringCafeSoft.service;

import com.utvt.ApiSpringCafeSoft.model.Proveedor;
import com.utvt.ApiSpringCafeSoft.repository.ProveedorRepository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ProveedorService {

    @Autowired
    private ProveedorRepository proveedorRepository;

    // Registrar proveedor
    public Proveedor crearProveedor(Proveedor proveedor) {

        proveedor.setActivo(true);

        return proveedorRepository.save(proveedor);
    }

    // Catálogo de proveedores activos
    public List<Proveedor> obtenerProveedores() {

        return proveedorRepository.findByActivoTrue();
    }

    // Obtener por ID
    public Proveedor obtenerProveedorPorId(Long id) {

        return proveedorRepository.findById(id)
                .orElseThrow(
                    () -> new RuntimeException(
                        "Proveedor no encontrado"
                    )
                );
    }

    // Dar de baja
    public Proveedor darDeBaja(Long id) {

        Proveedor proveedor =
                obtenerProveedorPorId(id);

        proveedor.setActivo(false);

        return proveedorRepository.save(proveedor);
    }

    // Eliminar definitivamente
    public void eliminarProveedor(Long id) {

        if (!proveedorRepository.existsById(id)) {
            throw new RuntimeException(
                "Proveedor no encontrado"
            );
        }

        proveedorRepository.deleteById(id);
    }
}