package cl.tienda.msregistro.dto;

import java.time.LocalDate;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Past;
import jakarta.validation.constraints.Size;

public record RegistroRequest(

        @NotBlank(message = "El RUT es obligatorio")
        @Size(max = 20)
        String rut,

        @NotBlank(message = "El nombre es obligatorio")
        @Size(max = 50)
        String nombre,

        @NotBlank(message = "Los apellidos son obligatorios")
        @Size(max = 100)
        String apellidos,

        @NotBlank(message = "El correo es obligatorio")
        @Email(message = "El correo no es valido")
        @Size(max = 100)
        String correo,

        @NotNull(message = "La fecha de nacimiento es obligatoria")
        @Past(message = "La fecha de nacimiento debe ser anterior a hoy")
        LocalDate fechaNacimiento,

        @NotBlank(message = "La region es obligatoria")
        @Size(max = 100)
        String region,

        @NotBlank(message = "La comuna es obligatoria")
        @Size(max = 100)
        String comuna,

        @NotBlank(message = "La direccion es obligatoria")
        @Size(max = 300)
        String direccion,

        @NotBlank(message = "La contrasena es obligatoria")
        @Size(min = 4, max = 10,
              message = "La contrasena debe tener entre 4 y 10 caracteres")
        String contrasena

) {
}