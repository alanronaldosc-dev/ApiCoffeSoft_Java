package com.utvt.ApiSpringCafeSoft.config;

import com.utvt.ApiSpringCafeSoft.model.*;
import com.utvt.ApiSpringCafeSoft.repository.*;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

/**
 * HU-047: Inicializa datos de prueba para el perfil H2.
 * Solo activo con spring.profiles.active=h2.
 *
 * Crea: sucursal → usuario ADMIN → categoría → productos →
 *       inventario → promoción → campañas → ventas → auditoría
 */
@Component
@Profile("h2")
public class DataInitializer implements ApplicationRunner {

    private final SucursalRepository   sucursalRepo;
    private final UsuarioRepository    usuarioRepo;
    private final CategoriaRepository  categoriaRepo;
    private final ProductoRepository   productoRepo;
    private final InventarioRepository inventarioRepo;
    private final PromocionRepository  promocionRepo;
    private final CampanaRepository    campanaRepo;
    private final VentaRepository      ventaRepo;
    private final VentaDetalleRepository ventaDetalleRepo;
    private final AuditoriaRepository  auditoriaRepo;

    private final BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();

    public DataInitializer(SucursalRepository sucursalRepo,
                           UsuarioRepository usuarioRepo,
                           CategoriaRepository categoriaRepo,
                           ProductoRepository productoRepo,
                           InventarioRepository inventarioRepo,
                           PromocionRepository promocionRepo,
                           CampanaRepository campanaRepo,
                           VentaRepository ventaRepo,
                           VentaDetalleRepository ventaDetalleRepo,
                           AuditoriaRepository auditoriaRepo) {
        this.sucursalRepo    = sucursalRepo;
        this.usuarioRepo     = usuarioRepo;
        this.categoriaRepo   = categoriaRepo;
        this.productoRepo    = productoRepo;
        this.inventarioRepo  = inventarioRepo;
        this.promocionRepo   = promocionRepo;
        this.campanaRepo     = campanaRepo;
        this.ventaRepo       = ventaRepo;
        this.ventaDetalleRepo = ventaDetalleRepo;
        this.auditoriaRepo   = auditoriaRepo;
    }

    @Override
    @Transactional
    public void run(ApplicationArguments args) {

        // ── 1. Sucursal ──────────────────────────────────────────
        Sucursal sucursal;
        if (sucursalRepo.count() == 0) {
            sucursal = new Sucursal();
            sucursal.setNombre("Sucursal Central");
            sucursal.setDireccion("Av. Principal 100");
            sucursal.setTelefono("7712000001");
            sucursal.setCiudad("Tuxpan");
            sucursal.setActivo(true);
            sucursal = sucursalRepo.save(sucursal);
        } else {
            sucursal = sucursalRepo.findAll().get(0);
        }

        // ── 2. Usuario ADMIN ────────────────────────────────────
        // userTipo=0 → JwtFilter mapea a ROLE_ADMIN
        if (!usuarioRepo.existsByEmail("admin@cafesoft.com")) {
            Usuario admin = new Usuario();
            admin.setNombre("Admin CafeSoft");
            admin.setEmail("admin@cafesoft.com");
            admin.setPassword(encoder.encode("admin123"));
            admin.setDireccion("Av. Principal 100");
            admin.setTelefono("7712000001");
            admin.setUserTipo(0);
            admin.setActivo(true);
            admin.setSucursal(sucursal);
            admin.setCreatedAt(LocalDateTime.now());
            usuarioRepo.save(admin);
            System.out.println("[DataInitializer] ✅ ADMIN creado: admin@cafesoft.com / admin123");
        }

        Usuario admin = usuarioRepo.findByEmail("admin@cafesoft.com").orElseThrow();

        // ── 3. Categoría ─────────────────────────────────────────
        Categoria cat;
        if (categoriaRepo.count() == 0) {
            cat = new Categoria();
            cat.setNombre("Garrafones");
            cat.setDescripcion("Productos de agua purificada");
            cat.setActivo(true);
            cat = categoriaRepo.save(cat);
        } else {
            cat = categoriaRepo.findAll().get(0);
        }

        // ── 4. Productos ─────────────────────────────────────────
        if (productoRepo.count() == 0) {
            Producto p1 = new Producto();
            p1.setNombre("Garrafon 20L");
            p1.setPrecio(45.0);
            p1.setDescripcion("Garrafon de agua purificada 20 litros");
            p1.setCategoria(cat);
            p1.setSucursal(sucursal);
            productoRepo.save(p1);

            Producto p2 = new Producto();
            p2.setNombre("Garrafon 10L");
            p2.setPrecio(25.0);
            p2.setDescripcion("Garrafon de agua purificada 10 litros");
            p2.setCategoria(cat);
            p2.setSucursal(sucursal);
            productoRepo.save(p2);
        }

        List<Producto> productos = productoRepo.findAll();
        Producto prod1 = productos.get(0);

        // ── 5. Inventario ────────────────────────────────────────
        if (inventarioRepo.count() == 0) {
            Inventario inv = new Inventario();
            inv.setNombre("Agua purificada");
            inv.setTipo("materia_prima");
            inv.setCantidad(500.0);
            inv.setUnidadMedida("litros");
            inv.setCantidadMinima(50.0);
            inv.setPrecioUnitario(2.5);
            inv.setSucursal(sucursal);
            inventarioRepo.save(inv);
        }

        // ── 6. Promoción ─────────────────────────────────────────
        Promocion promo;
        if (promocionRepo.count() == 0) {
            promo = new Promocion();
            promo.setNombre("Promo Mayo 10%");
            promo.setDescripcion("Descuento del 10% en garrafones");
            promo.setDescuentoPorcentaje(10.0);
            promo.setFechaInicio(LocalDateTime.now().minusMonths(1));
            promo.setFechaFin(LocalDateTime.now().plusMonths(1));
            promo.setActiva(true);
            promo.setMinGarrafones(0);
            promo.setAcumulaPuntos(true);
            promo.setSucursal(sucursal);
            promo = promocionRepo.save(promo);
        } else {
            promo = promocionRepo.findAll().get(0);
        }

        // ── 7. Campañas ──────────────────────────────────────────
        if (campanaRepo.count() == 0) {
            String[] segmentos = {"ACTIVO", "DORMIDO", "POTENCIAL"};
            for (String seg : segmentos) {
                Campana c = new Campana();
                c.setNombre("Campaña " + seg);
                c.setDescripcion("Campaña para clientes " + seg.toLowerCase());
                c.setSegmento(seg);
                c.setPromocion(promo);
                c.setFechaInicio(LocalDateTime.now().minusDays(5));
                c.setFechaFin(LocalDateTime.now().plusDays(25));
                c.setActiva(true);
                c.setSucursal(sucursal);
                campanaRepo.save(c);
            }
        }

        // ── 8. Ventas (10 registros) ─────────────────────────────
        if (ventaRepo.count() == 0) {
            String[] metodos = {"efectivo", "tarjeta", "transferencia", "efectivo", "tarjeta",
                                "efectivo", "efectivo", "tarjeta", "puntos", "tarjeta"};
            int[] dias       = {0, 0, 0, 0, 1, 2, 3, 5, 7, 10};

            for (int i = 0; i < 10; i++) {
                Venta v = new Venta();
                v.setFolio("V-" + String.format("%04d", i + 1));
                v.setFecha(LocalDateTime.now().minusDays(dias[i]).minusHours(i));
                v.setSubtotal(86.21);
                v.setImpuestos(13.79);
                v.setDescuento(0.0);
                v.setTotal(100.0);
                v.setMetodoPago(metodos[i]);
                v.setUsuario(admin);
                v.setSucursal(sucursal);
                v.setEstadoPedido("entregado");
                v.setCreatedAt(LocalDateTime.now().minusDays(dias[i]));

                VentaDetalle det = new VentaDetalle();
                det.setProducto(prod1);
                det.setCantidad(2);
                det.setPrecioUnitario(45.0);
                det.setSubtotal(90.0);
                v.addDetalle(det);

                ventaRepo.save(v);
            }
            System.out.println("[DataInitializer] ✅ 10 ventas creadas");
        }

        // ── 9. Auditoría ─────────────────────────────────────────
        if (auditoriaRepo.count() == 0) {
            String[][] eventos = {
                {"LOGIN",      "USUARIO", "Inicio de sesion"},
                {"CREAR",      "VENTA",   "Venta V-0001 registrada"},
                {"CREAR",      "CAMPANA", "Campana ACTIVO creada"},
                {"ACTUALIZAR", "PRODUCTO","Precio actualizado"},
                {"LISTADO",    "VENTA",   "Reporte de ventas consultado"}
            };
            for (int i = 0; i < eventos.length; i++) {
                Auditoria a = new Auditoria();
                a.setUsuario("admin@cafesoft.com");
                a.setAccion(eventos[i][0]);
                a.setEntidad(eventos[i][1]);
                a.setDetalle(eventos[i][2]);
                a.setFecha(LocalDateTime.now().minusMinutes(eventos.length - i));
                auditoriaRepo.save(a);
            }
        }

        System.out.println("[DataInitializer] ✅ Datos H2 listos para HU-047");
    }
}
