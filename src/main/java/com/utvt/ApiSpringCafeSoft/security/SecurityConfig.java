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

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration config = new CorsConfiguration();
        config.setAllowedOrigins(List.of(
            "http://localhost:5173",
            "http://localhost:3000",
            "http://78.14.95.237:8080"
        ));
        config.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "OPTIONS"));
        config.setAllowedHeaders(List.of("*"));
        config.setAllowCredentials(false);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/api/**", config);
        return source;
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            .cors(cors -> cors.configurationSource(corsConfigurationSource()))
            .csrf(csrf -> csrf.disable())
            .sessionManagement(session ->
                session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .authorizeHttpRequests(auth -> auth
                .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()
                .requestMatchers("/api/usuarios/login").permitAll()
                .requestMatchers(HttpMethod.POST, "/api/usuarios").permitAll()
                .requestMatchers(
                    "/v3/api-docs", "/v3/api-docs/**",
                    "/api-docs",    "/api-docs/**",
                    "/swagger-ui/**", "/swagger-ui.html",
                    "/swagger-resources/**", "/webjars/**"
                ).permitAll()
                .requestMatchers(HttpMethod.GET,    "/api/usuarios/**").hasRole("ADMIN")
                .requestMatchers(HttpMethod.PUT,    "/api/usuarios/**").hasRole("ADMIN")
                .requestMatchers(HttpMethod.DELETE, "/api/usuarios/**").hasRole("ADMIN")
                .requestMatchers("/api/insumos/**").hasAnyRole("ADMIN", "EMPLEADO")
                .requestMatchers("/api/lotes/**").hasAnyRole("ADMIN", "EMPLEADO")
                .requestMatchers("/api/proveedores/**").hasAnyRole("ADMIN", "EMPLEADO")
                .requestMatchers("/api/categorias/**").hasAnyRole("ADMIN", "EMPLEADO")
                .requestMatchers("/api/sucursales/**").hasAnyRole("ADMIN", "EMPLEADO")
                .requestMatchers("/api/ventas/**").hasAnyRole("ADMIN", "EMPLEADO")
                .requestMatchers("/api/inventario/**").hasAnyRole("ADMIN", "EMPLEADO")
                .requestMatchers(HttpMethod.GET,    "/api/productos/**").authenticated()
                .requestMatchers(HttpMethod.POST,   "/api/productos/**").hasRole("ADMIN")
                .requestMatchers(HttpMethod.PUT,    "/api/productos/**").hasRole("ADMIN")
                .requestMatchers(HttpMethod.DELETE, "/api/productos/**").hasRole("ADMIN")
                .requestMatchers("/api/cargas/**").hasAnyRole("ADMIN", "REPARTIDOR")
                .requestMatchers("/api/mermas/**").hasAnyRole("ADMIN", "REPARTIDOR")
                .anyRequest().authenticated()
            )
            .addFilterBefore(jwtFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }
}
