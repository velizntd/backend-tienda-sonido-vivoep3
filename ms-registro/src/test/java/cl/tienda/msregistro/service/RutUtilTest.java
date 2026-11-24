package cl.tienda.msregistro.service;

import org.junit.jupiter.api.Test;

import cl.tienda.msregistro.exception.DatosInvalidosException;

import static org.junit.jupiter.api.Assertions.*;

class RutUtilTest {

    @Test
    void debeNormalizarRutValido() {
        assertEquals("123456785",
                RutUtil.normalizar("12.345.678-5"));
    }

    @Test
    void debeAceptarRutSinFormato() {
        assertEquals("123456785",
                RutUtil.normalizar("123456785"));
    }

    @Test
    void debeRechazarDigitoIncorrecto() {
        assertThrows(DatosInvalidosException.class,
                () -> RutUtil.normalizar("12.345.678-9"));
    }

    @Test
    void debeRechazarRutVacio() {
        assertThrows(DatosInvalidosException.class,
                () -> RutUtil.normalizar(""));
    }

    @Test
    void debeRechazarFormatoIncorrecto() {
        assertThrows(DatosInvalidosException.class,
                () -> RutUtil.normalizar("abc123"));
    }
}