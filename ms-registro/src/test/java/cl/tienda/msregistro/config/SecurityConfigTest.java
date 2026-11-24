package cl.tienda.msregistro.config;

import cl.tienda.msregistro.controller.AuthController;
import cl.tienda.msregistro.dto.LoginResponse;
import cl.tienda.msregistro.service.*;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.hamcrest.Matchers.*;

@WebMvcTest(AuthController.class)
@Import({SecurityConfig.class, RefreshCookieService.class})
class SecurityConfigTest {
    @Autowired MockMvc mvc;
    @MockitoBean AuthService auth;
    @MockitoBean RefreshTokenService refresh;
    @MockitoBean JwtDecoder decoder;
    @ParameterizedTest @ValueSource(strings = {"login", "refresh", "logout"})
    void rechazaPostSinCabeceraCsrf(String endpoint) throws Exception {
        String ruta = "/api/auth/" + endpoint;
        mvc.perform(post(ruta).servletPath(ruta).contentType(MediaType.APPLICATION_JSON).content("{}"))
            .andExpect(status().isForbidden()).andExpect(jsonPath("$.mensaje").value("Solicitud CSRF no permitida"));
        verifyNoInteractions(auth, refresh);
    }
    @Test void meSinJwtRetorna401() throws Exception {
        mvc.perform(get("/api/auth/me")).andExpect(status().isUnauthorized()); verifyNoInteractions(auth);
    }
    @Test void loginConCsrfEntregaCookieHttpOnlyStrictYDuracionCorrecta() throws Exception {
        when(auth.iniciarSesion(any())).thenReturn(new LoginResponse("jwt-unitario", "Bearer", 900, "u1", "cliente"));
        when(refresh.generarToken("u1")).thenReturn("refresh-unitario");
        mvc.perform(post("/api/auth/login").servletPath("/api/auth/login").header("X-CSRF-Protection", "1")
                .contentType(MediaType.APPLICATION_JSON).content("{\"correo\":\"qa@example.test\",\"contrasena\":\"Prueba1\"}"))
            .andExpect(status().isOk()).andExpect(jsonPath("$.expiresIn").value(900))
            .andExpect(header().string("Set-Cookie", allOf(containsString("HttpOnly"), containsString("SameSite=Strict"), containsString("Max-Age=604800"))))
            .andExpect(jsonPath("$.refreshToken").doesNotExist());
    }
    @Test void logoutConCsrfRevocaYExpiraCookie() throws Exception {
        mvc.perform(post("/api/auth/logout").servletPath("/api/auth/logout").header("X-CSRF-Protection", "1")
            .cookie(new jakarta.servlet.http.Cookie("refresh_token", "refresh-unitario")))
            .andExpect(status().isNoContent()).andExpect(header().string("Set-Cookie", containsString("Max-Age=0")));
        verify(refresh).revocarToken("refresh-unitario");
    }
}
