package com.utvt.ApiSpringCafeSoft.security;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Base64;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = SecurityConfigTest.ProbeController.class)
@Import({
        SecurityConfig.class,
        JwtAuthenticationFilter.class,
        SecurityConfigTest.TestBeans.class
})
class SecurityConfigTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void requiresAuthenticationForApiRoutes() throws Exception {
        mockMvc.perform(get("/api/probe"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void restrictsUserManagementToAdministrators() throws Exception {
        mockMvc.perform(get("/api/usuarios/1").with(user("cliente").roles("USER")))
                .andExpect(status().isForbidden());
    }

    @Test
    void allowsAdminThroughUserManagementAuthorization() throws Exception {
        mockMvc.perform(get("/api/usuarios/1").with(user("admin").roles("ADMIN")))
                .andExpect(status().isNotFound());
    }

    @Test
    void restrictsPromotionManagementAndCustomerSegmentationToAdministrators()
            throws Exception {
        mockMvc.perform(get("/api/promociones")
                        .with(user("staff").roles("USER")))
                .andExpect(status().isForbidden());
        mockMvc.perform(get("/api/clientes/segmentos")
                        .with(user("staff").roles("USER")))
                .andExpect(status().isForbidden());
        mockMvc.perform(get("/api/promociones").with(user("admin").roles("ADMIN")))
                .andExpect(status().isNotFound());
    }

    @Test
    void preventsCustomersFromApplyingPromotionsOrCreatingSales() throws Exception {
        mockMvc.perform(get("/api/promociones/aplicables")
                        .with(user("cliente").roles("CLIENT")))
                .andExpect(status().isForbidden());
        mockMvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders
                        .post("/api/ventas")
                        .with(user("cliente").roles("CLIENT")))
                .andExpect(status().isForbidden());
    }

    @Test
    void restrictsCustomerPointsOperationsToStaff() throws Exception {
        mockMvc.perform(get("/api/clientes")
                        .with(user("cliente").roles("CLIENT")))
                .andExpect(status().isForbidden());
        mockMvc.perform(get("/api/clientes/1/puntos")
                        .with(user("cliente").roles("CLIENT")))
                .andExpect(status().isForbidden());
        mockMvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders
                        .post("/api/clientes/1/beneficios/cotizaciones")
                        .with(user("cliente").roles("CLIENT")))
                .andExpect(status().isForbidden());

        mockMvc.perform(get("/api/clientes").with(user("staff").roles("USER")))
                .andExpect(status().isNotFound());
        mockMvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders
                        .post("/api/clientes/1/beneficios/cotizaciones")
                        .with(user("staff").roles("USER")))
                .andExpect(status().isNotFound());
    }

    @RestController
    static class ProbeController {
        @GetMapping("/api/probe")
        String probe() {
            return "ok";
        }

        @GetMapping("/api/promociones")
        String promotions() {
            return "ok";
        }

        @GetMapping("/api/clientes/segmentos")
        String segments() {
            return "ok";
        }

        @GetMapping("/api/promociones/aplicables")
        String applicablePromotions() {
            return "ok";
        }

        @GetMapping("/api/clientes")
        String customers() {
            return "ok";
        }

        @GetMapping("/api/clientes/{id}/puntos")
        String customerPoints() {
            return "ok";
        }

        @PostMapping("/api/clientes/{id}/beneficios/cotizaciones")
        String quotePoints() {
            return "ok";
        }

        @PostMapping("/api/ventas")
        String createSale() {
            return "ok";
        }
    }

    @TestConfiguration
    static class TestBeans {
        @Bean
        JwtService jwtService() {
            byte[] key = "12345678901234567890123456789012".getBytes();
            return new JwtService(Base64.getEncoder().encodeToString(key), 3600);
        }

        @Bean
        UserDetailsService userDetailsService() {
            return username -> User.withUsername(username)
                    .password("unused")
                    .roles("USER")
                    .build();
        }
    }
}
