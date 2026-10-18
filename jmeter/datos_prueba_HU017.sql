-- ============================================================
-- Datos de prueba para HU-017 — CafeSoft
-- Corregido con la estructura real de tablas Hibernate
-- ============================================================

-- 1. Categoría (con activo y created_at obligatorios)
INSERT INTO categorias (nombre, descripcion, activo, created_at)
VALUES ('Bebidas JMeter', 'Categoria de prueba HU-017', true, NOW())
ON CONFLICT (nombre) DO NOTHING;

-- 2. Producto (sin columna activo — no existe en la tabla)
INSERT INTO productos (nombre, descripcion, precio, categoria_id, sucursal_id)
VALUES (
  'Producto JMeter',
  'Producto para prueba de carga HU-017',
  50.00,
  (SELECT id FROM categorias WHERE nombre = 'Bebidas JMeter' LIMIT 1),
  (SELECT id FROM sucursales WHERE nombre = 'Sucursal Centro' LIMIT 1)
)
ON CONFLICT DO NOTHING;

-- 3. Insumo (tabla separada de inventario)
INSERT INTO insumos (nombre, tipo, unidad_medida, precio, sucursal_id)
VALUES (
  'Insumo JMeter',
  'Materia Prima',
  'piezas',
  5.00,
  (SELECT id FROM sucursales WHERE nombre = 'Sucursal Centro' LIMIT 1)
)
ON CONFLICT DO NOTHING;

-- 4. Inventario (stock = 300, unidad_medida = piezas)
INSERT INTO inventario (nombre, tipo, cantidad, cantidad_minima, unidad_medida, precio_unitario, sucursal_id)
VALUES (
  'Insumo JMeter',
  'Materia Prima',
  300.0,
  10.0,
  'piezas',
  5.00,
  (SELECT id FROM sucursales WHERE nombre = 'Sucursal Centro' LIMIT 1)
)
ON CONFLICT DO NOTHING;

-- 5. Relacion producto-insumo (tabla: producto_insumo sin s)
INSERT INTO producto_insumo (producto_id, insumo_id, cantidad, unidad_medida)
VALUES (
  (SELECT id FROM productos WHERE nombre = 'Producto JMeter' LIMIT 1),
  (SELECT id FROM inventario WHERE nombre = 'Insumo JMeter' LIMIT 1),
  1.0,
  'piezas'
)
ON CONFLICT DO NOTHING;

-- 6. Lote (apunta a tabla insumos, no inventario)
INSERT INTO lotes (cantidad, fecha_caducidad, fecha_entrada, tipo_lote, insumo_id, sucursal_id)
VALUES (
  300.0,
  CURRENT_DATE + INTERVAL '1 year',
  CURRENT_DATE,
  'insumo',
  (SELECT id FROM insumos WHERE nombre = 'Insumo JMeter' LIMIT 1),
  (SELECT id FROM sucursales WHERE nombre = 'Sucursal Centro' LIMIT 1)
);

-- ============================================================
-- Verificacion final
-- ============================================================
SELECT 'sucursal_id=' || id FROM sucursales WHERE nombre = 'Sucursal Centro';
SELECT 'usuario_id=' || id_usuario || ' email=' || email FROM usuarios WHERE email = 'admin@gmail.com';
SELECT 'producto_id=' || id FROM productos WHERE nombre = 'Producto JMeter';
SELECT 'inventario_id=' || id || ' stock=' || cantidad FROM inventario WHERE nombre = 'Insumo JMeter';
SELECT 'insumo_id=' || id FROM insumos WHERE nombre = 'Insumo JMeter';
SELECT 'lote_id=' || id || ' cantidad=' || cantidad FROM lotes ORDER BY id DESC LIMIT 1;
SELECT 'producto_insumo_id=' || id FROM producto_insumo WHERE producto_id = (SELECT id FROM productos WHERE nombre = 'Producto JMeter' LIMIT 1);
