package cl.tienda.mscarrito.model;

import jakarta.persistence.*;

import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;

@Entity
@Table(
        name = "detalle_carrito",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_carrito_producto",
                        columnNames = {
                                "carrito_id",
                                "codigo_producto"
                        })
        })
@Getter
@Setter
@NoArgsConstructor
public class DetalleCarrito {

    @Id
    @Column(length = 36)
    private String id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "carrito_id", nullable = false)
    private Carrito carrito;

    @Column(
            name = "codigo_producto",
            nullable = false,
            length = 50)
    private String codigoProducto;

    @Column(nullable = false)
    private int cantidad;
}