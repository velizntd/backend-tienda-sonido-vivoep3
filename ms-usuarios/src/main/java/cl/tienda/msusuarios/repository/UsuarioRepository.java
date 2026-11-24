package cl.tienda.msusuarios.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import cl.tienda.msusuarios.model.Usuario;

public interface UsuarioRepository extends JpaRepository<Usuario, String> {

    List<Usuario> findAllByOrderByFechaRegistroDesc();

    boolean existsByRut(String rut);

    boolean existsByCorreo(String correo);

    boolean existsByCorreoAndIdNot(String correo, String id);

    boolean existsByRolAndActivoTrue(String rol);
}
