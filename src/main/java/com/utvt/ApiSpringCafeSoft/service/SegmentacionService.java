package com.utvt.ApiSpringCafeSoft.service;

import com.utvt.ApiSpringCafeSoft.model.Cliente;
import com.utvt.ApiSpringCafeSoft.repository.ClienteRepository;
import com.utvt.ApiSpringCafeSoft.repository.SegmentacionRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.http.HttpStatus;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.Collectors;

/** HU-035/HU-036: clasificación calculada al consultar, sin alterar entidades existentes. */
@Service
public class SegmentacionService {
    private final ClienteRepository clientes;
    private final SegmentacionRepository ventas;
    public SegmentacionService(ClienteRepository clientes, SegmentacionRepository ventas) {
        this.clientes = clientes;
        this.ventas = ventas;
    }
    public record SegmentoCliente(Long clienteId, String nombre, String segmento,
        String regla, long compras, LocalDate ultimaCompra, Long diasSinCompra) {}
    public record Resumen(long total, Map<String, Long> segmentos) {}
    private record Actividad(long compras, LocalDateTime ultima) {}

    private Map<Long, Actividad> actividad() {
        Map<Long, Actividad> mapa = new HashMap<>();
        // Priorizar cliente de fidelidad si la venta tiene ambas referencias;
        // cada venta se contabiliza una sola vez.
        for (Object[] fila : ventas.actividadPorCliente()) agregar(mapa, fila);
        return mapa;
    }
    private void agregar(Map<Long, Actividad> mapa, Object[] fila) {
        Long id = ((Number)fila[0]).longValue();
        long cantidad = ((Number)fila[1]).longValue();
        LocalDateTime fecha = (LocalDateTime)fila[2];
        Actividad previo = mapa.get(id);
        mapa.put(id, previo == null ? new Actividad(cantidad, fecha) :
            new Actividad(previo.compras + cantidad,
                previo.ultima.isAfter(fecha) ? previo.ultima : fecha));
    }
    private SegmentoCliente clasificar(Cliente cliente, Actividad actividad) {
        long compras = actividad == null ? 0 : actividad.compras;
        LocalDate ultima = actividad == null ? null : actividad.ultima.toLocalDate();
        Long dias = ultima == null ? null : Math.max(0, ChronoUnit.DAYS.between(ultima, LocalDate.now()));
        String segmento, regla;
        if (compras == 0) {
            segmento = "POTENCIAL"; regla = "Sin compras registradas";
        } else if (dias > 90) {
            segmento = "DORMIDO"; regla = "Última compra hace más de 90 días";
        } else if (dias <= 30 && compras >= 3) {
            segmento = "ACTIVO"; regla = "Al menos 3 compras históricas y una compra en los últimos 30 días";
        } else {
            segmento = "POCO_FRECUENTE"; regla = "Tiene compras, pero no cumple las condiciones de activo ni dormido";
        }
        return new SegmentoCliente(cliente.getId(), cliente.getNombre(), segmento, regla, compras, ultima, dias);
    }
    @Transactional(readOnly = true)
    public List<SegmentoCliente> listar(String segmento) {
        Set<String> validos = Set.of("ACTIVO", "POTENCIAL", "DORMIDO", "POCO_FRECUENTE");
        if (segmento != null && !segmento.isBlank() && !validos.contains(segmento.toUpperCase(Locale.ROOT)))
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Segmento no válido");
        Map<Long, Actividad> datos = actividad();
        return clientes.findAll().stream().map(c -> clasificar(c, datos.get(c.getId())))
            .filter(c -> segmento == null || segmento.isBlank() || c.segmento().equalsIgnoreCase(segmento))
            .collect(Collectors.toList());
    }
    @Transactional(readOnly = true)
    public SegmentoCliente porCliente(Long id) {
        Cliente cliente = clientes.findById(id).orElseThrow(() ->
            new ResponseStatusException(HttpStatus.NOT_FOUND, "Cliente no encontrado"));
        return clasificar(cliente, actividad().get(id));
    }
    @Transactional(readOnly = true)
    public Resumen resumen() {
        List<SegmentoCliente> lista = listar(null);
        Map<String, Long> totales = new LinkedHashMap<>();
        for (String s : List.of("ACTIVO", "POTENCIAL", "DORMIDO", "POCO_FRECUENTE")) totales.put(s, 0L);
        lista.forEach(c -> totales.merge(c.segmento(), 1L, Long::sum));
        return new Resumen(lista.size(), totales);
    }
}
