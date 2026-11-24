package cl.tienda.msproductos.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

public class StockException extends ResponseStatusException {

    private final String codigo;

    public StockException(String codigo, String mensaje) {
        super(HttpStatus.CONFLICT, mensaje);
        this.codigo = codigo;
    }

    public String getCodigo() {
        return codigo;
    }
}