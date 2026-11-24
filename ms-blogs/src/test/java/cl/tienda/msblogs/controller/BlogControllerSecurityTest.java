package cl.tienda.msblogs.controller;

import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import cl.tienda.msblogs.config.SecurityConfig;
import cl.tienda.msblogs.dto.BlogResponse;
import cl.tienda.msblogs.dto.SeccionDto;
import cl.tienda.msblogs.service.BlogService;

@WebMvcTest(BlogController.class)
@Import(SecurityConfig.class)
@TestPropertySource(properties = "app.jwt.secret=MDEyMzQ1Njc4OWFiY2RlZjAxMjM0NTY3ODlhYmNkZWY=")
class BlogControllerSecurityTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private BlogService blogService;

    @Test
    void leerSinSesionResponde401() throws Exception {
        mockMvc.perform(get("/api/blogs"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void clienteLeeLosArticulos() throws Exception {
        when(blogService.listar()).thenReturn(List.of(new BlogResponse("a", "Título", "Resumen", "Intro",
                "/img/a.jpg", "Imagen", List.of(new SeccionDto("S", "T")))));

        mockMvc.perform(get("/api/blogs")
                        .with(jwt().authorities(new SimpleGrantedAuthority("ROLE_cliente"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].secciones[0].titulo").value("S"));
    }

    @Test
    void clienteNoPuedeCrearArticulos() throws Exception {
        mockMvc.perform(post("/api/blogs")
                        .with(jwt().authorities(new SimpleGrantedAuthority("ROLE_cliente")))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isForbidden());
    }

    @Test
    void articuloSinSeccionesResponde400() throws Exception {
        mockMvc.perform(post("/api/blogs")
                        .with(jwt().authorities(new SimpleGrantedAuthority("ROLE_administrador")))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"titulo":"Nuevo","resumen":"R","introduccion":"I","imagen":"/img/a.jpg",
                                 "descripcionImagen":"D","secciones":[]}
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.mensaje").value("El artículo necesita al menos una sección."));
    }

    @Test
    void administradorEliminaYRecibe204() throws Exception {
        mockMvc.perform(delete("/api/blogs/a")
                        .with(jwt().authorities(new SimpleGrantedAuthority("ROLE_administrador"))))
                .andExpect(status().isNoContent());
    }
}
