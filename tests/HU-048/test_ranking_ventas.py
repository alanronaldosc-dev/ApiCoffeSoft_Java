"""
HU-048: Automatización de pruebas - Ranking de Ventas
======================================================
QA Automation: Homero

Criterios de aceptación:
  CA-1: Se muestra ranking por número de ventas (unidades vendidas)
  CA-2: Se muestra ranking por monto recaudado
  CA-3: Solo usuarios autorizados pueden consultar el ranking

Endpoints bajo prueba:
  GET /api/ventas/reporte/top-productos?periodo={hoy|mes|mes_anterior}
  GET /api/ventas/reporte?periodo={hoy|mes|mes_anterior}

Ejecución:
  pytest tests/HU-048/test_ranking_ventas.py -v --html=tests/HU-048/reporte_HU048.html
"""

import pytest
import requests

# ── Configuración ──────────────────────────────────────────────────────────────
BASE_URL = "http://localhost:8080"
ADMIN_EMAIL = "adminalan@gmail.com"
ADMIN_PASSWORD = "admin123456"


# ── Fixtures ───────────────────────────────────────────────────────────────────

@pytest.fixture(scope="session")
def token_admin():
    """Obtiene el JWT del usuario ADMIN y lo reutiliza en toda la sesión."""
    resp = requests.post(
        f"{BASE_URL}/api/usuarios/login",
        json={"email": ADMIN_EMAIL, "password": ADMIN_PASSWORD},
        timeout=10,
    )
    assert resp.status_code == 200, f"Login falló: {resp.status_code} {resp.text}"
    data = resp.json()
    assert "token" in data, f"No se recibió token en login: {data}"
    return data["token"]


@pytest.fixture(scope="session")
def headers_admin(token_admin):
    """Headers con JWT de ADMIN para todos los requests autenticados."""
    return {
        "Authorization": f"Bearer {token_admin}",
        "Content-Type": "application/json",
        "Accept": "application/json",
    }


# ── Helpers ────────────────────────────────────────────────────────────────────

def get_top_productos(headers, periodo="mes"):
    """Llama al endpoint de top productos y devuelve la lista."""
    resp = requests.get(
        f"{BASE_URL}/api/ventas/reporte/top-productos",
        params={"periodo": periodo},
        headers=headers,
        timeout=10,
    )
    return resp


def get_reporte_ventas(headers, periodo="mes"):
    """Llama al endpoint de reporte de ventas."""
    resp = requests.get(
        f"{BASE_URL}/api/ventas/reporte",
        params={"periodo": periodo},
        headers=headers,
        timeout=10,
    )
    return resp


# ══════════════════════════════════════════════════════════════════════════════
# CA-1: Ranking por número de ventas (unidades vendidas)
# ══════════════════════════════════════════════════════════════════════════════

class TestRankingPorUnidades:

    def test_endpoint_responde_200(self, headers_admin):
        """CA-1: El endpoint devuelve HTTP 200 con token válido."""
        resp = get_top_productos(headers_admin, "mes")
        assert resp.status_code == 200, (
            f"Se esperaba 200, se obtuvo {resp.status_code}: {resp.text}"
        )

    def test_respuesta_es_lista(self, headers_admin):
        """CA-1: La respuesta es una lista JSON (array)."""
        resp = get_top_productos(headers_admin, "mes")
        assert resp.status_code == 200
        data = resp.json()
        assert isinstance(data, list), f"Se esperaba lista, se obtuvo: {type(data)}"

    def test_productos_tienen_campos_requeridos(self, headers_admin):
        """CA-1: Cada item tiene productoId, nombre, unidadesVendidas y montoTotal."""
        resp = get_top_productos(headers_admin, "mes")
        assert resp.status_code == 200
        productos = resp.json()
        if len(productos) == 0:
            pytest.skip("No hay ventas registradas en el periodo 'mes'")
        campos = {"productoId", "nombre", "unidadesVendidas", "montoTotal"}
        for p in productos:
            faltantes = campos - set(p.keys())
            assert not faltantes, f"Campos faltantes en producto {p}: {faltantes}"

    def test_ranking_ordenado_por_unidades_descendente(self, headers_admin):
        """CA-1: Los productos están ordenados de mayor a menor unidades vendidas."""
        resp = get_top_productos(headers_admin, "mes")
        assert resp.status_code == 200
        productos = resp.json()
        if len(productos) < 2:
            pytest.skip("Se necesitan al menos 2 productos para verificar orden")
        unidades = [p["unidadesVendidas"] for p in productos]
        assert unidades == sorted(unidades, reverse=True), (
            f"Ranking NO está ordenado por unidades descendente: {unidades}"
        )

    def test_ranking_periodo_hoy(self, headers_admin):
        """CA-1: El ranking funciona con periodo 'hoy'."""
        resp = get_top_productos(headers_admin, "hoy")
        assert resp.status_code == 200
        assert isinstance(resp.json(), list)

    def test_ranking_periodo_mes_anterior(self, headers_admin):
        """CA-1: El ranking funciona con periodo 'mes_anterior'."""
        resp = get_top_productos(headers_admin, "mes_anterior")
        assert resp.status_code == 200
        assert isinstance(resp.json(), list)

    def test_unidades_vendidas_son_positivas(self, headers_admin):
        """CA-1: Todas las unidades vendidas son mayores a 0."""
        resp = get_top_productos(headers_admin, "mes")
        assert resp.status_code == 200
        productos = resp.json()
        if not productos:
            pytest.skip("No hay productos en el ranking")
        for p in productos:
            assert p["unidadesVendidas"] > 0, (
                f"Producto {p['nombre']} tiene unidades <= 0: {p['unidadesVendidas']}"
            )


# ══════════════════════════════════════════════════════════════════════════════
# CA-2: Ranking por monto recaudado
# ══════════════════════════════════════════════════════════════════════════════

class TestRankingPorMonto:

    def test_productos_tienen_monto_total(self, headers_admin):
        """CA-2: Cada producto en el ranking incluye su montoTotal."""
        resp = get_top_productos(headers_admin, "mes")
        assert resp.status_code == 200
        productos = resp.json()
        if not productos:
            pytest.skip("No hay productos en el ranking")
        for p in productos:
            assert "montoTotal" in p, f"Producto sin montoTotal: {p}"
            assert p["montoTotal"] is not None

    def test_monto_total_es_positivo(self, headers_admin):
        """CA-2: El monto total de cada producto es mayor a 0."""
        resp = get_top_productos(headers_admin, "mes")
        assert resp.status_code == 200
        productos = resp.json()
        if not productos:
            pytest.skip("No hay productos en el ranking")
        for p in productos:
            assert p["montoTotal"] > 0, (
                f"Producto {p['nombre']} tiene montoTotal <= 0: {p['montoTotal']}"
            )

    def test_reporte_incluye_monto_total_periodo(self, headers_admin):
        """CA-2: El reporte de ventas incluye montoTotal del periodo."""
        resp = get_reporte_ventas(headers_admin, "mes")
        assert resp.status_code == 200
        reporte = resp.json()
        assert "montoTotal" in reporte, f"Reporte sin montoTotal: {reporte.keys()}"
        assert reporte["montoTotal"] >= 0

    def test_reporte_incluye_numero_ventas(self, headers_admin):
        """CA-2: El reporte incluye el número de ventas del periodo."""
        resp = get_reporte_ventas(headers_admin, "mes")
        assert resp.status_code == 200
        reporte = resp.json()
        assert "numeroVentas" in reporte, f"Reporte sin numeroVentas: {reporte.keys()}"
        assert reporte["numeroVentas"] >= 0

    def test_monto_total_coincide_con_suma_productos(self, headers_admin):
        """CA-2: El montoTotal del reporte es >= suma de montos de top-productos."""
        resp_reporte = get_reporte_ventas(headers_admin, "mes")
        resp_top = get_top_productos(headers_admin, "mes")
        assert resp_reporte.status_code == 200
        assert resp_top.status_code == 200
        reporte = resp_reporte.json()
        productos = resp_top.json()
        if not productos:
            pytest.skip("No hay productos en el ranking")
        suma_productos = sum(p["montoTotal"] for p in productos)
        monto_reporte = reporte.get("montoTotal", 0)
        # El monto del reporte debe ser >= suma de los top productos
        assert monto_reporte >= suma_productos * 0.95, (
            f"Monto reporte ({monto_reporte}) no coincide con suma top-productos ({suma_productos})"
        )


# ══════════════════════════════════════════════════════════════════════════════
# CA-3: Solo usuarios autorizados pueden consultar el ranking
# ══════════════════════════════════════════════════════════════════════════════

class TestAutorizacion:

    def test_sin_token_devuelve_401_o_403(self):
        """CA-3: Sin token el endpoint rechaza la petición (401 o 403)."""
        resp = requests.get(
            f"{BASE_URL}/api/ventas/reporte/top-productos",
            params={"periodo": "mes"},
            timeout=10,
        )
        assert resp.status_code in (401, 403), (
            f"Se esperaba 401 o 403 sin token, se obtuvo {resp.status_code}"
        )

    def test_token_invalido_devuelve_401_o_403(self):
        """CA-3: Token inválido es rechazado."""
        headers = {"Authorization": "Bearer token.falso.invalido"}
        resp = requests.get(
            f"{BASE_URL}/api/ventas/reporte/top-productos",
            params={"periodo": "mes"},
            headers=headers,
            timeout=10,
        )
        assert resp.status_code in (401, 403), (
            f"Se esperaba 401 o 403 con token inválido, se obtuvo {resp.status_code}"
        )

    def test_token_expirado_devuelve_401_o_403(self):
        """CA-3: Token expirado (JWT manipulado) es rechazado."""
        # JWT con firma incorrecta
        token_expirado = (
            "eyJhbGciOiJIUzI1NiJ9"
            ".eyJzdWIiOiJ0ZXN0QHRlc3QuY29tIiwiZXhwIjoxMDAwMDAwMDAwfQ"
            ".firma_invalida_aqui"
        )
        headers = {"Authorization": f"Bearer {token_expirado}"}
        resp = requests.get(
            f"{BASE_URL}/api/ventas/reporte/top-productos",
            params={"periodo": "mes"},
            headers=headers,
            timeout=10,
        )
        assert resp.status_code in (401, 403), (
            f"Token expirado debería dar 401/403, se obtuvo {resp.status_code}"
        )

    def test_sin_token_reporte_devuelve_401_o_403(self):
        """CA-3: Reporte de ventas también requiere autenticación."""
        resp = requests.get(
            f"{BASE_URL}/api/ventas/reporte",
            params={"periodo": "mes"},
            timeout=10,
        )
        assert resp.status_code in (401, 403), (
            f"Se esperaba 401 o 403 sin token, se obtuvo {resp.status_code}"
        )

    def test_con_token_valido_devuelve_200(self, headers_admin):
        """CA-3: Con token ADMIN válido el acceso es permitido."""
        resp = get_top_productos(headers_admin, "mes")
        assert resp.status_code == 200, (
            f"ADMIN con token válido debería recibir 200, se obtuvo {resp.status_code}"
        )

    def test_reporte_con_token_valido_devuelve_200(self, headers_admin):
        """CA-3: Reporte de ventas con token ADMIN válido es permitido."""
        resp = get_reporte_ventas(headers_admin, "mes")
        assert resp.status_code == 200, (
            f"ADMIN con token válido debería recibir 200, se obtuvo {resp.status_code}"
        )
