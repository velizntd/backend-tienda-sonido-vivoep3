package cl.tienda.msproductos.repository;

import cl.tienda.msproductos.entity.DescuentoStock;

import jakarta.persistence.LockModeType;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface DescuentoStockRepository
        extends JpaRepository<DescuentoStock, String> {

    @Modifying
    @Query(value = """
        INSERT INTO descuentos_stock
            (pedido_id, huella_solicitud, procesado)
        VALUES (:pedidoId, :huella, FALSE)
        ON DUPLICATE KEY UPDATE pedido_id = pedido_id
        """, nativeQuery = true)
    int registrarSolicitud(
            @Param("pedidoId") String pedidoId,
            @Param("huella") String huella);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("""
        SELECT d FROM DescuentoStock d
        WHERE d.pedidoId = :pedidoId
        """)
    Optional<DescuentoStock> buscarBloqueado(
            @Param("pedidoId") String pedidoId);
}