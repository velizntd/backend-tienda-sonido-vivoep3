package cl.tienda.msproductos.dto;

import jakarta.validation.constraints.*;
import lombok.*;
import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ProductoDTO {

    @NotBlank(message = "El código es obligatorio")
    @Size(max = 30)
    private String codigo;

    @NotBlank(message = "La categoría es obligatoria")
    @Size(max = 100)
    private String categoria;

    @NotBlank(message = "El nombre es obligatorio")
    @Size(max = 150)
    private String nombre;

    @NotBlank(message = "La marca es obligatoria")
    @Size(max = 100)
    private String marca;

    @NotBlank(message = "El modelo es obligatorio")
    @Size(max = 100)
    private String modelo;

    @NotNull(message = "El stock es obligatorio")
    @Min(value = 0, message = "El stock no puede ser negativo")
    private Integer stock;

    @NotNull(message = "El precio es obligatorio")
    @DecimalMin(value = "0.0", message = "El precio no puede ser negativo")
    @Digits(integer = 10, fraction = 2)
    private BigDecimal precio;

    @NotBlank(message = "La descripción es obligatoria")
    @Size(max = 16000)
    private String descripcion;

    @NotBlank(message = "La imagen es obligatoria")
    @Size(max = 500)
    private String imagen;
}