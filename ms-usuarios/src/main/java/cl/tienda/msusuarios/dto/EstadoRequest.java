package cl.tienda.msusuarios.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;

public record EstadoRequest(

        @Schema(example = "false", description = "true activa la cuenta; false la desactiva (desactivación lógica).")
        @NotNull(message = "Indica si el usuario queda activo.")
        Boolean activo) {
}
