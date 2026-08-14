package com.utvt.ApiSpringCafeSoft;  // ← ¡CAMBIAR AQUÍ!

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class ApiSpringCafeSoftApplication {
    public static void main(String[] args) {
        SpringApplication.run(ApiSpringCafeSoftApplication.class, args);
        System.out.println("🚀 API CoffeeSoft iniciada exitosamente!");
    }
}