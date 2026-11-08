package com.utvt.ApiSpringCafeSoft.dto;

/**
 * HU-018: DTO para el reporte de clientes frecuentes.
 * Agrupa las compras de un cliente en un periodo dado.
 */
public class ClienteFrecuenteDTO {

    private Long   clienteId;
    private String nombre;
    private String email;
    private String telefono;
    private int    totalCompras;   // número de compras en el periodo
    private double totalGastado;   // suma de totales en el periodo
    private boolean esFrecuente;   // totalCompras >= minimoCompras

    public ClienteFrecuenteDTO() {}

    public ClienteFrecuenteDTO(Long clienteId, String nombre, String email,
                                String telefono, int totalCompras,
                                double totalGastado, boolean esFrecuente) {
        this.clienteId    = clienteId;
        this.nombre       = nombre;
        this.email        = email;
        this.telefono     = telefono;
        this.totalCompras = totalCompras;
        this.totalGastado = totalGastado;
        this.esFrecuente  = esFrecuente;
    }

    public Long    getClienteId()    { return clienteId; }
    public void    setClienteId(Long clienteId) { this.clienteId = clienteId; }

    public String  getNombre()       { return nombre; }
    public void    setNombre(String nombre) { this.nombre = nombre; }

    public String  getEmail()        { return email; }
    public void    setEmail(String email) { this.email = email; }

    public String  getTelefono()     { return telefono; }
    public void    setTelefono(String telefono) { this.telefono = telefono; }

    public int     getTotalCompras() { return totalCompras; }
    public void    setTotalCompras(int totalCompras) { this.totalCompras = totalCompras; }

    public double  getTotalGastado() { return totalGastado; }
    public void    setTotalGastado(double totalGastado) { this.totalGastado = totalGastado; }

    public boolean isEsFrecuente()   { return esFrecuente; }
    public void    setEsFrecuente(boolean esFrecuente) { this.esFrecuente = esFrecuente; }
}
