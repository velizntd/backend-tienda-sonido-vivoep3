package cl.tienda.msregistro.dto;

public record LoginResponse(

        String accessToken,
        String tokenType,
        long expiresIn,
        String usuarioId,
        String rol

) {
}