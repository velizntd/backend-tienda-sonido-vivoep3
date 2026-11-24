package cl.tienda.msregistro.service;

import cl.tienda.msregistro.exception.DatosInvalidosException;

public final class RutUtil {

    private RutUtil() {
    }

    public static String normalizar(String rut) {

        if (rut == null || rut.isBlank()) {
            throw new DatosInvalidosException(
                    "Ingresa el RUT.");
        }

        String limpio = rut.replaceAll("[.\\-\\s]", "")
                .toUpperCase();

        if (!limpio.matches("\\d{7,8}[\\dK]")) {
            throw new DatosInvalidosException(
                    "Ingresa el RUT con su digito verificador.");
        }

        String cuerpo = limpio.substring(0, limpio.length() - 1);

        char digito = limpio.charAt(limpio.length() - 1);

        if (calcularDigito(cuerpo) != digito) {
            throw new DatosInvalidosException(
                    "El digito verificador del RUT no es valido.");
        }

        return Integer.parseInt(cuerpo) + String.valueOf(digito);
    }

    static char calcularDigito(String cuerpo) {

        int suma = 0;
        int multiplicador = 2;

        for (int i = cuerpo.length() - 1; i >= 0; i--) {

            suma += Character.getNumericValue(
                    cuerpo.charAt(i)) * multiplicador;

            multiplicador = multiplicador == 7
                    ? 2 : multiplicador + 1;
        }

        int resto = 11 - (suma % 11);

        if (resto == 11) {
            return '0';
        }

        if (resto == 10) {
            return 'K';
        }

        return Character.forDigit(resto, 10);
    }
}