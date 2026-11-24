package cl.tienda.mscarrito.dto;

import java.math.BigDecimal;

public record ItemCarritoResponse(

        String codigo,
        String nombre,
        String imagen,
        int cantidad,
        int stock,
        BigDecimal precioUnitario,
        BigDecimal subtotal,
        boolean disponible

) {
}