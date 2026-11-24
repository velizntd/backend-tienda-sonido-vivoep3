package cl.tienda.msproductos.repository;

import cl.tienda.msproductos.entity.Producto;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface ProductoRepository extends JpaRepository<Producto, String> {

    List<Producto> findByCategoriaNombreIgnoreCase(String categoria);

    List<Producto> findByNombreContainingIgnoreCase(String nombre);

    List<Producto> findByMarcaIgnoreCase(String marca);

    List<Producto> findByStockGreaterThan(Integer stock);

    boolean existsByCategoriaId(Long categoriaId);
}