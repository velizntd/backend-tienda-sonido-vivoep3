package cl.tienda.mscarrito.dto;

import jakarta.validation.constraints.*;

public record EntregaRequest(

        @NotBlank(message = "El tipo de entrega es obligatorio")
        @Pattern(
                regexp = "despacho|retiro",
                message = "El tipo de entrega debe ser despacho o retiro"
        )
        String tipo,

        @Size(max = 200)
        String calle,

        @Size(max = 20)
        String numero,

        @Size(max = 100)
        String complemento,

        @Size(max = 100)
        String region,

        @Size(max = 100)
        String comuna,

        @Size(max = 300)
        String indicaciones

) {
}