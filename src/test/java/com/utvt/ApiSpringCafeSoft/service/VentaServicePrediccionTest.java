package com.utvt.ApiSpringCafeSoft.service;

import com.utvt.ApiSpringCafeSoft.dto.PrediccionProductosDTO;
import com.utvt.ApiSpringCafeSoft.model.Producto;
import com.utvt.ApiSpringCafeSoft.model.Sucursal;
import com.utvt.ApiSpringCafeSoft.model.Usuario;
import com.utvt.ApiSpringCafeSoft.model.Venta;
import com.utvt.ApiSpringCafeSoft.model.VentaDetalle;
import com.utvt.ApiSpringCafeSoft.repository.ProductoRepository;
import com.utvt.ApiSpringCafeSoft.repository.UsuarioRepository;
import com.utvt.ApiSpringCafeSoft.repository.VentaRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class VentaServicePrediccionTest {

    private static final String CORREO = "ana@cafesoft.mx";

    @Mock
    private VentaRepository ventaRepository;

    @Mock
    private ProductoRepository productoRepository;

    @Mock
    private UsuarioRepository usuarioRepository;

    @InjectMocks
    private VentaService service;

    @BeforeEach
    void inicializarSeguridad() {
        SecurityContextHolder.getContext().setAuthentication(
            new UsernamePasswordAuthenticationToken(CORREO, "", List.of())
        );
    }

    @AfterEach
    void limpiarSeguridad() {
        SecurityContextHolder.clearContext();
    }

    private void configurarSucursal() {
        Sucursal sucursal = new Sucursal();
        sucursal.setId(7L);
        sucursal.setNombre("Bonafont");

        Usuario usuario = new Usuario();
        usuario.setSucursal(sucursal);

        when(usuarioRepository.findByEmail(CORREO))
            .thenReturn(Optional.of(usuario));
    }

    private Producto producto(long id, String nombre) {
        Producto producto = new Producto();
        producto.setId(id);
        producto.setNombre(nombre);
        return producto;
    }

    private Venta ventaCon(
            Producto producto, int cantidad, LocalDateTime fecha) {

        Venta venta = new Venta();
        venta.setFecha(fecha);

        VentaDetalle detalle = new VentaDetalle();
        detalle.setProducto(producto);
        detalle.setCantidad(cantidad);
        detalle.setSubtotal(cantidad * 10.0);

        venta.addDetalle(detalle);
        return venta;
    }

    @Test
    void horizonteNoValidoSeRechaza() {
        IllegalArgumentException error = assertThrows(
            IllegalArgumentException.class,
            () -> service.obtenerPrediccionProductos(15)
        );

        assertTrue(error.getMessage().contains("7, 30 o 90"));
        verifyNoInteractions(ventaRepository, productoRepository);
    }

    @Test
    void generaAmbosRankingsConEstimaciones() {
        configurarSucursal();

        Producto p1 = producto(1L, "Garrafón 20L");
        Producto p2 = producto(2L, "Botella 1L");
        Producto p3 = producto(3L, "Agua 500ml");

        List<Producto> catalogo = List.of(
            p1, p2, p3,
            producto(4L, "Producto D"),
            producto(5L, "Producto E"),
            producto(6L, "Producto F"),
            producto(7L, "Producto G")
        );

        when(productoRepository.findBySucursalId(7L))
            .thenReturn(catalogo);

        LocalDateTime reciente = LocalDateTime.now().minusDays(3);
        LocalDateTime anterior = LocalDateTime.now().minusDays(20);

        when(ventaRepository.findByFechaBetweenAndSucursalId(
                any(), any(), eq(7L)))
            .thenReturn(List.of(
                ventaCon(p1, 140, anterior),
                ventaCon(p1, 140, reciente),
                ventaCon(p2, 28, reciente),
                ventaCon(p3, 140, anterior)
            ));

        PrediccionProductosDTO prediccion =
            service.obtenerPrediccionProductos(7);

        assertTrue(prediccion.isEsEstimacion());
        assertEquals(7, prediccion.getHorizonteDias());
        assertEquals(28, prediccion.getHistorialDias());
        assertEquals("Bonafont", prediccion.getSucursalNombre());
        assertNotNull(prediccion.getGeneradoEn());

        assertEquals(5, prediccion.getMasVendidos().size());
        assertEquals(5, prediccion.getMenosVendidos().size());

        PrediccionProductosDTO.Producto primero =
            prediccion.getMasVendidos().get(0);

        assertEquals("Garrafón 20L", primero.getNombre());
        assertEquals(280L, primero.getUnidadesHistoricas());
        assertEquals(70L, primero.getUnidadesEstimadas());

        assertEquals(
            4L,
            prediccion.getMenosVendidos().stream()
                .filter(p -> p.getUnidadesEstimadas() == 0L)
                .count()
        );
    }

    @Test
    void elHorizonteModificaLaCantidadEstimada() {
        configurarSucursal();

        Producto p1 = producto(1L, "Garrafón 20L");

        when(productoRepository.findBySucursalId(7L))
            .thenReturn(List.of(p1));

        when(ventaRepository.findByFechaBetweenAndSucursalId(
                any(), any(), eq(7L)))
            .thenReturn(List.of(
                ventaCon(p1, 140, LocalDateTime.now().minusDays(20)),
                ventaCon(p1, 140, LocalDateTime.now().minusDays(3))
            ));

        PrediccionProductosDTO prediccion =
            service.obtenerPrediccionProductos(30);

        assertEquals(
            300L, prediccion.getMasVendidos().get(0).getUnidadesEstimadas()
        );
    }

    @Test
    void sinVentasGeneraEstimacionesEnCero() {
        configurarSucursal();

        when(productoRepository.findBySucursalId(7L))
            .thenReturn(List.of(producto(1L, "Garrafón 20L")));

        when(ventaRepository.findByFechaBetweenAndSucursalId(
                any(), any(), eq(7L)))
            .thenReturn(List.of());

        PrediccionProductosDTO prediccion =
            service.obtenerPrediccionProductos(7);

        assertEquals(1, prediccion.getMasVendidos().size());
        assertEquals(1, prediccion.getMenosVendidos().size());
        assertEquals(
            0L, prediccion.getMasVendidos().get(0).getUnidadesEstimadas()
        );
    }
}
