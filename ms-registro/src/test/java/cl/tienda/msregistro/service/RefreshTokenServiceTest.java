package cl.tienda.msregistro.service;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.Optional;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.HexFormat;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import cl.tienda.msregistro.exception.CredencialesInvalidasException;
import cl.tienda.msregistro.model.RefreshToken;
import cl.tienda.msregistro.model.Usuario;
import cl.tienda.msregistro.repository.RefreshTokenRepository;
import cl.tienda.msregistro.repository.UsuarioRepository;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class RefreshTokenServiceTest {

    private RefreshTokenRepository refreshTokenRepository;
    private UsuarioRepository usuarioRepository;
    private JwtService jwtService;
    private RefreshTokenService service;

    @BeforeEach
    void configurar() {

        refreshTokenRepository =
                mock(RefreshTokenRepository.class);

        usuarioRepository =
                mock(UsuarioRepository.class);

        jwtService = mock(JwtService.class);

        service = new RefreshTokenService(
                refreshTokenRepository,
                usuarioRepository,
                jwtService);
    }

    private Usuario crearUsuario(boolean activo) {

        Usuario usuario = new Usuario();

        usuario.setId("USR-001");
        usuario.setRol("cliente");
        usuario.setActivo(activo);

        return usuario;
    }

    private RefreshToken crearRefreshToken(
            boolean revocado,
            boolean expirado,
            boolean usuarioActivo) {

        RefreshToken token = new RefreshToken();

        token.setId("TOKEN-001");
        token.setUsuario(crearUsuario(usuarioActivo));
        token.setTokenHash("hash-prueba");
        token.setRevocado(revocado);

        LocalDateTime ahora =
                LocalDateTime.now(ZoneOffset.UTC);

        token.setFechaCreacion(ahora.minusDays(1));

        token.setFechaExpiracion(
                expirado
                        ? ahora.minusMinutes(1)
                        : ahora.plusDays(6));

        return token;
    }

    @Test
    void debeGenerarRefreshTokenSeguro() throws Exception {

        when(usuarioRepository.findById("USR-001"))
                .thenReturn(Optional.of(crearUsuario(true)));

        String tokenOriginal =
                service.generarToken("USR-001");

        ArgumentCaptor<RefreshToken> captor =
                ArgumentCaptor.forClass(RefreshToken.class);

        verify(refreshTokenRepository)
                .save(captor.capture());

        RefreshToken guardado = captor.getValue();

        String hashEsperado = HexFormat.of().formatHex(
                MessageDigest.getInstance("SHA-256")
                        .digest(tokenOriginal.getBytes(
                                StandardCharsets.UTF_8)));

        assertNotNull(tokenOriginal);
        assertEquals(43, tokenOriginal.length());
        assertEquals(64, guardado.getTokenHash().length());
        assertEquals(hashEsperado, guardado.getTokenHash());
        assertNotEquals(tokenOriginal, guardado.getTokenHash());

        assertFalse(guardado.isRevocado());

        assertEquals("USR-001",
                guardado.getUsuario().getId());

        assertEquals(
                guardado.getFechaCreacion().plusDays(7),
                guardado.getFechaExpiracion());
    }

    @Test
    void debeGenerarTokensDiferentes() {

        when(usuarioRepository.findById("USR-001"))
                .thenReturn(Optional.of(crearUsuario(true)));

        String token1 = service.generarToken("USR-001");
        String token2 = service.generarToken("USR-001");

        assertNotEquals(token1, token2);
    }

    @Test
    void debeRechazarUsuarioInexistente() {

        when(usuarioRepository.findById("USR-999"))
                .thenReturn(Optional.empty());

        assertThrows(CredencialesInvalidasException.class,
                () -> service.generarToken("USR-999"));

        verifyNoInteractions(refreshTokenRepository);
    }

    @Test
    void debeRechazarUsuarioDesactivado() {

        when(usuarioRepository.findById("USR-001"))
                .thenReturn(Optional.of(crearUsuario(false)));

        assertThrows(CredencialesInvalidasException.class,
                () -> service.generarToken("USR-001"));

        verifyNoInteractions(refreshTokenRepository);
    }

    @Test
    void debeRenovarTokenCorrectamente() {

        RefreshToken anterior =
                crearRefreshToken(false, false, true);

        when(refreshTokenRepository.findByTokenHash(anyString()))
                .thenReturn(Optional.of(anterior));

        when(jwtService.generarToken("USR-001", "cliente"))
                .thenReturn("nuevo-jwt");

        RefreshTokenService.SesionRenovada resultado =
                service.renovarToken("refresh-original");

        assertTrue(anterior.isRevocado());

        assertEquals("nuevo-jwt",
                resultado.respuesta().accessToken());

        assertEquals("cliente",
                resultado.respuesta().rol());

        assertNotNull(resultado.refreshToken());

        assertNotEquals("refresh-original",
                resultado.refreshToken());

        verify(refreshTokenRepository, times(2))
                .save(any(RefreshToken.class));
    }

    @Test
    void debeRechazarTokenRevocado() {

        RefreshToken token =
                crearRefreshToken(true, false, true);

        when(refreshTokenRepository.findByTokenHash(anyString()))
                .thenReturn(Optional.of(token));

        assertThrows(CredencialesInvalidasException.class,
                () -> service.renovarToken("token-revocado"));

        verifyNoInteractions(jwtService);
    }

    @Test
    void debeRechazarTokenExpirado() {

        RefreshToken token =
                crearRefreshToken(false, true, true);

        when(refreshTokenRepository.findByTokenHash(anyString()))
                .thenReturn(Optional.of(token));

        assertThrows(CredencialesInvalidasException.class,
                () -> service.renovarToken("token-expirado"));

        verifyNoInteractions(jwtService);
    }

    @Test
    void debeRechazarTokenInexistente() {

        when(refreshTokenRepository.findByTokenHash(anyString()))
                .thenReturn(Optional.empty());

        assertThrows(CredencialesInvalidasException.class,
                () -> service.renovarToken("token-inexistente"));

        verifyNoInteractions(jwtService);
    }

    @Test
    void debeRechazarRenovacionDeUsuarioDesactivado() {

        RefreshToken token =
                crearRefreshToken(false, false, false);

        when(refreshTokenRepository.findByTokenHash(anyString()))
                .thenReturn(Optional.of(token));

        assertThrows(CredencialesInvalidasException.class,
                () -> service.renovarToken("refresh-original"));

        verifyNoInteractions(jwtService);
    }

    @Test
    void debeRechazarTokenVacio() {

        assertThrows(CredencialesInvalidasException.class,
                () -> service.renovarToken(""));

        verifyNoInteractions(refreshTokenRepository);
    }

    @Test
    void debeRevocarTokenCorrectamente() {

        RefreshToken token =
                crearRefreshToken(false, false, true);

        when(refreshTokenRepository.findByTokenHash(anyString()))
                .thenReturn(Optional.of(token));

        service.revocarToken("refresh-original");

        assertTrue(token.isRevocado());

        verify(refreshTokenRepository).save(token);
    }

    @Test
    void debePermitirRevocarTokenInexistente() {

        when(refreshTokenRepository.findByTokenHash(anyString()))
                .thenReturn(Optional.empty());

        assertDoesNotThrow(
                () -> service.revocarToken("token-inexistente"));

        verify(refreshTokenRepository, never())
                .save(any(RefreshToken.class));
    }

    @Test
    void debePermitirLogoutSinToken() {

        assertDoesNotThrow(
                () -> service.revocarToken(null));

        verifyNoInteractions(refreshTokenRepository);
    }
}