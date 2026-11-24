package cl.tienda.mscontacto.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import cl.tienda.mscontacto.config.SecurityConfig;
import cl.tienda.mscontacto.dto.ContactoResponse;
import cl.tienda.mscontacto.exception.RecursoNoEncontradoException;
import cl.tienda.mscontacto.service.ContactoService;

@WebMvcTest(ContactoController.class)
@Import(SecurityConfig.class)
@TestPropertySource(properties = "app.jwt.secret=MDEyMzQ1Njc4OWFiY2RlZjAxMjM0NTY3ODlhYmNkZWY=")
class ContactoControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private ContactoService contactoService;

    private static final String JSON_VALIDO = """
            {"nombre":"Camila Rojas","correo":"camila@correo.cl","comentario":"¿Tienen stock?"}
            """;

    @Test
    void enviarSinSesionResponde401() throws Exception {
        mockMvc.perform(post("/api/contactos").contentType(MediaType.APPLICATION_JSON).content(JSON_VALIDO))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void clienteEnviaMensajeYSeGuardaConSuIdDelToken() throws Exception {
        when(contactoService.registrar(any(), eq("cliente-1"))).thenReturn(
                new ContactoResponse(1L, "cliente-1", "Camila Rojas", "camila@correo.cl", "¿Tienen stock?", "2026-11-24T03:00:00Z"));

        mockMvc.perform(post("/api/contactos")
                        .with(jwt().jwt(token -> token.subject("cliente-1"))
                                .authorities(new SimpleGrantedAuthority("ROLE_cliente")))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(JSON_VALIDO))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1));
        verify(contactoService).registrar(any(), eq("cliente-1"));
    }

    @Test
    void comentarioVacioResponde400() throws Exception {
        mockMvc.perform(post("/api/contactos")
                        .with(jwt().authorities(new SimpleGrantedAuthority("ROLE_cliente")))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"nombre\":\"Camila\",\"correo\":\"\",\"comentario\":\"  \"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.mensaje").value("El comentario es obligatorio."));
    }

    @Test
    void clienteNoPuedeListarMensajes() throws Exception {
        mockMvc.perform(get("/api/contactos")
                        .with(jwt().authorities(new SimpleGrantedAuthority("ROLE_cliente"))))
                .andExpect(status().isForbidden());
    }

    @Test
    void mensajeInexistenteResponde404() throws Exception {
        when(contactoService.buscarPorId(99L)).thenThrow(new RecursoNoEncontradoException("No se encontró el mensaje solicitado."));

        mockMvc.perform(get("/api/contactos/99")
                        .with(jwt().authorities(new SimpleGrantedAuthority("ROLE_administrador"))))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.mensaje").value("No se encontró el mensaje solicitado."));
    }
}
