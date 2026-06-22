package com.utvt.ApiSpringCafeSoft.service;

import com.utvt.ApiSpringCafeSoft.dto.InventarioDTO;
import com.utvt.ApiSpringCafeSoft.model.Inventario;
import com.utvt.ApiSpringCafeSoft.repository.InventarioRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class InventarioService {

    @Autowired
    private InventarioRepository inventarioRepository;

    private InventarioDTO convertToDTO(Inventario inventario) {
        return new InventarioDTO(
            inventario.getId(),
            inventario.getNombre(),
            inventario.getTipo(),
            inventario.getCantidad(),
            inventario.getUnidadMedida(),
            inventario.getCantidadMinima(),
            inventario.getCaducidad(),
            inventario.getProveedor(),
            inventario.getPrecioUnitario()
        );
    }

    private Inventario convertToEntity(InventarioDTO dto) {
        Inventario inventario = new Inventario();
        inventario.setId(dto.getId());
        inventario.setNombre(dto.getNombre());
        inventario.setTipo(dto.getTipo());
        inventario.setCantidad(dto.getCantidad());
        inventario.setUnidadMedida(dto.getUnidadMedida());
        inventario.setCantidadMinima(dto.getCantidadMinima());
        inventario.setCaducidad(dto.getCaducidad());
        inventario.setProveedor(dto.getProveedor());
        inventario.setPrecioUnitario(dto.getPrecioUnitario());
        return inventario;
    }

    @Transactional
    public InventarioDTO crearInsumo(InventarioDTO dto) {
        return convertToDTO(inventarioRepository.save(convertToEntity(dto)));
    }

    public List<InventarioDTO> obtenerTodosLosInsumos() {
        return inventarioRepository.findAll().stream()
                .map(this::convertToDTO).collect(Collectors.toList());
    }

    public InventarioDTO obtenerInsumoPorId(Long id) {
        return convertToDTO(inventarioRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Insumo no encontrado con ID: " + id)));
    }

    @Transactional
    public InventarioDTO actualizarInsumo(Long id, InventarioDTO dto) {
        Inventario existing = inventarioRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Insumo no encontrado con ID: " + id));
        existing.setNombre(dto.getNombre());
        existing.setTipo(dto.getTipo());
        existing.setCantidad(dto.getCantidad());
        existing.setUnidadMedida(dto.getUnidadMedida());
        existing.setCantidadMinima(dto.getCantidadMinima());
        existing.setCaducidad(dto.getCaducidad());
        existing.setProveedor(dto.getProveedor());
        existing.setPrecioUnitario(dto.getPrecioUnitario());
        return convertToDTO(inventarioRepository.save(existing));
    }

    @Transactional
    public void eliminarInsumo(Long id) {
        if (!inventarioRepository.existsById(id))
            throw new RuntimeException("Insumo no encontrado con ID: " + id);
        inventarioRepository.deleteById(id);
    }

    public List<InventarioDTO> buscarPorNombre(String nombre) {
        return inventarioRepository.findByNombreContainingIgnoreCase(nombre).stream()
                .map(this::convertToDTO).collect(Collectors.toList());
    }

    public List<InventarioDTO> buscarPorTipo(String tipo) {
        return inventarioRepository.findByTipoContainingIgnoreCase(tipo).stream()
                .map(this::convertToDTO).collect(Collectors.toList());
    }

    public List<InventarioDTO> buscarPorProveedor(String proveedor) {
        return inventarioRepository.findByProveedorContainingIgnoreCase(proveedor).stream()
                .map(this::convertToDTO).collect(Collectors.toList());
    }

    public List<InventarioDTO> obtenerInsumosBajoStock() {
        return inventarioRepository.findLowStockItems().stream()
                .map(this::convertToDTO).collect(Collectors.toList());
    }

    public List<InventarioDTO> obtenerInsumosStockCritico() {
        return inventarioRepository.findCriticalStockItems().stream()
                .map(this::convertToDTO).collect(Collectors.toList());
    }

    public List<InventarioDTO> obtenerPorUnidadMedida(String unidadMedida) {
        return inventarioRepository.findByUnidadMedida(unidadMedida).stream()
                .map(this::convertToDTO).collect(Collectors.toList());
    }

    public List<InventarioDTO> obtenerInsumosCaducanAntesDe(String fecha) {
        return inventarioRepository.findByCaducidadBefore(fecha).stream()
                .map(this::convertToDTO).collect(Collectors.toList());
    }

    public List<InventarioDTO> obtenerInsumosPorRangoPrecio(Double precioMin, Double precioMax) {
        return inventarioRepository.findByPrecioRange(precioMin, precioMax).stream()
                .map(this::convertToDTO).collect(Collectors.toList());
    }
}
