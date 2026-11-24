package cl.tienda.msnovedades.repository;

import cl.tienda.msnovedades.entity.Novedad;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface NovedadRepository
        extends JpaRepository<Novedad, Long> {

    List<Novedad> findByActivoTrue();

    List<Novedad> findByTipoIgnoreCase(String tipo);
}