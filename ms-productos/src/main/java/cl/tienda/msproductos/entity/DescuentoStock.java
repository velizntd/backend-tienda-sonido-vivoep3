package cl.tienda.msproductos.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "descuentos_stock")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class DescuentoStock {

    @Id
    @Column(name = "pedido_id")
    private Long pedidoId;

    @Column(
        name = "huella_solicitud",
        nullable = false,
        columnDefinition = "CHAR(64)"
    )
    private String huellaSolicitud;

    @Column(nullable = false)
    private Boolean procesado;
}