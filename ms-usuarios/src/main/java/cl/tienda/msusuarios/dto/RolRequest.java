package cl.tienda.msusuarios.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

public record RolRequest(

        @Schema(example = "vendedor", allowableValues = {"cliente", "vendedor", "administrador"})
        @NotBlank(message = "Selecciona un rol.")
        @Pattern(regexp = "cliente|vendedor|administrador", message = "El rol debe ser cliente, vendedor o administrador.")
        String rol) {
}
