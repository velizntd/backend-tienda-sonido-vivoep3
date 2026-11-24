package cl.tienda.mscontacto.dto;

import java.time.ZoneOffset;

import cl.tienda.mscontacto.model.Contacto;

public record ContactoResponse(
        Long id,
        String usuarioId,
        String nombre,
        String correo,
        String comentario,
        String fechaEnvio) {

    public static ContactoResponse desde(Contacto contacto) {
        return new ContactoResponse(
                contacto.getId(),
                contacto.getUsuarioId(),
                contacto.getNombre(),
                contacto.getCorreo() == null ? "" : contacto.getCorreo(),
                contacto.getComentario(),
                contacto.getFechaEnvio().toInstant(ZoneOffset.UTC).toString());
    }
}
