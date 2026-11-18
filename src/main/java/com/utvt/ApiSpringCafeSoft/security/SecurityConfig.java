
package com.utvt.ApiSpringCafeSoft.security;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.List;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityConfig {

    @Autowired
    private JwtFilter jwtFilter;

    // =========================================================
    // CONFIGURACIÓN CORS
    // =========================================================

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {

        CorsConfiguration config = new CorsConfiguration();

        config.setAllowedOriginPatterns(List.of("*"));

        config.setAllowedMethods(
            List.of(
                "GET",
                "POST",
                "PUT",
                "DELETE",
                "OPTIONS",
                "PATCH"
            )
        );

        config.setAllowedHeaders(List.of("*"));

        config.setExposedHeaders(
            List.of("Authorization")
        );

        config.setAllowCredentials(false);

        UrlBasedCorsConfigurationSource source =
            new UrlBasedCorsConfigurationSource();

        source.registerCorsConfiguration(
            "/**",
            config
        );

        return source;
    }

    // =========================================================
    // CONFIGURACIÓN DE SEGURIDAD
    // =========================================================

    @Bean
    public SecurityFilterChain filterChain(
            HttpSecurity http) throws Exception {

        http
            .cors(cors ->
                cors.configurationSource(
                    corsConfigurationSource()
                )
            )

            .csrf(csrf -> csrf.disable())

            .httpBasic(httpBasic ->
                httpBasic.disable()
            )

            .formLogin(formLogin ->
                formLogin.disable()
            )

            .sessionManagement(session ->
                session.sessionCreationPolicy(
                    SessionCreationPolicy.STATELESS
                )
            )

            .authorizeHttpRequests(auth -> auth

                // ============================================
                // SOLICITUDES OPTIONS
                // ============================================

                .requestMatchers(
                    HttpMethod.OPTIONS,
                    "/**"
                ).permitAll()

                // ============================================
                // AUTENTICACIÓN Y REGISTRO
                // ============================================

                .requestMatchers(
                    "/api/usuarios/login"
                ).permitAll()

                .requestMatchers(
                    "/api/health"
                ).permitAll()

                .requestMatchers(
                    HttpMethod.POST,
                    "/api/usuarios"
                ).permitAll()

                // ============================================
                // SWAGGER
                // ============================================

                .requestMatchers(
                    "/v3/api-docs",
                    "/v3/api-docs/**",
                    "/api-docs",
                    "/api-docs/**",
                    "/swagger-ui/**",
                    "/swagger-ui.html",
                    "/swagger-resources/**",
                    "/webjars/**"
                ).permitAll()

                // ============================================
                // NOTIFICACIONES PUSH
                // ============================================

                // Solo administradores pueden acceder
                // a los endpoints de notificaciones.

                .requestMatchers(
                    "/api/notificaciones/**"
                ).hasRole("ADMIN")

                // Un usuario autenticado puede registrar
                // su propio token de notificaciones.
                // La autorización sobre el ID del usuario
                // debe verificarse también en el endpoint.

                .requestMatchers(
                    HttpMethod.PUT,
                    "/api/usuarios/*/push-token"
                ).authenticated()

                // ============================================
                // USUARIOS
                // ============================================

                .requestMatchers(
                    HttpMethod.GET,
                    "/api/usuarios/**"
                ).hasRole("ADMIN")

                .requestMatchers(
                    HttpMethod.PUT,
                    "/api/usuarios/**"
                ).hasRole("ADMIN")

                .requestMatchers(
                    HttpMethod.DELETE,
                    "/api/usuarios/**"
                ).hasRole("ADMIN")

                // ============================================
                // INSUMOS
                // ============================================

                .requestMatchers(
                    "/api/insumos/**"
                ).hasAnyRole(
                    "ADMIN",
                    "EMPLEADO"
                )

                // ============================================
                // LOTES
                // ============================================

                .requestMatchers(
                    "/api/lotes/**"
                ).hasAnyRole(
                    "ADMIN",
                    "EMPLEADO"
                )

                // ============================================
                // PROVEEDORES
                // ============================================

                .requestMatchers(
                    "/api/proveedores/**"
                ).hasAnyRole(
                    "ADMIN",
                    "EMPLEADO"
                )

                // ============================================
                // CATEGORÍAS
                // ============================================

                .requestMatchers(
                    "/api/categorias/**"
                ).hasAnyRole(
                    "ADMIN",
                    "EMPLEADO"
                )

                // ============================================
                // SUCURSALES
                // ============================================

                .requestMatchers(
                    "/api/sucursales/**"
                ).hasAnyRole(
                    "ADMIN",
                    "EMPLEADO"
                )

                // ============================================
                // HU-045 Y HU-046
                // REPORTES DE PATRONES DE CONSUMO
                // ============================================

                // Solo ADMIN puede consultar y exportar
                // los reportes de ventas por horario
                // y patrones de consumo diario.

                // Estas reglas deben estar antes de
                // la regla general /api/ventas/**.

                .requestMatchers(
                    HttpMethod.GET,
                    "/api/ventas/reporte/patrones",
                    "/api/ventas/reporte/patrones/exportar"
                ).hasRole("ADMIN")

                // ============================================
                // VENTAS
                // ============================================

                .requestMatchers(
                    "/api/ventas/**"
                ).hasAnyRole(
                    "ADMIN",
                    "EMPLEADO",
                    "REPARTIDOR"
                )

                // ============================================
                // INVENTARIO
                // ============================================

                .requestMatchers(
                    HttpMethod.GET,
                    "/api/inventario/**"
                ).hasAnyRole(
                    "ADMIN",
                    "EMPLEADO",
                    "REPARTIDOR"
                )

                .requestMatchers(
                    HttpMethod.POST,
                    "/api/inventario/**"
                ).hasAnyRole(
                    "ADMIN",
                    "EMPLEADO"
                )

                .requestMatchers(
                    HttpMethod.PUT,
                    "/api/inventario/**"
                ).hasAnyRole(
                    "ADMIN",
                    "EMPLEADO"
                )

                .requestMatchers(
                    HttpMethod.DELETE,
                    "/api/inventario/**"
                ).hasAnyRole(
                    "ADMIN",
                    "EMPLEADO"
                )

                // ============================================
                // PRODUCTOS
                // ============================================

                .requestMatchers(
                    HttpMethod.GET,
                    "/api/productos/**"
                ).authenticated()

                .requestMatchers(
                    HttpMethod.POST,
                    "/api/productos/**"
                ).hasRole("ADMIN")

                .requestMatchers(
                    HttpMethod.PUT,
                    "/api/productos/**"
                ).hasRole("ADMIN")

                .requestMatchers(
                    HttpMethod.DELETE,
                    "/api/productos/**"
                ).hasRole("ADMIN")

                // ============================================
                // CARGAS
                // ============================================

                .requestMatchers(
                    "/api/cargas/**"
                ).hasAnyRole(
                    "ADMIN",
                    "EMPLEADO",
                    "REPARTIDOR"
                )

                // ============================================
                // RUTAS
                // ============================================

                .requestMatchers(
                    "/api/rutas/**"
                ).hasAnyRole(
                    "ADMIN",
                    "EMPLEADO",
                    "REPARTIDOR"
                )

                // ============================================
                // CLIENTES Y SEGMENTOS
                // ============================================

                .requestMatchers(
                    "/api/clientes/segmentos/**"
                ).hasRole("ADMIN")

                .requestMatchers(
                    "/api/clientes/**"
                ).hasAnyRole(
                    "ADMIN",
                    "REPARTIDOR"
                )

                // ============================================
                // AUDITORÍA
                // ============================================

                .requestMatchers(
                    "/api/auditoria/**"
                ).hasAnyRole(
                    "ADMIN",
                    "EMPLEADO",
                    "REPARTIDOR"
                )

                // ============================================
                // MERMAS
                // ============================================

                .requestMatchers(
                    "/api/mermas/**"
                ).hasAnyRole(
                    "ADMIN",
                    "REPARTIDOR"
                )

                // ============================================
                // PROMOCIONES
                // ============================================

                .requestMatchers(
                    HttpMethod.GET,
                    "/api/promociones/**"
                ).authenticated()

                .requestMatchers(
                    HttpMethod.POST,
                    "/api/promociones/**"
                ).hasRole("ADMIN")

                .requestMatchers(
                    HttpMethod.PUT,
                    "/api/promociones/**"
                ).hasRole("ADMIN")

                .requestMatchers(
                    HttpMethod.DELETE,
                    "/api/promociones/**"
                ).hasRole("ADMIN")

                // ============================================
                // TRACCAR
                // ============================================

                .requestMatchers(
                    HttpMethod.GET,
                    "/api/traccar/**"
                ).hasAnyRole(
                    "ADMIN",
                    "EMPLEADO"
                )

                .requestMatchers(
                    HttpMethod.POST,
                    "/api/traccar/**"
                ).hasRole("ADMIN")

                // ============================================
                // DEMÁS SOLICITUDES
                // ============================================

                .anyRequest().authenticated()
            )

            // ================================================
            // FILTRO JWT
            // ================================================

            .addFilterBefore(
                jwtFilter,
                UsernamePasswordAuthenticationFilter.class
            );

        return http.build();
    }
}
