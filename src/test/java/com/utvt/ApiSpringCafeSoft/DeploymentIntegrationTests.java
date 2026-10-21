package com.utvt.ApiSpringCafeSoft;

import com.utvt.ApiSpringCafeSoft.security.JwtUtil;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class DeploymentIntegrationTests {
    @Autowired MockMvc mvc;
    @Autowired JwtUtil jwt;

    @Test void healthChecksDatabaseWithoutAuthentication() throws Exception {
        mvc.perform(get("/api/health")).andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("UP"))
                .andExpect(jsonPath("$.database").value("UP"))
                .andExpect(header().string("Cache-Control", "no-store"));
    }

    @Test void malformedBearerDoesNotBlockPublicHealth() throws Exception {
        mvc.perform(get("/api/health").header("Authorization", "Bearer invalid"))
                .andExpect(status().isOk());
    }

    @Test void protectedResourcesRejectAnonymousAndInvalidToken() throws Exception {
        mvc.perform(get("/api/productos")).andExpect(status().isForbidden());
        mvc.perform(get("/api/productos").header("Authorization", "Bearer invalid"))
                .andExpect(status().isForbidden());
    }

    @Test void productionRegistrationRejectsAnonymousAdminCreation() throws Exception {
        mvc.perform(post("/api/usuarios").contentType("application/json")
                .content("{\"userTipo\":0}" )).andExpect(status().isForbidden());
    }

    @Test void registrationAcceptsAdministratorAndRejectsEmployee() throws Exception {
        mvc.perform(post("/api/usuarios").contentType("application/json").content("{}")
                .header("Authorization", "Bearer " + jwt.generateToken("admin@example.com", 0)))
                .andExpect(status().isBadRequest());
        mvc.perform(post("/api/usuarios").contentType("application/json").content("{}")
                .header("Authorization", "Bearer " + jwt.generateToken("employee@example.com", 1)))
                .andExpect(status().isForbidden());
    }

    @Test void corsOnlyAcceptsConfiguredWebOrigin() throws Exception {
        mvc.perform(options("/api/productos")
                .header("Origin", "https://web-prueba.example")
                .header("Access-Control-Request-Method", "GET")
                .header("Access-Control-Request-Headers", "Authorization"))
                .andExpect(status().isOk())
                .andExpect(header().string("Access-Control-Allow-Origin", "https://web-prueba.example"));
        mvc.perform(options("/api/productos")
                .header("Origin", "https://otro.example")
                .header("Access-Control-Request-Method", "GET"))
                .andExpect(status().isForbidden());
    }
}
