package cl.tienda.msblogs.dto;

import java.util.List;

import cl.tienda.msblogs.model.Blog;

public record BlogResponse(
        String id,
        String titulo,
        String resumen,
        String introduccion,
        String imagen,
        String descripcionImagen,
        List<SeccionDto> secciones) {

    public static BlogResponse desde(Blog blog) {
        return new BlogResponse(
                blog.getId(),
                blog.getTitulo(),
                blog.getResumen(),
                blog.getIntroduccion(),
                blog.getImagen(),
                blog.getDescripcionImagen(),
                blog.getSecciones().stream()
                        .map(seccion -> new SeccionDto(seccion.getTitulo(), seccion.getTexto()))
                        .toList());
    }
}
