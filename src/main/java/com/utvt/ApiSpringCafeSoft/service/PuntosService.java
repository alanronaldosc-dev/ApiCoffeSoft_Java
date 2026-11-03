package com.utvt.ApiSpringCafeSoft.service;

import com.utvt.ApiSpringCafeSoft.dto.ClientePuntosDTO;
import com.utvt.ApiSpringCafeSoft.dto.CotizacionCanjeDTO;
import com.utvt.ApiSpringCafeSoft.dto.CotizacionCanjeRequestDTO;
import com.utvt.ApiSpringCafeSoft.dto.MovimientoPuntosDTO;
import com.utvt.ApiSpringCafeSoft.dto.PuntosResumenDTO;
import com.utvt.ApiSpringCafeSoft.model.Usuario;
import com.utvt.ApiSpringCafeSoft.repository.UsuarioRepository;
import com.utvt.ApiSpringCafeSoft.repository.VentaRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

@Service
public class PuntosService {

    private static final BigDecimal PUNTOS_POR_PESO = BigDecimal.valueOf(5);

    private final UsuarioRepository usuarioRepository;
    private final VentaRepository ventaRepository;

    public PuntosService(
            UsuarioRepository usuarioRepository,
            VentaRepository ventaRepository) {
        this.usuarioRepository = usuarioRepository;
        this.ventaRepository = ventaRepository;
    }

    @Transactional(readOnly = true)
    public List<ClientePuntosDTO> listarClientesActivos() {
        return usuarioRepository.findByUserTipoAndActivo(2, true).stream()
                .map(cliente -> new ClientePuntosDTO(
                        cliente.getId(),
                        cliente.getNombre(),
                        cliente.getActivo(),
                        validarSaldo(cliente)))
                .toList();
    }

    @Transactional(readOnly = true)
    public PuntosResumenDTO obtenerResumen(Long clienteId) {
        Usuario cliente = obtenerCliente(clienteId);
        List<MovimientoPuntosDTO> movimientos = ventaRepository
                .findByCliente_IdAndPuntosCanjeadosGreaterThanOrderByFechaDesc(
                        clienteId, 0)
                .stream()
                .map(venta -> new MovimientoPuntosDTO(
                        venta.getId(), -venta.getPuntosCanjeados()))
                .toList();
        return new PuntosResumenDTO(
                cliente.getNombre(), validarSaldo(cliente), movimientos);
    }

    @Transactional(readOnly = true)
    public CotizacionCanjeDTO cotizar(
            Long clienteId,
            CotizacionCanjeRequestDTO solicitud) {
        if (solicitud == null) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST, "La solicitud de canje es obligatoria.");
        }
        Usuario cliente = obtenerCliente(clienteId);
        return crearCotizacion(
                solicitud.getPuntosSolicitados(),
                solicitud.getTotalCompra(),
                validarSaldo(cliente));
    }

    @Transactional
    public CanjeAplicado aplicarCanje(
            Long clienteId,
            Integer puntosSolicitados,
            BigDecimal totalCompra) {
        if (clienteId == null) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST, "Selecciona un cliente para validar el canje.");
        }
        Usuario cliente = usuarioRepository.findByIdForUpdate(clienteId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Cliente no encontrado."));
        validarCliente(cliente);
        CotizacionCanjeDTO cotizacion = crearCotizacion(
                puntosSolicitados, totalCompra, validarSaldo(cliente));

        int saldoRestante = cliente.getSaldoPuntos() - cotizacion.getPuntosCanjeados();
        cliente.setSaldoPuntos(saldoRestante);
        usuarioRepository.save(cliente);

        return new CanjeAplicado(
                cotizacion.getPuntosCanjeados(),
                cotizacion.getDescuento(),
                saldoRestante,
                cliente);
    }

    private Usuario obtenerCliente(Long clienteId) {
        Usuario cliente = usuarioRepository.findById(clienteId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Cliente no encontrado."));
        validarCliente(cliente);
        return cliente;
    }

    private void validarCliente(Usuario cliente) {
        if (!Integer.valueOf(2).equals(cliente.getUserTipo())) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST, "El usuario indicado no es un cliente.");
        }
        if (!Boolean.TRUE.equals(cliente.getActivo())) {
            throw new ResponseStatusException(
                    HttpStatus.FORBIDDEN, "El cliente está inactivo.");
        }
    }

    private int validarSaldo(Usuario cliente) {
        Integer saldo = cliente.getSaldoPuntos();
        if (saldo == null || saldo < 0) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT, "El saldo de puntos del cliente no está inicializado.");
        }
        return saldo;
    }

    private CotizacionCanjeDTO crearCotizacion(
            Integer puntosSolicitados,
            BigDecimal totalCompra,
            int saldoPuntos) {
        if (puntosSolicitados == null || puntosSolicitados <= 0) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST, "Solicita una cantidad positiva de puntos.");
        }
        if (totalCompra == null || totalCompra.signum() < 0) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST, "El total de compra no puede ser negativo.");
        }
        if (puntosSolicitados > saldoPuntos) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST, "El saldo de puntos es insuficiente.");
        }

        int puntosDisponiblesPorCompra = totalCompra
                .multiply(PUNTOS_POR_PESO)
                .setScale(0, RoundingMode.DOWN)
                .min(BigDecimal.valueOf(Integer.MAX_VALUE))
                .intValueExact();
        int puntosCanjeados = Math.min(puntosSolicitados, puntosDisponiblesPorCompra);
        if (puntosCanjeados == 0) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "El total de compra no permite canjear puntos.");
        }
        BigDecimal descuento = BigDecimal.valueOf(puntosCanjeados)
                .divide(PUNTOS_POR_PESO, 2, RoundingMode.UNNECESSARY);
        return new CotizacionCanjeDTO(puntosCanjeados, descuento);
    }

    public record CanjeAplicado(
            int puntosCanjeados,
            BigDecimal descuento,
            int saldoPuntosRestante,
            Usuario cliente) {
    }
}
