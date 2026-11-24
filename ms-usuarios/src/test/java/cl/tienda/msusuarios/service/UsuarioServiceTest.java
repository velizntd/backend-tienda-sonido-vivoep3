package cl.tienda.msusuarios.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

import cl.tienda.msusuarios.dto.UsuarioRequest;
import cl.tienda.msusuarios.dto.UsuarioResponse;
import cl.tienda.msusuarios.exception.ConflictoException;
import cl.tienda.msusuarios.exception.DatosInvalidosException;
import cl.tienda.msusuarios.exception.RecursoNoEncontradoException;
import cl.tienda.msusuarios.model.Usuario;
import cl.tienda.msusuarios.repository.UsuarioRepository;

@ExtendWith(MockitoExtension.class)
class UsuarioServiceTest {

    @Mock
    private UsuarioRepository usuarioRepository;

    private final PasswordEncoder passwordEncoder = new BCryptPasswordEncoder(4);

    private UsuarioService usuarioService;

    @BeforeEach
    void preparar() {
        usuarioService = new UsuarioService(usuarioRepository, passwordEncoder);
    }

    private UsuarioRequest datos(String rut, String correo, String rol, String contrasena) {
        return new UsuarioRequest(rut, "Camila", "Rojas Soto", correo, "2000-05-20",
                "Región de Valparaíso", "Viña del Mar", "Avenida Libertad 123", rol, contrasena);
    }

    private Usuario existente(String id, String rol) {
        Usuario usuario = new Usuario();
        usuario.setId(id);
        usuario.setRut("123456785");
        usuario.setNombre("Camila");
        usuario.setApellidos("Rojas Soto");
        usuario.setCorreo("camila@correo.cl");
        usuario.setRegion("Región de Valparaíso");
        usuario.setComuna("Viña del Mar");
        usuario.setDireccion("Avenida Libertad 123");
        usuario.setRol(rol);
        usuario.setActivo(true);
        usuario.setFechaRegistro(LocalDateTime.of(2026, 11, 24, 3, 0));
        usuario.setPasswordHash(passwordEncoder.encode("anterior"));
        return usuario;
    }

    @Test
    void crearGuardaHashBcryptYNoDevuelveLaContrasena() {
        when(usuarioRepository.existsByRut("123456785")).thenReturn(false);
        when(usuarioRepository.existsByCorreo("camila@correo.cl")).thenReturn(false);
        when(usuarioRepository.save(any(Usuario.class))).thenAnswer(invocacion -> invocacion.getArgument(0));

        UsuarioResponse respuesta = usuarioService.crear(datos("12.345.678-5", " Camila@Correo.CL ", "vendedor", "clave123"));

        ArgumentCaptor<Usuario> guardado = ArgumentCaptor.forClass(Usuario.class);
        verify(usuarioRepository).save(guardado.capture());
        assertThat(guardado.getValue().getPasswordHash()).isNotEqualTo("clave123");
        assertThat(passwordEncoder.matches("clave123", guardado.getValue().getPasswordHash())).isTrue();
        assertThat(respuesta.rut()).isEqualTo("123456785");
        assertThat(respuesta.correo()).isEqualTo("camila@correo.cl");
        assertThat(respuesta.rol()).isEqualTo("vendedor");
        assertThat(respuesta.activo()).isTrue();
        assertThat(respuesta.fechaRegistro()).endsWith("Z");
    }

    @Test
    void crearConCorreoDuplicadoLanzaConflicto() {
        when(usuarioRepository.existsByRut("123456785")).thenReturn(false);
        when(usuarioRepository.existsByCorreo("camila@correo.cl")).thenReturn(true);

        assertThatThrownBy(() -> usuarioService.crear(datos("12345678-5", "camila@correo.cl", "cliente", "clave123")))
                .isInstanceOf(ConflictoException.class)
                .hasMessageContaining("correo");
        verify(usuarioRepository, never()).save(any());
    }

    @Test
    void crearConRutInvalidoLanzaDatosInvalidos() {
        assertThatThrownBy(() -> usuarioService.crear(datos("12345678-9", "camila@correo.cl", "cliente", "clave123")))
                .isInstanceOf(DatosInvalidosException.class)
                .hasMessageContaining("dígito verificador");
    }

    @Test
    void crearConContrasenaCortaLanzaDatosInvalidos() {
        when(usuarioRepository.existsByRut("123456785")).thenReturn(false);
        when(usuarioRepository.existsByCorreo("camila@correo.cl")).thenReturn(false);

        assertThatThrownBy(() -> usuarioService.crear(datos("12345678-5", "camila@correo.cl", "cliente", "123")))
                .isInstanceOf(DatosInvalidosException.class);
        verify(usuarioRepository, never()).save(any());
    }

    @Test
    void crearConFechaDeNacimientoFuturaLanzaDatosInvalidos() {
        when(usuarioRepository.existsByRut("123456785")).thenReturn(false);
        when(usuarioRepository.existsByCorreo("camila@correo.cl")).thenReturn(false);
        UsuarioRequest futura = new UsuarioRequest("12345678-5", "Camila", "Rojas Soto", "camila@correo.cl",
                LocalDate.now().plusDays(1).toString(), "Región de Valparaíso", "Viña del Mar",
                "Avenida Libertad 123", "cliente", "clave123");

        assertThatThrownBy(() -> usuarioService.crear(futura))
                .isInstanceOf(DatosInvalidosException.class)
                .hasMessageContaining("futura");
        verify(usuarioRepository, never()).save(any());
    }

    @Test
    void crearConFechaAnteriorA1900LanzaDatosInvalidos() {
        when(usuarioRepository.existsByRut("123456785")).thenReturn(false);
        when(usuarioRepository.existsByCorreo("camila@correo.cl")).thenReturn(false);
        UsuarioRequest antigua = new UsuarioRequest("12345678-5", "Camila", "Rojas Soto", "camila@correo.cl",
                "1313-03-31", "Región de Valparaíso", "Viña del Mar", "Avenida Libertad 123", "cliente", "clave123");

        assertThatThrownBy(() -> usuarioService.crear(antigua))
                .isInstanceOf(DatosInvalidosException.class)
                .hasMessageContaining("1900");
    }

    @Test
    void buscarUsuarioInexistenteLanzaNoEncontrado() {
        when(usuarioRepository.findById("no-existe")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> usuarioService.buscarPorId("no-existe"))
                .isInstanceOf(RecursoNoEncontradoException.class);
    }

    @Test
    void actualizarSinContrasenaMantieneElHashAnterior() {
        Usuario usuario = existente("u-1", "cliente");
        String hashAnterior = usuario.getPasswordHash();
        when(usuarioRepository.findById("u-1")).thenReturn(Optional.of(usuario));
        when(usuarioRepository.existsByCorreoAndIdNot("camila@correo.cl", "u-1")).thenReturn(false);
        when(usuarioRepository.save(any(Usuario.class))).thenAnswer(invocacion -> invocacion.getArgument(0));

        UsuarioResponse respuesta = usuarioService.actualizar("u-1",
                datos("12345678-5", "camila@correo.cl", "vendedor", ""), "admin-1");

        assertThat(respuesta.rol()).isEqualTo("vendedor");
        assertThat(usuario.getPasswordHash()).isEqualTo(hashAnterior);
    }

    @Test
    void actualizarNoPermiteCambiarElRut() {
        when(usuarioRepository.findById("u-1")).thenReturn(Optional.of(existente("u-1", "cliente")));

        assertThatThrownBy(() -> usuarioService.actualizar("u-1",
                datos("11111111-1", "camila@correo.cl", "cliente", ""), "admin-1"))
                .isInstanceOf(DatosInvalidosException.class)
                .hasMessageContaining("RUT");
    }

    @Test
    void administradorNoPuedeDesactivarSuPropiaCuenta() {
        when(usuarioRepository.findById("admin-1")).thenReturn(Optional.of(existente("admin-1", "administrador")));

        assertThatThrownBy(() -> usuarioService.cambiarEstado("admin-1", false, "admin-1"))
                .isInstanceOf(ConflictoException.class);
        verify(usuarioRepository, never()).save(any());
    }

    @Test
    void administradorNoPuedeQuitarseSuPropioRol() {
        when(usuarioRepository.findById("admin-1")).thenReturn(Optional.of(existente("admin-1", "administrador")));

        assertThatThrownBy(() -> usuarioService.cambiarRol("admin-1", "cliente", "admin-1"))
                .isInstanceOf(ConflictoException.class);
    }

    @Test
    void desactivarOtroUsuarioLoDejaInactivo() {
        Usuario usuario = existente("u-1", "cliente");
        when(usuarioRepository.findById("u-1")).thenReturn(Optional.of(usuario));
        when(usuarioRepository.save(any(Usuario.class))).thenAnswer(invocacion -> invocacion.getArgument(0));

        UsuarioResponse respuesta = usuarioService.cambiarEstado("u-1", false, "admin-1");

        assertThat(respuesta.activo()).isFalse();
    }
}
