package cl.tienda.mscontacto.model;

import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "contactos")
@Getter
@Setter
@NoArgsConstructor
public class Contacto {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "usuario_id", length = 36)
    private String usuarioId;

    @Column(nullable = false, length = 100)
    private String nombre;

    @Column(length = 100)
    private String correo;

    @Column(nullable = false, length = 500)
    private String comentario;

    @Column(name = "fecha_envio", nullable = false)
    private LocalDateTime fechaEnvio;
}
