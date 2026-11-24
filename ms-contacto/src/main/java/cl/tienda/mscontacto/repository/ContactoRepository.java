package cl.tienda.mscontacto.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import cl.tienda.mscontacto.model.Contacto;

public interface ContactoRepository extends JpaRepository<Contacto, Long> {

    List<Contacto> findAllByOrderByFechaEnvioDesc();
}
