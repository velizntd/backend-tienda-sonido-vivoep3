package cl.tienda.msblogs.model;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OrderColumn;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "blogs")
@Getter
@Setter
@NoArgsConstructor
public class Blog {

    @Id
    @Column(length = 100)
    private String id;

    @Column(nullable = false, length = 150)
    private String titulo;

    @Column(nullable = false, length = 500)
    private String resumen;

    @Column(nullable = false, length = 2000)
    private String introduccion;

    @Column(nullable = false, length = 300)
    private String imagen;

    @Column(name = "descripcion_imagen", nullable = false, length = 200)
    private String descripcionImagen;

    @Column(name = "fecha_publicacion", nullable = false)
    private LocalDateTime fechaPublicacion;

    @ElementCollection
    @CollectionTable(name = "blog_secciones", joinColumns = @JoinColumn(name = "blog_id"))
    @OrderColumn(name = "orden")
    private List<SeccionBlog> secciones = new ArrayList<>();
}
