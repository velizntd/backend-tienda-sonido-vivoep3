package cl.tienda.mscarrito.dto;

import jakarta.validation.constraints.*;

public record ItemCarritoRequest(

        @NotBlank(message = "El codigo es obligatorio")
        @Size(max = 50)
        String codigo,

        @NotNull(message = "La cantidad es obligatoria")
        @Min(value = 1, message = "La cantidad minima es 1")
        @Max(value = 999, message = "La cantidad maxima es 999")
        Integer cantidad

) {
}