package com.utvt.ApiSpringCafeSoft.service;

import com.utvt.ApiSpringCafeSoft.dto.CotizacionCanjeDTO;
import com.utvt.ApiSpringCafeSoft.model.Cliente;
import com.utvt.ApiSpringCafeSoft.repository.ClienteRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.math.RoundingMode;

/** HU-023: cotiza beneficios y consume puntos de forma segura al completar una venta. */
@Service
public class BeneficiosService {

    private final ClienteRepository clienteRepository;
    private final int puntosPorPeso;

    public BeneficiosService(
            ClienteRepository clienteRepository,
            @Value("${cafesoft.fidelizacion.puntos-por-peso:5}") int puntosPorPeso) {
        if (puntosPorPeso <= 0) {
            throw new IllegalStateException("Los puntos por peso deben ser mayores a cero");
        }
        this.clienteRepository = clienteRepository;
        this.puntosPorPeso = puntosPorPeso;
    }

    @Transactional(readOnly = true)
    public CotizacionCanjeDTO cotizar(Long clienteId, CotizacionCanjeDTO solicitud) {
        Cliente cliente = clienteRepository.findById(clienteId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Cliente no encontrado"));
        validarClienteActivo(cliente);
        validarSaldo(cliente, solicitud.getPuntosSolicitados());

        int puntosCanjeados = puntosCanjeables(
                solicitud.getPuntosSolicitados(), solicitud.getTotalCompra());
        if (puntosCanjeados <= 0) {
            throw new IllegalArgumentException("El total de compra no permite aplicar puntos");
        }

        solicitud.setPuntosCanjeados(puntosCanjeados);
        solicitud.setDescuento(descuentoPorPuntos(puntosCanjeados));
        return solicitud;
    }

    @Transactional
    public int consumir(Long clienteId, Integer puntosSolicitados, Double descuentoSolicitado,
                        double totalCompra) {
        if (clienteId == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Se requiere un cliente para canjear puntos");
        }
        Cliente cliente = clienteRepository.findWithLockById(clienteId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Cliente no encontrado"));
        validarClienteActivo(cliente);
        validarSaldo(cliente, puntosSolicitados);

        int puntosCanjeados = puntosCanjeables(puntosSolicitados, totalCompra);
        double descuentoEsperado = descuentoPorPuntos(puntosCanjeados);
        if (puntosCanjeados != puntosSolicitados ||
                descuentoSolicitado == null ||
                BigDecimal.valueOf(descuentoSolicitado)
                        .setScale(2, RoundingMode.HALF_UP)
                        .compareTo(BigDecimal.valueOf(descuentoEsperado)) != 0) {
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                    "La cotización cambió; vuelve a cotizar el canje antes de cobrar");
        }

        int saldoRestante = cliente.getSaldoPuntos() - puntosCanjeados;
        cliente.setSaldoPuntos(saldoRestante);
        clienteRepository.save(cliente);
        return saldoRestante;
    }

    private int puntosCanjeables(int puntosSolicitados, double totalCompra) {
        BigDecimal total = BigDecimal.valueOf(totalCompra).setScale(2, RoundingMode.HALF_UP);
        if (total.signum() <= 0) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "El total de compra debe ser mayor a cero");
        }
        long limitePorTotal = total.multiply(BigDecimal.valueOf(puntosPorPeso))
                .setScale(0, RoundingMode.DOWN)
                .longValueExact();
        return (int) Math.min(puntosSolicitados, limitePorTotal);
    }

    private double descuentoPorPuntos(int puntosCanjeados) {
        return BigDecimal.valueOf(puntosCanjeados)
                .divide(BigDecimal.valueOf(puntosPorPeso), 2, RoundingMode.DOWN)
                .doubleValue();
    }

    private void validarSaldo(Cliente cliente, Integer puntosSolicitados) {
        if (puntosSolicitados == null || puntosSolicitados <= 0) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Los puntos solicitados deben ser mayores a cero");
        }
        if (cliente.getSaldoPuntos() == null || cliente.getSaldoPuntos() < puntosSolicitados) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "El cliente no tiene puntos suficientes");
        }
    }

    private void validarClienteActivo(Cliente cliente) {
        if (!Boolean.TRUE.equals(cliente.getActivo())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "El cliente está inactivo");
        }
    }
}
