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

    // HU-081: estimación de riesgo de abandono (RNF-10).
    public record DatosReferencia(long compras, LocalDate periodoDesde, LocalDate periodoHasta,
        long comprasUltimos30, double promedioDiasEntreCompras, double comprasEsperadas30) {}
    public record ClienteRiesgo(Long clienteId, String nombre, String nivel, String motivo,
        Long diasSinCompra, double caidaFrecuencia, String telefono, String linkGoogleMaps,
        LocalDateTime fechaCalculo, DatosReferencia datosReferencia) {}

    private static final int VENTANA_DIAS = 30;
    private record Actividad(long compras, LocalDateTime ultima) {}
    private record ActividadRiesgo(long compras, LocalDateTime primera, LocalDateTime ultima,
        long comprasRecientes) {}

    private static int pesoNivel(String nivel) {
        return switch (nivel) {
            case "ALTO" -> 3;
            case "MEDIO" -> 2;
            default -> 1;
        };
    }

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

    private Map<Long, ActividadRiesgo> actividadRiesgo() {
        Map<Long, ActividadRiesgo> mapa = new HashMap<>();
        LocalDateTime desde = LocalDateTime.now().minusDays(VENTANA_DIAS);
        for (Object[] fila : ventas.actividadRiesgoPorCliente(desde)) {
            Long id = ((Number) fila[0]).longValue();
            long compras = ((Number) fila[1]).longValue();
            LocalDateTime primera = (LocalDateTime) fila[2];
            LocalDateTime ultima = (LocalDateTime) fila[3];
            long recientes = fila[4] == null ? 0 : ((Number) fila[4]).longValue();
            mapa.put(id, new ActividadRiesgo(compras, primera, ultima, recientes));
        }
        return mapa;
    }

    private ClienteRiesgo clasificarRiesgo(Cliente cliente, ActividadRiesgo datos,
            LocalDateTime fechaCalculo) {
        if (datos == null || datos.compras() == 0) {
            return new ClienteRiesgo(cliente.getId(), cliente.getNombre(), "ALTO",
                "Sin compras registradas", null, 0.0, cliente.getTelefono(),
                cliente.getLinkGoogleMaps(), fechaCalculo,
                new DatosReferencia(0, null, null, 0, 0, 0));
        }

        LocalDate hoy = fechaCalculo.toLocalDate();
        LocalDate ultima = datos.ultima().toLocalDate();
        LocalDate primera = datos.primera().toLocalDate();
        long dias = Math.max(0, ChronoUnit.DAYS.between(ultima, hoy));
        long promedioDias = datos.compras() > 1
            ? ChronoUnit.DAYS.between(primera, datos.ultima()) / (datos.compras() - 1)
            : 0;
        double esperadas = promedioDias > 0 ? 30.0 / promedioDias : datos.compras();
        double caida = esperadas > 0
            ? Math.max(0, (esperadas - datos.comprasRecientes()) * 100.0 / esperadas)
            : 0;

        String nivelDias = dias > 90 ? "ALTO" : dias > 30 ? "MEDIO" : "BAJO";
        String nivelCaida = caida >= 50 ? "ALTO" : caida >= 25 ? "MEDIO" : "BAJO";
        String nivel = pesoNivel(nivelDias) >= pesoNivel(nivelCaida) ? nivelDias : nivelCaida;

        List<String> motivos = new ArrayList<>();
        if (!"BAJO".equals(nivelDias)) motivos.add(dias + " días sin pedido");
        if (!"BAJO".equals(nivelCaida)) motivos.add("Frecuencia bajó " + Math.round(caida) + "%");
        String motivo = motivos.isEmpty()
            ? "Compras y frecuencia dentro de lo habitual"
            : String.join(" · ", motivos);

        DatosReferencia referencia = new DatosReferencia(
            datos.compras(), primera, ultima, datos.comprasRecientes(), promedioDias, esperadas);

        return new ClienteRiesgo(cliente.getId(), cliente.getNombre(), nivel, motivo,
            dias, caida, cliente.getTelefono(), cliente.getLinkGoogleMaps(),
            fechaCalculo, referencia);
    }

    @Transactional(readOnly = true)
    public List<ClienteRiesgo> listarRiesgo() {
        LocalDateTime fechaCalculo = LocalDateTime.now();
        Map<Long, ActividadRiesgo> datos = actividadRiesgo();
        Comparator<ClienteRiesgo> orden = Comparator
            .comparingInt((ClienteRiesgo c) -> pesoNivel(c.nivel())).reversed()
            .thenComparing(ClienteRiesgo::diasSinCompra,
                Comparator.nullsLast(Comparator.reverseOrder()));

        return clientes.findAll().stream()
            .map(c -> clasificarRiesgo(c, datos.get(c.getId()), fechaCalculo))
            .sorted(orden)
            .collect(Collectors.toList());
    }
}
