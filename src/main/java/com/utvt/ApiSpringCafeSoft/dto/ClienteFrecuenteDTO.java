package com.utvt.ApiSpringCafeSoft.dto;

<<<<<<< HEAD
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
=======
import io.swagger.v3.oas.annotations.media.Schema;

/**
 * HU-018: Clientes frecuentes.
 *
 * Representa el resultado de una consulta de frecuencia de compra.
 * Contiene el identificador del cliente de ruta, su nombre,
 * el número de compras realizadas en el periodo y el monto total
 * acumulado. También incluye si cumple el criterio de frecuencia.
 */
@Schema(description = "Cliente frecuente con su número de compras en el periodo")
public class ClienteFrecuenteDTO {

    /** ID del cliente en la tabla clientes (clienteRutaId en Venta). */
    @Schema(description = "ID del cliente de ruta", example = "3")
    private Long clienteId;

    /** Nombre del cliente registrado en la venta. */
    @Schema(description = "Nombre del cliente", example = "Doña Irma")
    private String nombre;

    /** Número de compras (ventas) realizadas en el periodo. */
    @Schema(description = "Número de compras en el periodo", example = "8")
    private long totalCompras;

    /** Suma de los montos de todas las ventas del cliente en el periodo. */
    @Schema(description = "Monto total acumulado en el periodo", example = "1200.00")
    private double montoTotal;

    /**
     * Indica si el cliente cumple el criterio de frecuencia mínima.
     * Se considera frecuente cuando totalCompras >= minimoCompras.
     */
    @Schema(description = "¿Cumple el criterio de cliente frecuente?", example = "true")
    private boolean esFrecuente;

    public ClienteFrecuenteDTO() {}

    public ClienteFrecuenteDTO(Long clienteId, String nombre,
                               long totalCompras, double montoTotal,
                               boolean esFrecuente) {
        this.clienteId   = clienteId;
        this.nombre      = nombre;
        this.totalCompras = totalCompras;
        this.montoTotal  = montoTotal;
        this.esFrecuente = esFrecuente;
    }

    public Long getClienteId()            { return clienteId; }
    public void setClienteId(Long v)      { this.clienteId = v; }

    public String getNombre()             { return nombre; }
    public void setNombre(String v)       { this.nombre = v; }

    public long getTotalCompras()         { return totalCompras; }
    public void setTotalCompras(long v)   { this.totalCompras = v; }

    public double getMontoTotal()         { return montoTotal; }
    public void setMontoTotal(double v)   { this.montoTotal = v; }

    public boolean isEsFrecuente()        { return esFrecuente; }
    public void setEsFrecuente(boolean v) { this.esFrecuente = v; }
>>>>>>> origin/master
}
