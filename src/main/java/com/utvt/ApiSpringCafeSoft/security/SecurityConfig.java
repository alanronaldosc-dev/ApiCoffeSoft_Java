package com.utvt.ApiSpringCafeSoft.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.authentication.HttpStatusEntryPoint;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
public class SecurityConfig {

    private static final String ROLE_ADMIN = "ADMIN";
    private static final String ROLE_USER = "USER";

    @Bean
    public SecurityFilterChain apiSecurityFilterChain(
            HttpSecurity http,
            JwtAuthenticationFilter jwtAuthenticationFilter) throws Exception {
        return http
                // This API is stateless and authenticates explicit Bearer tokens, not cookies.
                .csrf(csrf -> csrf.disable())
                .sessionManagement(session -> session.sessionCreationPolicy(
                        SessionCreationPolicy.STATELESS))
                .exceptionHandling(exceptions -> exceptions.authenticationEntryPoint(
                        new HttpStatusEntryPoint(HttpStatus.UNAUTHORIZED)))
                .authorizeHttpRequests(authorize -> authorize
                        .requestMatchers(HttpMethod.POST, "/api/usuarios/login").permitAll()
                        .requestMatchers(HttpMethod.POST, "/api/usuarios").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/clientes")
                        .hasAnyRole(ROLE_ADMIN, ROLE_USER)
                        .requestMatchers(HttpMethod.GET, "/api/clientes/*/puntos")
                        .hasAnyRole(ROLE_ADMIN, ROLE_USER)
                        .requestMatchers(
                                HttpMethod.POST,
                                "/api/clientes/*/beneficios/cotizaciones")
                        .hasAnyRole(ROLE_ADMIN, ROLE_USER)
                        .requestMatchers(HttpMethod.GET, "/api/promociones/aplicables")
                        .hasAnyRole(ROLE_ADMIN, ROLE_USER)
                        .requestMatchers(HttpMethod.POST, "/api/ventas")
                        .hasAnyRole(ROLE_ADMIN, ROLE_USER)
                        .requestMatchers(
                                HttpMethod.POST,
                                "/api/productos",
                                "/api/productos/**")
                        .hasAnyRole(ROLE_ADMIN, ROLE_USER)
                        .requestMatchers(
                                HttpMethod.PUT,
                                "/api/productos",
                                "/api/productos/**")
                        .hasAnyRole(ROLE_ADMIN, ROLE_USER)
                        .requestMatchers(
                                HttpMethod.PATCH,
                                "/api/productos",
                                "/api/productos/**")
                        .hasAnyRole(ROLE_ADMIN, ROLE_USER)
                        .requestMatchers(
                                HttpMethod.DELETE,
                                "/api/productos",
                                "/api/productos/**")
                        .hasAnyRole(ROLE_ADMIN, ROLE_USER)
                        .requestMatchers(
                                "/api/promociones/**",
                                "/api/clientes/segmentos",
                                "/api/clientes/segmentos/**").hasRole(ROLE_ADMIN)
                        .requestMatchers("/api/clientes/**")
                        .hasAnyRole(ROLE_ADMIN, ROLE_USER)
                        .requestMatchers("/api/usuarios/**").hasRole(ROLE_ADMIN)
                        .requestMatchers(
                                "/swagger-ui.html",
                                "/swagger-ui/**",
                                "/api-docs/**",
                                "/v3/api-docs/**").permitAll()
                        .requestMatchers("/api/**").authenticated()
                        .anyRequest().permitAll())
                .addFilterBefore(
                        jwtAuthenticationFilter,
                        UsernamePasswordAuthenticationFilter.class)
                .build();
    }
}
