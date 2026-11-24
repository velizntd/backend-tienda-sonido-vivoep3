package cl.tienda.mscarrito.dto;

import java.math.BigDecimal;

public record ItemPedidoResponse(

        String codigo,
        String nombre,
        int cantidad,
        BigDecimal precioUnitario

) {
}