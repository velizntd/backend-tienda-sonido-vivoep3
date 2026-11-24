package cl.tienda.mscontacto.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record ContactoRequest(

        @Schema(example = "Camila Rojas")
        @NotBlank(message = "El nombre es obligatorio.")
        @Size(max = 100, message = "El nombre no puede tener más de 100 caracteres.")
        String nombre,

        @Schema(example = "camila@correo.cl", description = "Opcional.")
        @Email(message = "Ingresa un correo electrónico válido.")
        @Size(max = 100, message = "El correo no puede tener más de 100 caracteres.")
        String correo,

        @Schema(example = "¿Tienen stock de la guitarra Yamaha C40?")
        @NotBlank(message = "El comentario es obligatorio.")
        @Size(max = 500, message = "El comentario no puede tener más de 500 caracteres.")
        String comentario) {
}
