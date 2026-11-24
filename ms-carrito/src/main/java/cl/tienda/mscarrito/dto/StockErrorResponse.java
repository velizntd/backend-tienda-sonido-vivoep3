package cl.tienda.mscarrito.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public record StockErrorResponse(

        String codigo,
        String mensaje

) {
}