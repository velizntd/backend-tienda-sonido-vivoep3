package cl.tienda.msregistro.service;

import java.time.Duration;

import org.junit.jupiter.api.Test;
import org.springframework.http.ResponseCookie;

import static org.junit.jupiter.api.Assertions.*;

class RefreshCookieServiceTest {

    @Test
    void debeCrearCookieHttpOnly() {

        RefreshCookieService service =
                new RefreshCookieService(true);

        ResponseCookie cookie =
                service.crearCookie("refresh-prueba");

        assertEquals("refresh_token", cookie.getName());
        assertEquals("refresh-prueba", cookie.getValue());
        assertTrue(cookie.isHttpOnly());
        assertTrue(cookie.isSecure());
        assertEquals("Strict", cookie.getSameSite());
        assertEquals("/api/auth", cookie.getPath());
        assertEquals(Duration.ofDays(7), cookie.getMaxAge());
    }

    @Test
    void debePermitirCookieEnDesarrolloLocal() {

        RefreshCookieService service =
                new RefreshCookieService(false);

        ResponseCookie cookie =
                service.crearCookie("refresh-prueba");

        assertFalse(cookie.isSecure());
        assertTrue(cookie.isHttpOnly());
    }

    @Test
    void debeEliminarCookie() {

        RefreshCookieService service =
                new RefreshCookieService(true);

        ResponseCookie cookie =
                service.eliminarCookie();

        assertEquals("refresh_token", cookie.getName());
        assertEquals("", cookie.getValue());
        assertEquals(Duration.ZERO, cookie.getMaxAge());
        assertEquals("/api/auth", cookie.getPath());
        assertTrue(cookie.isHttpOnly());
    }
}