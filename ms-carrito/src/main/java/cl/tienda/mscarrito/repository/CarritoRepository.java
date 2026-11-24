package cl.tienda.mscarrito.repository;

import java.util.Optional;
import java.time.LocalDateTime;

import jakarta.persistence.LockModeType;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import cl.tienda.mscarrito.model.Carrito;

public interface CarritoRepository
        extends JpaRepository<Carrito, String> {

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    Optional<Carrito> findByUsuarioId(String usuarioId);

    @Modifying
    @Query(value = """
            INSERT INTO carritos (
                id,
                usuario_id,
                fecha_creacion,
                fecha_actualizacion
            )
            VALUES (
                :id,
                :usuarioId,
                :fecha,
                :fecha
            )
            ON DUPLICATE KEY UPDATE
                id = carritos.id
            """, nativeQuery = true)
    void crearSiNoExiste(
            @Param("id") String id,
            @Param("usuarioId") String usuarioId,
            @Param("fecha") LocalDateTime fecha);
}