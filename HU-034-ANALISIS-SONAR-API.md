# HU-034 - Análisis estático de seguridad de la API de promociones

**Rama:** `HU-034-sonarqube-api-promociones`  
**Repositorio analizado:** `ApiCoffeSoft_Java`  
**Alcance solicitado:** API de promociones personalizadas por segmento de cliente

## Resultado de inspección del repositorio

Se inspeccionaron las fuentes Java disponibles y las ramas locales/remotas
presentes en este clon. El repositorio contiene controladores y servicios para
categorías, insumos, inventario, lotes, productos, proveedores, usuarios y
ventas, pero **no contiene implementación Java de promociones ni segmentación
de clientes**. No se encontraron clases de promociones, endpoints
`/api/promociones` o endpoints de segmentación de clientes en las fuentes Java
disponibles.

Por este motivo, no es posible validar en este repositorio las reglas
específicas solicitadas por HU-034:

- autorización por rol/sucursal para crear o asignar promociones;
- manipulación de parámetros de segmento o identificadores de cliente;
- validación de descuentos y fechas de expiración en el servidor;
- prevención de aplicar promociones vencidas o fuera del segmento asignado.

La interfaz web contiene referencias a esos endpoints, pero eso no demuestra
que exista una API correspondiente ni que el servidor aplique controles de
seguridad. Se necesita integrar o identificar el backend que implemente esa
funcionalidad antes de emitir una conclusión de seguridad sobre ella.

## Estado del análisis SonarQube

Se ejecutó un análisis de servidor con SonarQube Community Build
26.10.0.133078, instalado localmente en `http://localhost:9000`. El proyecto
`cafesoft-api-hu034` analizó 58 fuentes Java y 4,351 líneas de código. El
Quality Gate reportó **PASSED**, pero eso no significa que el código esté libre
de vulnerabilidades: el análisis encontró issues abiertos que se detallan
abajo. No es un análisis de SonarQube Cloud ni cubre una API de promociones que
no existe en este repositorio.

| Métrica del proyecto | Resultado |
|---|---:|
| Quality Gate | PASSED |
| Bugs | 0 |
| Vulnerabilidades abiertas | 10 (4 CRITICAL, 6 MAJOR) |
| Security Hotspots reportados | 0 |
| Code smells abiertos | 29 |
| Duplicación | 7.2% |
| Cobertura | 0.0%; no se importó reporte de cobertura |

El proyecto `cafesoft-react-hu033` también se analizó desde esta instancia y
obtuvo Quality Gate PASSED con 0 bugs, 0 vulnerabilidades, 0 Security Hotspots
y 0 code smells en sus dos archivos objetivo.

La instancia y sus proyectos están en esta máquina, no en la nube. El token de
análisis temporal se revocó al terminar. No se guardaron tokens en los
repositorios.

## Hallazgo de revisión de seguridad manual

La revisión del código Java existente identificó un problema de **control de
acceso roto (severidad alta, confianza 9/10)**: no se encontró autenticación ni
autorización de servidor que proteja las rutas de la API. El controlador de
usuarios expone operaciones de lectura y administración de cuentas, y se
reportan rutas de ventas para crear o modificar registros sin controles de
acceso. Además, el flujo de registro acepta el tipo privilegiado de usuario.

Referencias al estado base revisado: `controller/UsuarioController.java`
(líneas 31-32, 101-104, 288-290, 804-813 y 864-905),
`controller/VentaController.java` (líneas 67-69 y 419-420), y
`service/UsuarioService.java` (líneas 50 y 69-73). Este es un hallazgo de la
revisión de seguridad, **no un resultado confirmado por SonarQube**. Esta rama
mitiga parcialmente el acceso anónimo con JWT, pero no cierra la autorización
por rol en los demás controladores.

El análisis Sonar actual además señala tres vulnerabilidades críticas por
recepción directa de entidades persistentes desde el request body en
`UsuarioController.java` (alta y actualización de usuarios) y
`ProveedorController.java` (alta de proveedor), una vulnerabilidad crítica
relacionada con la configuración de CSRF y seis hallazgos mayores de CORS
permisivo. Los hallazgos de CORS
aparecen en `WebConfig.java` y varios controladores con `@CrossOrigin("*")`.
Se deben revisar y corregir antes de declarar segura la API. El escaneo no
detectó vulnerabilidades de inyección en este análisis.

## Endurecimiento agregado en esta rama

- Se agregó autenticación stateless con JWT Bearer y expiración configurable.
- Todos los endpoints bajo `/api/**` requieren token, excepto login y registro
  público.
- Las operaciones `/api/usuarios/**` requieren rol administrador, excepto
  login y registro. El registro sin administrador autenticado fuerza el tipo
  cliente (`userTipo=2`) y elimina permisos recibidos del cliente.
- El login ahora devuelve `token` además del DTO de usuario.
- Se agregaron pruebas de token, acceso a rutas y restricción del rol durante
  el registro.
- Se eliminó una declaración duplicada/mal formada de búsqueda de usuarios
  que impedía compilar el backend.

**Límite importante:** las rutas generales de la API requieren autenticación,
pero esta rama todavía no define una matriz de autorización por rol para cada
controlador fuera de usuarios. Por lo tanto, no se debe concluir que cada
operación de ventas, inventario o administración tenga ya el permiso mínimo
necesario. La corrección y el despliegue también requieren configurar `JWT_SECRET`
en el entorno; no se incluye una clave en el repositorio.

Ejemplo PowerShell para crear una clave aleatoria solo en la sesión actual (no
la guardes en Git):

```powershell
$bytes = New-Object byte[] 32
$rng = [System.Security.Cryptography.RandomNumberGenerator]::Create()
$rng.GetBytes($bytes)
$env:JWT_SECRET = [Convert]::ToBase64String($bytes)
```

La variable debe configurarse también de forma segura en el entorno de
despliegue antes de iniciar la API.

La cuenta administradora inicial debe provisionarse mediante un proceso
controlado de base de datos/operaciones. Después de ese bootstrap, solo un
administrador autenticado puede crear cuentas con tipos privilegiados desde
la API.

## Compilación y pruebas

El estado inicial de esta rama tenía un paréntesis de cierre extra y una
declaración duplicada de `buscarUsuariosPorNombre` en
`UsuarioController.java`; se eliminaron para poder compilar el API.

| Validación | Resultado |
|---|---|
| `mvnw.cmd -DskipTests package` | Aprobada tras corregir el error de sintaxis preexistente. |
| Pruebas focalizadas (`JwtServiceTest`, `SecurityConfigTest`, `UsuarioControllerSecurityTest`) | 8 pruebas aprobadas, 0 fallidas. |
| `mvnw.cmd test` completo | Bloqueado al iniciar el contexto de pruebas existente por autenticación fallida de PostgreSQL. |
| SonarQube Community Build para `cafesoft-api-hu034` | Ejecutado; Quality Gate PASSED con 10 vulnerabilidades y 29 code smells abiertos. |
| Pruebas de API de promociones/segmentación | No aplicables: no existen esos endpoints/clases en este repositorio. |

## Configuración y siguientes pasos

La configuración base se encuentra en `sonar-project.properties`; el escaneo
Maven usó explícitamente la clave `cafesoft-api-hu034`. Para completar HU-034 se
requiere:

1. Localizar e incorporar el backend que implementa promociones y
   segmentación. Se buscaron clases, endpoints y todas las ramas/historial
   disponibles de este repositorio; no se encontró ese código. El archivo
   `ProductoInsumoDTO.java` seleccionado corresponde a una relación producto-
   insumo, no a promociones.
2. Revisar y corregir las vulnerabilidades y code smells del API que sí se
   analizó, empezando por las tres vulnerabilidades críticas de binding de
   entidades y definiendo los orígenes CORS permitidos para producción.
3. Completar la autorización por rol para los controladores existentes y
   agregar cobertura de pruebas; la cobertura Sonar actual es 0.0%.
4. Ejecutar Sonar sobre el código real de promociones y adjuntar el resultado
   de ese proyecto. El servidor local ya está disponible para repetir análisis
   en esta máquina; SonarQube Cloud requiere que la usuaria autorice la
   conexión con GitHub.

**Conclusión:** el análisis de Sonar del API Java disponible sí se ejecutó y
obtuvo Quality Gate PASSED, pero HU-034 **no está terminada**. El repositorio no
contiene la API de promociones/segmentación, el análisis encontró 10
vulnerabilidades y 29 code smells, la matriz de roles está incompleta y las
pruebas completas están bloqueadas por la configuración local de PostgreSQL.
