package cl.tienda.mscarrito.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public record PedidoResponse(

        String id,
        LocalDateTime fecha,
        String usuarioId,
        String estado,
        CompradorRequest comprador,
        EntregaRequest entrega,
        List<ItemPedidoResponse> items,
        BigDecimal total

) {
}