package cl.tienda.msregistro.config;

import org.junit.jupiter.api.Test;
import org.springframework.security.crypto.password.PasswordEncoder;

import static org.junit.jupiter.api.Assertions.*;

class PasswordConfigTest {

    private final PasswordEncoder encoder =
            new PasswordConfig().passwordEncoder();

    @Test
    void debeGenerarHashSeguro() {

        String contrasena = "Ejemplo123";
        String hash = encoder.encode(contrasena);

        assertNotEquals(contrasena, hash);
        assertTrue(encoder.matches(contrasena, hash));
    }

    @Test
    void debeRechazarContrasenaIncorrecta() {

        String hash = encoder.encode("Ejemplo123");

        assertFalse(encoder.matches("Incorrecta123", hash));
    }

    @Test
    void debeGenerarHashesDiferentes() {

        String hash1 = encoder.encode("Ejemplo123");
        String hash2 = encoder.encode("Ejemplo123");

        assertNotEquals(hash1, hash2);
    }
}