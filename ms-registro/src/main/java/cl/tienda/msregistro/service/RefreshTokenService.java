package cl.tienda.msregistro.service;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.Base64;
import java.util.HexFormat;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import cl.tienda.msregistro.dto.LoginResponse;
import cl.tienda.msregistro.exception.CredencialesInvalidasException;
import cl.tienda.msregistro.model.RefreshToken;
import cl.tienda.msregistro.model.Usuario;
import cl.tienda.msregistro.repository.RefreshTokenRepository;
import cl.tienda.msregistro.repository.UsuarioRepository;

@Service
public class RefreshTokenService {

    private static final int DURACION_DIAS = 7;

    private final SecureRandom secureRandom = new SecureRandom();

    private final RefreshTokenRepository refreshTokenRepository;
    private final UsuarioRepository usuarioRepository;
    private final JwtService jwtService;

    public RefreshTokenService(
            RefreshTokenRepository refreshTokenRepository,
            UsuarioRepository usuarioRepository,
            JwtService jwtService) {

        this.refreshTokenRepository = refreshTokenRepository;
        this.usuarioRepository = usuarioRepository;
        this.jwtService = jwtService;
    }

    @Transactional
    public String generarToken(String usuarioId) {

        Usuario usuario = usuarioRepository
                .findById(usuarioId)
                .orElseThrow(CredencialesInvalidasException::new);

        if (!usuario.isActivo()) {
            throw new CredencialesInvalidasException();
        }

        return crearToken(usuario);
    }

    private String crearToken(Usuario usuario) {

        byte[] bytes = new byte[32];

        secureRandom.nextBytes(bytes);

        String tokenOriginal = Base64.getUrlEncoder()
                .withoutPadding()
                .encodeToString(bytes);

        LocalDateTime ahora = LocalDateTime.now(ZoneOffset.UTC);

        RefreshToken refreshToken = new RefreshToken();

        refreshToken.setId(UUID.randomUUID().toString());
        refreshToken.setUsuario(usuario);
        refreshToken.setTokenHash(calcularHash(tokenOriginal));
        refreshToken.setFechaCreacion(ahora);
        refreshToken.setFechaExpiracion(
                ahora.plusDays(DURACION_DIAS));
        refreshToken.setRevocado(false);

        refreshTokenRepository.save(refreshToken);

        return tokenOriginal;
    }

    @Transactional
    public SesionRenovada renovarToken(String tokenOriginal) {

        if (tokenOriginal == null || tokenOriginal.isBlank()) {
            throw new CredencialesInvalidasException();
        }

        RefreshToken token = refreshTokenRepository
                .findByTokenHash(calcularHash(tokenOriginal))
                .orElseThrow(CredencialesInvalidasException::new);

        LocalDateTime ahora = LocalDateTime.now(ZoneOffset.UTC);

        if (token.isRevocado()
                || !token.getFechaExpiracion().isAfter(ahora)) {

            throw new CredencialesInvalidasException();
        }

        Usuario usuario = token.getUsuario();

        if (!usuario.isActivo()) {
            throw new CredencialesInvalidasException();
        }

        token.setRevocado(true);

        refreshTokenRepository.save(token);

        String nuevoRefreshToken = crearToken(usuario);

        String nuevoAccessToken = jwtService.generarToken(
                usuario.getId(),
                usuario.getRol());

        LoginResponse respuesta = new LoginResponse(
                nuevoAccessToken,
                "Bearer",
                900,
                usuario.getId(),
                usuario.getRol());

        return new SesionRenovada(
                respuesta,
                nuevoRefreshToken);
    }

    @Transactional
    public void revocarToken(String tokenOriginal) {

        if (tokenOriginal == null || tokenOriginal.isBlank()) {
            return;
        }

        refreshTokenRepository
                .findByTokenHash(calcularHash(tokenOriginal))
                .ifPresent(token -> {

                    token.setRevocado(true);

                    refreshTokenRepository.save(token);
                });
    }

    private String calcularHash(String token) {

        try {

            MessageDigest digest =
                    MessageDigest.getInstance("SHA-256");

            byte[] hash = digest.digest(
                    token.getBytes(StandardCharsets.UTF_8));

            return HexFormat.of().formatHex(hash);

        } catch (NoSuchAlgorithmException ex) {

            throw new IllegalStateException(
                    "No fue posible calcular el hash", ex);
        }
    }

    public record SesionRenovada(
            LoginResponse respuesta,
            String refreshToken) {
    }
}