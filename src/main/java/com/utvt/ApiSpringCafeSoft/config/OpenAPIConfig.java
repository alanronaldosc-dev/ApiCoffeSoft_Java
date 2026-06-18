package com.utvt.ApiSpringCafeSoft.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
public class OpenAPIConfig {

    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("API CoffeeSoft - Sistema de Gestión de Usuarios")
                        .version("1.0.0")
                        .description("""
                                # API REST para la gestión de usuarios del sistema CoffeeSoft
                                
                                ## 📋 Funcionalidades Principales:
                                - **CRUD completo** de usuarios (Crear, Leer, Actualizar, Eliminar)
                                - **Búsquedas** por email, nombre y tipo de usuario
                                - **Validaciones** de datos en tiempo real
                                - **Encriptación** de contraseñas con BCrypt
                                - **Documentación interactiva** con Swagger UI
                                
                                ## 👥 Roles de Usuario:
                                - **0**: Administrador (acceso total al sistema)
                                - **1**: Empleado (acceso limitado a funcionalidades)
                                - **2**: Cliente (acceso básico a la plataforma)
                                
                                ## 🔒 Seguridad:
                                - Contraseñas encriptadas con BCrypt
                                - Validación de emails (solo Gmail y Hotmail)
                                - Validación de teléfonos (10 dígitos)
                                """)
                        .contact(new Contact()
                                .name("UTVT - CoffeeSoft Team")
                                .email("soporte@coffeesoft.com")
                                .url("https://coffeesoft.com"))
                        .license(new License()
                                .name("MIT License")
                                .url("https://opensource.org/licenses/MIT")))
                .servers(List.of(
                        new Server()
                                .url("http://localhost:8080")
                                .description("🌐 Servidor de Desarrollo"),
                        new Server()
                                .url("https://api.coffeesoft.com")
                                .description("🚀 Servidor de Producción")
                ));
    }
}