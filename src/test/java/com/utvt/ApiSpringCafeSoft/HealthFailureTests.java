package com.utvt.ApiSpringCafeSoft;

import com.utvt.ApiSpringCafeSoft.controller.HealthController;
import org.junit.jupiter.api.Test;
import javax.sql.DataSource;
import java.sql.SQLException;
import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;

class HealthFailureTests {
    @Test void databaseFailureReturns503WithoutConnectionDetails() throws Exception {
        DataSource dataSource = mock(DataSource.class);
        when(dataSource.getConnection()).thenThrow(new SQLException("sensitive connection detail"));
        var response = new HealthController(dataSource).health();
        assertEquals(503, response.getStatusCode().value());
        assertEquals("DOWN", response.getBody().get("database"));
        assertFalse(response.getBody().toString().contains("sensitive"));
    }
}
