package com.utvt.ApiSpringCafeSoft.controller;

import org.springframework.http.CacheControl;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Map;

@RestController
public class HealthController {
    private final DataSource dataSource;

    public HealthController(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    @GetMapping("/api/health")
    public ResponseEntity<Map<String, String>> health() {
        try (Connection connection = dataSource.getConnection();
             PreparedStatement statement = connection.prepareStatement("SELECT 1")) {
            statement.setQueryTimeout(2);
            try (ResultSet result = statement.executeQuery()) {
                if (result.next() && result.getInt(1) == 1) {
                    return ResponseEntity.ok().cacheControl(CacheControl.noStore())
                            .body(Map.of("status", "UP", "database", "UP"));
                }
            }
        } catch (SQLException exception) {
            // No exponer los detalles de conexion en la respuesta publica.
        }
        return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE)
                .cacheControl(CacheControl.noStore())
                .body(Map.of("status", "DOWN", "database", "DOWN"));
    }
}
