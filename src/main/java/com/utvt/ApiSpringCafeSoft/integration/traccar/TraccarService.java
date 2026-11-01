package com.utvt.ApiSpringCafeSoft.integration.traccar;

import com.utvt.ApiSpringCafeSoft.repository.UsuarioRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class TraccarService {

    private static final Logger log =
            LoggerFactory.getLogger(TraccarService.class);

    private final TraccarClient traccarClient;
    private final UsuarioRepository usuarioRepository;

    public TraccarService(TraccarClient traccarClient,
                          UsuarioRepository usuarioRepository) {
        this.traccarClient = traccarClient;
        this.usuarioRepository = usuarioRepository;
    }

    public List<TraccarDeviceDTO> listarDispositivos() {
        log.info("Consultando dispositivos Traccar");
        return traccarClient.getDevices();
    }

    public List<TraccarPositionDTO> listarPosiciones() {
        log.info("Consultando posiciones Traccar");

        List<TraccarPositionDTO> posiciones = traccarClient.getPositions();
        List<TraccarDeviceDTO> devices = traccarClient.getDevices();

        posiciones.forEach(pos -> {
            devices.stream()
                    .filter(d -> d.getId() != null &&
                            d.getId().equals(pos.getDeviceId()))
                    .findFirst()
                    .ifPresent(device -> {
                        pos.setTraccarDeviceId(device.getUniqueId());
                        usuarioRepository
                                .findByTraccarDeviceId(device.getUniqueId())
                                .ifPresent(u ->
                                        pos.setRepartidorNombre(u.getNombre()));
                        if (pos.getRepartidorNombre() == null) {
                            pos.setRepartidorNombre(device.getName());
                        }
                    });
        });

        return posiciones;
    }

    public List<TraccarPositionDTO> posicionesPorRepartidor(Long usuarioId) {
        var usuario = usuarioRepository.findById(usuarioId)
                .orElseThrow(() ->
                        new RuntimeException("Usuario no encontrado"));

        String deviceId = usuario.getTraccarDeviceId();
        if (deviceId == null || deviceId.isBlank()) {
            throw new RuntimeException(
                    "Este repartidor no tiene un device Traccar vinculado");
        }

        return listarPosiciones().stream()
                .filter(p -> deviceId.equals(p.getTraccarDeviceId()))
                .collect(Collectors.toList());
    }

    public List<TraccarPositionDTO> obtenerRutaDelDia(String traccarDeviceId) {
    // Buscar el deviceId numérico de Traccar a partir del uniqueId
    List<TraccarDeviceDTO> devices = traccarClient.getDevices();
    Long deviceId = devices.stream()
            .filter(d -> traccarDeviceId.equals(d.getUniqueId()))
            .map(TraccarDeviceDTO::getId)
            .findFirst()
            .orElseThrow(() -> new RuntimeException("Device no encontrado: " + traccarDeviceId));

    return traccarClient.getRoute(deviceId);
}

}
