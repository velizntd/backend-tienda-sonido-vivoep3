package cl.tienda.msregistro.controller;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import cl.tienda.msregistro.dto.LoginRequest;
import cl.tienda.msregistro.dto.LoginResponse;
import cl.tienda.msregistro.dto.RegistroRequest;
import cl.tienda.msregistro.dto.RegistroResponse;

import cl.tienda.msregistro.exception.CredencialesInvalidasException;
import cl.tienda.msregistro.exception.DuplicadoException;
import cl.tienda.msregistro.exception.GlobalExceptionHandler;

import cl.tienda.msregistro.service.AuthService;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

class AuthControllerTest {

    private MockMvc mockMvc;
    private AuthService authService;

    @BeforeEach
    void configurar() {

        authService = mock(AuthService.class);

        mockMvc = MockMvcBuilders
                .standaloneSetup(new AuthController(authService))
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
    }

    private String jsonRegistro() {

        return """
                {
                    "rut": "12.345.678-5",
                    "nombre": "Juan",
                    "apellidos": "Perez",
                    "correo": "cliente@ejemplo.cl",
                    "fechaNacimiento": "2000-05-10",
                    "region": "Metropolitana",
                    "comuna": "Santiago",
                    "direccion": "Calle Ejemplo 123",
                    "contrasena": "Clave123"
                }
                """;
    }

    private String jsonLogin() {

        return """
                {
                    "correo": "cliente@ejemplo.cl",
                    "contrasena": "Clave123"
                }
                """;
    }

    @Test
    void debeRegistrarUsuarioYRetornar201() throws Exception {

        when(authService.registrar(any(RegistroRequest.class)))
                .thenReturn(new RegistroResponse(
                        "USR-001",
                        "Juan",
                        "cliente@ejemplo.cl",
                        "cliente",
                        "Usuario registrado correctamente"));

        mockMvc.perform(post("/api/auth/registro")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonRegistro()))

                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value("USR-001"))
                .andExpect(jsonPath("$.rol").value("cliente"))
                .andExpect(jsonPath("$.passwordHash").doesNotExist());
    }

    @Test
    void debeIniciarSesionYRetornar200() throws Exception {

        when(authService.iniciarSesion(any(LoginRequest.class)))
                .thenReturn(new LoginResponse(
                        "jwt-prueba",
                        "Bearer",
                        900,
                        "USR-001",
                        "cliente"));

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonLogin()))

                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accessToken").value("jwt-prueba"))
                .andExpect(jsonPath("$.tokenType").value("Bearer"))
                .andExpect(jsonPath("$.rol").value("cliente"));
    }

    @Test
    void debeRechazarCredencialesInvalidasCon401() throws Exception {

        when(authService.iniciarSesion(any(LoginRequest.class)))
                .thenThrow(new CredencialesInvalidasException());

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonLogin()))

                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.mensaje")
                        .value("Correo o contrasena incorrectos"));
    }

    @Test
    void debeRechazarUsuarioDuplicadoCon409() throws Exception {

        when(authService.registrar(any(RegistroRequest.class)))
                .thenThrow(new DuplicadoException(
                        "El correo ya se encuentra registrado"));

        mockMvc.perform(post("/api/auth/registro")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonRegistro()))

                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.mensaje")
                        .value("El correo ya se encuentra registrado"));
    }

    @Test
    void debeRechazarCamposInvalidosCon400() throws Exception {

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                    "correo": "correo-invalido",
                                    "contrasena": ""
                                }
                                """))

                .andExpect(status().isBadRequest());

        verifyNoInteractions(authService);
    }

    @Test
    void debeRechazarJsonIncorrectoCon400() throws Exception {

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("json-invalido"))

                .andExpect(status().isBadRequest());

        verifyNoInteractions(authService);
    }
}