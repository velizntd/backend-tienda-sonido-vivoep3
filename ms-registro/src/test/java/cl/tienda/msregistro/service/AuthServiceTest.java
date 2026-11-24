package cl.tienda.msregistro.service;

import java.time.LocalDate;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.security.crypto.password.PasswordEncoder;

import cl.tienda.msregistro.dto.LoginRequest;
import cl.tienda.msregistro.dto.LoginResponse;
import cl.tienda.msregistro.dto.RegistroRequest;
import cl.tienda.msregistro.dto.RegistroResponse;
import cl.tienda.msregistro.exception.CredencialesInvalidasException;
import cl.tienda.msregistro.exception.DuplicadoException;
import cl.tienda.msregistro.exception.DatosInvalidosException;
import cl.tienda.msregistro.model.Usuario;
import cl.tienda.msregistro.repository.UsuarioRepository;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class AuthServiceTest {

    private UsuarioRepository repository;
    private PasswordEncoder passwordEncoder;
    private JwtService jwtService;
    private AuthService authService;

    @BeforeEach
    void configurar() {

        repository = mock(UsuarioRepository.class);
        passwordEncoder = mock(PasswordEncoder.class);
        jwtService = mock(JwtService.class);

        authService = new AuthService(
                repository,
                passwordEncoder,
                jwtService);
    }

    private RegistroRequest datosRegistro() {

        return new RegistroRequest(
                "12.345.678-5",
                "Juan",
                "Perez",
                "cliente@ejemplo.cl",
                LocalDate.of(2000, 5, 10),
                "Metropolitana",
                "Santiago",
                "Calle Ejemplo 123",
                "Clave123");
    }

    private Usuario crearUsuario(boolean activo) {

        Usuario usuario = new Usuario();

        usuario.setId("USR-001");
        usuario.setCorreo("cliente@ejemplo.cl");
        usuario.setRol("cliente");
        usuario.setActivo(activo);
        usuario.setPasswordHash("hash-prueba");

        return usuario;
    }

    @Test
    void debeRegistrarClienteCorrectamente() {

        when(passwordEncoder.encode("Clave123"))
                .thenReturn("hash-prueba");

        RegistroResponse respuesta =
                authService.registrar(datosRegistro());

        ArgumentCaptor<Usuario> captor =
                ArgumentCaptor.forClass(Usuario.class);

        verify(repository).save(captor.capture());

        Usuario guardado = captor.getValue();

        assertNotNull(guardado.getId());
        assertEquals("123456785", guardado.getRut());
        assertEquals("cliente", guardado.getRol());
        assertTrue(guardado.isActivo());
        assertEquals("hash-prueba", guardado.getPasswordHash());
        assertNotNull(guardado.getFechaRegistro());

        assertEquals("cliente", respuesta.rol());
        assertEquals("cliente@ejemplo.cl", respuesta.correo());
    }

    @Test
    void debeRechazarRutDuplicado() {

        when(repository.existsByRut("123456785"))
                .thenReturn(true);

        assertThrows(DuplicadoException.class,
                () -> authService.registrar(datosRegistro()));

        verify(repository, never()).save(any(Usuario.class));
    }

    @Test
    void debeRechazarCorreoDuplicado() {

        when(repository.existsByCorreo("cliente@ejemplo.cl"))
                .thenReturn(true);

        assertThrows(DuplicadoException.class,
                () -> authService.registrar(datosRegistro()));

        verify(repository, never()).save(any(Usuario.class));
    }

    @Test
    void debeRechazarRutInvalido() {

        RegistroRequest request = new RegistroRequest(
                "12.345.678-9",
                "Juan",
                "Perez",
                "cliente@ejemplo.cl",
                LocalDate.of(2000, 5, 10),
                "Metropolitana",
                "Santiago",
                "Calle Ejemplo 123",
                "Clave123");

        assertThrows(DatosInvalidosException.class,
                () -> authService.registrar(request));

        verify(repository, never()).save(any(Usuario.class));
    }

    @Test
    void debeIniciarSesionCorrectamente() {

        Usuario usuario = crearUsuario(true);

        when(repository.findByCorreo("cliente@ejemplo.cl"))
                .thenReturn(Optional.of(usuario));

        when(passwordEncoder.matches("Clave123", "hash-prueba"))
                .thenReturn(true);

        when(jwtService.generarToken("USR-001", "cliente"))
                .thenReturn("jwt-prueba");

        LoginResponse respuesta = authService.iniciarSesion(
                new LoginRequest("cliente@ejemplo.cl", "Clave123"));

        assertEquals("jwt-prueba", respuesta.accessToken());
        assertEquals("Bearer", respuesta.tokenType());
        assertEquals(900, respuesta.expiresIn());
        assertEquals("USR-001", respuesta.usuarioId());
        assertEquals("cliente", respuesta.rol());
    }

    @Test
    void debeRechazarContrasenaIncorrecta() {

        when(repository.findByCorreo("cliente@ejemplo.cl"))
                .thenReturn(Optional.of(crearUsuario(true)));

        when(passwordEncoder.matches("Incorrecta", "hash-prueba"))
                .thenReturn(false);

        assertThrows(CredencialesInvalidasException.class,
                () -> authService.iniciarSesion(
                        new LoginRequest(
                                "cliente@ejemplo.cl",
                                "Incorrecta")));

        verifyNoInteractions(jwtService);
    }

    @Test
    void debeRechazarUsuarioDesactivado() {

        when(repository.findByCorreo("cliente@ejemplo.cl"))
                .thenReturn(Optional.of(crearUsuario(false)));

        assertThrows(CredencialesInvalidasException.class,
                () -> authService.iniciarSesion(
                        new LoginRequest(
                                "cliente@ejemplo.cl",
                                "Clave123")));

        verifyNoInteractions(jwtService);
    }

    @Test
    void debeRechazarUsuarioInexistente() {

        when(repository.findByCorreo("cliente@ejemplo.cl"))
                .thenReturn(Optional.empty());

        assertThrows(CredencialesInvalidasException.class,
                () -> authService.iniciarSesion(
                        new LoginRequest(
                                "cliente@ejemplo.cl",
                                "Clave123")));

        verifyNoInteractions(jwtService);
    }
}