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

## Resumen listo para el reporte académico

La entidad seleccionada para la sincronización es la promoción aplicada a una
venta y su segmento de cliente. La API Java ofrece consulta y administración
de promociones, consulta de segmentos, cotización de puntos y registro de
ventas. La aplicación React consulta la API, permite seleccionar una promoción
o cotizar un canje, y envía la venta. El descuento no se confía al navegador:
el servidor lo vuelve a calcular usando los precios persistidos, el estado y
vigencia de la promoción, el segmento y, cuando aplica, el saldo de puntos.

Se agregaron servicios y controladores de promociones, segmentos y puntos; se
establecieron reglas de autorización por rol; y se amplió la venta para
relacionarla con el cliente/promoción y registrar el canje. El saldo se valida
y descuenta con bloqueo de fila dentro de la transacción. Las promociones no
se combinan con puntos. La integración usa un saldo local que comienza en cero
y debe cargarse por un proceso administrativo; no acumula puntos
automáticamente.

La evidencia de pruebas disponible es: 17 pruebas Java focalizadas aprobadas,
6 pruebas Vitest aprobadas y build/lint focalizado de React aprobados. En la
suite Java total, 22 de 23 pruebas pasan; el test de contexto no pudo iniciar
por la contraseña PostgreSQL local. Esto se informa como limitación del
entorno, no como prueba de funcionamiento integrado con una base QA.

## Guion para la demostración

1. **Explicar el flujo:** React envía una consulta de promociones con el cliente
   y la cantidad; Java decide qué promociones aplican. Al cobrar, Java valida
   nuevamente y calcula el importe. El navegador no puede imponer un
   descuento.
2. **Mostrar el control de acceso:** enseñar `SecurityConfig` y la matriz de
   endpoints de este documento. Explicar que administración/segmentación
   requiere `ADMIN`, mientras consulta de promociones aplicables y venta
   permite `ADMIN`/`USER`. Las rutas de API requieren JWT.
3. **Mostrar reglas de negocio:** recorrer el servicio de promociones y
   segmentación; indicar vigencia, cantidad mínima y categorías de segmento.
   Mostrar el servicio de puntos para la regla de 5 puntos por $1 MXN, la
   cotización no consumible y el descuento protegido por transacción.
4. **Mostrar el cliente React:** abrir el carrito, agregar productos y
   seleccionar cliente. Mostrar cómo aparece una promoción aplicable o cómo se
   solicita una cotización de puntos. Aclarar que no pueden combinarse.
5. **Mostrar pruebas automatizadas:** presentar las pruebas focalizadas Java y
   las seis pruebas Vitest. Indicar honestamente que la prueba de contexto
   completa está bloqueada por la configuración local de PostgreSQL.
6. **Mostrar SonarQube:** si se abre `http://localhost:9000`, aclarar que el
   servidor responde `UP`, pero no hay análisis completo reciente ni Quality
   Gate de esta rama. El intento de escaneo recibió HTTP 401 y no se pudo
   completar Connected Mode. No presentar el escaneo base anterior ni los
   intentos de análisis por archivo como resultado de HU-034.
7. **Concluir con los límites:** el despliegue local usa HTTP; producción debe
   usar HTTPS. El esquema nuevo requiere verificarse en una base QA. El saldo
   inicial se carga administrativamente y no se acumula por compras en esta
   implementación.

### Texto breve para decir durante la exposición

> “HU-034 integra promociones por segmento en el flujo de venta. La interfaz
> consulta y presenta las opciones, pero el backend es quien autoriza y
> recalcula el descuento; también valida puntos y evita consumos simultáneos
> del mismo saldo. Las pruebas focalizadas pasan. No afirmo que el Quality Gate
> esté aprobado: falta autenticación para ejecutar el análisis Sonar completo,
> y una prueba de contexto necesita credenciales válidas de PostgreSQL.”

### Material que conviene tener abierto

- Este reporte, especialmente la tabla de endpoints, la matriz de pruebas y el
  estado de SonarQube.
- La pantalla React de promociones y el carrito, con datos ficticios o de QA.
- Las clases `SecurityConfig`, `PromocionService`,
  `SegmentacionClienteService`, `PuntosService` y `VentaService`.
- La terminal con la salida de pruebas. No ejecutar una venta real en
  producción ni mostrar tokens, contraseñas o datos personales.
