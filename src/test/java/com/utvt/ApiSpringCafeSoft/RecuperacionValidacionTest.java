package com.utvt.ApiSpringCafeSoft;
import com.utvt.ApiSpringCafeSoft.model.Producto;
import com.utvt.ApiSpringCafeSoft.model.Inventario;
import jakarta.validation.Validation;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
class RecuperacionValidacionTest {
    @Test void validaPreciosDecimales() {
        try(var f=Validation.buildDefaultValidatorFactory()) {
            var v=f.getValidator();var p=new Producto();p.setPrecio(0.5);
            assertTrue(v.validateProperty(p,"precio").isEmpty());p.setPrecio(-0.5);
            assertFalse(v.validateProperty(p,"precio").isEmpty());
            var i=new Inventario();i.setCantidad(-0.5);i.setCantidadMinima(-0.5);i.setPrecioUnitario(-0.5);
            assertFalse(v.validateProperty(i,"cantidad").isEmpty());
            assertFalse(v.validateProperty(i,"cantidadMinima").isEmpty());
            assertFalse(v.validateProperty(i,"precioUnitario").isEmpty());
        }
    }
}
