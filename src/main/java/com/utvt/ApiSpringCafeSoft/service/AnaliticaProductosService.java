package com.utvt.ApiSpringCafeSoft.service;

import com.utvt.ApiSpringCafeSoft.model.*;
import com.utvt.ApiSpringCafeSoft.repository.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.*;
import java.util.*;
import java.util.stream.*;

/** HU-055 y HU-056: analítica de tickets y recomendaciones por historial. */
@Service
public class AnaliticaProductosService {
    private final VentaRepository ventas;
    private final ProductoRepository productos;
    public AnaliticaProductosService(VentaRepository ventas, ProductoRepository productos) {
        this.ventas = ventas; this.productos = productos;
    }
    public record ProductoSimple(Long id, String nombre) {}
    public record Combinacion(List<ProductoSimple> productos, long frecuencia, double soporte) {}
    public record Recomendacion(Long productoId, String nombre, Double precio, long puntuacion, String motivo) {}

    @Transactional(readOnly = true)
    public List<Combinacion> combinaciones(LocalDate inicio, LocalDate fin, int minSupport, int limite) {
        if (minSupport < 1 || limite < 1 || limite > 100) throw new IllegalArgumentException("minSupport debe ser >= 1 y limite entre 1 y 100");
        if (inicio != null && fin != null && inicio.isAfter(fin)) throw new IllegalArgumentException("inicio no puede ser posterior a fin");
        List<Venta> tickets = ventas.findAll().stream()
            .filter(v -> v.getFecha() != null && (inicio == null || !v.getFecha().toLocalDate().isBefore(inicio)) && (fin == null || !v.getFecha().toLocalDate().isAfter(fin)))
            .toList();
        Map<String,Long> conteos = new HashMap<>();
        Map<Long,String> nombres = new HashMap<>();
        for (Venta v : tickets) {
            List<Long> ids = v.getDetalles().stream().filter(d -> d.getProducto() != null && d.getProducto().getId() != null)
                .map(d -> { nombres.put(d.getProducto().getId(), d.getProducto().getNombre()); return d.getProducto().getId(); })
                .distinct().sorted().toList();
            for (int i=0;i<ids.size();i++) for (int j=i+1;j<ids.size();j++)
                conteos.merge(ids.get(i)+":"+ids.get(j),1L,Long::sum);
        }
        return conteos.entrySet().stream().filter(e -> e.getValue() >= minSupport)
            .sorted(Comparator.<Map.Entry<String,Long>>comparingLong(Map.Entry::getValue).reversed().thenComparing(Map.Entry::getKey))
            .limit(limite).map(e -> {
                String[] p=e.getKey().split(":"); Long a=Long.valueOf(p[0]),b=Long.valueOf(p[1]);
                return new Combinacion(List.of(new ProductoSimple(a,nombres.get(a)),new ProductoSimple(b,nombres.get(b))),e.getValue(),tickets.isEmpty()?0:100.0*e.getValue()/tickets.size());
            }).toList();
    }

    @Transactional(readOnly = true)
    public List<Recomendacion> recomendaciones(Long clienteId, int limite) {
        if (clienteId == null || clienteId < 1 || limite < 1 || limite > 30) throw new IllegalArgumentException("Parámetros inválidos");
        List<Venta> historial = ventas.findAll().stream().filter(v -> clienteId.equals(v.getClienteRutaId())).toList();
        if (historial.isEmpty()) return List.of();
        Set<Long> comprados = new HashSet<>();
        Map<Long,Long> puntuacion = new HashMap<>();
        for (Venta v : historial) for (VentaDetalle d : v.getDetalles()) if (d.getProducto()!=null) comprados.add(d.getProducto().getId());
        // Productos comprados por otros clientes junto a productos del historial.
        for (Venta v : ventas.findAll()) {
            Set<Long> ids = v.getDetalles().stream().filter(d -> d.getProducto()!=null).map(d -> d.getProducto().getId()).collect(Collectors.toSet());
            long coincidencias = ids.stream().filter(comprados::contains).count();
            if (coincidencias > 0) for (Long id : ids) if (!comprados.contains(id)) puntuacion.merge(id,coincidencias,Long::sum);
        }
        // El modelo actual de Producto no tiene campo activo; se consideran disponibles los registros existentes.
        return productos.findAll().stream().filter(p -> p.getId()!=null && !comprados.contains(p.getId()))
            .filter(p -> puntuacion.containsKey(p.getId()))
            .sorted(Comparator.<Producto>comparingLong(p -> puntuacion.getOrDefault(p.getId(),0L)).reversed().thenComparing(Producto::getId))
            .limit(limite).map(p -> new Recomendacion(p.getId(),p.getNombre(),p.getPrecio(),puntuacion.get(p.getId()),"Comprado junto con productos del historial"))
            .toList();
    }
}
