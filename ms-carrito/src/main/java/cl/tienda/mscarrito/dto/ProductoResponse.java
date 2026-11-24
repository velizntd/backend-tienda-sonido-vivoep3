package cl.tienda.mscarrito.dto;

import java.math.BigDecimal;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public record ProductoResponse(

        String codigo,
        String nombre,
        Integer stock,
        BigDecimal precio,
        String imagen

) {
}