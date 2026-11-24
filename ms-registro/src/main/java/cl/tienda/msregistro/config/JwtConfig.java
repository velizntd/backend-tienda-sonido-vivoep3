package cl.tienda.msregistro.config;

import java.util.Base64;
import javax.crypto.spec.SecretKeySpec;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.NimbusJwtEncoder;

import com.nimbusds.jose.jwk.source.ImmutableSecret;

@Configuration
public class JwtConfig {

    @Bean
    public JwtEncoder jwtEncoder(
            @Value("${app.jwt.secret}") String secret) {

        byte[] key = Base64.getDecoder().decode(secret);

        SecretKeySpec secretKey =
                new SecretKeySpec(key, "HmacSHA256");

        return new NimbusJwtEncoder(
                new ImmutableSecret<>(secretKey));
    }
}