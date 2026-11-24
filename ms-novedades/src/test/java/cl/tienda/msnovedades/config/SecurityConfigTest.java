package cl.tienda.msnovedades.config;

import org.junit.jupiter.api.Test;
import org.springframework.security.oauth2.jwt.*;

import com.nimbusds.jose.*;
import com.nimbusds.jose.crypto.MACSigner;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;

import java.time.Instant;
import java.util.Base64;
import java.util.Date;

import static org.junit.jupiter.api.Assertions.*;

class SecurityConfigTest {

    private final SecurityConfig config = new SecurityConfig();

    private final byte[] clave = new byte[32];

    private JwtDecoder crearDecoder() {
        return config.jwtDecoder(
                Base64.getEncoder().encodeToString(clave)
        );
    }

    private String generarToken(
            String emisor,
            Instant expiracion,
            byte[] claveFirma) throws Exception {

        JWTClaimsSet claims = new JWTClaimsSet.Builder()
                .issuer(emisor)
                .subject("1")
                .claim("rol", "administrador")
                .issueTime(Date.from(Instant.now().minusSeconds(600)))
                .expirationTime(Date.from(expiracion))
                .build();

        SignedJWT token = new SignedJWT(
                new JWSHeader(JWSAlgorithm.HS256),
                claims
        );

        token.sign(new MACSigner(claveFirma));

        return token.serialize();
    }

    @Test
    void aceptarTokenValido() throws Exception {

        String token = generarToken(
                "sonidovivo",
                Instant.now().plusSeconds(3600),
                clave
        );

        Jwt resultado = crearDecoder().decode(token);

        assertEquals("1", resultado.getSubject());
        assertEquals(
                "administrador",
                resultado.getClaimAsString("rol")
        );
    }

    @Test
    void rechazarTokenVencido() throws Exception {

        String token = generarToken(
                "sonidovivo",
                Instant.now().minusSeconds(300),
                clave
        );

        assertThrows(
                JwtException.class,
                () -> crearDecoder().decode(token)
        );
    }

    @Test
    void rechazarEmisorIncorrecto() throws Exception {

        String token = generarToken(
                "emisor-incorrecto",
                Instant.now().plusSeconds(3600),
                clave
        );

        assertThrows(
                JwtException.class,
                () -> crearDecoder().decode(token)
        );
    }

    @Test
    void rechazarFirmaIncorrecta() throws Exception {

        byte[] otraClave = new byte[32];
        otraClave[0] = 1;

        String token = generarToken(
                "sonidovivo",
                Instant.now().plusSeconds(3600),
                otraClave
        );

        assertThrows(
                JwtException.class,
                () -> crearDecoder().decode(token)
        );
    }
}