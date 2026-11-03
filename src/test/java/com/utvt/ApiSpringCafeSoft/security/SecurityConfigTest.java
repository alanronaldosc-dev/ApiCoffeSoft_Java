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

    @RestController
    static class ProbeController {
        @GetMapping("/api/probe")
        String probe() {
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
