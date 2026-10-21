package com.utvt.ApiSpringCafeSoft;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.security.servlet.UserDetailsServiceAutoConfiguration;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;

@SpringBootApplication(exclude = { UserDetailsServiceAutoConfiguration.class })
@ConfigurationPropertiesScan
public class ApiSpringCafeSoftApplication {
    public static void main(String[] args) {
        SpringApplication.run(ApiSpringCafeSoftApplication.class, args);
        System.out.println("🚀 API CoffeeSoft iniciada exitosamente!");
    }
}
