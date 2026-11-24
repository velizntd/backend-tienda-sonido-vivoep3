package cl.tienda.msnovedades.exception;

import org.junit.jupiter.api.Test;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class GlobalExceptionHandlerTest {

    private final GlobalExceptionHandler handler =
            new GlobalExceptionHandler();

    @Test
    void manejarProductoNoEncontrado() {

        ResponseStatusException error =
                new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Producto no encontrado"
                );

        ResponseEntity<Map<String, Object>> respuesta =
                handler.manejarError(error);

        assertEquals(404, respuesta.getStatusCode().value());
        assertNotNull(respuesta.getBody());

        assertEquals(
                "Producto no encontrado",
                respuesta.getBody().get("mensaje")
        );

        assertEquals(404, respuesta.getBody().get("estado"));
    }

    @Test
    void manejarErrorDeValidacion() {

        MethodArgumentNotValidException error =
                mock(MethodArgumentNotValidException.class);

        BindingResult bindingResult = mock(BindingResult.class);

        FieldError campo = new FieldError(
                "producto",
                "stock",
                "El stock no puede ser negativo"
        );

        when(error.getBindingResult())
                .thenReturn(bindingResult);

        when(bindingResult.getFieldErrors())
                .thenReturn(List.of(campo));

        ResponseEntity<Map<String, Object>> respuesta =
                handler.manejarValidaciones(error);

        assertEquals(400, respuesta.getStatusCode().value());
        assertNotNull(respuesta.getBody());

        assertEquals(
                "Error de validación",
                respuesta.getBody().get("mensaje")
        );

        assertEquals(
                Map.of("stock", "El stock no puede ser negativo"),
                respuesta.getBody().get("errores")
        );
    }

    @Test
    void manejarConflictoDeIntegridad() {

        DataIntegrityViolationException error =
                new DataIntegrityViolationException(
                        "Conflicto de datos"
                );

        ResponseEntity<Map<String, Object>> respuesta =
                handler.manejarIntegridad(error);

        assertEquals(409, respuesta.getStatusCode().value());
        assertNotNull(respuesta.getBody());
        assertEquals(409, respuesta.getBody().get("estado"));
    }
}