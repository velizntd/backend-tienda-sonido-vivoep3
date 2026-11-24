package cl.tienda.mscarrito.repository;

import java.util.List;
import java.util.Optional;

import jakarta.persistence.LockModeType;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;

import cl.tienda.mscarrito.model.Pedido;

public interface PedidoRepository
        extends JpaRepository<Pedido, String> {

    List<Pedido> findByUsuarioIdOrderByFechaDesc(
            String usuarioId);

    List<Pedido> findAllByOrderByFechaDesc();

    Optional<Pedido> findByIdAndUsuarioId(
            String id,
            String usuarioId);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    Optional<Pedido> findOneById(String id);

    Optional<Pedido>
            findFirstByUsuarioIdAndEstadoOrderByFechaDesc(
                    String usuarioId,
                    String estado);

    boolean existsByUsuarioIdAndEstado(
            String usuarioId,
            String estado);
}