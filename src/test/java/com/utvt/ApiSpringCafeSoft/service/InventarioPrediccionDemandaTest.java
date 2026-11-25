package com.utvt.ApiSpringCafeSoft.service;

import com.utvt.ApiSpringCafeSoft.controller.InventarioController;
import com.utvt.ApiSpringCafeSoft.dto.PrediccionDemandaDTO;
import com.utvt.ApiSpringCafeSoft.model.Inventario;
import com.utvt.ApiSpringCafeSoft.model.Producto;
import com.utvt.ApiSpringCafeSoft.model.ProductoInsumo;
import com.utvt.ApiSpringCafeSoft.model.Sucursal;
import com.utvt.ApiSpringCafeSoft.model.Venta;
import com.utvt.ApiSpringCafeSoft.model.VentaDetalle;
import com.utvt.ApiSpringCafeSoft.repository.InventarioRepository;
import com.utvt.ApiSpringCafeSoft.repository.ProductoRepository;
import com.utvt.ApiSpringCafeSoft.repository.SucursalRepository;
import com.utvt.ApiSpringCafeSoft.repository.VentaRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class InventarioPrediccionDemandaTest {

    @Mock
    private ProductoRepository productoRepository;

    @Mock
    private InventarioRepository inventarioRepository;

    @Mock
    private SucursalRepository sucursalRepository;

    @Mock
    private VentaRepository ventaRepository;

    @InjectMocks
    private InventarioService inventarioService;

    @Mock
    private InventarioService inventarioServiceMock;

    @InjectMocks
    private InventarioController inventarioController;

    @Test
    void calculaConsumoDemandaFaltanteYPeriodosSinFiltroDeSucursal() {
        Sucursal sucursal = crearSucursal(7L, "Sucursal Centro");
        Inventario insumo = crearInsumo(10L, "Azucar", "insumo", 4.0, sucursal);
        insumo.setUnidadMedida("kilogramos");
        Producto producto = crearProducto(
                new ProductoInsumo(insumo, 2.0, "kilogramos"));

        Venta venta = crearVenta(new VentaDetalle(producto, 3, 1.0, 3.0));
        when(productoRepository.findAllWithInsumos()).thenReturn(List.of(producto));
        when(ventaRepository.findByFechaBetween(any(LocalDateTime.class), any(LocalDateTime.class)))
                .thenReturn(List.of(venta));

        PrediccionDemandaDTO resultado = inventarioService.obtenerPrediccionDemanda(null);

        assertNotNull(resultado);
        assertEquals(1, resultado.getInsumos().size());
        PrediccionDemandaDTO.InsumoPredichoDTO prediccion = resultado.getInsumos().get(0);
        assertEquals(10L, prediccion.getId());
        assertEquals("Azucar", prediccion.getNombre());
        assertEquals("kilogramos", prediccion.getUnidadMedida());
        assertEquals(4.0, prediccion.getStockActual(), 0.0001);
        assertEquals(6.0, prediccion.getConsumoUltimos30Dias(), 0.0001);
        assertEquals(0.2, prediccion.getPromedioDiario(), 0.0001);
        assertEquals(6.0, prediccion.getDemandaSiguiente30Dias(), 0.0001);
        assertEquals(2.0, prediccion.getFaltante(), 0.0001);
        assertEquals("Stock insuficiente", prediccion.getEstadoStock());
        assertEquals(7L, prediccion.getSucursalId());
        assertEquals("Sucursal Centro", prediccion.getSucursalNombre());
        assertEquals(30L, Duration.between(
                resultado.getInicioPeriodoReferencia(), resultado.getFinPeriodoReferencia()).toDays());
        assertEquals(resultado.getFinPeriodoReferencia(), resultado.getInicioPeriodoPrediccion());
        assertEquals(30L, Duration.between(
                resultado.getInicioPeriodoPrediccion(), resultado.getFinPeriodoPrediccion()).toDays());
        verify(ventaRepository).findByFechaBetween(any(LocalDateTime.class), any(LocalDateTime.class));
    }

    @Test
    void filtraPorSucursalYOmiteregistrosInvalidosDuranteLaPrediccion() {
        Sucursal sucursalSolicitada = crearSucursal(1L, "Centro");
        Sucursal otraSucursal = crearSucursal(2L, "Norte");

        Inventario insumoValido = crearInsumo(11L, "Harina", "insumo", null, sucursalSolicitada);
        Inventario insumoSinId = crearInsumo(null, "Sin id", "insumo", 5.0, sucursalSolicitada);
        Inventario productoTerminado = crearInsumo(14L, "Producto terminado", "producto", 20.0,
                sucursalSolicitada);
        Inventario insumoSinSucursal = crearInsumo(12L, "Sin sucursal", "insumo", 5.0, null);
        Inventario insumoOtraSucursal = crearInsumo(13L, "Otra sucursal", "insumo", 5.0, otraSucursal);

        Producto productoSinRecetas = new Producto();
        productoSinRecetas.setInsumos(null);

        Producto productoDeCatalogo = crearProducto(
                new ProductoInsumo(null, 1.0, "piezas"),
                new ProductoInsumo(insumoSinId, 1.0, "piezas"),
                new ProductoInsumo(productoTerminado, 1.0, "piezas"),
                new ProductoInsumo(insumoSinSucursal, 1.0, "piezas"),
                new ProductoInsumo(insumoOtraSucursal, 1.0, "piezas"),
                new ProductoInsumo(insumoValido, null, "piezas"));

        when(productoRepository.findAllWithInsumos())
                .thenReturn(List.of(productoSinRecetas, productoDeCatalogo));

        Venta ventaSinDetalles = new Venta();
        ventaSinDetalles.setDetalles(null);

        Producto productoConRecetasNulas = new Producto();
        productoConRecetasNulas.setInsumos(null);

        Producto productoDeVenta = crearProducto(
                new ProductoInsumo(null, 1.0, "piezas"),
                new ProductoInsumo(insumoSinId, 1.0, "piezas"),
                new ProductoInsumo(productoTerminado, 1.0, "piezas"),
                new ProductoInsumo(insumoSinSucursal, 1.0, "piezas"),
                new ProductoInsumo(insumoOtraSucursal, 1.0, "piezas"),
                new ProductoInsumo(insumoValido, null, "piezas"),
                new ProductoInsumo(insumoValido, 0.5, "piezas"));

        Venta ventaConDetallesValidosEInvalidos = crearVenta(
                new VentaDetalle(null, 1, 1.0, 1.0),
                new VentaDetalle(productoDeVenta, null, 1.0, 1.0),
                new VentaDetalle(productoConRecetasNulas, 2, 1.0, 2.0),
                new VentaDetalle(productoDeVenta, 2, 1.0, 2.0));

        when(ventaRepository.findByFechaBetweenAndSucursalId(
                any(LocalDateTime.class), any(LocalDateTime.class), eq(1L)))
                .thenReturn(List.of(ventaSinDetalles, ventaConDetallesValidosEInvalidos));

        PrediccionDemandaDTO resultado = inventarioService.obtenerPrediccionDemanda(1L);

        assertEquals(1, resultado.getInsumos().size());
        PrediccionDemandaDTO.InsumoPredichoDTO prediccion = resultado.getInsumos().get(0);
        assertEquals(11L, prediccion.getId());
        assertEquals(0.0, prediccion.getStockActual(), 0.0001);
        assertEquals(1.0, prediccion.getConsumoUltimos30Dias(), 0.0001);
        assertEquals(1.0, prediccion.getDemandaSiguiente30Dias(), 0.0001);
        assertEquals(1.0, prediccion.getFaltante(), 0.0001);
        assertEquals("Stock insuficiente", prediccion.getEstadoStock());
        assertEquals(1L, prediccion.getSucursalId());
        verify(ventaRepository).findByFechaBetweenAndSucursalId(
                any(LocalDateTime.class), any(LocalDateTime.class), eq(1L));
    }

    @Test
    void consideraStockSuficienteYPermiteInsumoSinSucursalCuandoNoHayFiltro() {
        Inventario insumo = crearInsumo(20L, "Agua", "insumo", 10.0, null);
        Producto producto = crearProducto(new ProductoInsumo(insumo, 1.0, "litros"));
        Venta venta = crearVenta(new VentaDetalle(producto, 1, 2.0, 2.0));

        when(productoRepository.findAllWithInsumos()).thenReturn(List.of(producto));
        when(ventaRepository.findByFechaBetween(any(LocalDateTime.class), any(LocalDateTime.class)))
                .thenReturn(List.of(venta));

        PrediccionDemandaDTO resultado = inventarioService.obtenerPrediccionDemanda(null);

        assertEquals(1, resultado.getInsumos().size());
        PrediccionDemandaDTO.InsumoPredichoDTO prediccion = resultado.getInsumos().get(0);
        assertEquals(10.0, prediccion.getStockActual(), 0.0001);
        assertEquals(1.0, prediccion.getDemandaSiguiente30Dias(), 0.0001);
        assertEquals(0.0, prediccion.getFaltante(), 0.0001);
        assertEquals("Stock suficiente", prediccion.getEstadoStock());
        assertNull(prediccion.getSucursalId());
        assertNull(prediccion.getSucursalNombre());
    }

    @Test
    void devuelveListaVaciaCuandoNoHayRecetasNiDetallesDeVenta() {
        Producto productoSinInsumos = new Producto();
        productoSinInsumos.setInsumos(null);
        Producto productoConListaVacia = crearProducto();
        Venta ventaSinDetalles = new Venta();
        ventaSinDetalles.setDetalles(null);

        when(productoRepository.findAllWithInsumos())
                .thenReturn(List.of(productoSinInsumos, productoConListaVacia));
        when(ventaRepository.findByFechaBetween(any(LocalDateTime.class), any(LocalDateTime.class)))
                .thenReturn(List.of(ventaSinDetalles));

        PrediccionDemandaDTO resultado = inventarioService.obtenerPrediccionDemanda(null);

        assertNotNull(resultado);
        assertTrue(resultado.getInsumos().isEmpty());
    }

    @Test
    void endpointDevuelvePrediccionYDelegaSucursalAlServicio() {
        PrediccionDemandaDTO prediccion = new PrediccionDemandaDTO();
        when(inventarioServiceMock.obtenerPrediccionDemanda(5L)).thenReturn(prediccion);

        ResponseEntity<PrediccionDemandaDTO> respuesta =
                inventarioController.obtenerPrediccionDemanda(5L);

        assertEquals(HttpStatus.OK, respuesta.getStatusCode());
        assertSame(prediccion, respuesta.getBody());
        verify(inventarioServiceMock).obtenerPrediccionDemanda(5L);
    }

    @Test
    void dtoExponeSusFechasYTodosLosCamposDelInsumo() {
        LocalDateTime inicioReferencia = LocalDateTime.of(2026, 1, 1, 0, 0);
        LocalDateTime finReferencia = LocalDateTime.of(2026, 1, 31, 0, 0);
        LocalDateTime inicioPrediccion = finReferencia;
        LocalDateTime finPrediccion = LocalDateTime.of(2026, 3, 2, 0, 0);

        PrediccionDemandaDTO.InsumoPredichoDTO insumo =
                new PrediccionDemandaDTO.InsumoPredichoDTO(
                        30L, "Cafe", "kilogramos", 8.0, 12.0, 0.4,
                        12.0, 4.0, "Stock insuficiente", 3L, "Sucursal Sur");
        PrediccionDemandaDTO dto = new PrediccionDemandaDTO(
                inicioReferencia, finReferencia, inicioPrediccion, finPrediccion,
                List.of(insumo));

        assertEquals(inicioReferencia, dto.getInicioPeriodoReferencia());
        assertEquals(finReferencia, dto.getFinPeriodoReferencia());
        assertEquals(inicioPrediccion, dto.getInicioPeriodoPrediccion());
        assertEquals(finPrediccion, dto.getFinPeriodoPrediccion());
        assertEquals(1, dto.getInsumos().size());
        assertEquals(30L, insumo.getId());
        assertEquals("Cafe", insumo.getNombre());
        assertEquals("kilogramos", insumo.getUnidadMedida());
        assertEquals(8.0, insumo.getStockActual(), 0.0001);
        assertEquals(12.0, insumo.getConsumoUltimos30Dias(), 0.0001);
        assertEquals(0.4, insumo.getPromedioDiario(), 0.0001);
        assertEquals(12.0, insumo.getDemandaSiguiente30Dias(), 0.0001);
        assertEquals(4.0, insumo.getFaltante(), 0.0001);
        assertEquals("Stock insuficiente", insumo.getEstadoStock());
        assertEquals(3L, insumo.getSucursalId());
        assertEquals("Sucursal Sur", insumo.getSucursalNombre());

        PrediccionDemandaDTO dtoVacio = new PrediccionDemandaDTO();
        PrediccionDemandaDTO.InsumoPredichoDTO insumoVacio =
                new PrediccionDemandaDTO.InsumoPredichoDTO();
        assertNull(dtoVacio.getInicioPeriodoReferencia());
        assertNull(dtoVacio.getFinPeriodoReferencia());
        assertNull(dtoVacio.getInicioPeriodoPrediccion());
        assertNull(dtoVacio.getFinPeriodoPrediccion());
        assertNull(dtoVacio.getInsumos());
        assertNull(insumoVacio.getId());
        assertNull(insumoVacio.getNombre());
        assertNull(insumoVacio.getUnidadMedida());
        assertNull(insumoVacio.getStockActual());
        assertNull(insumoVacio.getConsumoUltimos30Dias());
        assertNull(insumoVacio.getPromedioDiario());
        assertNull(insumoVacio.getDemandaSiguiente30Dias());
        assertNull(insumoVacio.getFaltante());
        assertNull(insumoVacio.getEstadoStock());
        assertNull(insumoVacio.getSucursalId());
        assertNull(insumoVacio.getSucursalNombre());
    }

    private static Sucursal crearSucursal(Long id, String nombre) {
        Sucursal sucursal = new Sucursal();
        sucursal.setId(id);
        sucursal.setNombre(nombre);
        return sucursal;
    }

    private static Inventario crearInsumo(
            Long id, String nombre, String tipo, Double cantidad, Sucursal sucursal) {
        Inventario insumo = new Inventario();
        insumo.setId(id);
        insumo.setNombre(nombre);
        insumo.setTipo(tipo);
        insumo.setCantidad(cantidad);
        insumo.setUnidadMedida("piezas");
        insumo.setCantidadMinima(1.0);
        insumo.setPrecioUnitario(1.0);
        insumo.setSucursal(sucursal);
        return insumo;
    }

    private static Producto crearProducto(ProductoInsumo... recetas) {
        Producto producto = new Producto();
        producto.setNombre("Producto de prueba");
        producto.setPrecio(10.0);
        producto.setInsumos(new ArrayList<>(Arrays.asList(recetas)));
        return producto;
    }

    private static Venta crearVenta(VentaDetalle... detalles) {
        Venta venta = new Venta();
        venta.setDetalles(new ArrayList<>(Arrays.asList(detalles)));
        return venta;
    }
}
