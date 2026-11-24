package cl.tienda.msusuarios.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.Test;

import cl.tienda.msusuarios.exception.DatosInvalidosException;

class RutUtilTest {

    @Test
    void normalizaRutConPuntosYGuion() {
        assertThat(RutUtil.normalizar("12.345.678-5")).isEqualTo("123456785");
        assertThat(RutUtil.normalizar("90000001-4")).isEqualTo("900000014");
    }

    @Test
    void aceptaDigitoK() {
        assertThat(RutUtil.normalizar("10000013-k")).isEqualTo("10000013K");
    }

    @Test
    void rechazaDigitoVerificadorIncorrecto() {
        assertThatThrownBy(() -> RutUtil.normalizar("12345678-9"))
                .isInstanceOf(DatosInvalidosException.class);
    }

    @Test
    void rechazaFormatoInvalido() {
        assertThatThrownBy(() -> RutUtil.normalizar("abc"))
                .isInstanceOf(DatosInvalidosException.class);
    }
}
