package cl.tienda.msblogs.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import cl.tienda.msblogs.dto.BlogRequest;
import cl.tienda.msblogs.dto.BlogResponse;
import cl.tienda.msblogs.dto.SeccionDto;
import cl.tienda.msblogs.exception.ConflictoException;
import cl.tienda.msblogs.exception.RecursoNoEncontradoException;
import cl.tienda.msblogs.model.Blog;
import cl.tienda.msblogs.model.SeccionBlog;
import cl.tienda.msblogs.repository.BlogRepository;

@ExtendWith(MockitoExtension.class)
class BlogServiceTest {

    @Mock
    private BlogRepository blogRepository;

    private BlogService blogService;

    @BeforeEach
    void preparar() {
        blogService = new BlogService(blogRepository);
    }

    private BlogRequest datos(String titulo, List<SeccionDto> secciones) {
        return new BlogRequest(titulo, "Resumen", "Introducción", "/img/a.jpg", "Imagen", secciones);
    }

    @Test
    void generaIdSinTildesNiEspacios() {
        assertThat(BlogService.generarId("Cómo elegir tu primera guitarra"))
                .isEqualTo("como-elegir-tu-primera-guitarra");
    }

    @Test
    void crearGuardaElArticuloConSusSecciones() {
        when(blogRepository.existsById("cuidado-del-teclado")).thenReturn(false);
        when(blogRepository.save(any(Blog.class))).thenAnswer(invocacion -> invocacion.getArgument(0));

        BlogResponse respuesta = blogService.crear(datos("Cuidado del teclado",
                List.of(new SeccionDto("Limpieza", "Usa un paño seco."), new SeccionDto("Guardado", "Usa una funda."))));

        assertThat(respuesta.id()).isEqualTo("cuidado-del-teclado");
        assertThat(respuesta.secciones()).hasSize(2);
        assertThat(respuesta.secciones().get(1).titulo()).isEqualTo("Guardado");
    }

    @Test
    void crearConTituloRepetidoLanzaConflicto() {
        when(blogRepository.existsById("cuidado-del-teclado")).thenReturn(true);

        assertThatThrownBy(() -> blogService.crear(datos("Cuidado del teclado", List.of(new SeccionDto("A", "B")))))
                .isInstanceOf(ConflictoException.class);
        verify(blogRepository, never()).save(any());
    }

    @Test
    void actualizarReemplazaLasSeccionesYConservaElId() {
        Blog blog = new Blog();
        blog.setId("cuidado-del-teclado");
        blog.getSecciones().add(new SeccionBlog("Antigua", "Texto antiguo"));
        when(blogRepository.findById("cuidado-del-teclado")).thenReturn(Optional.of(blog));
        when(blogRepository.save(any(Blog.class))).thenAnswer(invocacion -> invocacion.getArgument(0));

        BlogResponse respuesta = blogService.actualizar("cuidado-del-teclado",
                datos("Nuevo título", List.of(new SeccionDto("Nueva", "Texto nuevo"))));

        assertThat(respuesta.id()).isEqualTo("cuidado-del-teclado");
        assertThat(respuesta.titulo()).isEqualTo("Nuevo título");
        assertThat(respuesta.secciones()).extracting(SeccionDto::titulo).containsExactly("Nueva");
    }

    @Test
    void eliminarArticuloInexistenteLanzaNoEncontrado() {
        when(blogRepository.findById("no-existe")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> blogService.eliminar("no-existe"))
                .isInstanceOf(RecursoNoEncontradoException.class);
    }
}
