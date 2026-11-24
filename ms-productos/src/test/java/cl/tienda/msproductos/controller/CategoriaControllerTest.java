package cl.tienda.msproductos.controller;

import cl.tienda.msproductos.dto.CategoriaDTO;
import cl.tienda.msproductos.service.CategoriaService;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import org.springframework.http.ResponseEntity;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CategoriaControllerTest {

    @Mock
    private CategoriaService categoriaService;

    @InjectMocks
    private CategoriaController categoriaController;

    private CategoriaDTO categoria;

    @BeforeEach
    void prepararDatos() {

        categoria = new CategoriaDTO(
                1L,
                "Accesorios",
                "Categoria de prueba"
        );
    }

    @Test
    void listarCategorias() {

        when(categoriaService.listar())
                .thenReturn(List.of(categoria));

        List<CategoriaDTO> resultado =
                categoriaController.listar();

        assertEquals(1, resultado.size());
        assertEquals("Accesorios", resultado.get(0).getNombre());
    }

    @Test
    void buscarCategoriaPorId() {

        when(categoriaService.buscarPorId(1L))
                .thenReturn(categoria);

        CategoriaDTO resultado =
                categoriaController.buscarPorId(1L);

        assertEquals(1L, resultado.getId());
        assertEquals("Accesorios", resultado.getNombre());
    }

    @Test
    void crearCategoria() {

        when(categoriaService.crear(categoria))
                .thenReturn(categoria);

        ResponseEntity<CategoriaDTO> respuesta =
                categoriaController.crear(categoria);

        assertEquals(201, respuesta.getStatusCode().value());
        assertNotNull(respuesta.getBody());
        assertEquals("Accesorios", respuesta.getBody().getNombre());
    }

    @Test
    void actualizarCategoria() {

        categoria.setNombre("Accesorios musicales");

        when(categoriaService.actualizar(1L, categoria))
                .thenReturn(categoria);

        CategoriaDTO resultado =
                categoriaController.actualizar(1L, categoria);

        assertEquals("Accesorios musicales", resultado.getNombre());
    }

    @Test
    void eliminarCategoria() {

        ResponseEntity<Void> respuesta =
                categoriaController.eliminar(1L);

        assertEquals(204, respuesta.getStatusCode().value());

        verify(categoriaService).eliminar(1L);
    }
}