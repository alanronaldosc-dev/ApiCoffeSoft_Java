package com.utvt.ApiSpringCafeSoft.repository;

import java.time.LocalDateTime;

public interface ClienteCompraStatsProjection {
    Long getClienteId();
    Long getCompras();
    LocalDateTime getUltimaCompra();
}
