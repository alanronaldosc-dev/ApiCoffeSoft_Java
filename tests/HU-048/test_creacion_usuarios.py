"""
HU-048: Automatización de creación de usuarios
===============================================
QA Automation: Homero

Historia de usuario:
  Como QA Automation (Homero), quiero automatizar la creación de usuarios
  mediante script, para verificar que el flujo de registro funciona
  correctamente en diferentes entornos.

Criterios de aceptación:
  CA-1: El administrador puede crear usuarios de forma automatizada.
  CA-2: La automatización es confiable en todos los entornos
        (valida campos requeridos, duplicados, tipos inválidos, etc.)

Endpoint bajo prueba:
  POST /api/usuarios          → público (registro)
  GET  /api/usuarios          → solo ADMIN
  GET  /api/usuarios/{id}     → solo ADMIN
  GET  /api/usuarios/email/{email} → solo ADMIN

Ejecución:
  pytest tests/HU-048/test_creacion_usuarios.py -v
         --html=tests/HU-048/reporte_HU048.html
"""

import uuid
import pytest
import requests

# ── Configuración ──────────────────────────────────────────────────────────────
BASE_URL       = "http://localhost:8080"
ADMIN_EMAIL    = "adminalan@gmail.com"
ADMIN_PASSWORD = "admin123456"
SUCURSAL_ID    = 1          # ID creado por DataInitializer


# ── Helpers ────────────────────────────────────────────────────────────────────

def email_unico():
    """Genera un email único para evitar conflictos entre ejecuciones."""
    return f"test_{uuid.uuid4().hex[:8]}@cafesoft.com"


def crear_usuario(payload, headers=None):
    """POST /api/usuarios — endpoint público."""
    return requests.post(
        f"{BASE_URL}/api/usuarios",
        json=payload,
        headers=headers or {"Content-Type": "application/json"},
        timeout=10,
    )


def payload_valido(email=None):
    """Devuelve un payload completo y válido para crear un empleado."""
    return {
        "nombre":    "Empleado Test",
        "email":     email or email_unico(),
        "password":  "Pass1234!",
        "direccion": "Calle Falsa 123",
        "telefono":  "7712345678",
        "userTipo":  1,                        # 1 = EMPLEADO
        "sucursal":  {"id": SUCURSAL_ID},
    }


# ── Fixtures ───────────────────────────────────────────────────────────────────

@pytest.fixture(scope="session")
def token_admin():
    """JWT del ADMIN reutilizado en toda la sesión."""
    resp = requests.post(
        f"{BASE_URL}/api/usuarios/login",
        json={"email": ADMIN_EMAIL, "password": ADMIN_PASSWORD},
        timeout=10,
    )
    assert resp.status_code == 200, f"Login ADMIN falló: {resp.status_code} {resp.text}"
    return resp.json()["token"]


@pytest.fixture(scope="session")
def headers_admin(token_admin):
    return {
        "Authorization": f"Bearer {token_admin}",
        "Content-Type":  "application/json",
        "Accept":        "application/json",
    }


@pytest.fixture()
def usuario_creado(headers_admin):
    """
    Crea un usuario de prueba antes del test y devuelve su ID.
    Fixture de función: cada test que lo use obtiene un usuario fresco.
    """
    email = email_unico()
    resp  = crear_usuario(payload_valido(email))
    assert resp.status_code == 201, f"Setup falló al crear usuario: {resp.text}"
    data    = resp.json()
    usuario = data.get("usuario", data)      # extraer del wrapper {"usuario":{...}}
    usuario_id = usuario.get("id") or usuario.get("idUsuario")
    yield {"id": usuario_id, "email": email}


# ══════════════════════════════════════════════════════════════════════════════
# CA-1: El administrador puede crear usuarios de forma automatizada
# ══════════════════════════════════════════════════════════════════════════════

class TestCreacionAutomatizada:

    def test_crear_empleado_devuelve_201(self):
        """CA-1: Crear un empleado válido devuelve HTTP 201."""
        resp = crear_usuario(payload_valido())
        assert resp.status_code == 201, (
            f"Se esperaba 201, se obtuvo {resp.status_code}: {resp.text}"
        )

    def test_respuesta_contiene_id(self):
        """CA-1: La respuesta incluye el ID del usuario creado."""
        resp = crear_usuario(payload_valido())
        assert resp.status_code == 201
        data    = resp.json()
        usuario = data.get("usuario", data)   # la API envuelve en {"usuario":{...}}
        tiene_id = "id" in usuario or "idUsuario" in usuario
        assert tiene_id, f"Respuesta sin ID: {data}"

    def test_respuesta_contiene_email(self):
        """CA-1: La respuesta devuelve el email del usuario creado."""
        email = email_unico()
        resp  = crear_usuario(payload_valido(email))
        assert resp.status_code == 201
        data    = resp.json()
        usuario = data.get("usuario", data)   # extraer del wrapper
        assert usuario.get("email") == email, (
            f"Email en respuesta '{usuario.get('email')}' no coincide con '{email}'"
        )

    def test_crear_usuario_tipo_repartidor(self):
        """CA-1: Se puede crear un usuario con userTipo=4 (REPARTIDOR)."""
        p = payload_valido()
        p["userTipo"] = 4
        resp = crear_usuario(p)
        assert resp.status_code == 201, (
            f"No se pudo crear REPARTIDOR: {resp.status_code} {resp.text}"
        )

    def test_crear_usuario_tipo_admin(self):
        """CA-1: Se puede crear un usuario con userTipo=0 (ADMIN)."""
        p = payload_valido()
        p["userTipo"] = 0
        resp = crear_usuario(p)
        assert resp.status_code == 201, (
            f"No se pudo crear ADMIN: {resp.status_code} {resp.text}"
        )

    def test_password_no_aparece_en_respuesta(self):
        """CA-1: La contraseña no se devuelve en texto plano en la respuesta."""
        resp = crear_usuario(payload_valido())
        assert resp.status_code == 201
        body = resp.text
        assert "Pass1234!" not in body, "La contraseña en texto plano aparece en la respuesta"

    def test_admin_puede_consultar_usuario_creado(self, headers_admin, usuario_creado):
        """CA-1: El ADMIN puede recuperar el usuario recién creado por su ID."""
        uid = usuario_creado["id"]
        resp = requests.get(
            f"{BASE_URL}/api/usuarios/{uid}",
            headers=headers_admin,
            timeout=10,
        )
        assert resp.status_code == 200, (
            f"No se pudo consultar usuario ID {uid}: {resp.status_code} {resp.text}"
        )

    def test_admin_puede_listar_todos_los_usuarios(self, headers_admin):
        """CA-1: El ADMIN puede listar todos los usuarios del sistema."""
        resp = requests.get(
            f"{BASE_URL}/api/usuarios",
            headers=headers_admin,
            timeout=10,
        )
        assert resp.status_code == 200
        usuarios = resp.json()
        assert isinstance(usuarios, list)
        assert len(usuarios) >= 1, "Debería haber al menos el usuario ADMIN"

    def test_usuario_creado_aparece_en_listado(self, headers_admin, usuario_creado):
        """CA-1: El usuario creado aparece en el listado general."""
        resp = requests.get(
            f"{BASE_URL}/api/usuarios",
            headers=headers_admin,
            timeout=10,
        )
        assert resp.status_code == 200
        emails = [u.get("email") for u in resp.json()]
        assert usuario_creado["email"] in emails, (
            f"Email '{usuario_creado['email']}' no encontrado en el listado"
        )


# ══════════════════════════════════════════════════════════════════════════════
# CA-2: La automatización es confiable en todos los entornos
# ══════════════════════════════════════════════════════════════════════════════

class TestConfiabilidadAutomatizacion:

    # ── Validaciones de campos requeridos ─────────────────────────────────────

    def test_sin_nombre_devuelve_400(self):
        """CA-2: Nombre obligatorio — sin nombre devuelve 400."""
        p = payload_valido()
        del p["nombre"]
        resp = crear_usuario(p)
        assert resp.status_code == 400, (
            f"Se esperaba 400 sin nombre, se obtuvo {resp.status_code}"
        )

    def test_sin_email_devuelve_400(self):
        """CA-2: Email obligatorio — sin email devuelve 400."""
        p = payload_valido()
        del p["email"]
        resp = crear_usuario(p)
        assert resp.status_code == 400, (
            f"Se esperaba 400 sin email, se obtuvo {resp.status_code}"
        )

    def test_sin_password_devuelve_400(self):
        """CA-2: Password obligatorio — sin password devuelve 400."""
        p = payload_valido()
        del p["password"]
        resp = crear_usuario(p)
        assert resp.status_code == 400, (
            f"Se esperaba 400 sin password, se obtuvo {resp.status_code}"
        )

    def test_sin_sucursal_devuelve_error(self):
        """CA-2: Sucursal obligatoria — sin sucursal devuelve 400 o 500."""
        p = payload_valido()
        del p["sucursal"]
        resp = crear_usuario(p)
        assert resp.status_code in (400, 500), (
            f"Se esperaba error sin sucursal, se obtuvo {resp.status_code}"
        )

    # ── Validaciones de formato ───────────────────────────────────────────────

    def test_email_invalido_devuelve_400(self):
        """CA-2: Email con formato inválido es rechazado."""
        p = payload_valido()
        p["email"] = "no-es-un-email"
        resp = crear_usuario(p)
        assert resp.status_code == 400, (
            f"Se esperaba 400 con email inválido, se obtuvo {resp.status_code}"
        )

    def test_telefono_corto_es_aceptado(self):
        """CA-2: El campo teléfono no tiene validación de longitud mínima en la API.
        Se documenta el comportamiento real: teléfonos cortos son aceptados.
        """
        p = payload_valido()
        p["telefono"] = "123"
        resp = crear_usuario(p)
        # La API acepta cualquier string en teléfono — comportamiento documentado
        assert resp.status_code == 201, (
            f"Comportamiento inesperado con teléfono corto: {resp.status_code} {resp.text}"
        )

    def test_tipo_usuario_invalido_devuelve_error(self):
        """CA-2: UserTipo fuera del rango válido (0-4) es rechazado."""
        p = payload_valido()
        p["userTipo"] = 99
        resp = crear_usuario(p)
        assert resp.status_code in (400, 500), (
            f"Se esperaba error con userTipo=99, se obtuvo {resp.status_code}"
        )

    # ── Integridad de datos ───────────────────────────────────────────────────

    def test_email_duplicado_devuelve_error(self):
        """CA-2: Registrar el mismo email dos veces devuelve error."""
        email = email_unico()
        # Primera creación — debe funcionar
        resp1 = crear_usuario(payload_valido(email))
        assert resp1.status_code == 201, f"Primera creación falló: {resp1.text}"
        # Segunda creación con mismo email — debe fallar
        resp2 = crear_usuario(payload_valido(email))
        assert resp2.status_code in (400, 409, 500), (
            f"Email duplicado debería ser rechazado, se obtuvo {resp2.status_code}"
        )

    def test_sucursal_inexistente_devuelve_error(self):
        """CA-2: Sucursal con ID que no existe es rechazada."""
        p = payload_valido()
        p["sucursal"] = {"id": 99999}
        resp = crear_usuario(p)
        assert resp.status_code in (400, 404, 500), (
            f"Se esperaba error con sucursal inexistente, se obtuvo {resp.status_code}"
        )

    # ── Seguridad ─────────────────────────────────────────────────────────────

    def test_sin_token_no_puede_listar_usuarios(self):
        """CA-2: Listar usuarios sin token es rechazado (401/403)."""
        resp = requests.get(f"{BASE_URL}/api/usuarios", timeout=10)
        assert resp.status_code in (401, 403), (
            f"Sin token debería dar 401/403, se obtuvo {resp.status_code}"
        )

    def test_creacion_no_requiere_token(self):
        """CA-2: El endpoint de registro es público (no requiere token)."""
        resp = crear_usuario(payload_valido())
        assert resp.status_code == 201, (
            f"El registro público debería dar 201 sin token, se obtuvo {resp.status_code}"
        )
