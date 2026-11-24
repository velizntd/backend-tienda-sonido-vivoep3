package cl.tienda.mscarrito.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import cl.tienda.mscarrito.model.DetallePedido;

public interface DetallePedidoRepository
        extends JpaRepository<DetallePedido, String> {

    List<DetallePedido> findByPedido_Id(String pedidoId);
}