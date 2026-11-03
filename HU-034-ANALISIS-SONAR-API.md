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

Referencias principales: `controller/UsuarioController.java` (líneas 31-32,
101-104, 288-290, 804-813 y 864-905), `controller/VentaController.java`
(líneas 67-69 y 419-420), y `service/UsuarioService.java` (líneas 50 y 69-73).
Este es un hallazgo de la revisión de seguridad, **no un resultado confirmado
por SonarQube**. Debe corregirse en una historia de seguridad/autenticación
antes de afirmar que la API está protegida.

## Bloqueo de compilación

La compilación Maven (`mvnw.cmd -DskipTests package`) falla en
`UsuarioController.java:543` por un paréntesis de cierre extra y contiene
también una declaración duplicada de `buscarUsuariosPorNombre`. La compilación
debe corregirse antes de usar un análisis Sonar Maven que dependa de bytecode.
Este defecto está fuera de las clases de promociones, que no existen en el
repositorio.

## Configuración y siguientes pasos

La configuración se encuentra en `sonar-project.properties`. Para completar
HU-034 se requiere:

1. Identificar e incorporar el backend que implementa promociones y
   segmentación, o crear esa implementación en una historia aparte.
2. Corregir el error de compilación existente.
3. Configurar SonarQube Server/Cloud y vincular el proyecto con el análisis de
   Maven/Java.
4. Ejecutar el análisis en la rama HU-034, revisar Security Hotspots,
   vulnerabilidades, code smells y Quality Gate, y adjuntar el reporte real.

**Conclusión:** HU-034 queda parcialmente preparada en una rama dedicada, pero
el escaneo de seguridad de la API de promociones no puede darse por completado
hasta disponer del código objetivo y de una instancia SonarQube conectada.
