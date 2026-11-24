package cl.tienda.msregistro.repository;

import java.util.Optional;

import jakarta.persistence.LockModeType;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;

import cl.tienda.msregistro.model.RefreshToken;

public interface RefreshTokenRepository
        extends JpaRepository<RefreshToken, String> {

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    Optional<RefreshToken> findByTokenHash(String tokenHash);
}