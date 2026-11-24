package cl.tienda.msproductos.service;

import cl.tienda.msproductos.dto.ProductoDTO;
import cl.tienda.msproductos.entity.Categoria;
import cl.tienda.msproductos.entity.Producto;
import cl.tienda.msproductos.repository.CategoriaRepository;
import cl.tienda.msproductos.repository.ProductoRepository;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ProductoServiceTest {

    @Mock
    private ProductoRepository productoRepository;

    @Mock
    private CategoriaRepository categoriaRepository;

    @InjectMocks
    private ProductoService productoService;

    private Producto producto;
    private ProductoDTO dto;
    private Categoria categoria;

    @BeforeEach
    void prepararDatos() {

        categoria = new Categoria(
                1L,
                "Accesorios",
                "Categoria de prueba"
        );

        producto = new Producto(
                "TEST001",
                categoria,
                "Producto de prueba",
                "Sonido Vivo",
                "TEST",
                10,
                new BigDecimal("9990"),
                "Descripcion de prueba",
                "/img/productos/prueba.jpg"
        );

        dto = new ProductoDTO(
                "TEST001",
                "Accesorios",
                "Producto de prueba",
                "Sonido Vivo",
                "TEST",
                10,
                new BigDecimal("9990"),
                "Descripcion de prueba",
                "/img/productos/prueba.jpg"
        );
    }

    @Test
    void listarProductos() {

        when(productoRepository.findAll())
                .thenReturn(List.of(producto));

        List<ProductoDTO> resultado = productoService.listar();

        assertEquals(1, resultado.size());
        assertEquals("TEST001", resultado.get(0).getCodigo());
    }

    @Test
    void buscarProductoExistente() {

        when(productoRepository.findById("TEST001"))
                .thenReturn(Optional.of(producto));

        ProductoDTO resultado =
                productoService.buscarPorCodigo("TEST001");

        assertEquals("Producto de prueba", resultado.getNombre());
    }

    @Test
    void buscarProductoInexistente() {

        when(productoRepository.findById("NOEXISTE"))
                .thenReturn(Optional.empty());

        ResponseStatusException error = assertThrows(
                ResponseStatusException.class,
                () -> productoService.buscarPorCodigo("NOEXISTE")
        );

        assertEquals(404, error.getStatusCode().value());
    }

    @Test
    void crearProducto() {

        when(productoRepository.existsById("TEST001"))
                .thenReturn(false);

        when(categoriaRepository.findByNombreIgnoreCase("Accesorios"))
                .thenReturn(Optional.of(categoria));

        when(productoRepository.save(any(Producto.class)))
                .thenAnswer(invocacion -> invocacion.getArgument(0));

        ProductoDTO resultado = productoService.crear(dto);

        assertEquals("TEST001", resultado.getCodigo());
        assertEquals(10, resultado.getStock());

        verify(productoRepository).save(any(Producto.class));
    }

    @Test
    void crearProductoDuplicado() {

        when(productoRepository.existsById("TEST001"))
                .thenReturn(true);

        ResponseStatusException error = assertThrows(
                ResponseStatusException.class,
                () -> productoService.crear(dto)
        );

        assertEquals(409, error.getStatusCode().value());

        verify(productoRepository, never())
                .save(any(Producto.class));
    }

    @Test
    void crearProductoConCategoriaInexistente() {

        when(productoRepository.existsById("TEST001"))
                .thenReturn(false);

        when(categoriaRepository.findByNombreIgnoreCase("Accesorios"))
                .thenReturn(Optional.empty());

        ResponseStatusException error = assertThrows(
                ResponseStatusException.class,
                () -> productoService.crear(dto)
        );

        assertEquals(400, error.getStatusCode().value());
    }

    @Test
    void actualizarProducto() {

        dto.setStock(20);
        dto.setPrecio(new BigDecimal("14990"));

        when(productoRepository.findById("TEST001"))
                .thenReturn(Optional.of(producto));

        when(categoriaRepository.findByNombreIgnoreCase("Accesorios"))
                .thenReturn(Optional.of(categoria));

        when(productoRepository.save(any(Producto.class)))
                .thenAnswer(invocacion -> invocacion.getArgument(0));

        ProductoDTO resultado =
                productoService.actualizar("TEST001", dto);

        assertEquals(20, resultado.getStock());
        assertEquals(new BigDecimal("14990"), resultado.getPrecio());
    }

    @Test
    void eliminarProducto() {

        when(productoRepository.findById("TEST001"))
                .thenReturn(Optional.of(producto));

        productoService.eliminar("TEST001");

        verify(productoRepository).delete(producto);
    }
}