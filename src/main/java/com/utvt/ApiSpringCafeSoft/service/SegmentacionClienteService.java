package com.utvt.ApiSpringCafeSoft.service;

import com.utvt.ApiSpringCafeSoft.dto.ClienteSegmentoDTO;
import com.utvt.ApiSpringCafeSoft.dto.SegmentosResumenDTO;
import com.utvt.ApiSpringCafeSoft.model.SegmentoCliente;
import com.utvt.ApiSpringCafeSoft.model.Usuario;
import com.utvt.ApiSpringCafeSoft.repository.ClienteCompraStatsProjection;
import com.utvt.ApiSpringCafeSoft.repository.UsuarioRepository;
import com.utvt.ApiSpringCafeSoft.repository.VentaRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
public class SegmentacionClienteService {

    private final UsuarioRepository usuarioRepository;
    private final VentaRepository ventaRepository;

    public SegmentacionClienteService(
            UsuarioRepository usuarioRepository,
            VentaRepository ventaRepository) {
        this.usuarioRepository = usuarioRepository;
        this.ventaRepository = ventaRepository;
    }

    @Transactional(readOnly = true)
    public List<ClienteSegmentoDTO> obtenerClientesSegmentados() {
        LocalDateTime ahora = ahora();
        Map<Long, ClienteCompraStatsProjection> estadisticas = estadisticasPorCliente();

        return usuarioRepository.findByUserTipoAndActivo(2, true).stream()
                .sorted((primero, segundo) ->
                        String.CASE_INSENSITIVE_ORDER.compare(
                                primero.getNombre(), segundo.getNombre()))
                .map(cliente -> toDto(cliente, estadisticas.get(cliente.getId()), ahora))
                .toList();
    }

    @Transactional(readOnly = true)
    public SegmentosResumenDTO obtenerResumen() {
        List<ClienteSegmentoDTO> clientes = obtenerClientesSegmentados();
        Map<SegmentoCliente, Long> conteos = new EnumMap<>(SegmentoCliente.class);
        for (SegmentoCliente segmento : SegmentoCliente.values()) {
            if (segmento != SegmentoCliente.TODOS) {
                conteos.put(segmento, 0L);
            }
        }
        clientes.forEach(cliente ->
                conteos.compute(cliente.segmento(), (segmento, total) -> total + 1));
        return new SegmentosResumenDTO(clientes.size(), conteos);
    }

    @Transactional(readOnly = true)
    public SegmentoCliente obtenerSegmento(Long clienteId) {
        Usuario cliente = obtenerClienteActivo(clienteId);
        ClienteCompraStatsProjection estadistica =
                estadisticasPorCliente().get(cliente.getId());
        return clasificar(
                estadistica == null ? 0L : estadistica.getCompras(),
                estadistica == null ? null : estadistica.getUltimaCompra(),
                ahora());
    }

    @Transactional(readOnly = true)
    public Usuario obtenerClienteActivo(Long clienteId) {
        Usuario cliente = usuarioRepository.findById(clienteId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Cliente no encontrado."));
        if (!Integer.valueOf(2).equals(cliente.getUserTipo())) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST, "El usuario indicado no es un cliente.");
        }
        if (!Boolean.TRUE.equals(cliente.getActivo())) {
            throw new ResponseStatusException(
                    HttpStatus.FORBIDDEN, "El cliente está inactivo.");
        }
        return cliente;
    }

    private Map<Long, ClienteCompraStatsProjection> estadisticasPorCliente() {
        return ventaRepository.findCustomerPurchaseStats().stream()
                .collect(Collectors.toMap(
                        ClienteCompraStatsProjection::getClienteId,
                        Function.identity()));
    }

    private ClienteSegmentoDTO toDto(
            Usuario cliente,
            ClienteCompraStatsProjection estadistica,
            LocalDateTime ahora) {
        long compras = estadistica == null ? 0L : estadistica.getCompras();
        LocalDateTime ultimaCompra =
                estadistica == null ? null : estadistica.getUltimaCompra();
        return new ClienteSegmentoDTO(
                cliente.getId(),
                cliente.getNombre(),
                clasificar(compras, ultimaCompra, ahora),
                compras,
                ultimaCompra);
    }

    SegmentoCliente clasificar(
            long compras,
            LocalDateTime ultimaCompra,
            LocalDateTime ahora) {
        if (compras == 0 || ultimaCompra == null) {
            return SegmentoCliente.POTENCIAL;
        }
        if (ultimaCompra.isBefore(ahora.minusDays(90))) {
            return SegmentoCliente.DORMIDO;
        }
        if (compras >= 3 && !ultimaCompra.isBefore(ahora.minusDays(30))) {
            return SegmentoCliente.ACTIVO;
        }
        return SegmentoCliente.POCO_FRECUENTE;
    }

    private LocalDateTime ahora() {
        return LocalDateTime.now(ZoneId.systemDefault());
    }
}
