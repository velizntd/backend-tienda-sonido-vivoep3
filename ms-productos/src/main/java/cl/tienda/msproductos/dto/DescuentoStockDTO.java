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

    @NotBlank(message = "El pedidoId es obligatorio")
    @Pattern(
        regexp = "^[0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12}$",
        message = "El pedidoId debe ser un UUID válido en minúsculas"
    )
    private String pedidoId;

    @NotEmpty(message = "El pedido debe contener productos")
    @Size(max = 100)
    private List<@NotNull @Valid StockItemDTO> items;
}