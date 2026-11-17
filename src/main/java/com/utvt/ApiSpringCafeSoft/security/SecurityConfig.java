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
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;
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
        config.setAllowedOriginPatterns(List.of("*"));
        config.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "OPTIONS", "PATCH"));
        config.setAllowedHeaders(List.of("*"));
        config.setExposedHeaders(List.of("Authorization"));
        config.setAllowCredentials(false);
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);
        return source;
    }

    private static AntPathRequestMatcher ant(HttpMethod method, String pattern) {
        return new AntPathRequestMatcher(pattern, method.name());
    }

    private static AntPathRequestMatcher ant(String pattern) {
        return new AntPathRequestMatcher(pattern);
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            .cors(cors -> cors.configurationSource(corsConfigurationSource()))
            .csrf(csrf -> csrf.disable())
            .httpBasic(httpBasic -> httpBasic.disable())
            .formLogin(formLogin -> formLogin.disable())
            .sessionManagement(session ->
                session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .authorizeHttpRequests(auth -> auth
                .requestMatchers(ant(HttpMethod.OPTIONS, "/**")).permitAll()
                .requestMatchers(ant("/api/usuarios/login")).permitAll()
                .requestMatchers(ant(HttpMethod.POST, "/api/usuarios")).permitAll()
                .requestMatchers(
                    ant("/v3/api-docs"),          ant("/v3/api-docs/**"),
                    ant("/api-docs"),             ant("/api-docs/**"),
                    ant("/swagger-ui/**"),        ant("/swagger-ui.html"),
                    ant("/swagger-resources/**"), ant("/webjars/**"),
                    ant("/h2-console"),           ant("/h2-console/**")
                ).permitAll()
                .requestMatchers(ant(HttpMethod.GET,    "/api/usuarios/**")).hasRole("ADMIN")
                .requestMatchers(ant(HttpMethod.PUT,    "/api/usuarios/**")).hasRole("ADMIN")
                .requestMatchers(ant(HttpMethod.DELETE, "/api/usuarios/**")).hasRole("ADMIN")
                .requestMatchers(ant("/api/insumos/**")).hasAnyRole("ADMIN", "EMPLEADO")
                .requestMatchers(ant("/api/lotes/**")).hasAnyRole("ADMIN", "EMPLEADO")
                .requestMatchers(ant("/api/proveedores/**")).hasAnyRole("ADMIN", "EMPLEADO")
                .requestMatchers(ant("/api/categorias/**")).hasAnyRole("ADMIN", "EMPLEADO")
                .requestMatchers(ant("/api/sucursales/**")).hasAnyRole("ADMIN", "EMPLEADO")
                .requestMatchers(ant("/api/ventas/**")).hasAnyRole("ADMIN", "EMPLEADO", "REPARTIDOR")
                .requestMatchers(ant(HttpMethod.GET,    "/api/inventario/**")).hasAnyRole("ADMIN", "EMPLEADO", "REPARTIDOR")
                .requestMatchers(ant(HttpMethod.POST,   "/api/inventario/**")).hasAnyRole("ADMIN", "EMPLEADO")
                .requestMatchers(ant(HttpMethod.PUT,    "/api/inventario/**")).hasAnyRole("ADMIN", "EMPLEADO")
                .requestMatchers(ant(HttpMethod.DELETE, "/api/inventario/**")).hasAnyRole("ADMIN", "EMPLEADO")
                .requestMatchers(ant(HttpMethod.GET,    "/api/productos/**")).authenticated()
                .requestMatchers(ant(HttpMethod.POST,   "/api/productos/**")).hasRole("ADMIN")
                .requestMatchers(ant(HttpMethod.PUT,    "/api/productos/**")).hasRole("ADMIN")
                .requestMatchers(ant(HttpMethod.DELETE, "/api/productos/**")).hasRole("ADMIN")
                .requestMatchers(ant("/api/cargas/**")).hasAnyRole("ADMIN", "EMPLEADO", "REPARTIDOR")
                .requestMatchers(ant("/api/rutas/**")).hasAnyRole("ADMIN", "EMPLEADO", "REPARTIDOR")
                .requestMatchers(ant("/api/clientes/segmentos/**")).hasRole("ADMIN")
                .requestMatchers(ant("/api/clientes/**")).hasAnyRole("ADMIN", "REPARTIDOR")
                .requestMatchers(ant("/api/auditoria/**")).hasAnyRole("ADMIN", "EMPLEADO", "REPARTIDOR")
                .requestMatchers(ant("/api/mermas/**")).hasAnyRole("ADMIN", "REPARTIDOR")
                .requestMatchers(ant(HttpMethod.GET,    "/api/promociones/**")).authenticated()
                .requestMatchers(ant(HttpMethod.POST,   "/api/promociones/**")).hasRole("ADMIN")
                .requestMatchers(ant(HttpMethod.PUT,    "/api/promociones/**")).hasRole("ADMIN")
                .requestMatchers(ant(HttpMethod.DELETE, "/api/promociones/**")).hasRole("ADMIN")
                .requestMatchers(ant(HttpMethod.GET,    "/api/traccar/**")).hasAnyRole("ADMIN", "EMPLEADO")
                .requestMatchers(ant(HttpMethod.POST,   "/api/traccar/**")).hasRole("ADMIN")
                .anyRequest().authenticated()
            )
            .addFilterBefore(jwtFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }
}
