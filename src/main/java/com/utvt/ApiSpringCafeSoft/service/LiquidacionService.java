package com.utvt.ApiSpringCafeSoft.service;
import com.utvt.ApiSpringCafeSoft.dto.LiquidacionDTO;
import com.utvt.ApiSpringCafeSoft.dto.RegistrarLiquidacionDTO;
import com.utvt.ApiSpringCafeSoft.dto.ResumenLiquidacionDTO;
import com.utvt.ApiSpringCafeSoft.model.Carga;
import com.utvt.ApiSpringCafeSoft.model.EntregaPedido;
import com.utvt.ApiSpringCafeSoft.model.EntregaGarrafonDetalle;
import com.utvt.ApiSpringCafeSoft.model.Inventario;
import com.utvt.ApiSpringCafeSoft.model.Liquidacion;
import com.utvt.ApiSpringCafeSoft.model.Ruta;
import com.utvt.ApiSpringCafeSoft.model.Usuario;
import com.utvt.ApiSpringCafeSoft.repository.CargaRepository;
import com.utvt.ApiSpringCafeSoft.repository.EntregaPedidoRepository;
import com.utvt.ApiSpringCafeSoft.repository.EntregaGarrafonDetalleRepository;
import com.utvt.ApiSpringCafeSoft.repository.InventarioRepository;
import com.utvt.ApiSpringCafeSoft.repository.LiquidacionRepository;
import com.utvt.ApiSpringCafeSoft.repository.RutaRepository;
import com.utvt.ApiSpringCafeSoft.repository.UsuarioRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
/**
 * HU-016:
 * Resumen y cierre por ruta.
 *
 * Los garrafones llenos no vendidos regresan
 * automáticamente al inventario de planta
 * correspondiente a cada carga.
 *
 * Los envases vacíos recibidos durante la ruta
 * se incorporan al inventario al liquidar la ruta.
 */
@Service
public class LiquidacionService {
    @Autowired
    private LiquidacionRepository liquidacionRepository;
    @Autowired
    private UsuarioRepository usuarioRepository;
    @Autowired
    private RutaRepository rutaRepository;
    @Autowired
    private CargaRepository cargaRepository;
    @Autowired
    private EntregaPedidoRepository entregaPedidoRepository;
    @Autowired
    private EntregaGarrafonDetalleRepository entregaGarrafonDetalleRepository;
    @Autowired
    private InventarioRepository inventarioRepository;
    // =====================================================
    // RESUMEN POR RUTA
    // =====================================================
    public ResumenLiquidacionDTO obtenerResumen(Long rutaId) {
        Ruta ruta = obtenerRuta(rutaId);
        if (ruta.getRepartidor() == null) {
            throw new RuntimeException(
                    "La ruta no tiene un repartidor asignado"
            );
        }
        if (!"TERMINADA".equalsIgnoreCase(ruta.getEstado())) {
            throw new RuntimeException(
                    "La ruta debe estar TERMINADA antes de realizar la liquidación"
            );
        }
        Usuario repartidor =
                obtenerRepartidor(
                        ruta.getRepartidor().getId()
                );
        // ============================================
        // CARGAS DE LA RUTA
        // ============================================
        List<Carga> cargas =
                cargaRepository
                        .findByRutaIdOrderByFechaHoraAsc(
                                ruta.getId()
                        );
        if (cargas.isEmpty()) {
            throw new RuntimeException(
                    "La ruta no tiene cargas registradas"
            );
        }
        // ============================================
        // ENTREGAS DE LA RUTA
        // ============================================
        List<EntregaPedido> entregas =
                entregaPedidoRepository
                        .findByRutaIdOrderByFechaAsc(
                                ruta.getId()
                        );
        List<EntregaPedido> entregasCompletadas =
                entregas.stream()
                        .filter(e ->
                                "ENTREGADO".equalsIgnoreCase(
                                        e.getResultado()
                                )
                        )
                        .toList();
        // ============================================
        // CARGA INICIAL
        // ============================================
        double cargaInicial =
                cargas.stream()
                        .mapToDouble(c ->
                                nvl(c.getCantidad())
                        )
                        .sum();
        // ============================================
        // GARRAFONES ENTREGADOS
        // ============================================
        double garrafonesEntregados =
                entregasCompletadas.stream()
                        .mapToDouble(e ->
                                nvl(
                                        e.getGarrafonesEntregados()
                                )
                        )
                        .sum();
        // ============================================
        // SOBRANTES REALES
        // ============================================
        /*
         * IMPORTANTE:
         *
         * Ya no calculamos los pendientes solamente
         * con:
         *
         * cargaInicial - vendidos
         *
         * Ahora tomamos la cantidadDisponible REAL
         * de cada carga.
         */
        double garrafonesPendientes =
                cargas.stream()
                        .mapToDouble(
                                this::cantidadDisponible
                        )
                        .sum();
        // ============================================
        // ENVASES VACÍOS
        // ============================================
        double envasesVacios =
                entregasCompletadas.stream()
                        .mapToDouble(e ->
                                nvl(
                                        e.getEnvasesVaciosRecibidos()
                                )
                        )
                        .sum();
        // ============================================
        // EFECTIVO
        // ============================================
        double efectivo =
                entregasCompletadas.stream()
                        .filter(e ->
                                "EFECTIVO".equalsIgnoreCase(
                                        e.getMetodoCobro()
                                )
                        )
                        .mapToDouble(e ->
                                nvl(
                                        e.getMontoCobrado()
                                )
                        )
                        .sum();
        // ============================================
        // TRANSFERENCIAS
        // ============================================
        double transferencias =
                entregasCompletadas.stream()
                        .filter(e ->
                                "TRANSFERENCIA".equalsIgnoreCase(
                                        e.getMetodoCobro()
                                )
                        )
                        .mapToDouble(e ->
                                nvl(
                                        e.getMontoCobrado()
                                )
                        )
                        .sum();
        // ============================================
        // FECHA
        // ============================================
        LocalDate fecha =
                LocalDate.now();
        if (!entregasCompletadas.isEmpty()
                && entregasCompletadas
                .get(0)
                .getFecha() != null) {
            fecha =
                    entregasCompletadas
                            .get(0)
                            .getFecha()
                            .toLocalDate();
        }
        // ============================================
        // DTO
        // ============================================
        ResumenLiquidacionDTO dto =
                new ResumenLiquidacionDTO();
        dto.setRepartidorId(
                repartidor.getId()
        );
        dto.setRepartidorNombre(
                repartidor.getNombre()
        );
        dto.setRutaId(
                ruta.getId()
        );
        dto.setRutaNombre(
                ruta.getNombre()
        );
        dto.setFecha(
                fecha
        );
        dto.setCargaInicial(
                cargaInicial
        );
        dto.setGarrafonesEntregados(
                garrafonesEntregados
        );
        dto.setGarrafonesPendientes(
                garrafonesPendientes
        );
        dto.setEnvasesVaciosRecibidos(
                envasesVacios
        );
        dto.setTotalEfectivo(
                efectivo
        );
        dto.setTotalTransferencias(
                transferencias
        );
        dto.setTotalCobrado(
                efectivo + transferencias
        );
        dto.setEntregasRealizadas(
                entregasCompletadas.size()
        );
        return dto;
    }
    // =====================================================
    // REGISTRAR LIQUIDACIÓN
    // =====================================================
    @Transactional
    public LiquidacionDTO registrarLiquidacion(
            RegistrarLiquidacionDTO request) {
        Ruta ruta =
                obtenerRuta(
                        request.getRutaId()
                );
        // ============================================
        // VALIDAR RUTA
        // ============================================
        if (!"TERMINADA".equalsIgnoreCase(
                ruta.getEstado()
        )) {
            throw new RuntimeException(
                    "La ruta debe estar TERMINADA antes de liquidarla"
            );
        }
        if (ruta.getRepartidor() == null) {
            throw new RuntimeException(
                    "La ruta no tiene repartidor asignado"
            );
        }
        // ============================================
        // VALIDAR REPARTIDOR
        // ============================================
        Usuario repartidor =
                obtenerRepartidor(
                        request.getRepartidorId()
                );
        if (!ruta.getRepartidor()
                .getId()
                .equals(
                        repartidor.getId()
                )) {
            throw new RuntimeException(
                    "La ruta no pertenece al repartidor seleccionado"
            );
        }
        // ============================================
        // EVITAR DOBLE LIQUIDACIÓN
        // ============================================
        if (liquidacionRepository
                .existsByRutaId(
                        ruta.getId()
                )) {
            throw new RuntimeException(
                    "Esta ruta ya cuenta con una liquidación registrada"
            );
        }
        // ============================================
        // RESUMEN
        // ============================================
        ResumenLiquidacionDTO resumen =
                obtenerResumen(
                        ruta.getId()
                );
        LocalDate fecha =
                request.getFecha() != null
                        ? request.getFecha()
                        : resumen.getFecha();
        if (fecha == null) {
            fecha =
                    LocalDate.now();
        }
        double efectivoEntregado =
                nvl(
                        request.getEfectivoEntregado()
                );
        // ============================================
        // DEVOLVER SOBRANTES AUTOMÁTICAMENTE
        // ============================================
        /*
         * Ya NO usamos como fuente real:
         *
         * request.getGarrafonesNoVendidosDevueltos()
         *
         * El sistema revisa cada carga y devuelve
         * exactamente su cantidadDisponible.
         */
        double devueltos =
                regresarGarrafonesAPlanta(
                        ruta.getId()
                );
        // ============================================
        // DEVOLVER ENVASES VACÍOS DE LA RUTA
        // ============================================
        double vaciosDevueltos =
                regresarEnvasesVaciosAPlanta(
                        ruta.getId()
                );
        System.out.println(
                "Envases vacíos regresados al inventario: "
                        + vaciosDevueltos
        );
        // ============================================
        // DIFERENCIAS
        // ============================================
        double diferenciaGarrafones =
                resumen.getCargaInicial()
                        - resumen.getGarrafonesEntregados()
                        - devueltos;
        double diferenciaEfectivo =
                resumen.getTotalEfectivo()
                        - efectivoEntregado;
        boolean conforme =
                Math.abs(
                        diferenciaGarrafones
                ) < 0.0001
                        &&
                Math.abs(
                        diferenciaEfectivo
                ) < 0.01;
        // ============================================
        // CREAR LIQUIDACIÓN
        // ============================================
        Liquidacion liquidacion =
                new Liquidacion();
        liquidacion.setRepartidor(
                repartidor
        );
        liquidacion.setRuta(
                ruta
        );
        liquidacion.setFechaOperacion(
                fecha
        );
        liquidacion.setFechaCierre(
                LocalDateTime.now()
        );
        liquidacion.setCargaInicial(
                resumen.getCargaInicial()
        );
        liquidacion.setGarrafonesEntregados(
                resumen.getGarrafonesEntregados()
        );
        /*
         * Guardamos exactamente la cantidad
         * que realmente regresó al inventario.
         */
        liquidacion.setGarrafonesNoVendidosDevueltos(
                devueltos
        );
        liquidacion.setTotalEfectivo(
                resumen.getTotalEfectivo()
        );
        liquidacion.setTotalTransferencias(
                resumen.getTotalTransferencias()
        );
        liquidacion.setEfectivoEntregado(
                efectivoEntregado
        );
        liquidacion.setDiferenciaGarrafones(
                diferenciaGarrafones
        );
        liquidacion.setDiferenciaEfectivo(
                diferenciaEfectivo
        );
        liquidacion.setEstado(
                conforme
                        ? "CONFORME"
                        : "DIFERENCIA REGISTRADA"
        );
        liquidacion.setObservaciones(
                request.getObservaciones()
        );
        // ============================================
        // RUTA LIQUIDADA
        // ============================================
        ruta.setActiva(
                false
        );
        ruta.setEstado(
                "LIQUIDADA"
        );
        rutaRepository.save(
                ruta
        );
        // ============================================
        // GUARDAR
        // ============================================
        Liquidacion guardada =
                liquidacionRepository.save(
                        liquidacion
                );
        return convertirDTO(
                guardada
        );
    }
    // =====================================================
    // REGRESAR SOBRANTES A PLANTA
    // =====================================================
    /**
     * Regresa automáticamente todos los garrafones
     * llenos sobrantes al inventario correspondiente.
     *
     * Ejemplo:
     *
     * Carga Ciel:
     * cantidadDisponible = 3
     * -> +3 Inventario Ciel
     *
     * Carga Bonafont:
     * cantidadDisponible = 5
     * -> +5 Inventario Bonafont
     *
     * Devuelve el total de garrafones regresados.
     */
    private double regresarGarrafonesAPlanta(
            Long rutaId) {
        List<Carga> cargas =
                cargaRepository
                        .findByRutaIdOrderByFechaHoraAsc(
                                rutaId
                        );
        double totalDevuelto =
                0.0;
        for (Carga carga :
                cargas) {
            // ========================================
            // DISPONIBLE REAL DE ESA CARGA
            // ========================================
            double disponible =
                    cantidadDisponible(
                            carga
                    );
            // ========================================
            // DEVOLVER AL INVENTARIO CORRECTO
            // ========================================
            if (disponible > 0.0001) {
                if (carga.getInventario() == null) {
                    throw new RuntimeException(
                            "La carga "
                                    + carga.getId()
                                    + " no tiene inventario asociado"
                    );
                }
                Inventario inventario =
                        carga.getInventario();
                double cantidadActual =
                        nvl(
                                inventario.getCantidad()
                        );
                inventario.setCantidad(
                        cantidadActual
                                + disponible
                );
                inventarioRepository.save(
                        inventario
                );
                totalDevuelto +=
                        disponible;
            }
            // ========================================
            // CERRAR CARGA
            // ========================================
            /*
             * Ya regresó todo lo disponible.
             */
            carga.setCantidadDisponible(
                    0.0
            );
            carga.setEstado(
                    "LIQUIDADA"
            );
            cargaRepository.save(
                    carga
            );
        }
        return totalDevuelto;
    }
    // =====================================================
    // REGRESAR ENVASES VACÍOS A PLANTA
    // =====================================================
    /**
     * Recorre todas las entregas completadas de la ruta
     * y suma al inventario correspondiente cada detalle
     * marcado como DEVUELTO.
     *
     * Los envases se registran durante la venta, pero
     * físicamente pasan al inventario de planta hasta
     * que se realiza la liquidación de la ruta.
     */
    private double regresarEnvasesVaciosAPlanta(
            Long rutaId) {
        List<EntregaPedido> entregas =
                entregaPedidoRepository
                        .findByRutaIdOrderByFechaAsc(
                                rutaId
                        );
        double totalVaciosDevueltos =
                0.0;
        for (EntregaPedido entrega :
                entregas) {
            if (!"ENTREGADO".equalsIgnoreCase(
                    entrega.getResultado()
            )) {
                continue;
            }
            List<EntregaGarrafonDetalle> detalles =
                    entregaGarrafonDetalleRepository
                            .findByEntregaPedidoId(
                                    entrega.getId()
                            );
            for (EntregaGarrafonDetalle detalle :
                    detalles) {
                if (!"DEVUELTO".equalsIgnoreCase(
                        detalle.getTipoMovimiento()
                )) {
                    continue;
                }
                if (detalle.getInventario() == null) {
                    continue;
                }
                double cantidad =
                        detalle.getCantidad() != null
                                ? detalle.getCantidad()
                                : 0.0;
                if (cantidad <= 0.0001) {
                    continue;
                }
                Inventario inventario =
                        detalle.getInventario();
                inventario.setCantidad(
                        nvl(
                                inventario.getCantidad()
                        )
                                + cantidad
                );
                inventarioRepository.save(
                        inventario
                );
                totalVaciosDevueltos +=
                        cantidad;
            }
        }
        return totalVaciosDevueltos;
    }
    // =====================================================
    // CANTIDAD DISPONIBLE REAL
    // =====================================================
    private double cantidadDisponible(
            Carga carga) {
        if (carga.getCantidadDisponible() != null) {
            return Math.max(
                    0.0,
                    carga.getCantidadDisponible()
            );
        }
        /*
         * Para cargas antiguas donde todavía
         * no existía cantidadDisponible.
         */
        return Math.max(
                0.0,
                nvl(
                        carga.getCantidad()
                )
        );
    }
    // =====================================================
    // OBTENER RUTA
    // =====================================================
    private Ruta obtenerRuta(
            Long rutaId) {
        if (rutaId == null) {
            throw new RuntimeException(
                    "La ruta es obligatoria"
            );
        }
        return rutaRepository
                .findById(
                        rutaId
                )
                .orElseThrow(() ->
                        new RuntimeException(
                                "Ruta no encontrada con ID: "
                                        + rutaId
                        )
                );
    }
    // =====================================================
    // OBTENER REPARTIDOR
    // =====================================================
    private Usuario obtenerRepartidor(
            Long id) {
        Usuario repartidor =
                usuarioRepository
                        .findById(id)
                        .orElseThrow(() ->
                                new RuntimeException(
                                        "Repartidor no encontrado con ID: "
                                                + id
                                )
                        );
        if (repartidor.getUserTipo() == null
                || repartidor.getUserTipo() != 4) {
            throw new RuntimeException(
                    "El usuario seleccionado no tiene el rol de Repartidor"
            );
        }
        return repartidor;
    }
    // =====================================================
    // NULL -> 0
    // =====================================================
    private double nvl(
            Double value) {
        return value != null
                ? value
                : 0.0;
    }
    // =====================================================
    // CONVERTIR DTO
    // =====================================================
    private LiquidacionDTO convertirDTO(
            Liquidacion l) {
        LiquidacionDTO dto =
                new LiquidacionDTO();
        dto.setId(
                l.getId()
        );
        dto.setRepartidorId(
                l.getRepartidor() != null
                        ? l.getRepartidor().getId()
                        : null
        );
        dto.setRepartidorNombre(
                l.getRepartidor() != null
                        ? l.getRepartidor().getNombre()
                        : null
        );
        dto.setRutaId(
                l.getRuta() != null
                        ? l.getRuta().getId()
                        : null
        );
        dto.setRutaNombre(
                l.getRuta() != null
                        ? l.getRuta().getNombre()
                        : null
        );
        dto.setFechaOperacion(
                l.getFechaOperacion()
        );
        dto.setFechaCierre(
                l.getFechaCierre()
        );
        dto.setCargaInicial(
                l.getCargaInicial()
        );
        dto.setGarrafonesEntregados(
                l.getGarrafonesEntregados()
        );
        dto.setGarrafonesNoVendidosDevueltos(
                l.getGarrafonesNoVendidosDevueltos()
        );
        dto.setTotalEfectivo(
                l.getTotalEfectivo()
        );
        dto.setTotalTransferencias(
                l.getTotalTransferencias()
        );
        dto.setEfectivoEntregado(
                l.getEfectivoEntregado()
        );
        dto.setDiferenciaGarrafones(
                l.getDiferenciaGarrafones()
        );
        dto.setDiferenciaEfectivo(
                l.getDiferenciaEfectivo()
        );
        dto.setEstado(
                l.getEstado()
        );
        dto.setObservaciones(
                l.getObservaciones()
        );
        return dto;
    }
}
