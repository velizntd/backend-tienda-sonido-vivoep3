package cl.tienda.msproductos.controller;

import cl.tienda.msproductos.dto.ProductoDTO;
import cl.tienda.msproductos.service.ProductoService;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import org.springframework.http.ResponseEntity;

import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ProductoControllerTest {

    @Mock
    private ProductoService productoService;

    @InjectMocks
    private ProductoController productoController;

    private ProductoDTO producto;

    @BeforeEach
    void prepararDatos() {

        producto = new ProductoDTO(
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

        when(productoService.listar())
                .thenReturn(List.of(producto));

        List<ProductoDTO> resultado =
                productoController.listar();

        assertEquals(1, resultado.size());
        assertEquals("TEST001", resultado.get(0).getCodigo());
    }

    @Test
    void buscarPorCodigo() {

        when(productoService.buscarPorCodigo("TEST001"))
                .thenReturn(producto);

        ProductoDTO resultado =
                productoController.buscarPorCodigo("TEST001");

        assertEquals("TEST001", resultado.getCodigo());
    }

    @Test
    void buscarPorCategoria() {

        when(productoService.buscarPorCategoria("Accesorios"))
                .thenReturn(List.of(producto));

        List<ProductoDTO> resultado =
                productoController.buscarPorCategoria("Accesorios");

        assertEquals(1, resultado.size());
        assertEquals("Accesorios", resultado.get(0).getCategoria());
    }

    @Test
    void buscarPorNombre() {

        when(productoService.buscarPorNombre("prueba"))
                .thenReturn(List.of(producto));

        List<ProductoDTO> resultado =
                productoController.buscarPorNombre("prueba");

        assertEquals(1, resultado.size());
    }

    @Test
    void buscarPorMarca() {

        when(productoService.buscarPorMarca("Sonido Vivo"))
                .thenReturn(List.of(producto));

        List<ProductoDTO> resultado =
                productoController.buscarPorMarca("Sonido Vivo");

        assertEquals("Sonido Vivo", resultado.get(0).getMarca());
    }

    @Test
    void listarDisponibles() {

        when(productoService.listarDisponibles())
                .thenReturn(List.of(producto));

        List<ProductoDTO> resultado =
                productoController.listarDisponibles();

        assertEquals(1, resultado.size());
        assertTrue(resultado.get(0).getStock() > 0);
    }

    @Test
    void crearProducto() {

        when(productoService.crear(producto))
                .thenReturn(producto);

        ResponseEntity<ProductoDTO> respuesta =
                productoController.crear(producto);

        assertEquals(201, respuesta.getStatusCode().value());
        assertNotNull(respuesta.getBody());
        assertEquals("TEST001", respuesta.getBody().getCodigo());
    }

    @Test
    void actualizarProducto() {

        producto.setStock(20);

        when(productoService.actualizar("TEST001", producto))
                .thenReturn(producto);

        ProductoDTO resultado =
                productoController.actualizar("TEST001", producto);

        assertEquals(20, resultado.getStock());
    }

    @Test
    void eliminarProducto() {

        ResponseEntity<Void> respuesta =
                productoController.eliminar("TEST001");

        assertEquals(204, respuesta.getStatusCode().value());

        verify(productoService).eliminar("TEST001");
    }
}