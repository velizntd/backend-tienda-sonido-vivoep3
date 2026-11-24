package cl.tienda.msproductos.dto;

import jakarta.validation.constraints.*;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class StockItemDTO {

    @NotBlank(message = "El código es obligatorio")
    @Size(max = 30)
    private String codigo;

    @NotNull(message = "La cantidad es obligatoria")
    @Positive(message = "La cantidad debe ser mayor a cero")
    private Integer cantidad;
}