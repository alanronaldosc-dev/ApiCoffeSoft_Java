package com.utvt.ApiSpringCafeSoft.service;

import com.utvt.ApiSpringCafeSoft.dto.MermaGarrafonDTO;
import com.utvt.ApiSpringCafeSoft.model.Carga;
import com.utvt.ApiSpringCafeSoft.model.MermaGarrafon;
import com.utvt.ApiSpringCafeSoft.repository.CargaRepository;
import com.utvt.ApiSpringCafeSoft.repository.MermaGarrafonRepository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class MermaGarrafonService {

    @Autowired
    private MermaGarrafonRepository mermaGarrafonRepository;

    @Autowired
    private CargaRepository cargaRepository;

    private static final Set<String> CAUSAS_VALIDAS = Set.of(
            "FUGA",
            "ROTURA",
            "DANO_EN_ROSCA",
            "CONTAMINACION",
            "OTRO"
    );

    /**
     * HU-009
     * Registra una merma de garrafones y descuenta la cantidad
     * de la responsabilidad del repartidor.
     */
    @Transactional
    public MermaGarrafonDTO registrarMerma(MermaGarrafonDTO dto) {

        if (dto.getCargaId() == null) {
            throw new RuntimeException("La carga es obligatoria");
        }

        if (dto.getCantidad() == null || dto.getCantidad() <= 0) {
            throw new RuntimeException(
                    "La cantidad de garrafones dañados debe ser mayor a cero"
            );
        }

        if (dto.getCausa() == null || dto.getCausa().isBlank()) {
            throw new RuntimeException("La causa de la merma es obligatoria");
        }

        String causa = normalizarCausa(dto.getCausa());

        if (!CAUSAS_VALIDAS.contains(causa)) {
            throw new RuntimeException(
                    "Causa no válida. Las causas permitidas son: "
                            + String.join(", ", CAUSAS_VALIDAS)
            );
        }

        /*
         * Bloqueamos la carga durante la operación para evitar que dos
         * registros simultáneos descuenten la misma cantidad.
         */
        Carga carga = cargaRepository.findWithLockById(dto.getCargaId())
                .orElseThrow(() ->
                        new RuntimeException(
                                "Carga no encontrada con ID: " + dto.getCargaId()
                        )
                );

        if (carga.getRepartidor() == null) {
            throw new RuntimeException(
                    "La carga no tiene un repartidor asignado"
            );
        }

        if (carga.getInventario() == null) {
            throw new RuntimeException(
                    "La carga no tiene un inventario asociado"
            );
        }

        /*
         * La merma debe registrarse sobre garrafones que ya se encuentran
         * bajo responsabilidad del repartidor.
         */
        if (!"CARGA EN TRÁNSITO".equalsIgnoreCase(carga.getEstado())
                && !"CARGA EN TRANSITO".equalsIgnoreCase(carga.getEstado())) {

            throw new RuntimeException(
                    "Solo se pueden registrar mermas de cargas en tránsito"
            );
        }

        if (carga.getCantidad() == null
                || dto.getCantidad() > carga.getCantidad()) {

            throw new RuntimeException(
                    "La cantidad de merma no puede superar los garrafones "
                            + "bajo responsabilidad del repartidor. Disponibles: "
                            + carga.getCantidad()
            );
        }

        MermaGarrafon merma = new MermaGarrafon();

        merma.setCarga(carga);
        merma.setRepartidor(carga.getRepartidor());
        merma.setInventario(carga.getInventario());
        merma.setCantidad(dto.getCantidad());
        merma.setCausa(causa);
        merma.setObservaciones(dto.getObservaciones());
        merma.setFechaHora(LocalDateTime.now());

        /*
         * Los garrafones ya fueron retirados del inventario de planta
         * cuando se creó la carga. Por ello NO se vuelve a modificar
         * Inventario.cantidad.
         *
         * Únicamente se reduce la cantidad que continúa bajo
         * responsabilidad del repartidor.
         */
        carga.setCantidad(
                carga.getCantidad() - dto.getCantidad()
        );

        cargaRepository.save(carga);

        MermaGarrafon guardada =
                mermaGarrafonRepository.save(merma);

        return convertirDTO(guardada);
    }

    public List<MermaGarrafonDTO> obtenerTodas() {
        return mermaGarrafonRepository.findAll()
                .stream()
                .map(this::convertirDTO)
                .collect(Collectors.toList());
    }

    public MermaGarrafonDTO obtenerPorId(Long id) {

        MermaGarrafon merma =
                mermaGarrafonRepository.findById(id)
                        .orElseThrow(() ->
                                new RuntimeException(
                                        "Merma no encontrada con ID: " + id
                                )
                        );

        return convertirDTO(merma);
    }

    public List<MermaGarrafonDTO> obtenerPorCarga(Long cargaId) {
        return mermaGarrafonRepository
                .findByCargaIdOrderByFechaHoraDesc(cargaId)
                .stream()
                .map(this::convertirDTO)
                .collect(Collectors.toList());
    }

    public List<MermaGarrafonDTO> obtenerPorRepartidor(Long repartidorId) {
        return mermaGarrafonRepository
                .findByRepartidorIdOrderByFechaHoraDesc(repartidorId)
                .stream()
                .map(this::convertirDTO)
                .collect(Collectors.toList());
    }

    public List<MermaGarrafonDTO> obtenerPorCausa(String causa) {

        String causaNormalizada = normalizarCausa(causa);

        return mermaGarrafonRepository
                .findByCausaIgnoreCaseOrderByFechaHoraDesc(causaNormalizada)
                .stream()
                .map(this::convertirDTO)
                .collect(Collectors.toList());
    }

    private String normalizarCausa(String causa) {
        return causa
                .trim()
                .toUpperCase()
                .replace("Á", "A")
                .replace("É", "E")
                .replace("Í", "I")
                .replace("Ó", "O")
                .replace("Ú", "U")
                .replace("Ñ", "N")
                .replace(" ", "_");
    }

    private MermaGarrafonDTO convertirDTO(MermaGarrafon merma) {

        return new MermaGarrafonDTO(
                merma.getId(),
                merma.getCarga() != null
                        ? merma.getCarga().getId()
                        : null,
                merma.getRepartidor() != null
                        ? merma.getRepartidor().getId()
                        : null,
                merma.getRepartidor() != null
                        ? merma.getRepartidor().getNombre()
                        : null,
                merma.getInventario() != null
                        ? merma.getInventario().getId()
                        : null,
                merma.getInventario() != null
                        ? merma.getInventario().getNombre()
                        : null,
                merma.getInventario() != null
                        ? merma.getInventario().getTipo()
                        : null,
                merma.getCantidad(),
                merma.getCausa(),
                merma.getObservaciones(),
                merma.getFechaHora()
        );
    }
}