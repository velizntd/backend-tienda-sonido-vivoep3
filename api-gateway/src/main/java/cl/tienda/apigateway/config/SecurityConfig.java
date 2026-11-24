package cl.tienda.apigateway.config;

import java.util.List;
import java.util.Base64;
import javax.crypto.spec.SecretKeySpec;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.oauth2.jwt.*;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.server.resource.authentication.*;
import org.springframework.security.web.server.SecurityWebFilterChain;
import org.springframework.security.web.server.context.NoOpServerSecurityContextRepository;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.reactive.CorsConfigurationSource;
import org.springframework.web.cors.reactive.UrlBasedCorsConfigurationSource;

@Configuration
@EnableWebFluxSecurity
public class SecurityConfig {

    @Bean
    public SecurityWebFilterChain securityFilterChain(
            ServerHttpSecurity http) {

        JwtGrantedAuthoritiesConverter roles =
                new JwtGrantedAuthoritiesConverter();

        roles.setAuthoritiesClaimName("rol");
        roles.setAuthorityPrefix("ROLE_");

        JwtAuthenticationConverter converter =
                new JwtAuthenticationConverter();

        converter.setJwtGrantedAuthoritiesConverter(roles);

        return http
                .csrf(csrf -> csrf.disable())
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))
                .httpBasic(basic -> basic.disable())
                .formLogin(form -> form.disable())
                .securityContextRepository(
                        NoOpServerSecurityContextRepository.getInstance())
                .authorizeExchange(auth -> auth

                        .pathMatchers(HttpMethod.OPTIONS, "/**")
                        .permitAll()

                        .pathMatchers(HttpMethod.POST,
                                "/api/auth/login",
                                "/api/auth/registro",
                                "/api/auth/refresh",
                                "/api/auth/logout",
                                "/api/contactos")
                        .permitAll()

                        .pathMatchers(HttpMethod.GET,
                                "/api/productos/**",
                                "/api/categorias/**",
                                "/api/novedades/**")
                        .hasAnyRole("cliente", "vendedor", "administrador")

                        .pathMatchers(HttpMethod.GET,
                                "/api/blogs/**")
                        .permitAll()

                        .pathMatchers("/api/usuarios/**")
                        .hasRole("administrador")

                        .pathMatchers("/api/productos/**",
                                "/api/categorias/**",
                                "/api/novedades/**",
                                "/api/blogs/**",
                                "/api/contactos/**")
                        .hasRole("administrador")

                        .pathMatchers("/api/carrito/**",
                                "/api/pedidos/**",
                                "/api/auth/me")
                        .authenticated()

                        .anyExchange().denyAll()
                )
                .oauth2ResourceServer(oauth -> oauth.jwt(jwt ->
                        jwt.jwtAuthenticationConverter(
                                new ReactiveJwtAuthenticationConverterAdapter(converter))))
                .build();
    }

    @Bean
    public ReactiveJwtDecoder jwtDecoder(
            @Value("${app.jwt.secret}") String secret) {

        byte[] key = Base64.getDecoder().decode(secret);

        NimbusReactiveJwtDecoder decoder =
                NimbusReactiveJwtDecoder.withSecretKey(
                        new SecretKeySpec(key, "HmacSHA256"))
                        .macAlgorithm(MacAlgorithm.HS256)
                        .build();

        decoder.setJwtValidator(
                JwtValidators.createDefaultWithIssuer("sonidovivo"));

        return decoder;
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {

        CorsConfiguration config = new CorsConfiguration();

        config.setAllowedOrigins(List.of("http://localhost:5173"));
        config.setAllowedMethods(List.of(
                "GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"));
        config.setAllowedHeaders(List.of(
                "Authorization", "Content-Type"));
        config.setAllowCredentials(true);

        UrlBasedCorsConfigurationSource source =
                new UrlBasedCorsConfigurationSource();

        source.registerCorsConfiguration("/**", config);

        return source;
    }
}
