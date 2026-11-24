package cl.tienda.msproductos.repository;

import cl.tienda.msproductos.entity.Producto;

import jakarta.persistence.LockModeType;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ProductoRepository
        extends JpaRepository<Producto, String> {

    List<Producto> findByCategoriaNombreIgnoreCase(String categoria);

    List<Producto> findByNombreContainingIgnoreCase(String nombre);

    List<Producto> findByMarcaIgnoreCase(String marca);

    List<Producto> findByStockGreaterThan(Integer stock);

    boolean existsByCategoriaId(Long categoriaId);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT p FROM Producto p WHERE p.codigo = :codigo")
    Optional<Producto> buscarParaActualizar(
            @Param("codigo") String codigo);
}