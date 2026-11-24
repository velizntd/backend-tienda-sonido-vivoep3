package cl.tienda.msnovedades.dto;

import jakarta.validation.constraints.*;
import lombok.*;
import java.time.LocalDate;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class NovedadDTO {

    private Long id;

    @NotBlank(message = "El título es obligatorio")
    @Size(max = 150)
    private String titulo;

    @NotBlank(message = "La descripción es obligatoria")
    @Size(max = 16000)
    private String descripcion;

    @NotBlank(message = "El tipo es obligatorio")
    @Pattern(
        regexp = "NOVEDAD|PROMOCION|DESTACADO",
        message = "Tipo de novedad no válido"
    )
    private String tipo;

    @NotBlank(message = "La imagen es obligatoria")
    @Size(max = 500)
    private String imagen;

    @Size(max = 30)
    private String codigoProducto;

    @NotNull(message = "La fecha de inicio es obligatoria")
    private LocalDate fechaInicio;

    private LocalDate fechaFin;

    @NotNull(message = "El estado es obligatorio")
    private Boolean activo;
}