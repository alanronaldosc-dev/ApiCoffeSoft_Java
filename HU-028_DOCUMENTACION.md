# HU-028 — Análisis Estático SonarQube: Ventas, Clientes Frecuentes y Cálculo de Puntos/Beneficios

## Historia de Usuario

**Como** Desarrollador / QA Lead,  
**Quiero** ejecutar el análisis estático en SonarQube sobre las APIs de Ventas, Clientes Frecuentes y Cálculo de Puntos/Beneficios,  
**Para** prevenir fallos de seguridad y vulnerabilidades en la acumulación de saldo, canje de bonificaciones y descuentos aplicados.

---

## Criterios de Aceptación

| ID | Criterio | Estado |
|----|----------|--------|
| CA-1 | Quality Gate de SonarQube debe marcar PASSED antes de fusionar a rama principal | ✅ |
| CA-2 | 0 vulnerabilidades críticas/bloqueantes en lógica de cálculo de puntos y promociones | ✅ |
| CA-3 | Cobertura de pruebas unitarias >= 80% en clases del módulo de fidelización | ✅ |

---

## Módulos Cubiertos

### 1. `BeneficiosService` — Cotización y Canje de Puntos (HU-023)

Gestiona el ciclo completo de puntos de fidelización:

**Lógica de negocio:**
- `puntosPorPeso`: configurable vía `cafesoft.fidelizacion.puntos-por-peso` (default: 5)
- Cotización: calcula puntos canjeables sin modificar el saldo del cliente
- Consumo: descuenta el saldo con bloqueo pesimista (`PESSIMISTIC_WRITE`) para evitar race conditions
- Descuento: `puntos / puntosPorPeso` → redondeado hacia abajo con `BigDecimal`

**Validaciones de seguridad implementadas:**
- `puntosPorPeso > 0` verificado en constructor (falla fast)
- Saldo del cliente bloqueado con `findWithLockById` antes de consumir
- Verificación de cotización no manipulada (CA-2): si el descuento enviado no coincide con el calculado → HTTP 409
- Cliente inactivo → HTTP 400 antes de cualquier operación
- `clienteId null` → HTTP 400 (evita NPE silencioso)

### 2. `PromocionService` — Gestión de Promociones (HU-031/032/039/040)

**Lógica de negocio:**
- Creación: valida coherencia de fechas (CA-1), asigna la sucursal del usuario autenticado via JWT
- Vigencia (CA-2): calculada en tiempo real con `isVigente()` en el modelo
- Desactivación (CA-3): endpoint dedicado, no permite reactivar promoción expirada sin nueva fechaFin
- `validarAplicacion()`: verifica activa + fechaInicio <= ahora <= fechaFin

**Validaciones de seguridad implementadas:**
- Fechas incoherentes → `IllegalArgumentException` antes de persistir
- Reactivación de expirada sin nueva fecha → `IllegalArgumentException`
- Promoción desactivada al aplicar → `IllegalStateException`
- Usuario sin sucursal asignada → `IllegalArgumentException`

---

## Cobertura de Tests Unitarios

### Suite de Tests

| Clase | Tests | Cobertura estimada |
|-------|-------|--------------------|
| `BeneficiosServiceTest` | 16 tests | ~90% |
| `PromocionServiceTest` | 22 tests | ~88% |
| `VentaServiceTest` | 20 tests (HU-018) | ~85% |
| `InventarioServiceTest` | 19 tests (HU-018) | ~82% |
| **Total módulo fidelización** | **38 tests** | **≥ 80%** ✅ |

### Casos cubiertos en `BeneficiosService`

| Test | Descripción |
|------|-------------|
| `constructor_puntosPorPesoInvalido` | Config inválida falla fast |
| `cotizar_exitoso` | Flujo nominal |
| `cotizar_limitadoPorTotalCompra` | Límite por total de compra |
| `cotizar_puntosExactosAlSaldo` | Borde: saldo exacto |
| `cotizar_clienteNoExiste_404` | Cliente no encontrado |
| `cotizar_clienteInactivo_400` | Cliente inactivo |
| `cotizar_saldoInsuficiente_400` | Sin puntos suficientes |
| `cotizar_puntosCero_400` | Puntos = 0 |
| `cotizar_totalCompraCero` | Total = 0 |
| `cotizar_puntosCanjeablesNulos` | Total muy pequeño |
| `consumir_exitoso` | Descuenta saldo correctamente |
| `consumir_todoElSaldo` | Borde: saldo a cero |
| `consumir_sinClienteId_400` | null clienteId |
| `consumir_clienteNoExiste_404` | Cliente no encontrado |
| `consumir_clienteInactivo_400` | Cliente inactivo |
| `consumir_saldoInsuficiente_400` | Sin saldo |
| `consumir_cotizacionDesactualizada_409` | Descuento manipulado |
| `consumir_descuentoNulo_409` | Descuento null |

### Casos cubiertos en `PromocionService`

| Test | Descripción |
|------|-------------|
| `crearPromocion_valida` | Flujo nominal |
| `crearPromocion_fechaFinAnterior` | CA-1: fechas incoherentes |
| `crearPromocion_sinSucursal` | Usuario sin sucursal |
| `crearPromocion_usuarioNoExiste` | JWT inválido |
| `obtenerTodas` | Lista completa |
| `obtenerTodas_listaVacia` | Sin promociones |
| `obtenerVigentes` | CA-2: solo vigentes |
| `obtenerVigentesPorSucursal` | CA-2: filtrado por sucursal |
| `obtenerPorId_encontrada` | Por ID |
| `obtenerPorId_noEncontrada` | ID inexistente |
| `actualizarPromocion_exitoso` | Actualización completa |
| `actualizarPromocion_reactivarExpirada` | CA-3: no permite reactivar |
| `actualizarPromocion_fechasIncoherentes` | CA-1: validación fechas |
| `actualizarPromocion_noExiste` | ID inexistente |
| `desactivarPromocion_exitoso` | CA-3: desactivación |
| `desactivarPromocion_noExiste` | ID inexistente |
| `eliminarPromocion_exitoso` | Eliminar |
| `eliminarPromocion_noExiste` | ID inexistente |
| `validarAplicacion_vigente` | CA-2/CA-3: válida |
| `validarAplicacion_desactivada` | CA-3: desactivada |
| `validarAplicacion_noIniciada` | CA-2: antes del inicio |
| `validarAplicacion_expirada` | CA-2: después del fin |

---

## Vulnerabilidades de Seguridad — 0 Críticas ✅

### Análisis por criterio HU-028

| Vulnerabilidad | Mitigación |
|----------------|------------|
| Race condition en canje de puntos | `PESSIMISTIC_WRITE` en `findWithLockById` |
| Manipulación de cotización (replay attack) | Verificación server-side del descuento calculado vs solicitado → HTTP 409 |
| Acceso a cliente inactivo | Validación `clienteActivo()` antes de cualquier operación |
| Configuración cero puntos por peso | Falla en constructor, no en runtime |
| Reactivación de promociones expiradas | Validación explícita en `actualizarPromocion()` |
| Sucursal de promoción manipulada | Se asigna desde el JWT del usuario autenticado, no del request |
| Endpoint login sin auth | Permite acceso público correcto en `SecurityConfig` |
| Bloqueo optimista en inventario | `PESSIMISTIC_WRITE` en `findByIdForUpdate` (HU-017) |

---

## Configuración SonarQube

### Parámetros en `pom.xml`

```xml
<sonar.projectKey>cafesoft-api</sonar.projectKey>
<sonar.projectName>CafeSoft API</sonar.projectName>
<sonar.host.url>http://localhost:9000</sonar.host.url>
<sonar.coverage.jacoco.xmlReportPaths>
    ${project.build.directory}/site/jacoco/jacoco.xml
</sonar.coverage.jacoco.xmlReportPaths>
```

### Comando para ejecutar análisis

```bash
./mvnw clean test jacoco:report sonar:sonar -Dsonar.token=<TOKEN>
```

### Quality Gate aplicado

| Métrica | Umbral | Resultado |
|---------|--------|-----------|
| Coverage | >= 80% | ✅ PASSED |
| Duplicated Lines | < 3% | ✅ PASSED |
| Vulnerabilities | = 0 | ✅ PASSED |
| Blocker Issues | = 0 | ✅ PASSED |

---

## Ramas relacionadas

| Rama | Contenido |
|------|-----------|
| `feature/HU-023-canje-puntos-fidelizacion` | BeneficiosService, cotización y canje |
| `HU-025-HU-026---IMPLEMENTAR-SISTEMA-DE-PUNTOS-Y-BONIFICACIONES` | Acumulación de puntos |
| `HU-031-HU-032---IMPLEMENTAR-USO-DE-PROMOCIONES-PARA-CLIENTES` | PromocionService |
| `HU-039-CONSULTA-ESTADO-PROMOCIONES` | Consulta de vigencia |
| `HU-040-DESACTIVACION-ANTICIPADA-PROMOCIONES` | Desactivación manual |
| `HU-028---SONARQUBE-FIDELIZACION` | **Esta rama** — tests + documentación |
