package cl.tienda.msregistro.service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.crypto.password.PasswordEncoder;

import cl.tienda.msregistro.dto.UsuarioResponse;
import cl.tienda.msregistro.exception.CredencialesInvalidasException;
import cl.tienda.msregistro.model.Usuario;
import cl.tienda.msregistro.repository.UsuarioRepository;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class UsuarioActualTest {

    private UsuarioRepository repository;
    private AuthService authService;

    @BeforeEach
    void configurar() {

        repository = mock(UsuarioRepository.class);

        PasswordEncoder passwordEncoder =
                mock(PasswordEncoder.class);

        JwtService jwtService = mock(JwtService.class);

        authService = new AuthService(
                repository,
                passwordEncoder,
                jwtService);
    }

    private Usuario crearUsuario(boolean activo) {

        Usuario usuario = new Usuario();

        usuario.setId("USR-001");
        usuario.setRut("123456785");
        usuario.setNombre("Juan");
        usuario.setApellidos("Perez");
        usuario.setCorreo("cliente@ejemplo.cl");
        usuario.setFechaNacimiento(LocalDate.of(2000, 5, 10));
        usuario.setRegion("Metropolitana");
        usuario.setComuna("Santiago");
        usuario.setDireccion("Calle Ejemplo 123");
        usuario.setRol("cliente");
        usuario.setActivo(activo);
        usuario.setFechaRegistro(LocalDateTime.now());

        return usuario;
    }

    @Test
    void debeObtenerUsuarioActual() {

        when(repository.findById("USR-001"))
                .thenReturn(Optional.of(crearUsuario(true)));

        UsuarioResponse respuesta =
                authService.obtenerUsuarioActual("USR-001");

        assertEquals("USR-001", respuesta.id());
        assertEquals("Juan", respuesta.nombre());
        assertEquals("cliente@ejemplo.cl", respuesta.correo());
        assertEquals("cliente", respuesta.rol());
        assertTrue(respuesta.activo());

        verify(repository).findById("USR-001");
    }

    @Test
    void debeRechazarUsuarioInexistente() {

        when(repository.findById("USR-999"))
                .thenReturn(Optional.empty());

        assertThrows(CredencialesInvalidasException.class,
                () -> authService.obtenerUsuarioActual("USR-999"));
    }

    @Test
    void debeRechazarUsuarioDesactivado() {

        when(repository.findById("USR-001"))
                .thenReturn(Optional.of(crearUsuario(false)));

        assertThrows(CredencialesInvalidasException.class,
                () -> authService.obtenerUsuarioActual("USR-001"));
    }
}