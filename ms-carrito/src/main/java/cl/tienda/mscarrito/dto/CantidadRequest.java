package cl.tienda.mscarrito.dto;

import jakarta.validation.constraints.*;

public record CantidadRequest(

        @NotNull(message = "La cantidad es obligatoria")
        @Min(value = 1, message = "La cantidad minima es 1")
        @Max(value = 999, message = "La cantidad maxima es 999")
        Integer cantidad

) {
}