-- HU-054 QA queries for the PostgreSQL 16 schema on origin/master.
-- The isolated local test branch has sucursal_id = 1.

-- Report by period and the entity query also used by top-products.
EXPLAIN (ANALYZE, BUFFERS, FORMAT TEXT)
SELECT v.*
FROM ventas v
WHERE v.fecha BETWEEN date_trunc('month', CURRENT_DATE)::timestamp
                   AND (date_trunc('month', CURRENT_DATE) + interval '1 month' - interval '1 microsecond')
  AND v.sucursal_id = 1;

-- Diagnostic aggregate for ranking. The current service loads sales and
-- eager detail/product relations, then aggregates in Java; inspect Hibernate
-- logs alongside this equivalent query for repeated detail fetches.
EXPLAIN (ANALYZE, BUFFERS, FORMAT TEXT)
SELECT d.producto_id,
       SUM(d.cantidad) AS unidades_vendidas,
       SUM(d.subtotal) AS monto_total
FROM ventas v
JOIN venta_detalles d ON d.venta_id = v.id
WHERE v.fecha BETWEEN date_trunc('month', CURRENT_DATE)::timestamp
                   AND (date_trunc('month', CURRENT_DATE) + interval '1 month' - interval '1 microsecond')
  AND v.sucursal_id = 1
GROUP BY d.producto_id
ORDER BY unidades_vendidas DESC, monto_total DESC;

-- Source query shape for the frequent-customer endpoint.
EXPLAIN (ANALYZE, BUFFERS, FORMAT TEXT)
SELECT v.*
FROM ventas v
WHERE v.fecha BETWEEN date_trunc('month', CURRENT_DATE)::timestamp
                   AND (date_trunc('month', CURRENT_DATE) + interval '1 month' - interval '1 microsecond')
  AND v.sucursal_id = 1
  AND v.cliente_ruta_id IS NOT NULL;

-- JPQL shape from SegmentacionRepository.actividadPorCliente().
EXPLAIN (ANALYZE, BUFFERS, FORMAT TEXT)
SELECT v.cliente_ruta_id,
       COUNT(v.id) AS compras,
       MAX(v.fecha) AS ultima_compra
FROM ventas v
WHERE v.cliente_ruta_id IS NOT NULL
GROUP BY v.cliente_ruta_id;
