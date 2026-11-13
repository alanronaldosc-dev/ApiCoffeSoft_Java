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

    public DesempenoRepartidoresDTO obtenerDesempenoRepartidores() {
        Usuario administrador = obtenerAdministradorConSucursal();
        Long sucursalId = administrador.getSucursal().getId();

        List<DesempenoRepartidorDTO> agregados =
                entregaPedidoRepository.obtenerDesempenoPorSucursal(sucursalId);

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
        List<DesempenoRepartidorDTO> rankingMonto = crearRanking(agregados, porMonto);

        long totalVentas = agregados.stream()
                .mapToLong(this::cantidadVentas)
                .sum();
        double montoTotal = agregados.stream()
                .mapToDouble(this::monto)
                .sum();

        DesempenoRepartidoresDTO respuesta = new DesempenoRepartidoresDTO();
        respuesta.setSucursalId(sucursalId);
        respuesta.setSucursalNombre(administrador.getSucursal().getNombre());
        respuesta.setTotalVentas(totalVentas);
        respuesta.setMontoTotal(montoTotal);
        respuesta.setRankingVentas(rankingVentas);
        respuesta.setRankingMonto(rankingMonto);
        return respuesta;
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
