package cl.tienda.mscarrito.repository;

import java.util.Optional;

import jakarta.persistence.LockModeType;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;

import cl.tienda.mscarrito.model.Carrito;

public interface CarritoRepository
        extends JpaRepository<Carrito, String> {

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    Optional<Carrito> findByUsuarioId(String usuarioId);
}