package com.utvt.ApiSpringCafeSoft.service;

import com.utvt.ApiSpringCafeSoft.dto.DesempenoRepartidorDTO;
import com.utvt.ApiSpringCafeSoft.dto.DesempenoRepartidoresDTO;
import com.utvt.ApiSpringCafeSoft.model.Usuario;
import com.utvt.ApiSpringCafeSoft.repository.EntregaPedidoRepository;
import com.utvt.ApiSpringCafeSoft.repository.UsuarioRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

/** Lógica de consulta para HU-049. No modifica el flujo de registro de ventas. */
@Service
public class DesempenoRepartidoresService {

    @Autowired
    private EntregaPedidoRepository entregaPedidoRepository;

    @Autowired
    private UsuarioRepository usuarioRepository;

public DesempenoRepartidoresDTO obtenerDesempenoRepartidores(
        String periodo, String desde, String hasta) {

    Usuario administrador = obtenerAdministradorConSucursal();
    Long sucursalId = administrador.getSucursal().getId();

    java.time.LocalDateTime[] rango = resolverRango(periodo, desde, hasta);
    java.time.LocalDateTime inicio = rango[0];
    java.time.LocalDateTime fin    = rango[1];

    List<DesempenoRepartidorDTO> agregados =
            entregaPedidoRepository.obtenerDesempenoPorSucursalYPeriodo(
                    sucursalId, inicio, fin);

    Comparator<DesempenoRepartidorDTO> porCantidad =
            Comparator.comparingLong(this::cantidadVentas)
                    .reversed()
                    .thenComparing(Comparator.comparingDouble(this::monto).reversed())
                    .thenComparing(this::nombre, String.CASE_INSENSITIVE_ORDER);

    Comparator<DesempenoRepartidorDTO> porMonto =
            Comparator.comparingDouble(this::monto)
                    .reversed()
                    .thenComparing(Comparator.comparingLong(this::cantidadVentas).reversed())
                    .thenComparing(this::nombre, String.CASE_INSENSITIVE_ORDER);

    List<DesempenoRepartidorDTO> rankingVentas = crearRanking(agregados, porCantidad);
    List<DesempenoRepartidorDTO> rankingMonto  = crearRanking(agregados, porMonto);

    long totalVentas = agregados.stream().mapToLong(this::cantidadVentas).sum();
    double montoTotal = agregados.stream().mapToDouble(this::monto).sum();

    DesempenoRepartidoresDTO respuesta = new DesempenoRepartidoresDTO();
    respuesta.setSucursalId(sucursalId);
    respuesta.setSucursalNombre(administrador.getSucursal().getNombre());
    respuesta.setTotalVentas(totalVentas);
    respuesta.setMontoTotal(montoTotal);
    respuesta.setRankingVentas(rankingVentas);
    respuesta.setRankingMonto(rankingMonto);
    respuesta.setPeriodoEtiqueta(etiquetaPeriodo(periodo, desde, hasta));
    respuesta.setPeriodoDesde(inicio.toLocalDate().toString());
    respuesta.setPeriodoHasta(fin.toLocalDate().toString());
    return respuesta;
}

private java.time.LocalDateTime[] resolverRango(String periodo, String desde, String hasta) {
    java.time.LocalDate hoy = java.time.LocalDate.now();
    switch (periodo == null ? "mes" : periodo) {
        case "hoy":
            return new java.time.LocalDateTime[]{
                hoy.atStartOfDay(),
                hoy.plusDays(1).atStartOfDay().minusSeconds(1)
            };
        case "semana":
            java.time.LocalDate lunes = hoy.with(
                java.time.DayOfWeek.MONDAY);
            return new java.time.LocalDateTime[]{
                lunes.atStartOfDay(),
                hoy.plusDays(1).atStartOfDay().minusSeconds(1)
            };
        case "mes_anterior":
            java.time.LocalDate primerMesAnt = hoy.minusMonths(1).withDayOfMonth(1);
            java.time.LocalDate ultimoMesAnt = primerMesAnt.withDayOfMonth(
                primerMesAnt.lengthOfMonth());
            return new java.time.LocalDateTime[]{
                primerMesAnt.atStartOfDay(),
                ultimoMesAnt.plusDays(1).atStartOfDay().minusSeconds(1)
            };
        case "personalizado":
            if (desde == null || hasta == null)
                throw new IllegalArgumentException("Se requieren fechas 'desde' y 'hasta'.");
            return new java.time.LocalDateTime[]{
                java.time.LocalDate.parse(desde).atStartOfDay(),
                java.time.LocalDate.parse(hasta).plusDays(1).atStartOfDay().minusSeconds(1)
            };
        default: // "mes"
            java.time.LocalDate primerMes = hoy.withDayOfMonth(1);
            return new java.time.LocalDateTime[]{
                primerMes.atStartOfDay(),
                hoy.plusDays(1).atStartOfDay().minusSeconds(1)
            };
    }
}

private String etiquetaPeriodo(String periodo, String desde, String hasta) {
    switch (periodo == null ? "mes" : periodo) {
        case "hoy": return "Hoy";
        case "semana": return "Esta semana";
        case "mes_anterior": return "Mes anterior";
        case "personalizado": return desde + " al " + hasta;
        default: return "Este mes";
    }
}


    private List<DesempenoRepartidorDTO> crearRanking(
            List<DesempenoRepartidorDTO> agregados,
            Comparator<DesempenoRepartidorDTO> comparador) {

        List<DesempenoRepartidorDTO> ranking = agregados.stream()
                .sorted(comparador)
                .map(item -> new DesempenoRepartidorDTO(
                        item.getRepartidorId(),
                        item.getNombre(),
                        cantidadVentas(item),
                        monto(item)))
                .collect(Collectors.toCollection(ArrayList::new));

        for (int i = 0; i < ranking.size(); i++) {
            ranking.get(i).setPosicion(i + 1);
        }
        return ranking;
    }

    private long cantidadVentas(DesempenoRepartidorDTO item) {
        return item.getCantidadVentas() != null ? item.getCantidadVentas() : 0L;
    }

    private double monto(DesempenoRepartidorDTO item) {
        return item.getMontoTotal() != null ? item.getMontoTotal() : 0.0;
    }

    private String nombre(DesempenoRepartidorDTO item) {
        return item.getNombre() != null ? item.getNombre() : "";
    }

    /** La sucursal se obtiene del JWT, nunca de un parámetro enviado por React. */
    private Usuario obtenerAdministradorConSucursal() {
        Authentication autenticacion = SecurityContextHolder.getContext().getAuthentication();
        if (autenticacion == null || !autenticacion.isAuthenticated()
                || autenticacion.getPrincipal() == null
                || "anonymousUser".equals(autenticacion.getPrincipal())) {
            throw new IllegalStateException("No hay un usuario autenticado.");
        }

        Usuario usuario = usuarioRepository.findByEmail(autenticacion.getName())
                .orElseThrow(() -> new IllegalStateException("Usuario autenticado no encontrado."));

        if (usuario.getSucursal() == null) {
            throw new IllegalArgumentException(
                    "Tu cuenta no tiene una sucursal asignada. Contacta al administrador.");
        }
        return usuario;
    }
}
