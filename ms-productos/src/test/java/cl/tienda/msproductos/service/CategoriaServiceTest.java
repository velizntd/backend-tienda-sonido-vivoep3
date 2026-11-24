package cl.tienda.msproductos.service;

import cl.tienda.msproductos.dto.CategoriaDTO;
import cl.tienda.msproductos.entity.Categoria;
import cl.tienda.msproductos.repository.CategoriaRepository;
import cl.tienda.msproductos.repository.ProductoRepository;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CategoriaServiceTest {

    @Mock
    private CategoriaRepository categoriaRepository;

    @Mock
    private ProductoRepository productoRepository;

    @InjectMocks
    private CategoriaService categoriaService;

    private Categoria categoria;
    private CategoriaDTO dto;

    @BeforeEach
    void prepararDatos() {

        categoria = new Categoria(
                1L,
                "Accesorios",
                "Categoria de prueba"
        );

        dto = new CategoriaDTO(
                1L,
                "Accesorios",
                "Categoria de prueba"
        );
    }

    @Test
    void listarCategorias() {

        when(categoriaRepository.findAll())
                .thenReturn(List.of(categoria));

        List<CategoriaDTO> resultado = categoriaService.listar();

        assertEquals(1, resultado.size());
        assertEquals("Accesorios", resultado.get(0).getNombre());
    }

    @Test
    void buscarCategoriaExistente() {

        when(categoriaRepository.findById(1L))
                .thenReturn(Optional.of(categoria));

        CategoriaDTO resultado = categoriaService.buscarPorId(1L);

        assertEquals("Accesorios", resultado.getNombre());
    }

    @Test
    void buscarCategoriaInexistente() {

        when(categoriaRepository.findById(99L))
                .thenReturn(Optional.empty());

        ResponseStatusException error = assertThrows(
                ResponseStatusException.class,
                () -> categoriaService.buscarPorId(99L)
        );

        assertEquals(404, error.getStatusCode().value());
    }

    @Test
    void crearCategoria() {

        when(categoriaRepository.existsByNombreIgnoreCase("Accesorios"))
                .thenReturn(false);

        when(categoriaRepository.save(any(Categoria.class)))
                .thenReturn(categoria);

        CategoriaDTO resultado = categoriaService.crear(dto);

        assertEquals("Accesorios", resultado.getNombre());

        verify(categoriaRepository).save(any(Categoria.class));
    }

    @Test
    void crearCategoriaDuplicada() {

        when(categoriaRepository.existsByNombreIgnoreCase("Accesorios"))
                .thenReturn(true);

        ResponseStatusException error = assertThrows(
                ResponseStatusException.class,
                () -> categoriaService.crear(dto)
        );

        assertEquals(409, error.getStatusCode().value());

        verify(categoriaRepository, never())
                .save(any(Categoria.class));
    }

    @Test
    void actualizarCategoria() {

        dto.setNombre("Accesorios musicales");

        when(categoriaRepository.findById(1L))
                .thenReturn(Optional.of(categoria));

        when(categoriaRepository.findByNombreIgnoreCase(
                "Accesorios musicales"))
                .thenReturn(Optional.empty());

        when(categoriaRepository.save(any(Categoria.class)))
                .thenAnswer(invocacion -> invocacion.getArgument(0));

        CategoriaDTO resultado = categoriaService.actualizar(1L, dto);

        assertEquals("Accesorios musicales", resultado.getNombre());
    }

    @Test
    void eliminarCategoriaSinProductos() {

        when(categoriaRepository.findById(1L))
                .thenReturn(Optional.of(categoria));

        when(productoRepository.existsByCategoriaId(1L))
                .thenReturn(false);

        categoriaService.eliminar(1L);

        verify(categoriaRepository).delete(categoria);
    }

    @Test
    void eliminarCategoriaConProductos() {

        when(categoriaRepository.findById(1L))
                .thenReturn(Optional.of(categoria));

        when(productoRepository.existsByCategoriaId(1L))
                .thenReturn(true);

        ResponseStatusException error = assertThrows(
                ResponseStatusException.class,
                () -> categoriaService.eliminar(1L)
        );

        assertEquals(409, error.getStatusCode().value());

        verify(categoriaRepository, never())
                .delete(any(Categoria.class));
    }
}