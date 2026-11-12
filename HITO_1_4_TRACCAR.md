# Hito 1.4 - Vinculacion de dispositivos Traccar

Se agrega `traccarDeviceId` opcional a la entidad `Usuario`, tabla `usuarios`, y al `UsuarioDTO`.
Solo usuarios `userTipo=4` pueden recibir una vinculacion mediante el nuevo endpoint.

## Endpoint (solo ADMIN con JWT)

`PUT /api/usuarios/{id}/traccar-device`

```json
{"traccarDeviceId":"86730947"}
```

`id` es el **ID del usuario repartidor en CafeSoft**, no el ID de Traccar.

Ejemplo PowerShell (ajustar ID y token):

```powershell
$body = @{ traccarDeviceId = "86730947" } | ConvertTo-Json
Invoke-RestMethod -Method Put -Uri "http://localhost:8080/api/usuarios/ID_REPARTIDOR/traccar-device" -Headers @{Authorization="Bearer TU_TOKEN_ADMIN"} -ContentType "application/json" -Body $body
```

La columna `traccar_device_id` se crea automaticamente si Hibernate `ddl-auto=update` y la base de datos esta disponible. Respaldar PostgreSQL antes de iniciar. No se altera la base H2 de Traccar.

La asignacion solo guarda la referencia; **no consulta Traccar ni comprueba que el dispositivo exista**. Para validar el rastreo, confirmar `Upload response 200` en Traccar Client y ubicacion reciente en Traccar Web. La URL de Quick Tunnel puede cambiar tras reiniciar.

## Pruebas pendientes
- Compilar con Maven (`mvn test`) y arrancar API con PostgreSQL local.
- Comprobar que un ADMIN puede vincular y consultar el ID en respuesta.
- Comprobar rechazo para usuario no repartidor y dispositivo duplicado.
- Confirmar rastreo real en Traccar.

Nota: `application.properties` original contiene credenciales de PostgreSQL en texto plano. Antes de publicar el repositorio, moverlas a variables de entorno y rotar la contrasena.
