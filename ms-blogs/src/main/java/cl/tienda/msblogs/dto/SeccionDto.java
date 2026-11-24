package cl.tienda.msblogs.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record SeccionDto(

        @Schema(example = "Limpieza después de tocar")
        @NotBlank(message = "Cada sección necesita un título.")
        @Size(max = 150, message = "El título de una sección admite hasta 150 caracteres.")
        String titulo,

        @Schema(example = "Pasa un paño seco por las cuerdas para retirar la humedad.")
        @NotBlank(message = "Cada sección necesita un texto.")
        @Size(max = 2000, message = "El texto de una sección admite hasta 2000 caracteres.")
        String texto) {
}
