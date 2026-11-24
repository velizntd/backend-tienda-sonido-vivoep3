package cl.tienda.msproductos.service;

import cl.tienda.msproductos.dto.CategoriaDTO;
import cl.tienda.msproductos.entity.Categoria;
import cl.tienda.msproductos.repository.CategoriaRepository;
import cl.tienda.msproductos.repository.ProductoRepository;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.http.HttpStatus;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class CategoriaService {

    private final CategoriaRepository categoriaRepository;
    private final ProductoRepository productoRepository;

    @Transactional(readOnly = true)
    public List<CategoriaDTO> listar() {
        return categoriaRepository.findAll()
                .stream()
                .map(this::convertirDTO)
                .toList();
    }

    @Transactional(readOnly = true)
    public CategoriaDTO buscarPorId(Long id) {
        return convertirDTO(buscarEntidad(id));
    }

    public CategoriaDTO crear(CategoriaDTO dto) {

        if (categoriaRepository.existsByNombreIgnoreCase(dto.getNombre())) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "La categoría ya existe"
            );
        }

        Categoria categoria = new Categoria();
        categoria.setNombre(dto.getNombre());
        categoria.setDescripcion(dto.getDescripcion());

        return convertirDTO(categoriaRepository.save(categoria));
    }

    public CategoriaDTO actualizar(Long id, CategoriaDTO dto) {

        Categoria categoria = buscarEntidad(id);

        categoriaRepository.findByNombreIgnoreCase(dto.getNombre())
                .ifPresent(existente -> {
                    if (!existente.getId().equals(id)) {
                        throw new ResponseStatusException(
                                HttpStatus.CONFLICT,
                                "El nombre de la categoría ya existe"
                        );
                    }
                });

        categoria.setNombre(dto.getNombre());
        categoria.setDescripcion(dto.getDescripcion());

        return convertirDTO(categoriaRepository.save(categoria));
    }

    public void eliminar(Long id) {

        Categoria categoria = buscarEntidad(id);

        if (productoRepository.existsByCategoriaId(id)) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "La categoría tiene productos asociados"
            );
        }

        categoriaRepository.delete(categoria);
    }

    private Categoria buscarEntidad(Long id) {
        return categoriaRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Categoría no encontrada"
                ));
    }

    private CategoriaDTO convertirDTO(Categoria categoria) {
        return new CategoriaDTO(
                categoria.getId(),
                categoria.getNombre(),
                categoria.getDescripcion()
        );
    }
}