package cl.tienda.mscarrito.dto;

import jakarta.validation.constraints.*;

public record CompradorRequest(

        @NotBlank(message = "El nombre es obligatorio")
        @Size(max = 50)
        String nombre,

        @NotBlank(message = "El apellido es obligatorio")
        @Size(max = 100)
        String apellido,

        @NotBlank(message = "El correo es obligatorio")
        @Email(message = "El correo no es valido")
        @Size(max = 100)
        String correo

) {
}