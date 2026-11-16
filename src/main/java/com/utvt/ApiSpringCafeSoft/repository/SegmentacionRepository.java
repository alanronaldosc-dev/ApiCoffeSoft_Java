
package com.utvt.ApiSpringCafeSoft.repository;

import com.utvt.ApiSpringCafeSoft.model.Venta;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import java.util.List;

/**
 * HU-035 y HU-036.
 * Obtiene la actividad de compras asociadas
 * a clientes de ruta.
 */
public interface SegmentacionRepository
        extends JpaRepository<Venta, Long> {

    @Query("""
        SELECT v.clienteRutaId, COUNT(v), MAX(v.fecha)
        FROM Venta v
        WHERE v.clienteRutaId IS NOT NULL
        GROUP BY v.clienteRutaId
        """)
    List<Object[]> actividadPorCliente();
}
