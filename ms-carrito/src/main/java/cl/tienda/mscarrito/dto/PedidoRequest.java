package cl.tienda.mscarrito.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;

public record PedidoRequest(

        @Valid
        @NotNull(message = "Los datos del comprador son obligatorios")
        CompradorRequest comprador,

        @Valid
        @NotNull(message = "Los datos de entrega son obligatorios")
        EntregaRequest entrega

) {
}