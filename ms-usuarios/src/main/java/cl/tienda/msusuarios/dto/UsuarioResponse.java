package cl.tienda.msusuarios.dto;

import java.time.ZoneOffset;

import cl.tienda.msusuarios.model.Usuario;

public record UsuarioResponse(
        String id,
        String rut,
        String nombre,
        String apellidos,
        String correo,
        String fechaNacimiento,
        String region,
        String comuna,
        String direccion,
        String rol,
        boolean activo,
        String fechaRegistro) {

    public static UsuarioResponse desde(Usuario usuario) {
        return new UsuarioResponse(
                usuario.getId(),
                usuario.getRut(),
                usuario.getNombre(),
                usuario.getApellidos(),
                usuario.getCorreo(),
                usuario.getFechaNacimiento() == null ? "" : usuario.getFechaNacimiento().toString(),
                usuario.getRegion(),
                usuario.getComuna(),
                usuario.getDireccion(),
                usuario.getRol(),
                usuario.isActivo(),
                usuario.getFechaRegistro().toInstant(ZoneOffset.UTC).toString());
    }
}
