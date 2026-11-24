package cl.tienda.msproductos.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import lombok.*;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class DescuentoStockDTO {

    @NotNull(message = "El pedidoId es obligatorio")
    @Positive
    private Long pedidoId;

    @NotEmpty(message = "El pedido debe contener productos")
    @Size(max = 100)
    private List<@NotNull @Valid StockItemDTO> items;
}