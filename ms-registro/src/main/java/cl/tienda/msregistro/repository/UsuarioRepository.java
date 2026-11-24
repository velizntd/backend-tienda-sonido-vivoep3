package cl.tienda.msregistro.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import cl.tienda.msregistro.model.Usuario;

public interface UsuarioRepository
        extends JpaRepository<Usuario, String> {

    Optional<Usuario> findByCorreo(String correo);

    boolean existsByCorreo(String correo);

    boolean existsByRut(String rut);
}