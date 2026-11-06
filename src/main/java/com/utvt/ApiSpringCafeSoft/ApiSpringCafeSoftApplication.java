package com.utvt.ApiSpringCafeSoft;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.security.servlet.UserDetailsServiceAutoConfiguration;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication(exclude = { UserDetailsServiceAutoConfiguration.class })
@ConfigurationPropertiesScan
@EnableScheduling
public class ApiSpringCafeSoftApplication {
    public static void main(String[] args) {
        SpringApplication.run(ApiSpringCafeSoftApplication.class, args);
        System.out.println("🚀 API CoffeeSoft iniciada exitosamente!");
    }
}
