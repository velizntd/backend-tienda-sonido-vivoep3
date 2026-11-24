package cl.tienda.msregistro.config;

import java.util.Base64;

import javax.crypto.spec.SecretKeySpec;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import org.springframework.http.HttpMethod;

import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;

import org.springframework.security.oauth2.jwt.*;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.server.resource.authentication.*;

import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import org.springframework.web.filter.OncePerRequestFilter;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http)
            throws Exception {

        JwtGrantedAuthoritiesConverter roles =
                new JwtGrantedAuthoritiesConverter();

        roles.setAuthoritiesClaimName("rol");
        roles.setAuthorityPrefix("ROLE_");

        JwtAuthenticationConverter converter =
                new JwtAuthenticationConverter();

        converter.setJwtGrantedAuthoritiesConverter(roles);

        return http
                .csrf(csrf -> csrf.disable())
                .httpBasic(basic -> basic.disable())
                .formLogin(form -> form.disable())
                .logout(logout -> logout.disable())

                .sessionManagement(session -> session
                        .sessionCreationPolicy(
                                SessionCreationPolicy.STATELESS))

                .addFilterBefore(
                        new ProteccionCsrfFilter(),
                        UsernamePasswordAuthenticationFilter.class)

                .authorizeHttpRequests(auth -> auth

                        .requestMatchers(
                                "/swagger-ui/**",
                                "/swagger-ui.html",
                                "/v3/api-docs/**")
                        .permitAll()

                        .requestMatchers(
                                HttpMethod.POST,
                                "/api/auth/login",
                                "/api/auth/registro",
                                "/api/auth/refresh",
                                "/api/auth/logout")
                        .permitAll()

                        .requestMatchers(
                                HttpMethod.GET,
                                "/api/auth/me")
                        .authenticated()

                        .anyRequest().denyAll())

                .oauth2ResourceServer(oauth -> oauth
                        .jwt(jwt -> jwt
                                .jwtAuthenticationConverter(converter)))

                .build();
    }

    @Bean
    public JwtDecoder jwtDecoder(
            @Value("${app.jwt.secret}") String secret) {

        byte[] key = Base64.getDecoder().decode(secret);

        NimbusJwtDecoder decoder = NimbusJwtDecoder
                .withSecretKey(
                        new SecretKeySpec(key, "HmacSHA256"))
                .macAlgorithm(MacAlgorithm.HS256)
                .build();

        decoder.setJwtValidator(
                JwtValidators.createDefaultWithIssuer("sonidovivo"));

        return decoder;
    }

    private static class ProteccionCsrfFilter
            extends OncePerRequestFilter {

        @Override
        protected void doFilterInternal(
                HttpServletRequest request,
                HttpServletResponse response,
                FilterChain filterChain)
                throws ServletException, IOException {

            String ruta = request.getServletPath();

            boolean requiereProteccion =
                    "POST".equals(request.getMethod())
                    && (
                            "/api/auth/login".equals(ruta)
                            || "/api/auth/refresh".equals(ruta)
                            || "/api/auth/logout".equals(ruta)
                    );

            if (requiereProteccion
                    && !"1".equals(
                            request.getHeader("X-CSRF-Protection"))) {

                response.setStatus(403);
                response.setContentType("application/json");
                response.setCharacterEncoding("UTF-8");

                response.getWriter().write(
                        "{\"mensaje\":\"Solicitud CSRF no permitida\"}");

                return;
            }

            filterChain.doFilter(request, response);
        }
    }
}