package cl.tienda.mscarrito.exception;

import org.springframework.http.HttpStatus;

public class CarritoException extends RuntimeException {

    private final HttpStatus estado;

    public CarritoException(
            HttpStatus estado,
            String mensaje) {

        super(mensaje);
        this.estado = estado;
    }

    public HttpStatus getEstado() {
        return estado;
    }
}