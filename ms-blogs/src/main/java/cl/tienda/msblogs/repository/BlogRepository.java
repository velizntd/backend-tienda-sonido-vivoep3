package cl.tienda.msblogs.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import cl.tienda.msblogs.model.Blog;

public interface BlogRepository extends JpaRepository<Blog, String> {

    List<Blog> findAllByOrderByFechaPublicacionDesc();
}
