package cl.tienda.msusuarios.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
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

import cl.tienda.msusuarios.config.SecurityConfig;
import cl.tienda.msusuarios.dto.UsuarioResponse;
import cl.tienda.msusuarios.exception.ConflictoException;
import cl.tienda.msusuarios.service.UsuarioService;

@WebMvcTest(UsuarioController.class)
@Import(SecurityConfig.class)
@TestPropertySource(properties = "app.jwt.secret=MDEyMzQ1Njc4OWFiY2RlZjAxMjM0NTY3ODlhYmNkZWY=")
class UsuarioControllerSecurityTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private UsuarioService usuarioService;

    private static final String JSON_VALIDO = """
            {"rut":"12345678-5","nombre":"Camila","apellidos":"Rojas Soto","correo":"camila@correo.cl",
             "fechaNacimiento":"","region":"Región de Valparaíso","comuna":"Viña del Mar",
             "direccion":"Avenida Libertad 123","rol":"vendedor","contrasena":"clave123"}
            """;

    private UsuarioResponse respuesta() {
        return new UsuarioResponse("u-1", "123456785", "Camila", "Rojas Soto", "camila@correo.cl", "",
                "Región de Valparaíso", "Viña del Mar", "Avenida Libertad 123", "vendedor", true,
                "2026-11-24T03:00:00Z");
    }

    @Test
    void sinTokenResponde401() throws Exception {
        mockMvc.perform(get("/api/usuarios"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void clienteRecibe403() throws Exception {
        mockMvc.perform(get("/api/usuarios")
                        .with(jwt().authorities(new SimpleGrantedAuthority("ROLE_cliente"))))
                .andExpect(status().isForbidden());
    }

    @Test
    void vendedorRecibe403() throws Exception {
        mockMvc.perform(get("/api/usuarios")
                        .with(jwt().authorities(new SimpleGrantedAuthority("ROLE_vendedor"))))
                .andExpect(status().isForbidden());
    }

    @Test
    void administradorListaUsuariosSinContrasena() throws Exception {
        when(usuarioService.listar()).thenReturn(List.of(respuesta()));

        mockMvc.perform(get("/api/usuarios")
                        .with(jwt().authorities(new SimpleGrantedAuthority("ROLE_administrador"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].correo").value("camila@correo.cl"))
                .andExpect(jsonPath("$[0].contrasena").doesNotExist())
                .andExpect(jsonPath("$[0].passwordHash").doesNotExist());
    }

    @Test
    void administradorCreaUsuarioYRecibe201() throws Exception {
        when(usuarioService.crear(any())).thenReturn(respuesta());

        mockMvc.perform(post("/api/usuarios")
                        .with(jwt().authorities(new SimpleGrantedAuthority("ROLE_administrador")))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(JSON_VALIDO))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value("u-1"));
    }

    @Test
    void datosIncompletosResponden400ConMensaje() throws Exception {
        mockMvc.perform(post("/api/usuarios")
                        .with(jwt().authorities(new SimpleGrantedAuthority("ROLE_administrador")))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"rut\":\"12345678-5\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.mensaje").exists());
    }

    @Test
    void desactivarseASiMismoResponde409() throws Exception {
        when(usuarioService.cambiarEstado("admin-1", false, "admin-1"))
                .thenThrow(new ConflictoException("No puedes desactivar tu propia cuenta mientras tienes la sesión iniciada."));

        mockMvc.perform(patch("/api/usuarios/admin-1/estado")
                        .with(jwt().jwt(token -> token.subject("admin-1"))
                                .authorities(new SimpleGrantedAuthority("ROLE_administrador")))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"activo\":false}"))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.mensaje").value("No puedes desactivar tu propia cuenta mientras tienes la sesión iniciada."));
    }
}
