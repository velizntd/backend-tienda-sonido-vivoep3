package cl.tienda.mscarrito.model;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import jakarta.persistence.*;

import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "pedidos")
@Getter
@Setter
@NoArgsConstructor
public class Pedido {

    @Id
    @Column(length = 36)
    private String id;

    @Column(
            name = "usuario_id",
            nullable = false,
            length = 36)
    private String usuarioId;

    @Column(nullable = false)
    private LocalDateTime fecha;

    @Column(nullable = false, length = 20)
    private String estado;

    @Column(
            name = "tipo_entrega",
            nullable = false,
            length = 20)
    private String tipoEntrega;

    @Column(
            name = "nombre_comprador",
            nullable = false,
            length = 50)
    private String nombreComprador;

    @Column(
            name = "apellido_comprador",
            nullable = false,
            length = 100)
    private String apellidoComprador;

    @Column(
            name = "correo_comprador",
            nullable = false,
            length = 100)
    private String correoComprador;

    @Column(length = 200)
    private String calle;

    @Column(length = 20)
    private String numero;

    @Column(length = 100)
    private String complemento;

    @Column(length = 100)
    private String region;

    @Column(length = 100)
    private String comuna;

    @Column(length = 300)
    private String indicaciones;

    @Column(
            nullable = false,
            precision = 14,
            scale = 2)
    private BigDecimal total;
}