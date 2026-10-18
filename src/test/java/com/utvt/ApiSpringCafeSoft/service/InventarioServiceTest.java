package com.utvt.ApiSpringCafeSoft.service;

import com.utvt.ApiSpringCafeSoft.dto.InventarioDTO;
import com.utvt.ApiSpringCafeSoft.model.Inventario;
import com.utvt.ApiSpringCafeSoft.model.Sucursal;
import com.utvt.ApiSpringCafeSoft.repository.InventarioRepository;
import com.utvt.ApiSpringCafeSoft.repository.ProductoRepository;
import com.utvt.ApiSpringCafeSoft.repository.SucursalRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * HU-018: Tests unitarios de InventarioService
 * Cubre lógica de stock, caducidad y alertas de inventario.
 */
@ExtendWith(MockitoExtension.class)
class InventarioServiceTest {

    @Mock
    private InventarioRepository inventarioRepository;

    @Mock
    private ProductoRepository productoRepository;

    @Mock
    private SucursalRepository sucursalRepository;

    @InjectMocks
    private InventarioService inventarioService;

    private Inventario inventarioBase;
    private InventarioDTO dtoBase;
    private Sucursal sucursalBase;

    @BeforeEach
    void setUp() {
        sucursalBase = new Sucursal();
        sucursalBase.setId(1L);
        sucursalBase.setNombre("Sucursal Centro");
        sucursalBase.setDireccion("Calle 1");
        sucursalBase.setTelefono("7200000001");

        inventarioBase = new Inventario();
        inventarioBase.setId(1L);
        inventarioBase.setNombre("Café Molido");
        inventarioBase.setTipo("Materia Prima");
        inventarioBase.setCantidad(100.0);
        inventarioBase.setUnidadMedida("kilogramos");
        inventarioBase.setCantidadMinima(10.0);
        inventarioBase.setPrecioUnitario(50.0);
        inventarioBase.setSucursal(sucursalBase);

        dtoBase = new InventarioDTO();
        dtoBase.setNombre("Café Molido");
        dtoBase.setTipo("Materia Prima");
        dtoBase.setCantidad(100.0);
        dtoBase.setUnidadMedida("kilogramos");
        dtoBase.setCantidadMinima(10.0);
        dtoBase.setPrecioUnitario(50.0);
    }

    // ================================================================
    // CREAR INSUMO
    // ================================================================

    @Test
    @DisplayName("HU-018 - Crear insumo exitosamente")
    void crearInsumo_exitoso() {
        when(inventarioRepository.save(any(Inventario.class))).thenReturn(inventarioBase);

        InventarioDTO resultado = inventarioService.crearInsumo(dtoBase);

        assertNotNull(resultado);
        assertEquals("Café Molido", resultado.getNombre());
        verify(inventarioRepository, times(1)).save(any(Inventario.class));
    }

    // ================================================================
    // OBTENER TODOS
    // ================================================================

    @Test
    @DisplayName("HU-018 - Obtener todos los insumos devuelve lista correcta")
    void obtenerTodosLosInsumos_devuelveLista() {
        Inventario inv2 = new Inventario();
        inv2.setId(2L);
        inv2.setNombre("Azúcar");
        inv2.setTipo("Materia Prima");
        inv2.setCantidad(50.0);
        inv2.setUnidadMedida("kilogramos");
        inv2.setCantidadMinima(5.0);
        inv2.setPrecioUnitario(20.0);

        when(inventarioRepository.findAll()).thenReturn(Arrays.asList(inventarioBase, inv2));

        List<InventarioDTO> resultado = inventarioService.obtenerTodosLosInsumos();

        assertEquals(2, resultado.size());
        verify(inventarioRepository, times(1)).findAll();
    }

    @Test
    @DisplayName("HU-018 - Obtener todos devuelve lista vacía cuando no hay insumos")
    void obtenerTodosLosInsumos_listaVacia() {
        when(inventarioRepository.findAll()).thenReturn(List.of());

        List<InventarioDTO> resultado = inventarioService.obtenerTodosLosInsumos();

        assertTrue(resultado.isEmpty());
    }

    // ================================================================
    // OBTENER POR ID
    // ================================================================

    @Test
    @DisplayName("HU-018 - Obtener insumo por ID existente")
    void obtenerInsumoPorId_encontrado() {
        when(inventarioRepository.findById(1L)).thenReturn(Optional.of(inventarioBase));

        InventarioDTO resultado = inventarioService.obtenerInsumoPorId(1L);

        assertNotNull(resultado);
        assertEquals(1L, resultado.getId());
        assertEquals("Café Molido", resultado.getNombre());
    }

    @Test
    @DisplayName("HU-018 - Obtener insumo por ID inexistente lanza excepción")
    void obtenerInsumoPorId_noEncontrado_lanzaExcepcion() {
        when(inventarioRepository.findById(99L)).thenReturn(Optional.empty());

        RuntimeException ex = assertThrows(RuntimeException.class,
                () -> inventarioService.obtenerInsumoPorId(99L));

        assertTrue(ex.getMessage().contains("99"));
    }

    // ================================================================
    // ACTUALIZAR INSUMO
    // ================================================================

    @Test
    @DisplayName("HU-018 - Actualizar insumo exitosamente")
    void actualizarInsumo_exitoso() {
        when(inventarioRepository.findById(1L)).thenReturn(Optional.of(inventarioBase));
        when(inventarioRepository.save(any(Inventario.class))).thenReturn(inventarioBase);

        dtoBase.setCantidad(200.0);
        InventarioDTO resultado = inventarioService.actualizarInsumo(1L, dtoBase);

        assertNotNull(resultado);
        verify(inventarioRepository, times(1)).save(any(Inventario.class));
    }

    @Test
    @DisplayName("HU-018 - Actualizar insumo inexistente lanza excepción")
    void actualizarInsumo_noExiste_lanzaExcepcion() {
        when(inventarioRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(RuntimeException.class,
                () -> inventarioService.actualizarInsumo(99L, dtoBase));
    }

    // ================================================================
    // ELIMINAR INSUMO
    // ================================================================

    @Test
    @DisplayName("HU-018 - Eliminar insumo existente")
    void eliminarInsumo_exitoso() {
        when(inventarioRepository.existsById(1L)).thenReturn(true);

        inventarioService.eliminarInsumo(1L);

        verify(inventarioRepository, times(1)).deleteById(1L);
    }

    @Test
    @DisplayName("HU-018 - Eliminar insumo inexistente lanza excepción")
    void eliminarInsumo_noExiste_lanzaExcepcion() {
        when(inventarioRepository.existsById(99L)).thenReturn(false);

        assertThrows(RuntimeException.class,
                () -> inventarioService.eliminarInsumo(99L));

        verify(inventarioRepository, never()).deleteById(any());
    }

    // ================================================================
    // BAJO STOCK — Criterio de alerta de caducidad e inventario
    // ================================================================

    @Test
    @DisplayName("HU-018 - Detectar insumos bajo stock")
    void obtenerInsumosBajoStock_devuelveLista() {
        when(inventarioRepository.findLowStockItems()).thenReturn(List.of(inventarioBase));

        List<InventarioDTO> resultado = inventarioService.obtenerInsumosBajoStock();

        assertEquals(1, resultado.size());
        verify(inventarioRepository, times(1)).findLowStockItems();
    }

    @Test
    @DisplayName("HU-018 - Detectar insumos en stock crítico")
    void obtenerInsumosStockCritico_devuelveLista() {
        when(inventarioRepository.findCriticalStockItems()).thenReturn(List.of(inventarioBase));

        List<InventarioDTO> resultado = inventarioService.obtenerInsumosStockCritico();

        assertFalse(resultado.isEmpty());
        verify(inventarioRepository, times(1)).findCriticalStockItems();
    }

    @Test
    @DisplayName("HU-018 - No hay insumos bajo stock")
    void obtenerInsumosBajoStock_listaVacia() {
        when(inventarioRepository.findLowStockItems()).thenReturn(List.of());

        List<InventarioDTO> resultado = inventarioService.obtenerInsumosBajoStock();

        assertTrue(resultado.isEmpty());
    }

    // ================================================================
    // CADUCIDAD
    // ================================================================

    @Test
    @DisplayName("HU-018 - Obtener insumos que caducan antes de una fecha")
    void obtenerInsumosCaducanAntesDe_devuelveLista() {
        when(inventarioRepository.findByCaducidadBefore("2027-01-01"))
                .thenReturn(List.of(inventarioBase));

        List<InventarioDTO> resultado =
                inventarioService.obtenerInsumosCaducanAntesDe("2027-01-01");

        assertEquals(1, resultado.size());
    }

    @Test
    @DisplayName("HU-018 - Sin insumos próximos a caducar")
    void obtenerInsumosCaducanAntesDe_listaVacia() {
        when(inventarioRepository.findByCaducidadBefore(anyString()))
                .thenReturn(List.of());

        List<InventarioDTO> resultado =
                inventarioService.obtenerInsumosCaducanAntesDe("2025-01-01");

        assertTrue(resultado.isEmpty());
    }

    // ================================================================
    // BÚSQUEDAS
    // ================================================================

    @Test
    @DisplayName("HU-018 - Buscar insumos por nombre")
    void buscarPorNombre_devuelveCoincidencias() {
        when(inventarioRepository.findByNombreContainingIgnoreCase("café"))
                .thenReturn(List.of(inventarioBase));

        List<InventarioDTO> resultado = inventarioService.buscarPorNombre("café");

        assertEquals(1, resultado.size());
        assertEquals("Café Molido", resultado.get(0).getNombre());
    }

    @Test
    @DisplayName("HU-018 - Buscar insumos por tipo")
    void buscarPorTipo_devuelveCoincidencias() {
        when(inventarioRepository.findByTipoContainingIgnoreCase("Materia"))
                .thenReturn(List.of(inventarioBase));

        List<InventarioDTO> resultado = inventarioService.buscarPorTipo("Materia");

        assertFalse(resultado.isEmpty());
    }

    @Test
    @DisplayName("HU-018 - Buscar insumos por proveedor")
    void buscarPorProveedor_devuelveCoincidencias() {
        when(inventarioRepository.findByProveedorContainingIgnoreCase("CafeMax"))
                .thenReturn(List.of(inventarioBase));

        List<InventarioDTO> resultado = inventarioService.buscarPorProveedor("CafeMax");

        assertFalse(resultado.isEmpty());
    }

    @Test
    @DisplayName("HU-018 - Obtener insumos por rango de precio")
    void obtenerInsumosPorRangoPrecio_devuelveLista() {
        when(inventarioRepository.findByPrecioRange(10.0, 100.0))
                .thenReturn(List.of(inventarioBase));

        List<InventarioDTO> resultado =
                inventarioService.obtenerInsumosPorRangoPrecio(10.0, 100.0);

        assertEquals(1, resultado.size());
    }

    @Test
    @DisplayName("HU-018 - Obtener insumos por sucursal")
    void obtenerPorSucursal_devuelveLista() {
        when(inventarioRepository.findBySucursalId(1L))
                .thenReturn(List.of(inventarioBase));

        List<InventarioDTO> resultado = inventarioService.obtenerPorSucursal(1L);

        assertEquals(1, resultado.size());
    }
}
