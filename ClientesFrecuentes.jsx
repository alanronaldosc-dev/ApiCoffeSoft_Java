import { useEffect, useState } from "react";
import api from "../services/api";

/**
 * HU-018 — Clientes Frecuentes
 *
 * Muestra:
 * 1. Selector de periodo y criterio de frecuencia.
 * 2. Lista completa con indicador de frecuente / no frecuente.
 * 3. Gráfica de dona SVG con los 5 clientes más frecuentes.
 */

const PERIODOS = [
  { id: "hoy",          etiqueta: "Hoy" },
  { id: "mes",          etiqueta: "Este mes" },
  { id: "mes_anterior", etiqueta: "Mes anterior" },
];

// Paleta de colores para la gráfica (top 5)
const COLORES = ["#7e4cfe", "#956bff", "#a37fff", "#b291fd", "#c4acff"];

function formatearMonto(valor) {
  return `$${Number(valor ?? 0).toFixed(2)}`;
}

// ─────────────────────────────────────────────────────────
// Gráfica de dona SVG pura (sin D3, sin dependencias extra)
// Muestra los 5 clientes con más compras
// ─────────────────────────────────────────────────────────
function DonaFrecuentes({ clientes }) {
  const top5 = clientes.slice(0, 5);
  const total = top5.reduce((s, c) => s + c.totalCompras, 0);

  if (total === 0) return null;

  const R       = 420;  // radio exterior
  const innerR  = R / 1.625;  // radio interior (hueco)
  const gap     = 0.015;  // separación entre segmentos (radianes)
  const corner  = 18;    // radio de esquina (efecto 3-D suave)

  let anguloActual = -Math.PI / 2;

  return (
    <div style={{ display: "flex", alignItems: "center", gap: "32px", flexWrap: "wrap" }}>

      {/* SVG de la dona */}
      <div style={{ position: "relative", width: "220px", flexShrink: 0 }}>

        {/* Texto central */}
        <div style={{
          position: "absolute", inset: 0,
          display: "flex", flexDirection: "column",
          alignItems: "center", justifyContent: "center",
          pointerEvents: "none",
        }}>
          <p style={{ margin: 0, fontSize: "12px", color: "#8D9DB6" }}>Top compras</p>
          <p style={{ margin: 0, fontSize: "26px", fontWeight: "800", color: "#f1f5f9" }}>
            {total}
          </p>
        </div>

        <svg
          viewBox={`-${R} -${R} ${R * 2} ${R * 2}`}
          style={{ width: "220px", height: "220px", overflow: "visible" }}
        >
          <defs>
            {top5.map((_, i) => (
              <linearGradient key={i} id={`grad-frecuente-${i}`}>
                <stop offset="55%" stopColor={COLORES[i]} stopOpacity="0.95" />
              </linearGradient>
            ))}
          </defs>

          {top5.map((cliente, i) => {
            const fraccion = cliente.totalCompras / total;
            const angulo   = fraccion * 2 * Math.PI - gap;
            const inicio   = anguloActual + gap / 2;
            const fin      = inicio + angulo;
            anguloActual  += fraccion * 2 * Math.PI;

            const x1o = Math.cos(inicio) * R;
            const y1o = Math.sin(inicio) * R;
            const x2o = Math.cos(fin) * R;
            const y2o = Math.sin(fin) * R;
            const x1i = Math.cos(fin) * innerR;
            const y1i = Math.sin(fin) * innerR;
            const x2i = Math.cos(inicio) * innerR;
            const y2i = Math.sin(inicio) * innerR;

            const largeArc = angulo > Math.PI ? 1 : 0;

            const d = [
              `M ${x1o.toFixed(2)} ${y1o.toFixed(2)}`,
              `A ${R} ${R} 0 ${largeArc} 1 ${x2o.toFixed(2)} ${y2o.toFixed(2)}`,
              `L ${x1i.toFixed(2)} ${y1i.toFixed(2)}`,
              `A ${innerR} ${innerR} 0 ${largeArc} 0 ${x2i.toFixed(2)} ${y2i.toFixed(2)}`,
              "Z",
            ].join(" ");

            // Etiqueta si el segmento es suficientemente grande
            const midAngle  = inicio + angulo / 2;
            const labelR    = R * 0.75;
            const lx        = Math.cos(midAngle) * labelR;
            const ly        = Math.sin(midAngle) * labelR;
            const pct       = (fraccion * 100).toFixed(0);
            const mostrar   = (angulo * 180) / Math.PI > 20;

            return (
              <g key={cliente.clienteId}>
                <path
                  d={d}
                  fill={`url(#grad-frecuente-${i})`}
                  stroke="#ffffff22"
                  strokeWidth={corner}
                />
                {mostrar && (
                  <g>
                    <text x={lx} y={ly - 14} textAnchor="middle"
                      fontSize={30} fontWeight="600" fill="#eee">
                      {cliente.nombre.length > 8
                        ? cliente.nombre.slice(0, 8) + "…"
                        : cliente.nombre}
                    </text>
                    <text x={lx} y={ly + 22} textAnchor="middle"
                      fontSize={26} fill="#eee" fillOpacity="0.7">
                      {pct}%
                    </text>
                  </g>
                )}
              </g>
            );
          })}
        </svg>
      </div>

      {/* Leyenda */}
      <ul style={{ listStyle: "none", margin: 0, padding: 0 }}>
        {top5.map((c, i) => (
          <li key={c.clienteId} style={{
            display: "flex", alignItems: "center", gap: "10px",
            marginBottom: "10px",
          }}>
            <span style={{
              width: "12px", height: "12px", borderRadius: "50%",
              background: COLORES[i], flexShrink: 0,
            }} />
            <span style={{ color: "var(--texto)", fontWeight: "600", fontSize: "14px" }}>
              {c.nombre}
            </span>
            <span style={{ color: "var(--texto-suave)", fontSize: "13px" }}>
              {c.totalCompras} compras · {formatearMonto(c.montoTotal)}
            </span>
          </li>
        ))}
      </ul>
    </div>
  );
}

// ─────────────────────────────────────────────────────────
// Componente principal
// ─────────────────────────────────────────────────────────
export default function ClientesFrecuentes() {
  const [periodo,        setPeriodo]        = useState("mes");
  const [minimoCompras,  setMinimoCompras]  = useState(3);
  const [clientes,       setClientes]       = useState([]);
  const [cargando,       setCargando]       = useState(false);
  const [error,          setError]          = useState(null);

  // Cargar datos cuando cambia el periodo o el criterio
  useEffect(() => {
    cargarClientes();
  }, [periodo, minimoCompras]);

  const cargarClientes = async () => {
    setCargando(true);
    setError(null);
    try {
      const res = await api.get(
        `/ventas/reporte/clientes-frecuentes?periodo=${periodo}&minimoCompras=${minimoCompras}`
      );
      setClientes(Array.isArray(res.data) ? res.data : []);
    } catch (err) {
      setError(
        err?.response?.data?.mensaje ||
        err?.response?.data?.message ||
        "No se pudo cargar la lista de clientes frecuentes."
      );
      setClientes([]);
    } finally {
      setCargando(false);
    }
  };

  // Separar frecuentes y no frecuentes para los contadores
  const frecuentes    = clientes.filter(c => c.esFrecuente);
  const noFrecuentes  = clientes.filter(c => !c.esFrecuente);

  return (
    <section className="panel">

      {/* ── ENCABEZADO ── */}
      <div style={{ marginBottom: "20px" }}>
        <h1>⭐ Clientes Frecuentes</h1>
        <p style={{ color: "var(--texto-suave)", margin: "4px 0 0", fontSize: "14px" }}>
          Identifica los clientes que compran con mayor regularidad.
          Se considera frecuente quien supera el mínimo de compras configurado.
        </p>
      </div>

      {/* ── CONTROLES ── */}
      <div style={{ display: "flex", gap: "24px", flexWrap: "wrap", marginBottom: "24px", alignItems: "flex-end" }}>

        {/* Selector de periodo */}
        <div>
          <p style={{ margin: "0 0 6px", fontSize: "12px", fontWeight: "700",
            textTransform: "uppercase", letterSpacing: "0.5px", color: "var(--texto-suave)" }}>
            Periodo
          </p>
          <div style={{ display: "flex", gap: "8px" }}>
            {PERIODOS.map(p => (
              <button
                key={p.id}
                onClick={() => setPeriodo(p.id)}
                style={{
                  padding: "7px 14px",
                  borderRadius: "8px",
                  border: "1px solid var(--borde)",
                  cursor: "pointer",
                  fontWeight: periodo === p.id ? "700" : "400",
                  background: periodo === p.id ? "var(--primario, #3B82F6)" : "var(--fondo-card)",
                  color: periodo === p.id ? "#fff" : "var(--texto)",
                }}
              >
                {p.etiqueta}
              </button>
            ))}
          </div>
        </div>

        {/* Criterio de frecuencia */}
        <div>
          <p style={{ margin: "0 0 6px", fontSize: "12px", fontWeight: "700",
            textTransform: "uppercase", letterSpacing: "0.5px", color: "var(--texto-suave)" }}>
            Mínimo de compras para ser frecuente
          </p>
          <input
            type="number"
            min="1"
            max="50"
            value={minimoCompras}
            onChange={e => setMinimoCompras(Math.max(1, Number(e.target.value)))}
            style={{
              width: "80px", padding: "7px 12px",
              borderRadius: "8px", border: "1px solid var(--borde)",
              background: "var(--fondo-card)", color: "var(--texto)",
              fontSize: "15px", fontWeight: "700",
            }}
          />
        </div>
      </div>

      {/* ── ERROR ── */}
      {error && (
        <div style={{
          background: "#2D1A1A", border: "1px solid #E05252",
          borderRadius: "10px", padding: "12px 16px", marginBottom: "16px", color: "#E05252",
        }}>
          ⚠️ {error}
        </div>
      )}

      {/* ── TARJETAS RESUMEN ── */}
      {!cargando && !error && (
        <div style={{ display: "grid", gridTemplateColumns: "repeat(3, 1fr)", gap: "14px", marginBottom: "24px" }}>
          <div style={{ background: "#1A2332", border: "1px solid #7e4cfe44", borderRadius: "12px", padding: "16px 20px" }}>
            <p style={{ fontSize: "11px", fontWeight: "700", textTransform: "uppercase",
              letterSpacing: "0.5px", color: "#7e4cfe", marginBottom: "6px" }}>
              ⭐ Clientes frecuentes
            </p>
            <p style={{ fontSize: "28px", fontWeight: "800", color: "#7e4cfe", margin: 0 }}>
              {frecuentes.length}
            </p>
            <p style={{ fontSize: "12px", color: "#8D9DB6", margin: "4px 0 0" }}>
              ≥ {minimoCompras} compras en el periodo
            </p>
          </div>
          <div style={{ background: "#1A2332", border: "1px solid #3AC87A44", borderRadius: "12px", padding: "16px 20px" }}>
            <p style={{ fontSize: "11px", fontWeight: "700", textTransform: "uppercase",
              letterSpacing: "0.5px", color: "#3AC87A", marginBottom: "6px" }}>
              👥 Total clientes con compras
            </p>
            <p style={{ fontSize: "28px", fontWeight: "800", color: "#3AC87A", margin: 0 }}>
              {clientes.length}
            </p>
            <p style={{ fontSize: "12px", color: "#8D9DB6", margin: "4px 0 0" }}>
              clientes con al menos 1 compra
            </p>
          </div>
          <div style={{ background: "#1A2332", border: "1px solid #FF990044", borderRadius: "12px", padding: "16px 20px" }}>
            <p style={{ fontSize: "11px", fontWeight: "700", textTransform: "uppercase",
              letterSpacing: "0.5px", color: "#FF9900", marginBottom: "6px" }}>
              💰 Monto total frecuentes
            </p>
            <p style={{ fontSize: "28px", fontWeight: "800", color: "#FF9900", margin: 0 }}>
              {formatearMonto(frecuentes.reduce((s, c) => s + c.montoTotal, 0))}
            </p>
            <p style={{ fontSize: "12px", color: "#8D9DB6", margin: "4px 0 0" }}>
              generado por clientes frecuentes
            </p>
          </div>
        </div>
      )}

      {/* ── GRÁFICA TOP 5 ── */}
      {!cargando && !error && clientes.length > 0 && (
        <div style={{
          background: "var(--fondo-card)", border: "1px solid var(--borde)",
          borderRadius: "14px", padding: "20px 24px", marginBottom: "24px",
        }}>
          <h3 style={{ margin: "0 0 16px", fontSize: "15px", fontWeight: "700", color: "var(--texto)" }}>
            🏆 Top 5 clientes más frecuentes
          </h3>
          <DonaFrecuentes clientes={clientes} />
        </div>
      )}

      {/* ── TABLA COMPLETA ── */}
      {cargando ? (
        <p style={{ color: "var(--texto-suave)" }}>Cargando clientes...</p>
      ) : !error && clientes.length === 0 ? (
        <p style={{ color: "var(--texto-suave)" }}>
          No hay compras registradas en el periodo seleccionado.
        </p>
      ) : !error && (
        <div style={{ background: "var(--fondo-card)", border: "1px solid var(--borde)",
          borderRadius: "14px", overflow: "hidden" }}>
          <div style={{ padding: "16px 20px", borderBottom: "1px solid var(--borde)" }}>
            <h3 style={{ margin: 0, fontSize: "15px", fontWeight: "700", color: "var(--texto)" }}>
              📋 Listado completo — {clientes.length} clientes
            </h3>
          </div>
          <table>
            <thead>
              <tr>
                <th>#</th>
                <th>Cliente</th>
                <th>Compras</th>
                <th>Monto total</th>
                <th>Estado</th>
              </tr>
            </thead>
            <tbody>
              {clientes.map((c, i) => (
                <tr key={c.clienteId}>
                  <td style={{ color: "var(--texto-suave)" }}>{i + 1}</td>
                  <td><strong>{c.nombre}</strong></td>
                  <td style={{ fontWeight: "700",
                    color: c.esFrecuente ? "#7e4cfe" : "var(--texto)" }}>
                    {c.totalCompras}
                  </td>
                  <td>{formatearMonto(c.montoTotal)}</td>
                  <td>
                    {c.esFrecuente ? (
                      <span style={{
                        background: "#7e4cfe22", color: "#7e4cfe",
                        border: "1px solid #7e4cfe44",
                        borderRadius: "999px", padding: "3px 12px",
                        fontSize: "12px", fontWeight: "600",
                      }}>
                        ⭐ Frecuente
                      </span>
                    ) : (
                      <span style={{
                        background: "#1A2332", color: "#8D9DB6",
                        border: "1px solid #334155",
                        borderRadius: "999px", padding: "3px 12px",
                        fontSize: "12px",
                      }}>
                        Ocasional
                      </span>
                    )}
                  </td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>
      )}

    </section>
  );
}
