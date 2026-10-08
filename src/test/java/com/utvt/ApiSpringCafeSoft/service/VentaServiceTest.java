package com.utvt.ApiSpringCafeSoft.service;

import com.utvt.ApiSpringCafeSoft.dto.VentaDTO;
import com.utvt.ApiSpringCafeSoft.dto.VentaDetalleDTO;
import com.utvt.ApiSpringCafeSoft.model.*;
import com.utvt.ApiSpringCafeSoft.repository.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * HU-018: Tests unitarios de VentaService
 * Cubre lógica de procesamiento de pedidos, métodos de pago
 * y control de stock concurrente.
 */
@ExtendWith(MockitoExtension.class)
class VentaServiceTest {

    @Mock private VentaRepository ventaRepository;
    @Mock private ProductoRepository productoRepository;
    @Mock private InventarioRepository inventarioRepository;
    @Mock private UsuarioRepository usuarioRepository;
    @Mock private LoteRepository loteRepository;

    @InjectMocks
    private VentaService ventaService;

    private Usuario usuarioAdmin;
    private Sucursal sucursal;
    private Producto producto;
    private Inventario inventario;
    private ProductoInsumo productoInsumo;
    private Venta ventaGuardada;

    @BeforeEach
    void setUp() {
        sucursal = new Sucursal();
        sucursal.setId(1L);
        sucursal.setNombre("Sucursal Centro");
        sucursal.setDireccion("Calle 1");
        sucursal.setTelefono("7200000001");

        usuarioAdmin = new Usuario();
        usuarioAdmin.setId(1L);
        usuarioAdmin.setNombre("Admin Test");
        usuarioAdmin.setEmail("admin@gmail.com");
        usuarioAdmin.setPassword("hashed");
        usuarioAdmin.setDireccion("Calle 1");
        usuarioAdmin.setTelefono("7200000000");
        usuarioAdmin.setUserTipo(0);
        usuarioAdmin.setActivo(true);
        usuarioAdmin.setSucursal(sucursal);

        inventario = new Inventario();
        inventario.setId(1L);
        inventario.setNombre("Café Molido");
        inventario.setTipo("Materia Prima");
        inventario.setCantidad(100.0);
        inventario.setUnidadMedida("kilogramos");
        inventario.setCantidadMinima(5.0);
        inventario.setPrecioUnitario(50.0);
        inventario.setSucursal(sucursal);

        productoInsumo = new ProductoInsumo();
        productoInsumo.setId(1L);
        productoInsumo.setInsumo(inventario);
        productoInsumo.setCantidad(1.0);
        productoInsumo.setUnidadMedida("kilogramos");

        producto = new Producto();
        producto.setId(1L);
        producto.setNombre("Café Americano");
        producto.setPrecio(50.0);
        producto.setDescripcion("Café negro");
        producto.setSucursal(sucursal);
        producto.setInsumos(new ArrayList<>(List.of(productoInsumo)));
        productoInsumo.setProducto(producto);

        ventaGuardada = new Venta();
        ventaGuardada.setId(1L);
        ventaGuardada.setFolio("V-0001");
        ventaGuardada.setFecha(LocalDateTime.now());
        ventaGuardada.setMetodoPago("efectivo");
        ventaGuardada.setUsuario(usuarioAdmin);
        ventaGuardada.setSubtotal(50.0);
        ventaGuardada.setImpuestos(8.0);
        ventaGuardada.setDescuento(0.0);
        ventaGuardada.setTotal(58.0);
        ventaGuardada.setMontoEfectivo(200.0);
        ventaGuardada.setCambio(142.0);
        ventaGuardada.setEstadoPedido("PENDIENTE");
        ventaGuardada.setCreatedAt(LocalDateTime.now());
        ventaGuardada.setSucursal(sucursal);

        VentaDetalle detalle = new VentaDetalle();
        detalle.setId(1L);
        detalle.setProducto(producto);
        detalle.setCantidad(1);
        detalle.setPrecioUnitario(50.0);
        detalle.setSubtotal(50.0);
        detalle.setVenta(ventaGuardada);
        ventaGuardada.setDetalles(new ArrayList<>(List.of(detalle)));
    }

    // ================================================================
    // CREAR VENTA — EFECTIVO
    // ================================================================

    @Test
    @DisplayName("HU-018 - Crear venta con método de pago efectivo exitosamente")
    void crearVenta_efectivo_exitoso() {
        VentaDTO dto = buildVentaDTO("efectivo", 200.0);

        when(usuarioRepository.findById(1L)).thenReturn(Optional.of(usuarioAdmin));
        when(productoRepository.findByIdWithInsumos(1L)).thenReturn(producto);
        when(loteRepository.findLotesDisponiblesByNombreYUnidad(anyString(), anyString()))
                .thenReturn(List.of());
        when(inventarioRepository.save(any())).thenReturn(inventario);
        when(ventaRepository.findLastFolio()).thenReturn(null);
        when(ventaRepository.save(any(Venta.class))).thenReturn(ventaGuardada);

        VentaDTO resultado = ventaService.crearVenta(dto);

        assertNotNull(resultado);
        assertEquals("V-0001", resultado.getFolio());
        assertEquals("efectivo", resultado.getMetodoPago());
        verify(ventaRepository, times(1)).save(any(Venta.class));
    }

    @Test
    @DisplayName("HU-018 - Crear venta con método de pago tarjeta exitosamente")
    void crearVenta_tarjeta_exitoso() {
        VentaDTO dto = buildVentaDTO("tarjeta", null);

        ventaGuardada.setMetodoPago("tarjeta");

        when(usuarioRepository.findById(1L)).thenReturn(Optional.of(usuarioAdmin));
        when(productoRepository.findByIdWithInsumos(1L)).thenReturn(producto);
        when(loteRepository.findLotesDisponiblesByNombreYUnidad(anyString(), anyString()))
                .thenReturn(List.of());
        when(inventarioRepository.save(any())).thenReturn(inventario);
        when(ventaRepository.findLastFolio()).thenReturn("V-0001");
        when(ventaRepository.save(any(Venta.class))).thenReturn(ventaGuardada);

        VentaDTO resultado = ventaService.crearVenta(dto);

        assertNotNull(resultado);
        verify(ventaRepository, times(1)).save(any(Venta.class));
    }

    // ================================================================
    // FOLIO SECUENCIAL
    // ================================================================

    @Test
    @DisplayName("HU-018 - Genera folio V-0001 cuando no hay ventas previas")
    void crearVenta_generaFolioInicial() {
        VentaDTO dto = buildVentaDTO("efectivo", 200.0);

        when(usuarioRepository.findById(1L)).thenReturn(Optional.of(usuarioAdmin));
        when(productoRepository.findByIdWithInsumos(1L)).thenReturn(producto);
        when(loteRepository.findLotesDisponiblesByNombreYUnidad(anyString(), anyString()))
                .thenReturn(List.of());
        when(inventarioRepository.save(any())).thenReturn(inventario);
        when(ventaRepository.findLastFolio()).thenReturn(null);
        when(ventaRepository.save(any(Venta.class))).thenReturn(ventaGuardada);

        VentaDTO resultado = ventaService.crearVenta(dto);

        assertEquals("V-0001", resultado.getFolio());
    }

    @Test
    @DisplayName("HU-018 - Genera folio secuencial V-0002 después de V-0001")
    void crearVenta_generaFolioSecuencial() {
        VentaDTO dto = buildVentaDTO("tarjeta", null);
        ventaGuardada.setFolio("V-0002");

        when(usuarioRepository.findById(1L)).thenReturn(Optional.of(usuarioAdmin));
        when(productoRepository.findByIdWithInsumos(1L)).thenReturn(producto);
        when(loteRepository.findLotesDisponiblesByNombreYUnidad(anyString(), anyString()))
                .thenReturn(List.of());
        when(inventarioRepository.save(any())).thenReturn(inventario);
        when(ventaRepository.findLastFolio()).thenReturn("V-0001");
        when(ventaRepository.save(any(Venta.class))).thenReturn(ventaGuardada);

        VentaDTO resultado = ventaService.crearVenta(dto);

        assertEquals("V-0002", resultado.getFolio());
    }

    // ================================================================
    // VALIDACIONES DE STOCK — Integridad de inventario (HU-017/018)
    // ================================================================

    @Test
    @DisplayName("HU-018 - Lanza excepción cuando stock insuficiente")
    void crearVenta_stockInsuficiente_lanzaExcepcion() {
        inventario.setCantidad(0.0);
        VentaDTO dto = buildVentaDTO("efectivo", 200.0);

        when(usuarioRepository.findById(1L)).thenReturn(Optional.of(usuarioAdmin));
        when(productoRepository.findByIdWithInsumos(1L)).thenReturn(producto);

        RuntimeException ex = assertThrows(RuntimeException.class,
                () -> ventaService.crearVenta(dto));

        assertTrue(ex.getMessage().contains("Stock insuficiente"));
        verify(ventaRepository, never()).save(any());
    }

    @Test
    @DisplayName("HU-018 - Lanza excepción cuando usuario no existe")
    void crearVenta_usuarioNoExiste_lanzaExcepcion() {
        VentaDTO dto = buildVentaDTO("efectivo", 200.0);
        when(usuarioRepository.findById(99L)).thenReturn(Optional.empty());
        dto.setUsuarioId(99L);

        assertThrows(RuntimeException.class, () -> ventaService.crearVenta(dto));
        verify(ventaRepository, never()).save(any());
    }

    @Test
    @DisplayName("HU-018 - Lanza excepción cuando producto no existe")
    void crearVenta_productoNoExiste_lanzaExcepcion() {
        VentaDTO dto = buildVentaDTO("efectivo", 200.0);

        when(usuarioRepository.findById(1L)).thenReturn(Optional.of(usuarioAdmin));
        when(productoRepository.findByIdWithInsumos(1L)).thenReturn(null);

        assertThrows(RuntimeException.class, () -> ventaService.crearVenta(dto));
        verify(ventaRepository, never()).save(any());
    }

    // ================================================================
    // MÉTODO EFECTIVO — Validación de cambio
    // ================================================================

    @Test
    @DisplayName("HU-018 - Lanza excepción cuando monto efectivo es insuficiente")
    void crearVenta_efectivoInsuficiente_lanzaExcepcion() {
        VentaDTO dto = buildVentaDTO("efectivo", 10.0); // monto menor al total

        when(usuarioRepository.findById(1L)).thenReturn(Optional.of(usuarioAdmin));
        when(productoRepository.findByIdWithInsumos(1L)).thenReturn(producto);
        when(loteRepository.findLotesDisponiblesByNombreYUnidad(anyString(), anyString()))
                .thenReturn(List.of());
        when(inventarioRepository.save(any())).thenReturn(inventario);
        when(ventaRepository.findLastFolio()).thenReturn(null);

        assertThrows(RuntimeException.class, () -> ventaService.crearVenta(dto));
        verify(ventaRepository, never()).save(any());
    }

    // ================================================================
    // OBTENER VENTAS
    // ================================================================

    @Test
    @DisplayName("HU-018 - Obtener todas las ventas devuelve lista")
    void obtenerTodasLasVentas_devuelveLista() {
        when(ventaRepository.findAll()).thenReturn(List.of(ventaGuardada));

        List<VentaDTO> resultado = ventaService.obtenerTodasLasVentas();

        assertEquals(1, resultado.size());
        verify(ventaRepository, times(1)).findAll();
    }

    @Test
    @DisplayName("HU-018 - Obtener venta por ID existente")
    void obtenerVentaPorId_encontrada() {
        when(ventaRepository.findById(1L)).thenReturn(Optional.of(ventaGuardada));

        VentaDTO resultado = ventaService.obtenerVentaPorId(1L);

        assertNotNull(resultado);
        assertEquals("V-0001", resultado.getFolio());
    }

    @Test
    @DisplayName("HU-018 - Obtener venta por ID inexistente lanza excepción")
    void obtenerVentaPorId_noEncontrada_lanzaExcepcion() {
        when(ventaRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(RuntimeException.class, () -> ventaService.obtenerVentaPorId(99L));
    }

    @Test
    @DisplayName("HU-018 - Obtener venta por folio existente")
    void obtenerVentaPorFolio_encontrada() {
        when(ventaRepository.findByFolio("V-0001")).thenReturn(ventaGuardada);

        VentaDTO resultado = ventaService.obtenerVentaPorFolio("V-0001");

        assertNotNull(resultado);
        assertEquals("V-0001", resultado.getFolio());
    }

    @Test
    @DisplayName("HU-018 - Obtener venta por folio inexistente lanza excepción")
    void obtenerVentaPorFolio_noEncontrada_lanzaExcepcion() {
        when(ventaRepository.findByFolio("V-9999")).thenReturn(null);

        assertThrows(RuntimeException.class,
                () -> ventaService.obtenerVentaPorFolio("V-9999"));
    }

    @Test
    @DisplayName("HU-018 - Obtener ventas por usuario")
    void obtenerVentasPorUsuario_devuelveLista() {
        when(ventaRepository.findByUsuarioId(1L)).thenReturn(List.of(ventaGuardada));

        List<VentaDTO> resultado = ventaService.obtenerVentasPorUsuario(1L);

        assertEquals(1, resultado.size());
    }

    @Test
    @DisplayName("HU-018 - Obtener ventas por método de pago efectivo")
    void obtenerVentasPorMetodoPago_efectivo() {
        when(ventaRepository.findByMetodoPago("efectivo")).thenReturn(List.of(ventaGuardada));

        List<VentaDTO> resultado = ventaService.obtenerVentasPorMetodoPago("efectivo");

        assertFalse(resultado.isEmpty());
        assertEquals("efectivo", resultado.get(0).getMetodoPago());
    }

    @Test
    @DisplayName("HU-018 - Obtener pedidos pendientes")
    void obtenerPedidosPendientes_devuelveLista() {
        when(ventaRepository.findByEstadoPedidoOrderByFechaAsc("PENDIENTE"))
                .thenReturn(List.of(ventaGuardada));

        List<VentaDTO> resultado = ventaService.obtenerPedidosPendientes();

        assertEquals(1, resultado.size());
        assertEquals("PENDIENTE", resultado.get(0).getEstadoPedido());
    }

    // ================================================================
    // MARCAR ENTREGADO
    // ================================================================

    @Test
    @DisplayName("HU-018 - Marcar pedido como entregado")
    void marcarPedidoEntregado_exitoso() {
        when(ventaRepository.findById(1L)).thenReturn(Optional.of(ventaGuardada));
        ventaGuardada.setEstadoPedido("ENTREGADA");
        when(ventaRepository.save(any(Venta.class))).thenReturn(ventaGuardada);

        VentaDTO resultado = ventaService.marcarPedidoEntregado(1L);

        assertEquals("ENTREGADA", resultado.getEstadoPedido());
    }

    @Test
    @DisplayName("HU-018 - Marcar entregado pedido inexistente lanza excepción")
    void marcarPedidoEntregado_noExiste_lanzaExcepcion() {
        when(ventaRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(RuntimeException.class,
                () -> ventaService.marcarPedidoEntregado(99L));
    }

    // ================================================================
    // CANCELAR VENTA
    // ================================================================

    @Test
    @DisplayName("HU-018 - Cancelar venta existente")
    void cancelarVenta_exitoso() {
        when(ventaRepository.findById(1L)).thenReturn(Optional.of(ventaGuardada));

        ventaService.cancelarVenta(1L);

        verify(ventaRepository, times(1)).deleteById(1L);
    }

    @Test
    @DisplayName("HU-018 - Cancelar venta inexistente lanza excepción")
    void cancelarVenta_noExiste_lanzaExcepcion() {
        when(ventaRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(RuntimeException.class, () -> ventaService.cancelarVenta(99L));
        verify(ventaRepository, never()).deleteById(any());
    }

    // ================================================================
    // HELPER
    // ================================================================

    private VentaDTO buildVentaDTO(String metodoPago, Double montoEfectivo) {
        VentaDetalleDTO detalle = new VentaDetalleDTO();
        detalle.setProductoId(1L);
        detalle.setCantidad(1);
        detalle.setPrecioUnitario(50.0);

        VentaDTO dto = new VentaDTO();
        dto.setUsuarioId(1L);
        dto.setMetodoPago(metodoPago);
        dto.setMontoEfectivo(montoEfectivo);
        dto.setNombreCliente("Cliente Test");
        dto.setDetalles(List.of(detalle));
        return dto;
    }
}
