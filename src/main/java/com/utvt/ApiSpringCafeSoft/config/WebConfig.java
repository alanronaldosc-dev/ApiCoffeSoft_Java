package com.utvt.ApiSpringCafeSoft.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

// CORS gestionado por SecurityConfig (JwtFilter + corsConfigurationSource)
// Este bean no aplica reglas adicionales para evitar conflictos con Spring Security.
@Configuration
public class WebConfig implements WebMvcConfigurer {
    // Vacío intencionalmente — ver SecurityConfig.corsConfigurationSource()
}
