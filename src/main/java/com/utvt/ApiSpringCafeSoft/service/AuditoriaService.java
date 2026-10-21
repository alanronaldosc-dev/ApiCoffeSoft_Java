package com.utvt.ApiSpringCafeSoft.service;

import com.utvt.ApiSpringCafeSoft.model.Auditoria;
import com.utvt.ApiSpringCafeSoft.model.Usuario;
import com.utvt.ApiSpringCafeSoft.repository.AuditoriaRepository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class AuditoriaService {

    @Autowired
    private AuditoriaRepository auditoriaRepository;

    /**
     * Registra una acción en el log de auditoría.
     */
    @Transactional
    public void registrar(Usuario usuario, String accion, String entidad,
                          Long entidadId, String detalle) {
        Auditoria registro = new Auditoria();
        registro.setUsuario(usuario.getEmail());
        registro.setAccion(accion);
        registro.setEntidad(entidad);
        registro.setEntidadId(entidadId);
        registro.setDetalle(
                detalle != null && detalle.length() > 500
                        ? detalle.substring(0, 500)
                        : detalle
        );
        registro.setFecha(LocalDateTime.now());

        auditoriaRepository.save(registro);
    }

    public List<Auditoria> obtenerRecientes() {
        return auditoriaRepository.findTop50ByOrderByFechaDesc();
    }
}
