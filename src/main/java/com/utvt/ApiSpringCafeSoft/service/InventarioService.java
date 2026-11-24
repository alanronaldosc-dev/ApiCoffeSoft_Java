package com.utvt.ApiSpringCafeSoft.service;

import com.utvt.ApiSpringCafeSoft.dto.InventarioDTO;
import com.utvt.ApiSpringCafeSoft.model.Inventario;
import com.utvt.ApiSpringCafeSoft.model.Producto;
import com.utvt.ApiSpringCafeSoft.model.Sucursal;
import com.utvt.ApiSpringCafeSoft.repository.InventarioRepository;
import com.utvt.ApiSpringCafeSoft.repository.ProductoRepository;
import com.utvt.ApiSpringCafeSoft.repository.SucursalRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

import com.utvt.ApiSpringCafeSoft.dto.PrediccionDemandaDTO;
import com.utvt.ApiSpringCafeSoft.model.ProductoInsumo;
import com.utvt.ApiSpringCafeSoft.model.Venta;
import com.utvt.ApiSpringCafeSoft.model.VentaDetalle;
import com.utvt.ApiSpringCafeSoft.repository.VentaRepository;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
@Service
public class InventarioService {

    @Autowired
    private ProductoRepository productoRepository;

    @Autowired
    private InventarioRepository inventarioRepository;

    @Autowired
    private SucursalRepository sucursalRepository;

    @Autowired
    private VentaRepository ventaRepository;

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
            inventario.getPrecioUnitario(),
            inventario.getProductoId()
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

    @Transactional
    public InventarioDTO recibirProducto(Long productoId, Double cantidad, Long sucursalId) {
        Producto producto = productoRepository.findById(productoId)
            .orElseThrow(() -> new RuntimeException("Producto no encontrado con ID: " + productoId));

        Inventario existente = inventarioRepository.findByProductoId(productoId).orElse(null);

        if (existente != null) {
            existente.setCantidad(existente.getCantidad() + cantidad);
            return convertToDTO(inventarioRepository.save(existente));
        } else {
            Sucursal sucursal = null;
            if (sucursalId != null) {
                sucursal = sucursalRepository.findById(sucursalId)
                    .orElseThrow(() -> new RuntimeException("Sucursal no encontrada con ID: " + sucursalId));
            }
            Inventario nuevo = new Inventario();
            nuevo.setNombre(producto.getNombre());
            nuevo.setTipo("producto");
            nuevo.setCantidad(cantidad);
            nuevo.setUnidadMedida("piezas");
            nuevo.setCantidadMinima(1.0);
            nuevo.setPrecioUnitario(producto.getPrecio());
            nuevo.setProveedor(null);
            nuevo.setCaducidad(null);
            nuevo.setProductoId(productoId);
            nuevo.setSucursal(sucursal);
            return convertToDTO(inventarioRepository.save(nuevo));
        }
    }

    public List<InventarioDTO> obtenerProductosEnInventario() {
        return inventarioRepository.findByTipo("producto").stream()
            .map(this::convertToDTO)
            .collect(Collectors.toList());
    }

    public List<InventarioDTO> obtenerPorSucursal(Long sucursalId) {
        return inventarioRepository.findBySucursalId(sucursalId).stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

@Transactional(readOnly = true)
public PrediccionDemandaDTO obtenerPrediccionDemanda(Long sucursalId) {

    LocalDateTime finReferencia = LocalDateTime.now();
    LocalDateTime inicioReferencia = finReferencia.minusDays(30);
    LocalDateTime finPrediccion = finReferencia.plusDays(30);

    Map<Long, Inventario> insumosPorId = new LinkedHashMap<>();
    Map<Long, Double> consumoPorInsumo = new HashMap<>();

    // Registrar los insumos utilizados en las recetas actuales.
    List<Producto> productos = productoRepository.findAllWithInsumos();

    for (Producto producto : productos) {
        if (producto.getInsumos() == null) {
            continue;
        }

        for (ProductoInsumo receta : producto.getInsumos()) {
            Inventario insumo = receta.getInsumo();

            if (insumo == null || insumo.getId() == null
                    || "producto".equalsIgnoreCase(insumo.getTipo())) {
                continue;
            }

            if (sucursalId != null
                    && (insumo.getSucursal() == null
                    || !sucursalId.equals(insumo.getSucursal().getId()))) {
                continue;
            }

            insumosPorId.putIfAbsent(insumo.getId(), insumo);
        }
    }

    // Consultar las ventas de los últimos 30 días.
    List<Venta> ventas = sucursalId == null
            ? ventaRepository.findByFechaBetween(
                    inicioReferencia, finReferencia)
            : ventaRepository.findByFechaBetweenAndSucursalId(
                    inicioReferencia, finReferencia, sucursalId);

    // Calcular cuánto insumo se utilizó en cada venta.
    for (Venta venta : ventas) {
        if (venta.getDetalles() == null) {
            continue;
        }

        for (VentaDetalle detalle : venta.getDetalles()) {
            if (detalle.getProducto() == null
                    || detalle.getCantidad() == null
                    || detalle.getProducto().getInsumos() == null) {
                continue;
            }

            for (ProductoInsumo receta : detalle.getProducto().getInsumos()) {
                Inventario insumo = receta.getInsumo();

                if (insumo == null || insumo.getId() == null
                        || receta.getCantidad() == null
                        || "producto".equalsIgnoreCase(insumo.getTipo())) {
                    continue;
                }

                if (sucursalId != null
                        && (insumo.getSucursal() == null
                        || !sucursalId.equals(insumo.getSucursal().getId()))) {
                    continue;
                }

                insumosPorId.putIfAbsent(insumo.getId(), insumo);

                double cantidadUtilizada =
                        receta.getCantidad() * detalle.getCantidad();

                consumoPorInsumo.merge(
                        insumo.getId(),
                        cantidadUtilizada,
                        Double::sum
                );
            }
        }
    }

    // Generar la predicción y comparar con las existencias actuales.
    List<PrediccionDemandaDTO.InsumoPredichoDTO> resultado =
            new ArrayList<>();

    insumosPorId.values().stream()
            .sorted(Comparator.comparing(
                    Inventario::getNombre,
                    String.CASE_INSENSITIVE_ORDER
            ))
            .forEach(insumo -> {

                double consumoHistorico =
                        consumoPorInsumo.getOrDefault(insumo.getId(), 0.0);

                double promedioDiario = consumoHistorico / 30.0;

                double demandaEstimada = promedioDiario * 30.0;

                double stockActual = insumo.getCantidad() != null
                        ? insumo.getCantidad()
                        : 0.0;

                double faltante = Math.max(
                        demandaEstimada - stockActual,
                        0.0
                );

                String estado = faltante > 0
                        ? "Stock insuficiente"
                        : "Stock suficiente";

                Long idSucursal = insumo.getSucursal() != null
                        ? insumo.getSucursal().getId()
                        : null;

                String nombreSucursal = insumo.getSucursal() != null
                        ? insumo.getSucursal().getNombre()
                        : null;

                resultado.add(
                        new PrediccionDemandaDTO.InsumoPredichoDTO(
                                insumo.getId(),
                                insumo.getNombre(),
                                insumo.getUnidadMedida(),
                                stockActual,
                                consumoHistorico,
                                promedioDiario,
                                demandaEstimada,
                                faltante,
                                estado,
                                idSucursal,
                                nombreSucursal
                        )
                );
            });

    return new PrediccionDemandaDTO(
            inicioReferencia,
            finReferencia,
            finReferencia,
            finPrediccion,
            resultado
    );
}

}
