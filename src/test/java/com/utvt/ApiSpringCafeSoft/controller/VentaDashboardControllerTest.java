package com.utvt.ApiSpringCafeSoft.controller;

import com.utvt.ApiSpringCafeSoft.dto.DashboardVentasDTO;
import com.utvt.ApiSpringCafeSoft.service.VentaDashboardService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * HU-041: tests del controlador del dashboard de ventas.
 *
 * MockMvc en modo standalone: valida el contrato HTTP sin levantar
 * el contexto de Spring ni la seguridad JWT.
 */
class VentaDashboardControllerTest {

    private VentaDashboardService service;
    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        service = mock(VentaDashboardService.class);
        mockMvc = MockMvcBuilders
                .standaloneSetup(new VentaDashboardController(service))
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
    }

    private DashboardVentasDTO dashboard() {
        DashboardVentasDTO dto = new DashboardVentasDTO();
        dto.setPeriodo("mes");
        dto.setPeriodoEtiqueta("noviembre de 2026");
        dto.setSucursalNombre("Bonafont");
        dto.setNumeroVentas(4);
        dto.setMontoTotal(1400.0);
        dto.setTicketPromedio(350.0);
        return dto;
    }

    @Test
    @DisplayName("GET /api/ventas/dashboard devuelve los indicadores del periodo")
    void obtenerDashboard_retornaIndicadores() throws Exception {
        when(service.obtenerDashboard("mes")).thenReturn(dashboard());

        mockMvc.perform(get("/api/ventas/dashboard").param("periodo", "mes"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.periodo").value("mes"))
                .andExpect(jsonPath("$.numeroVentas").value(4))
                .andExpect(jsonPath("$.montoTotal").value(1400.0))
                .andExpect(jsonPath("$.ticketPromedio").value(350.0));

        verify(service).obtenerDashboard("mes");
    }

    @Test
    @DisplayName("Sin parametro periodo usa 'hoy' por defecto")
    void obtenerDashboard_sinPeriodo_usaHoy() throws Exception {
        when(service.obtenerDashboard("hoy")).thenReturn(dashboard());

        mockMvc.perform(get("/api/ventas/dashboard"))
                .andExpect(status().isOk());

        verify(service).obtenerDashboard("hoy");
    }

    @Test
    @DisplayName("Un periodo no valido responde 400")
    void obtenerDashboard_periodoInvalido_responde400() throws Exception {
        when(service.obtenerDashboard(anyString()))
                .thenThrow(new IllegalArgumentException("Periodo no valido"));

        mockMvc.perform(get("/api/ventas/dashboard").param("periodo", "semestre"))
                .andExpect(status().isBadRequest());
    }
}
