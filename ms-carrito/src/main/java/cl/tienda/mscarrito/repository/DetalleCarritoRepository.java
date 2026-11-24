package cl.tienda.mscarrito.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import cl.tienda.mscarrito.model.DetalleCarrito;

public interface DetalleCarritoRepository
        extends JpaRepository<DetalleCarrito, String> {

    List<DetalleCarrito> findByCarrito_Id(String carritoId);

    Optional<DetalleCarrito>
            findByCarrito_IdAndCodigoProducto(
                    String carritoId,
                    String codigoProducto);

    void deleteByCarrito_Id(String carritoId);
}