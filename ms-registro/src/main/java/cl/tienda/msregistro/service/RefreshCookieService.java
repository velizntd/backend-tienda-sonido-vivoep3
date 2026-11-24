package cl.tienda.msregistro.service;

import java.time.Duration;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseCookie;
import org.springframework.stereotype.Service;

@Service
public class RefreshCookieService {

    public static final String NOMBRE_COOKIE = "refresh_token";

    private final boolean secure;

    public RefreshCookieService(
            @Value("${app.cookie.secure:true}") boolean secure) {

        this.secure = secure;
    }

    public ResponseCookie crearCookie(String token) {

        return ResponseCookie
                .from(NOMBRE_COOKIE, token)
                .httpOnly(true)
                .secure(secure)
                .sameSite("Strict")
                .path("/api/auth")
                .maxAge(Duration.ofDays(7))
                .build();
    }

    public ResponseCookie eliminarCookie() {

        return ResponseCookie
                .from(NOMBRE_COOKIE, "")
                .httpOnly(true)
                .secure(secure)
                .sameSite("Strict")
                .path("/api/auth")
                .maxAge(Duration.ZERO)
                .build();
    }
}