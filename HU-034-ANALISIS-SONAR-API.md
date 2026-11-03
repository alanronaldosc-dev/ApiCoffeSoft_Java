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

Se agregó `sonar-project.properties` para preparar el análisis de las fuentes
Java y las pruebas Maven. **No se obtuvo un análisis SonarQube de servidor ni un
Quality Gate**: en este entorno no hay SonarScanner CLI ni instancia local de
SonarQube disponible. SonarQube for IDE indicó que el workspace no está
vinculado a un proyecto remoto y que requiere Connected Mode para devolver
Security Hotspots y vulnerabilidades de taint de servidor/nube.

Se solicitaron análisis de archivo desde SonarQube for IDE para las 49 fuentes
Java del repositorio. Estos análisis no proporcionaron un reporte consolidado
ni una lista verificable de hallazgos Sonar; la recuperación de Security
Hotspots fue bloqueada porque el workspace no está conectado a un proyecto
SonarQube Server/Cloud. Por eso no se reportan conteos de vulnerabilidades,
code smells o cobertura como si provinieran de un escaneo SonarQube completo.

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
| Pruebas completas del proyecto | Pendientes; requieren validar dependencias externas/configuración de base de datos. |
| Análisis SonarQube Server/Cloud y Quality Gate | Pendiente por falta de organización/proyecto SonarCloud configurado. |

## Configuración y siguientes pasos

La configuración se encuentra en `sonar-project.properties`. Para completar
HU-034 se requiere:

1. Identificar e incorporar el backend que implementa promociones y
   segmentación; el usuario indicó que compartirá el repositorio/rama correctos.
2. Terminar y probar la matriz de autorización por rol para los controladores
   existentes, sin bloquear los flujos legítimos de los clientes.
3. Crear la organización/proyecto SonarCloud, configurar el token como secreto
   y vincular el análisis Maven/Java. No se debe compartir el token en el chat
   ni guardarlo en el repositorio.
4. Ejecutar el análisis en la rama HU-034, revisar Security Hotspots,
   vulnerabilidades, code smells y Quality Gate, y adjuntar el reporte real.

**Conclusión:** HU-034 tiene endurecimiento JWT inicial y pruebas focalizadas,
pero no está terminada. Falta analizar el código real de promociones,
configurar SonarCloud, obtener un Quality Gate y cerrar la autorización
específica por roles.
