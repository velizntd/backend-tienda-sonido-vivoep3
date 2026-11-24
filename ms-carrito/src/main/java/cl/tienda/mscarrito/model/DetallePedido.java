package cl.tienda.mscarrito.model;

import java.math.BigDecimal;

import jakarta.persistence.*;

import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;

@Entity
@Table(
        name = "detalle_pedido",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_pedido_producto",
                        columnNames = {
                                "pedido_id",
                                "codigo_producto"
                        })
        })
@Getter
@Setter
@NoArgsConstructor
public class DetallePedido {

    @Id
    @Column(length = 36)
    private String id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "pedido_id", nullable = false)
    private Pedido pedido;

    @Column(
            name = "codigo_producto",
            nullable = false,
            length = 50)
    private String codigoProducto;

    @Column(
            name = "nombre_producto",
            nullable = false,
            length = 255)
    private String nombreProducto;

    @Column(nullable = false)
    private int cantidad;

    @Column(
            name = "precio_unitario",
            nullable = false,
            precision = 12,
            scale = 2)
    private BigDecimal precioUnitario;
}