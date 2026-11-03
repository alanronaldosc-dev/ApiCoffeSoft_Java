package com.utvt.ApiSpringCafeSoft.dto;

import java.util.List;

public class PuntosResumenDTO {

    private final String nombre;
    private final Integer saldo;
    private final List<MovimientoPuntosDTO> movimientos;

    public PuntosResumenDTO(
            String nombre,
            Integer saldo,
            List<MovimientoPuntosDTO> movimientos) {
        this.nombre = nombre;
        this.saldo = saldo;
        this.movimientos = List.copyOf(movimientos);
    }

    public String getNombre() {
        return nombre;
    }

    public Integer getSaldo() {
        return saldo;
    }

    public List<MovimientoPuntosDTO> getMovimientos() {
        return movimientos;
    }
}
