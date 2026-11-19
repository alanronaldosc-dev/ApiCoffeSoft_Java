-- ============================================================
-- Seed data para perfil H2 (pruebas de carga HU-047)
-- NOTA: Sucursal y Usuario ADMIN son creados por DataInitializer.java
--       con BCrypt correcto. Este script asume sucursal id=1, usuario id=1.
-- ============================================================

-- ── Categorías ───────────────────────────────────────────────
INSERT INTO categorias (id, nombre, descripcion, activo)
VALUES (1, 'Garrafones',  'Productos de agua purificada', TRUE),
       (2, 'Accesorios',  'Accesorios para garrafones',   TRUE);

-- ── Productos ────────────────────────────────────────────────
INSERT INTO productos (id, nombre, precio, descripcion, sucursal_id, categoria_id)
VALUES (1, 'Garrafon 20L',      45.00, 'Garrafon de agua purificada 20 litros', 1, 1),
       (2, 'Garrafon 10L',      25.00, 'Garrafon de agua purificada 10 litros', 1, 1),
       (3, 'Dispensador basico', 150.00, 'Dispensador de agua de pie',          1, 2);

-- ── Promociones ──────────────────────────────────────────────
INSERT INTO promociones (id, nombre, descripcion, descuento_porcentaje, fecha_inicio, fecha_fin,
                         activa, minimo_garrafones, acumula_puntos, sucursal_id, created_at)
VALUES (1, 'Promo Mayo 10%',  'Descuento del 10% en garrafones', 10.0,
           DATEADD('MONTH', -1, NOW()), DATEADD('MONTH', 1, NOW()), TRUE, 2, TRUE, 1, NOW()),
       (2, 'Promo Verano 15%', 'Descuento del 15% en todo',      15.0,
           DATEADD('MONTH', -2, NOW()), DATEADD('MONTH', 2, NOW()), TRUE, 0, TRUE, 1, NOW());

-- ── Campañas ─────────────────────────────────────────────────
INSERT INTO campanas (id, nombre, descripcion, segmento, promocion_id,
                      fecha_inicio, fecha_fin, activa, sucursal_id, created_at)
VALUES (1, 'Campaña Clientes Activos',   'Recompensa clientes frecuentes', 'ACTIVO',    1,
           DATEADD('DAY', -5, NOW()), DATEADD('DAY', 25, NOW()),  TRUE, 1, NOW()),
       (2, 'Campaña Clientes Dormidos',  'Reactiva clientes inactivos',    'DORMIDO',   2,
           DATEADD('DAY', -3, NOW()), DATEADD('DAY', 27, NOW()),  TRUE, 1, NOW()),
       (3, 'Campaña Potenciales',        'Convierte leads en clientes',    'POTENCIAL', 1,
           DATEADD('DAY', -1, NOW()), DATEADD('DAY', 29, NOW()),  TRUE, 1, NOW());

-- ── Ventas (10 registros para que los reportes devuelvan datos) ──
INSERT INTO ventas (id, folio, fecha, subtotal, impuestos, descuento, total, metodo_pago,
                    usuario_id, sucursal_id, estado_pedido, created_at)
VALUES
  (1,  'V-0001', NOW(),                           86.21, 13.79, 0.00, 100.00, 'efectivo',     1, 1, 'entregado', NOW()),
  (2,  'V-0002', DATEADD('HOUR', -1, NOW()),      129.31, 20.69, 0.00, 150.00, 'tarjeta',     1, 1, 'entregado', NOW()),
  (3,  'V-0003', DATEADD('HOUR', -2, NOW()),       43.10,  6.90, 0.00,  50.00, 'efectivo',    1, 1, 'entregado', NOW()),
  (4,  'V-0004', DATEADD('HOUR', -3, NOW()),       86.21, 13.79, 5.00,  95.00, 'transferencia',1, 1, 'entregado', NOW()),
  (5,  'V-0005', DATEADD('DAY',  -1, NOW()),      172.41, 27.59, 0.00, 200.00, 'efectivo',    1, 1, 'entregado', NOW()),
  (6,  'V-0006', DATEADD('DAY',  -2, NOW()),       64.66, 10.34, 0.00,  75.00, 'tarjeta',     1, 1, 'entregado', NOW()),
  (7,  'V-0007', DATEADD('DAY',  -3, NOW()),       43.10,  6.90, 0.00,  50.00, 'efectivo',    1, 1, 'entregado', NOW()),
  (8,  'V-0008', DATEADD('DAY',  -5, NOW()),      258.62, 41.38, 10.00, 290.00,'efectivo',    1, 1, 'entregado', NOW()),
  (9,  'V-0009', DATEADD('DAY',  -7, NOW()),       86.21, 13.79, 0.00, 100.00, 'puntos',      1, 1, 'entregado', NOW()),
  (10, 'V-0010', DATEADD('DAY', -10, NOW()),      129.31, 20.69, 0.00, 150.00, 'tarjeta',     1, 1, 'entregado', NOW());

-- ── Detalles de venta ────────────────────────────────────────
INSERT INTO venta_detalles (id, venta_id, producto_id, cantidad, precio_unitario, subtotal)
VALUES
  (1,  1,  1, 2, 45.00,  90.00),
  (2,  2,  1, 3, 45.00, 135.00),
  (3,  3,  2, 2, 25.00,  50.00),
  (4,  4,  1, 2, 45.00,  90.00),
  (5,  5,  1, 4, 45.00, 180.00),
  (6,  6,  2, 3, 25.00,  75.00),
  (7,  7,  2, 2, 25.00,  50.00),
  (8,  8,  1, 6, 45.00, 270.00),
  (9,  9,  3, 1, 45.00,  45.00),
  (10, 10, 1, 3, 45.00, 135.00);

-- ── Auditoria (actividad reciente) ───────────────────────────
INSERT INTO auditoria (id, usuario, accion, entidad, entidad_id, detalle, fecha)
VALUES
  (1, 'admin@cafesoft.com', 'LOGIN',      'USUARIO', 1, 'Inicio de sesion exitoso', DATEADD('MINUTE', -5, NOW())),
  (2, 'admin@cafesoft.com', 'CREAR',      'VENTA',   1, 'Venta V-0001 registrada',  DATEADD('MINUTE', -4, NOW())),
  (3, 'admin@cafesoft.com', 'CREAR',      'CAMPANA', 1, 'Campana Clientes Activos', DATEADD('MINUTE', -3, NOW())),
  (4, 'admin@cafesoft.com', 'ACTUALIZAR', 'PRODUCTO',2, 'Precio actualizado',       DATEADD('MINUTE', -2, NOW())),
  (5, 'admin@cafesoft.com', 'LISTADO',    'VENTA',   0, 'Reporte de ventas',        DATEADD('MINUTE', -1, NOW()));
