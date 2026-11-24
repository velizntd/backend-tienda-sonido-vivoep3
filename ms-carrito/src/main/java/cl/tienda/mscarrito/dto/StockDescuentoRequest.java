package cl.tienda.mscarrito.dto;

import java.util.List;

public record StockDescuentoRequest(

        String pedidoId,
        List<StockItemRequest> items

) {
}