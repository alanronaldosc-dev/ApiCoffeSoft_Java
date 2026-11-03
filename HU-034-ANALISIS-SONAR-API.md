# HU-034 — API de promociones por segmento y seguridad

**Rama:** `HU-034-sonarqube-api-promociones`

**Repositorios:** `ApiCoffeSoft_Java` y `CafeSoft_React`
**Resultado:** implementación y pruebas focalizadas listas; el Quality Gate actualizado sigue pendiente de autenticación en SonarQube.

## Alcance implementado

- CRUD de promociones globales segmentadas por `TODOS`, `ACTIVO`, `POTENCIAL`,
  `DORMIDO` o `POCO_FRECUENTE`.
- La segmentación se calcula con compras vinculadas al cliente en ventas. Las
  ventas históricas permanecen sin cliente relacionado.
- El servidor valida vigencia, segmento, cantidad mínima y calcula el descuento
  a partir del precio del producto persistido.
- Administración de promociones y consulta de segmentos: solo `ADMIN`.
  Consulta de promociones aplicables y registro de ventas: `ADMIN` o `USER`.
  Las cuentas `CLIENT` no pueden administrar promociones, cotizarlas ni crear
  ventas.
- Canje de puntos con saldo local: 5 puntos equivalen a $1 MXN; la cotización no
  consume puntos y limita el descuento al total. El cobro vuelve a validar el
  saldo bajo bloqueo de fila y lo descuenta en la misma transacción de la venta.
  Promociones y puntos no se combinan.
- Los saldos iniciales se cargan mediante un proceso administrativo controlado.
  No se acumulan puntos automáticamente en esta integración.

## Endpoints

Los endpoints requieren JWT. El prefijo `/api` corresponde al backend Java.

| Método y ruta | Acceso | Propósito |
|---|---|---|
| `GET /api/promociones` | ADMIN | Consultar promociones |
| `POST /api/promociones` | ADMIN | Crear promoción |
| `PUT /api/promociones/{id}` | ADMIN | Actualizar promoción |
| `PUT /api/promociones/{id}/desactivar` | ADMIN | Desactivar promoción |
| `DELETE /api/promociones/{id}` | ADMIN | Eliminar promoción no aplicada |
| `GET /api/promociones/aplicables?clienteId=…&cantidadGarrafones=…` | ADMIN, USER | Consultar promociones del cliente |
| `GET /api/clientes/segmentos` y `/resumen` | ADMIN | Consultar segmentación |
| `GET /api/clientes` | ADMIN, USER | Listar clientes activos y sus saldos |
| `GET /api/clientes/{id}/puntos` | ADMIN, USER | Consultar saldo e historial de canjes |
| `POST /api/clientes/{id}/beneficios/cotizaciones` | ADMIN, USER | Cotizar sin consumir puntos |
| `POST /api/ventas` | ADMIN, USER | Registrar venta y aplicar promoción o canje validado |

El frontend envía `clienteId`, `promocionId` o `puntosCanjeados` al registrar una
venta. El servidor ignora importes de descuento manipulados y calcula el total
con precios de productos de base de datos. El saldo se almacena en
`usuarios.saldo_puntos` y el número de puntos canjeados en `ventas.puntos_canjeados`.
Hibernate está configurado con `ddl-auto=update`; no se verificó la actualización
contra la base local porque sus credenciales de PostgreSQL no son válidas en
este entorno.

Para cargar un saldo inicial, un operador autorizado puede ejecutar un cambio
controlado equivalente a:

```sql
UPDATE usuarios
SET saldo_puntos = <saldo_inicial>
WHERE id_usuario = <id_cliente>
  AND user_tipo = 2;
```

No se debe aceptar `saldoPuntos` de un request de cliente ni usar la cotización
del navegador como autorización de descuento. La aplicación local usa HTTP;
el despliegue debe terminar TLS y servir estos endpoints por HTTPS.

## Matriz de pruebas

| Prueba | Resultado |
|---|---|
| Servicios de promociones y segmentación, venta con descuento recalculado y reglas de puntos | 17 pruebas focalizadas aprobadas, 0 fallidas. |
| Seguridad de rutas y roles de promoción, segmentación, puntos y venta | Incluida en las 17 aprobadas; `CLIENT` recibe 403 en operaciones protegidas. |
| Suite Java completa | 22 de 23 pruebas pasan. `ApiSpringCafeSoftApplicationTests.contextLoads` no puede iniciar Hibernate porque PostgreSQL rechaza la contraseña local de `postgres`; no es una falla de aserción de HU-034. |
| Build React y ESLint de archivos modificados | Aprobados. Vite mantiene una advertencia de tamaño de chunk mayor a 500 kB. |
| Pruebas Vitest React | 6 aprobadas, 0 fallidas. Incluyen selección de promoción y cotización/envío de puntos. |
| Validación de runtime por plan | La primera ejecución pasó; la repetición no pudo ejecutar `npm ci` por `EPERM` al quitar un binario bloqueado. Build, lint y Vitest se ejecutaron después manualmente y pasaron. |

Las pruebas Java se ejecutaron con `JAVA_TOOL_OPTIONS=-Dnet.bytebuddy.experimental=true`
porque este entorno usa Java 27 con Byte Buddy 1.14.9; el proyecto compila con
`--release 17`.

## Estado de SonarQube

La instancia gratuita local SonarQube Community Build `26.10.0.133078` responde
`UP` en `http://localhost:9000`. No se pudo obtener un análisis completo nuevo
para esta implementación:

- `sonar:sonar` no estaba registrado como prefijo Maven; se intentó el goal
  completo del plugin SonarScanner Maven.
- El escáner fue rechazado con HTTP `401` al solicitar el JRE del análisis.
  No hay token disponible en el entorno y no se guardaron secretos en Git.
- Se intentó analizar por archivo los controladores y servicios de promociones,
  segmentación, puntos y ventas, además de `SecurityConfig`. SonarQube for IDE
  confirmó que el workspace no está enlazado a un proyecto remoto y no devolvió
  hallazgos de seguridad. Estos intentos no constituyen un análisis de proyecto
  ni un Quality Gate.
- El intento previo de enlazar el workspace con SonarQube for IDE terminó con
  error interno. Sigue siendo necesario configurar Connected Mode con un
  proyecto y credenciales autorizadas.

El último escaneo completo disponible es anterior a la implementación de esta
API. En ese estado base reportó 10 vulnerabilidades abiertas (4 críticas, 6
mayores) y 29 code smells. Esos conteos no representan un escaneo actualizado
de esta rama y no deben presentarse como Quality Gate de HU-034. También siguen
pendientes de revisión los hallazgos heredados de CORS permisivo y binding de
entidades en controladores fuera del alcance directo de esta historia.

## Cierre

La integración de promociones/segmentación y la validación del canje de puntos
están implementadas, y sus pruebas focalizadas pasan. HU-034 no debe declararse
“sin vulnerabilidades” ni con Quality Gate aprobado hasta ejecutar un escaneo
completo autenticado contra el servidor SonarQube y revisar los hallazgos
vigentes. La suite completa también requiere corregir la configuración local de
PostgreSQL para que pase el test de contexto.
