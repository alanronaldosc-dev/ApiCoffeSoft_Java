package com.utvt.ApiSpringCafeSoft.service;

import com.utvt.ApiSpringCafeSoft.model.Usuario;
import com.utvt.ApiSpringCafeSoft.model.Venta;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;

/**
 * Pruebas básicas de las validaciones previas al envío.
 * No realizan llamadas reales a Expo Push Service.
 */
class NotificacionServiceTest {

    private final NotificacionService notificacionService =
            new NotificacionService();

    @Test
    @DisplayName("No envía notificación si el usuario no tiene token")
    void noEnviaSinToken() {
        Venta venta = new Venta();
        venta.setUsuario(new Usuario());

        assertFalse(
                notificacionService.enviarCambioEstado(venta, "PENDIENTE")
        );
    }

    @Test
    @DisplayName("No envía notificación si el token no es de Expo")
    void noEnviaConTokenInvalido() {
        Usuario usuario = new Usuario();
        usuario.setPushToken("token-invalido");

        Venta venta = new Venta();
        venta.setUsuario(usuario);

        assertFalse(
                notificacionService.enviarCambioEstado(venta, "ENTREGADA")
        );
    }

    @Test
    @DisplayName("No envía notificación de prueba sin token válido")
    void noEnviaPruebaSinToken() {
        assertFalse(
                notificacionService.enviarPrueba(null, "Título", "Cuerpo", null)
        );
    }
}
