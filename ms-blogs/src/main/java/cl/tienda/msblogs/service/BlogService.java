package cl.tienda.msblogs.service;

import java.text.Normalizer;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Locale;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import cl.tienda.msblogs.dto.BlogRequest;
import cl.tienda.msblogs.dto.BlogResponse;
import cl.tienda.msblogs.exception.ConflictoException;
import cl.tienda.msblogs.exception.DatosInvalidosException;
import cl.tienda.msblogs.exception.RecursoNoEncontradoException;
import cl.tienda.msblogs.model.Blog;
import cl.tienda.msblogs.model.SeccionBlog;
import cl.tienda.msblogs.repository.BlogRepository;

@Service
public class BlogService {

    private final BlogRepository blogRepository;

    public BlogService(BlogRepository blogRepository) {
        this.blogRepository = blogRepository;
    }

    @Transactional(readOnly = true)
    public List<BlogResponse> listar() {
        return blogRepository.findAllByOrderByFechaPublicacionDesc().stream()
                .map(BlogResponse::desde)
                .toList();
    }

    @Transactional(readOnly = true)
    public BlogResponse buscarPorId(String id) {
        return BlogResponse.desde(obtener(id));
    }

    @Transactional
    public BlogResponse crear(BlogRequest datos) {
        String id = generarId(datos.titulo());
        if (blogRepository.existsById(id)) {
            throw new ConflictoException("Ya existe un artículo con ese título.");
        }
        Blog blog = new Blog();
        blog.setId(id);
        blog.setFechaPublicacion(LocalDateTime.now(ZoneOffset.UTC));
        copiarDatos(datos, blog);
        return BlogResponse.desde(blogRepository.save(blog));
    }

    @Transactional
    public BlogResponse actualizar(String id, BlogRequest datos) {
        Blog blog = obtener(id);
        copiarDatos(datos, blog);
        return BlogResponse.desde(blogRepository.save(blog));
    }

    @Transactional
    public void eliminar(String id) {
        blogRepository.delete(obtener(id));
    }

    private Blog obtener(String id) {
        return blogRepository.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("No se encontró el artículo solicitado."));
    }

    private void copiarDatos(BlogRequest datos, Blog blog) {
        blog.setTitulo(datos.titulo().trim());
        blog.setResumen(datos.resumen().trim());
        blog.setIntroduccion(datos.introduccion().trim());
        blog.setImagen(datos.imagen().trim());
        blog.setDescripcionImagen(datos.descripcionImagen().trim());
        // se reutiliza la misma lista para que hibernate actualice blog_secciones
        blog.getSecciones().clear();
        datos.secciones().forEach(seccion ->
                blog.getSecciones().add(new SeccionBlog(seccion.titulo().trim(), seccion.texto().trim())));
    }

    static String generarId(String titulo) {
        String sinTildes = Normalizer.normalize(titulo, Normalizer.Form.NFD).replaceAll("\\p{M}", "");
        String id = sinTildes.toLowerCase(Locale.ROOT)
                .replaceAll("[^a-z0-9]+", "-")
                .replaceAll("(^-+|-+$)", "");
        if (id.length() > 100) {
            id = id.substring(0, 100).replaceAll("-+$", "");
        }
        if (id.isEmpty()) {
            throw new DatosInvalidosException("El título debe contener letras o números.");
        }
        return id;
    }
}
