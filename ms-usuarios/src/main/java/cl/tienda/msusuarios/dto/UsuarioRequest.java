package cl.tienda.msusuarios.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record UsuarioRequest(

        @Schema(example = "12345678-5", description = "Con dígito verificador. No se puede modificar al editar.")
        @NotBlank(message = "Ingresa el RUT.")
        String rut,

        @Schema(example = "Camila")
        @NotBlank(message = "Ingresa el nombre.")
        @Size(max = 50, message = "El nombre admite hasta 50 caracteres.")
        String nombre,

        @Schema(example = "Rojas Soto")
        @NotBlank(message = "Ingresa los apellidos.")
        @Size(max = 100, message = "Los apellidos admiten hasta 100 caracteres.")
        String apellidos,

        @Schema(example = "camila@correo.cl")
        @NotBlank(message = "Ingresa el correo.")
        @Email(message = "Ingresa un correo válido.")
        @Size(max = 100, message = "El correo admite hasta 100 caracteres.")
        String correo,

        @Schema(example = "2000-05-20", description = "Opcional. Formato yyyy-MM-dd o vacío.")
        String fechaNacimiento,

        @Schema(example = "Región de Valparaíso")
        @NotBlank(message = "Selecciona una región.")
        @Size(max = 100, message = "La región admite hasta 100 caracteres.")
        String region,

        @Schema(example = "Viña del Mar")
        @NotBlank(message = "Selecciona una comuna.")
        @Size(max = 100, message = "La comuna admite hasta 100 caracteres.")
        String comuna,

        @Schema(example = "Avenida Libertad 123")
        @NotBlank(message = "Ingresa la dirección.")
        @Size(max = 300, message = "La dirección admite hasta 300 caracteres.")
        String direccion,

        @Schema(example = "vendedor", allowableValues = {"cliente", "vendedor", "administrador"})
        @NotBlank(message = "Selecciona un rol.")
        @Pattern(regexp = "cliente|vendedor|administrador", message = "El rol debe ser cliente, vendedor o administrador.")
        String rol,

        @Schema(example = "clave123", description = "Obligatoria al crear (4 a 10 caracteres). Al editar, vacía mantiene la actual.")
        String contrasena) {
}
