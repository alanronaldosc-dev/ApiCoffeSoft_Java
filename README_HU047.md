# HU-047 — Prueba de Carga: Analítica y Dashboard

**Rol:** Performance QA (Homero)  
**Criterios de aceptación:**
- El sistema soporta la carga esperada sin fallos (tasa de error = 0 %).
- El administrador no percibe lentitud en el dashboard (p95 de tiempo de respuesta < 1 500 ms).

---

## 1. Paso a paso completo

### 1.1 Traer los últimos cambios del repositorio

```bash
cd c:\Proyectos\ApiCoffeSoft_Java
git pull
```

### 1.2 Levantar la API localmente

```bash
mvnw.cmd spring-boot:run
# o desde IntelliJ: Run → ApiSpringCafeSoftApplication
```

Verifica que el servidor esté en `http://localhost:8080`.  
Puedes comprobarlo con:

```
GET http://localhost:8080/v3/api-docs
```

### 1.3 Requisitos previos para JMeter

| Requisito | Versión mínima |
|-----------|---------------|
| Apache JMeter | 5.6.x |
| Java (JRE/JDK) | 17 |

Descarga JMeter desde: https://jmeter.apache.org/download_jmeter.cgi

### 1.4 Ajustar credenciales en el plan de prueba

Abre `HU-047_Dashboard_Analytics.jmx` en JMeter y edita las **User Defined Variables** del `Test Plan`:

| Variable | Valor por defecto | Cambiar a |
|----------|-------------------|-----------|
| `HOST` | `localhost` | IP/host de la API si no es local |
| `PORT` | `8080` | Puerto real de la API |
| `ADMIN_EMAIL` | `admin@cafesoft.com` | Email de un usuario ADMIN real |
| `ADMIN_PASSWORD` | `admin123` | Contraseña real del ADMIN |

> **Nota:** El plan obtiene el JWT automáticamente en el SetupThread y lo inyecta en todos los requests de carga.

### 1.5 Ejecutar desde la GUI de JMeter (modo validación)

1. Abre JMeter → `File → Open` → selecciona `HU-047_Dashboard_Analytics.jmx`.
2. Activa el listener **[Debug] View Results Tree** (clic derecho → Enable) para ver respuestas.
3. Haz clic en **▶ Start** (Ctrl + R).
4. Revisa que el SetupThread obtenga el token sin errores.
5. Verifica que todos los samplers muestren barra verde.
6. Desactiva el View Results Tree y vuelve a Step 1.7 para carga real.

### 1.6 Ejecutar en modo headless (carga real)

```bash
jmeter -n -t HU-047_Dashboard_Analytics.jmx -l HU-047_resultados.jtl -e -o HU-047_report/
```

| Flag | Significado |
|------|-------------|
| `-n` | No-GUI (headless) |
| `-t` | Plan de prueba |
| `-l` | Archivo JTL de salida |
| `-e -o` | Genera informe HTML en la carpeta indicada |

### 1.7 Ver el informe HTML

Abre `HU-047_report/index.html` en el navegador.

---

## 2. Endpoints incluidos en la prueba

| ID | Método | Endpoint | Descripción |
|----|--------|----------|-------------|
| A1 | GET | `/api/ventas/reporte?periodo=hoy` | Reporte de ventas del día |
| A2 | GET | `/api/ventas/reporte?periodo=mes` | Reporte de ventas del mes |
| A3 | GET | `/api/ventas/reporte/top-productos?periodo=hoy` | Productos más vendidos hoy |
| A4 | GET | `/api/ventas/reporte/top-productos?periodo=mes` | Productos más vendidos del mes |
| A5 | GET | `/api/ventas` | Listado general de ventas |
| B1 | GET | `/api/clientes/segmentos/resumen` | Resumen de segmentación (ADMIN) |
| B2 | GET | `/api/clientes/segmentos` | Segmentos de clientes (ADMIN) |
| C1 | GET | `/api/campanas/vigentes` | Campañas vigentes |
| C2 | GET | `/api/campanas` | Todas las campañas |
| D1 | GET | `/api/auditoria` | Registro de actividad reciente |
| E1 | GET | `/api/productos` | Catálogo de productos |
| E2 | GET | `/api/inventario` | Estado del inventario |

---

## 3. Parámetros de carga configurados

| Parámetro | Valor |
|-----------|-------|
| Usuarios concurrentes | 20 |
| Ramp-up | 10 s |
| Duración de la prueba | 60 s |
| Think time entre requests | 200–500 ms (aleatorio) |
| Timeout de conexión | 5 000 ms |
| Timeout de respuesta | 3 000 ms |

---

## 4. Criterios de aceptación y umbrales de aserciones

Cada request tiene dos aserciones automáticas en JMeter:

1. **Código HTTP = 200** — fallo si devuelve 4xx o 5xx.
2. **Duración < 1 500 ms** — fallo si supera el umbral.

### Criterios para aprobar la HU

| Métrica | Umbral | ✅ Aprobado |
|---------|--------|------------|
| Tasa de error | 0 % | Ningún fallo HTTP ni timeout |
| Tiempo p95 | < 1 500 ms | Visible en el Aggregate Report |
| Throughput | Estable | Sin caídas bruscas en la gráfica |
| Errores de aserción JMeter | 0 | Todas las DurationAssertion en verde |

---

## 5. Resultados esperados (plantilla)

> Rellena esta tabla después de ejecutar la prueba y adjunta el `HU-047_report/`.

| Endpoint | # Muestras | Error % | Avg (ms) | p90 (ms) | p95 (ms) | p99 (ms) | Throughput (req/s) |
|----------|-----------|---------|----------|----------|----------|----------|--------------------|
| A1 – /ventas/reporte (hoy) | — | — | — | — | — | — | — |
| A2 – /ventas/reporte (mes) | — | — | — | — | — | — | — |
| A3 – top-productos (hoy) | — | — | — | — | — | — | — |
| A4 – top-productos (mes) | — | — | — | — | — | — | — |
| A5 – /ventas | — | — | — | — | — | — | — |
| B1 – /segmentos/resumen | — | — | — | — | — | — | — |
| B2 – /segmentos | — | — | — | — | — | — | — |
| C1 – /campanas/vigentes | — | — | — | — | — | — | — |
| C2 – /campanas | — | — | — | — | — | — | — |
| D1 – /auditoria | — | — | — | — | — | — | — |
| E1 – /productos | — | — | — | — | — | — | — |
| E2 – /inventario | — | — | — | — | — | — | — |
| **TOTAL** | — | **0 %** | — | — | **< 1 500** | — | — |

**Veredicto:** ☐ APROBADO &nbsp;&nbsp; ☐ RECHAZADO  
**Fecha de ejecución:** _______________  
**Ejecutado por:** Homero (Performance QA)

---

## 6. Problemas comunes

| Síntoma | Causa probable | Solución |
|---------|---------------|----------|
| SetupThread falla en login | Credenciales incorrectas | Ajusta `ADMIN_EMAIL`/`ADMIN_PASSWORD` |
| 401 en todos los endpoints | Token no propagado | Verifica que `props.put("GLOBAL_JWT", ...)` en el JSR223 se ejecutó |
| 403 en `/api/clientes/segmentos` | El usuario no tiene rol ADMIN | Usa una cuenta con `userTipo = 1` (ADMIN) |
| `Connection refused` | API no levantada | Levanta la API antes de correr JMeter |
| DurationAssertion falla en /ventas | Consulta MongoDB lenta | Revisar índices en colección `ventas` |
