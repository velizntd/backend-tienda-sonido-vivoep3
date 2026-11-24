package cl.tienda.msregistro.dto;

public record RegistroResponse(

        String id,
        String nombre,
        String correo,
        String rol,
        String mensaje

) {
}