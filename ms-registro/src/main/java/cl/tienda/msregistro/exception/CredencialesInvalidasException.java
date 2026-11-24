package cl.tienda.msregistro.exception;

public class CredencialesInvalidasException extends RuntimeException {

    public CredencialesInvalidasException() {
        super("Correo o contrasena incorrectos");
    }
}