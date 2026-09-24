package com.utvt.ApiSpringCafeSoft.repository;

import com.utvt.ApiSpringCafeSoft.model.MermaGarrafon;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MermaGarrafonRepository
        extends JpaRepository<MermaGarrafon, Long> {

    // Historial completo de mermas de una carga.
    List<MermaGarrafon> findByCargaIdOrderByFechaHoraDesc(Long cargaId);

    // Historial de mermas asociadas a un repartidor.
    List<MermaGarrafon> findByRepartidorIdOrderByFechaHoraDesc(Long repartidorId);

    // Historial filtrado por causa.
    List<MermaGarrafon> findByCausaIgnoreCaseOrderByFechaHoraDesc(String causa);
}