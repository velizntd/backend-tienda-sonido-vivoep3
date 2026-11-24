package cl.tienda.msregistro.dto;

import java.time.LocalDate;
import java.time.LocalDateTime;

public record UsuarioResponse(

        String id,
        String rut,
        String nombre,
        String apellidos,
        String correo,
        LocalDate fechaNacimiento,
        String region,
        String comuna,
        String direccion,
        String rol,
        boolean activo,
        LocalDateTime fechaRegistro

) {
}