package cl.tienda.msregistro.service;

import java.util.Base64;

import org.junit.jupiter.api.Test;
import org.springframework.security.oauth2.jwt.Jwt;

import cl.tienda.msregistro.config.JwtConfig;
import cl.tienda.msregistro.config.SecurityConfig;

import static org.junit.jupiter.api.Assertions.*;

class JwtServiceTest {

    private final String secreto = Base64.getEncoder()
            .encodeToString(new byte[32]);

    @Test
    void debeGenerarTokenValido() {

        JwtConfig config = new JwtConfig();

        JwtService servicio =
                new JwtService(config.jwtEncoder(secreto));

        String token = servicio.generarToken(
                "USR-001", "cliente");

        assertNotNull(token);
        assertFalse(token.isBlank());

        SecurityConfig seguridad = new SecurityConfig();

        Jwt jwt = seguridad.jwtDecoder(secreto)
                .decode(token);

        assertEquals("USR-001", jwt.getSubject());
        assertEquals("cliente", jwt.getClaimAsString("rol"));
        assertEquals("sonidovivo", jwt.getClaimAsString("iss"));

        assertEquals(
                jwt.getIssuedAt().plusSeconds(900),
                jwt.getExpiresAt());
    }

    @Test
    void debeRechazarTokenInvalido() {

        SecurityConfig seguridad = new SecurityConfig();

        assertThrows(
                org.springframework.security.oauth2.jwt.JwtException.class,
                () -> seguridad.jwtDecoder(secreto)
                        .decode("token-invalido"));
    }
}