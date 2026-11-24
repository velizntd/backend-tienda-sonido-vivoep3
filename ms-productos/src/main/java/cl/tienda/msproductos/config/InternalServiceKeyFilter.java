package cl.tienda.msproductos.config;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.springframework.web.filter.OncePerRequestFilter;

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.List;

public class InternalServiceKeyFilter extends OncePerRequestFilter {

    private final String claveInterna;

    public InternalServiceKeyFilter(String claveInterna) {
        this.claveInterna = claveInterna;
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {

        return !(
                "POST".equals(request.getMethod())
                && "/api/productos/stock/descontar"
                        .equals(request.getServletPath())
        );
    }

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain)
            throws ServletException, IOException {

        String claveRecibida =
                request.getHeader("X-Internal-Service-Key");

        if (claveInterna == null
                || claveInterna.isBlank()
                || claveRecibida == null
                || !MessageDigest.isEqual(
                        claveInterna.getBytes(StandardCharsets.UTF_8),
                        claveRecibida.getBytes(StandardCharsets.UTF_8)
                )) {

            response.sendError(
                    HttpServletResponse.SC_FORBIDDEN,
                    "Credencial interna no autorizada"
            );

            return;
        }

        var autenticacion =
                new UsernamePasswordAuthenticationToken(
                        "ms-carrito",
                        null,
                        List.of(new SimpleGrantedAuthority(
                                "ROLE_SERVICIO_INTERNO"
                        ))
                );

        var contexto = SecurityContextHolder.createEmptyContext();

        contexto.setAuthentication(autenticacion);

        SecurityContextHolder.setContext(contexto);

        filterChain.doFilter(request, response);
    }
}