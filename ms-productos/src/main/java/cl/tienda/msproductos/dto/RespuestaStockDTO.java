package cl.tienda.msproductos.dto;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class RespuestaStockDTO {

    private String pedidoId;

    private String estado;
}