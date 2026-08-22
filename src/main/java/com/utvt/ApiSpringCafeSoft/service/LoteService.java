package com.utvt.ApiSpringCafeSoft.service;

import com.utvt.ApiSpringCafeSoft.dto.LoteDTO;
import com.utvt.ApiSpringCafeSoft.model.Insumo;
import com.utvt.ApiSpringCafeSoft.model.Inventario;
import com.utvt.ApiSpringCafeSoft.model.Lote;
import com.utvt.ApiSpringCafeSoft.model.Proveedor;
import com.utvt.ApiSpringCafeSoft.repository.InsumoRepository;
import com.utvt.ApiSpringCafeSoft.repository.InventarioRepository;
import com.utvt.ApiSpringCafeSoft.repository.LoteRepository;
import com.utvt.ApiSpringCafeSoft.repository.ProveedorRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class LoteService {

    @Autowired private LoteRepository loteRepository;
    @Autowired private InsumoRepository insumoRepository;
    @Autowired private InventarioRepository inventarioRepository;
    @Autowired private ProveedorRepository proveedorRepository;

    private LoteDTO convertToDTO(Lote lote) {
        Long proveedorId = null;
        String proveedorNombre = null;
        if (lote.getProveedor() != null) {
            proveedorId = lote.getProveedor().getId();
            proveedorNombre = lote.getProveedor().getNombreEmpresa();
        } else if (lote.getInsumo() != null && lote.getInsumo().getProveedor() != null) {
            proveedorId = lote.getInsumo().getProveedor().getId();
            proveedorNombre = lote.getInsumo().getProveedor().getNombreEmpresa();
        }
        return new LoteDTO(
            lote.getId(),
            lote.getInsumo().getId(),
            lote.getInsumo().getNombre(),
            lote.getInsumo().getUnidadMedida(),
            proveedorId,
            proveedorNombre,
            lote.getCantidad(),
            lote.getFechaCaducidad(),
            lote.getFechaEntrada(),
            lote.getObservaciones()
        );
    }

    private Proveedor resolverProveedor(Long proveedorId, Insumo insumo) {
        if (proveedorId != null) {
            return proveedorRepository.findById(proveedorId)
                .orElseThrow(() -> new RuntimeException("Proveedor no encontrado con ID: " + proveedorId));
        }
        return insumo.getProveedor(); // fallback: proveedor del insumo
    }

    @Transactional
    public LoteDTO registrarLote(LoteDTO dto) {
        Insumo insumo = insumoRepository.findById(dto.getInsumoId())
            .orElseThrow(() -> new RuntimeException("Insumo no encontrado con ID: " + dto.getInsumoId()));

        Proveedor proveedor = resolverProveedor(dto.getProveedorId(), insumo);
        Lote lote = new Lote(insumo, proveedor, dto.getCantidad(), dto.getFechaCaducidad(), dto.getObservaciones());
        loteRepository.save(lote);

        String nombreProveedor = proveedor != null ? proveedor.getNombreEmpresa() : null;

        List<Inventario> existentes = inventarioRepository.findByNombreContainingIgnoreCase(insumo.getNombre());
        Inventario inventario = existentes.stream()
            .filter(i -> i.getUnidadMedida().equals(insumo.getUnidadMedida()))
            .findFirst().orElse(null);

        if (inventario != null) {
            inventario.setCantidad(inventario.getCantidad() + dto.getCantidad());
            if (nombreProveedor != null) inventario.setProveedor(nombreProveedor);
            inventarioRepository.save(inventario);
        } else {
            inventarioRepository.save(new Inventario(
                insumo.getNombre(), insumo.getTipo(), dto.getCantidad(),
                insumo.getUnidadMedida(), 0.0, dto.getFechaCaducidad().toString(),
                nombreProveedor, insumo.getPrecio()
            ));
        }
        return convertToDTO(lote);
    }

    public List<LoteDTO> obtenerTodos() {
        return loteRepository.findAll().stream().map(this::convertToDTO).collect(Collectors.toList());
    }

    public List<LoteDTO> obtenerPorInsumo(Long insumoId) {
        return loteRepository.findByInsumoId(insumoId).stream().map(this::convertToDTO).collect(Collectors.toList());
    }

    public List<LoteDTO> obtenerPorProveedor(Long proveedorId) {
        return loteRepository.findByProveedorId(proveedorId).stream().map(this::convertToDTO).collect(Collectors.toList());
    }

    @Transactional
    public void eliminarLote(Long id) {
        if (!loteRepository.existsById(id))
            throw new RuntimeException("Lote no encontrado con ID: " + id);
        loteRepository.deleteById(id);
    }
}