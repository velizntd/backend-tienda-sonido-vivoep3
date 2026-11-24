package cl.tienda.mscarrito.dto;

import jakarta.validation.constraints.*;

public record EstadoPedidoRequest(

        @NotBlank(message = "El estado es obligatorio")
        @Pattern(
                regexp = "enviado|entregado|cancelado",
                message = "El estado solicitado no es valido"
        )
        String estado

) {
}