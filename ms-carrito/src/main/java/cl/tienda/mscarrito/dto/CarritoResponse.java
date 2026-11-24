package cl.tienda.mscarrito.dto;

import java.math.BigDecimal;
import java.util.List;

public record CarritoResponse(

        String id,
        String usuarioId,
        List<ItemCarritoResponse> items,
        int cantidadTotal,
        BigDecimal total

) {
}