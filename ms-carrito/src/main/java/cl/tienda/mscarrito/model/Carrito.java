package cl.tienda.mscarrito.model;

import java.time.LocalDateTime;

import jakarta.persistence.*;

import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "carritos")
@Getter
@Setter
@NoArgsConstructor
public class Carrito {

    @Id
    @Column(length = 36)
    private String id;

    @Column(
            name = "usuario_id",
            nullable = false,
            unique = true,
            length = 36)
    private String usuarioId;

    @Column(
            name = "fecha_creacion",
            nullable = false)
    private LocalDateTime fechaCreacion;

    @Column(
            name = "fecha_actualizacion",
            nullable = false)
    private LocalDateTime fechaActualizacion;
}