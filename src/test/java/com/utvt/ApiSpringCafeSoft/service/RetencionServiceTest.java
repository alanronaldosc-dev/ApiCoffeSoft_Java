package com.utvt.ApiSpringCafeSoft.service;

import com.utvt.ApiSpringCafeSoft.dto.NuevaAccionRetencionDTO;
import com.utvt.ApiSpringCafeSoft.model.Auditoria;
import com.utvt.ApiSpringCafeSoft.repository.AuditoriaRepository;
import com.utvt.ApiSpringCafeSoft.repository.ClienteRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class RetencionServiceTest {

    @Mock
    private AuditoriaRepository auditoriaRepository;

    @Mock
    private ClienteRepository clienteRepository;

    @InjectMocks
    private RetencionService service;

    private Auditoria auditoria(Long id, String accion, String detalle) {
        Auditoria a = new Auditoria();
        a.setId(id);
        a.setUsuario("Ana");
        a.setAccion(accion);
        a.setEntidad("CLIENTE");
        a.setEntidadId(1L);
        a.setDetalle(detalle);
        a.setFecha(LocalDateTime.now());
        return a;
    }

    @Test
    void registrar_accionValida_seGuardaEnAuditoria() {
        when(clienteRepository.existsById(1L)).thenReturn(true);
        when(auditoriaRepository.save(any(Auditoria.class))).thenAnswer(inv -> {
            Auditoria a = inv.getArgument(0);
            a.setId(10L);
            return a;
        });

        var resultado = service.registrar(1L,
                new NuevaAccionRetencionDTO("llamada", " Ana ", LocalDateTime.now(), "Llamar mañana"));

        assertEquals("LLAMADA", resultado.tipo());
        assertEquals("Ana", resultado.responsable());
        assertFalse(resultado.atendida());

        ArgumentCaptor<Auditoria> captor = ArgumentCaptor.forClass(Auditoria.class);
        verify(auditoriaRepository).save(captor.capture());
        assertEquals("CLIENTE", captor.getValue().getEntidad());
        assertEquals(1L, captor.getValue().getEntidadId());
    }

    @Test
    void registrar_tipoInvalido_lanzaBadRequest() {
        when(clienteRepository.existsById(1L)).thenReturn(true);

        assertThrows(ResponseStatusException.class, () -> service.registrar(1L,
                new NuevaAccionRetencionDTO("OTRO", "Ana", LocalDateTime.now(), null)));
    }

    @Test
    void listar_accionConRegistroAtendida_devuelveAtendida() {
        when(clienteRepository.existsById(1L)).thenReturn(true);
        when(auditoriaRepository.findByEntidadAndEntidadIdAndAccionInOrderByFechaDesc(
                any(), any(), any())).thenReturn(List.of(auditoria(10L, "LLAMADA", "Llamar")));
        when(auditoriaRepository.findByEntidadAndEntidadIdAndAccionOrderByFechaDesc(
                any(), any(), any())).thenReturn(List.of(auditoria(11L, "ATENDIDA", "accionId=10")));

        var resultado = service.listar(1L);

        assertEquals(1, resultado.size());
        assertTrue(resultado.get(0).atendida());
        assertNotNull(resultado.get(0).fechaAtencion());
    }

    @Test
    void atender_accionPendiente_registraAtencion() {
        when(clienteRepository.existsById(1L)).thenReturn(true);
        when(auditoriaRepository.findByIdAndEntidadAndEntidadIdAndAccionIn(
                10L, "CLIENTE", 1L, List.of("LLAMADA", "VISITA", "PROMOCION")))
                .thenReturn(Optional.of(auditoria(10L, "LLAMADA", "Llamar")));
        when(auditoriaRepository.findByEntidadAndEntidadIdAndAccionOrderByFechaDesc(
                any(), any(), any())).thenReturn(List.of());
        when(auditoriaRepository.save(any(Auditoria.class))).thenAnswer(inv -> inv.getArgument(0));

        var resultado = service.atender(1L, 10L);

        assertTrue(resultado.atendida());
        assertNotNull(resultado.fechaAtencion());
        ArgumentCaptor<Auditoria> captor = ArgumentCaptor.forClass(Auditoria.class);
        verify(auditoriaRepository).save(captor.capture());
        assertEquals("ATENDIDA", captor.getValue().getAccion());
        assertEquals("accionId=10", captor.getValue().getDetalle());
    }
}
