package com.utvt.ApiSpringCafeSoft.controller;

import com.utvt.ApiSpringCafeSoft.dto.UsuarioDTO;
import com.utvt.ApiSpringCafeSoft.model.Usuario;
import com.utvt.ApiSpringCafeSoft.service.UsuarioService;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.http.ResponseEntity;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;

class UsuarioControllerSecurityTest {

    @Test
    void publicRegistrationCannotPromoteAccountOrAssignPermissions() throws Exception {
        UsuarioController controller = createController();
        Usuario submitted = new Usuario();
        submitted.setUserTipo(0);
        submitted.setPermisos(new ArrayList<>(List.of("usuarios")));

        ResponseEntity<?> response = controller.crearUsuario(submitted, null);

        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertEquals(2, submitted.getUserTipo());
        assertEquals(List.of(), submitted.getPermisos());
        assertEquals(submitted, controllerService(controller).createdUser);
    }

    @Test
    void adminRegistrationCanAssignManagedRole() throws Exception {
        UsuarioController controller = createController();
        Usuario submitted = new Usuario();
        submitted.setUserTipo(1);
        var admin = new UsernamePasswordAuthenticationToken(
                "admin@example.com",
                "n/a",
                List.of(new SimpleGrantedAuthority("ROLE_ADMIN")));

        ResponseEntity<?> response = controller.crearUsuario(submitted, admin);

        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertEquals(1, submitted.getUserTipo());
        assertEquals(submitted, controllerService(controller).createdUser);
    }

    private UsuarioController createController() throws Exception {
        UsuarioController controller = new UsuarioController();
        Field serviceField = UsuarioController.class.getDeclaredField("usuarioService");
        serviceField.setAccessible(true);
        serviceField.set(controller, new CapturingUsuarioService());
        return controller;
    }

    private CapturingUsuarioService controllerService(UsuarioController controller)
            throws Exception {
        Field serviceField = UsuarioController.class.getDeclaredField("usuarioService");
        serviceField.setAccessible(true);
        return (CapturingUsuarioService) serviceField.get(controller);
    }

    private static class CapturingUsuarioService extends UsuarioService {
        private Usuario createdUser;

        @Override
        public Usuario crearUsuario(Usuario usuario) {
            createdUser = usuario;
            usuario.setId(10L);
            return usuario;
        }

        @Override
        public Optional<UsuarioDTO> obtenerUsuarioPorId(Long id) {
            return Optional.of(new UsuarioDTO());
        }
    }
}
