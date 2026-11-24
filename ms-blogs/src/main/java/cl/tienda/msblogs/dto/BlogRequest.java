package cl.tienda.msblogs.dto;

import java.util.List;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;

public record BlogRequest(

        @Schema(example = "Cómo cuidar tu teclado")
        @NotBlank(message = "Ingresa el título.")
        @Size(max = 150, message = "El título admite hasta 150 caracteres.")
        String titulo,

        @Schema(example = "Consejos simples para que tu teclado dure más.")
        @NotBlank(message = "Ingresa el resumen.")
        @Size(max = 500, message = "El resumen admite hasta 500 caracteres.")
        String resumen,

        @Schema(example = "Un teclado bien cuidado mantiene su sonido y sus teclas por años.")
        @NotBlank(message = "Ingresa la introducción.")
        @Size(max = 2000, message = "La introducción admite hasta 2000 caracteres.")
        String introduccion,

        @Schema(example = "/img/productos/yamaha-c40-GA003.jpg")
        @NotBlank(message = "Ingresa la ruta de la imagen.")
        @Size(max = 300, message = "La ruta de la imagen admite hasta 300 caracteres.")
        String imagen,

        @Schema(example = "Teclado electrónico")
        @NotBlank(message = "Ingresa la descripción de la imagen.")
        @Size(max = 200, message = "La descripción de la imagen admite hasta 200 caracteres.")
        String descripcionImagen,

        @NotEmpty(message = "El artículo necesita al menos una sección.")
        @Size(max = 20, message = "El artículo admite hasta 20 secciones.")
        List<@Valid SeccionDto> secciones) {
}
