package cl.tienda.mscarrito.service;

import cl.tienda.mscarrito.client.ProductoClient;
import cl.tienda.mscarrito.dto.*;
import cl.tienda.mscarrito.exception.CarritoException;
import cl.tienda.mscarrito.model.*;
import cl.tienda.mscarrito.repository.*;
import java.math.BigDecimal;
import java.util.*;
import org.junit.jupiter.api.*;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.*;
import org.springframework.http.HttpStatus;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class CarritoServiceTest {
    CarritoRepository carritos;
    DetalleCarritoRepository detalles;
    PedidoRepository pedidos;
    ProductoClient productos;
    CarritoService servicio;
    Carrito carrito;
    List<DetalleCarrito> items;

    @BeforeEach void configurar() {
        carritos = mock(CarritoRepository.class);
        detalles = mock(DetalleCarritoRepository.class);
        pedidos = mock(PedidoRepository.class);
        productos = mock(ProductoClient.class);
        servicio = new CarritoService(carritos, detalles, productos, pedidos);
        carrito = new Carrito(); carrito.setId("c1"); carrito.setUsuarioId("u1");
        items = new ArrayList<>();
        when(carritos.findByUsuarioId("u1")).thenReturn(Optional.of(carrito));
        when(detalles.findByCarrito_Id("c1")).thenAnswer(inv -> List.copyOf(items));
        when(detalles.findByCarrito_IdAndCodigoProducto(eq("c1"), anyString()))
                .thenAnswer(inv -> items.stream().filter(i -> i.getCodigoProducto().equals(inv.getArgument(1))).findFirst());
        when(detalles.save(any())).thenAnswer(inv -> {
            DetalleCarrito item = inv.getArgument(0);
            if (!items.contains(item)) items.add(item);
            return item;
        });
        when(productos.obtenerProducto("QA001", "jwt"))
                .thenReturn(new ProductoResponse("QA001", "Guitarra", 10, new BigDecimal("129990"), null));
    }

    DetalleCarrito agregar(int cantidad) {
        DetalleCarrito item = new DetalleCarrito(); item.setId("i1"); item.setCarrito(carrito);
        item.setCodigoProducto("QA001"); item.setCantidad(cantidad); items.add(item); return item;
    }
    void error(HttpStatus estado, org.junit.jupiter.api.function.Executable accion) {
        assertEquals(estado, assertThrows(CarritoException.class, accion).getEstado());
    }

    @Test void obtieneYInicializaCarritoUnicoVacio() {
        CarritoResponse r = servicio.obtenerCarrito("u1", "jwt");
        assertEquals("u1", r.usuarioId()); assertEquals(BigDecimal.ZERO, r.total()); assertTrue(r.items().isEmpty());
        verify(carritos).crearSiNoExiste(anyString(), eq("u1"), any());
    }
    @Test void informaFalloAlCrearCarrito() {
        when(carritos.findByUsuarioId("u1")).thenReturn(Optional.empty());
        error(HttpStatus.INTERNAL_SERVER_ERROR, () -> servicio.obtenerCarrito("u1", "jwt"));
    }
    @Test void agregaYCalculaTotalesConPrecioDelBackend() {
        CarritoResponse r = servicio.agregarProducto("u1", new ItemCarritoRequest("QA001", 2), "jwt");
        assertEquals(2, r.cantidadTotal()); assertEquals(new BigDecimal("259980"), r.total());
        assertEquals(r.total(), r.items().getFirst().subtotal()); assertTrue(r.items().getFirst().disponible());
        assertNotNull(carrito.getFechaActualizacion());
    }
    @Test void acumulaUnProductoExistente() {
        agregar(2);
        var r = servicio.agregarProducto("u1", new ItemCarritoRequest("QA001", 3), "jwt");
        assertEquals(5, r.cantidadTotal()); assertEquals(1, r.items().size());
    }
    @ParameterizedTest @NullSource @ValueSource(ints = {0, -1, 1000})
    void rechazaCantidadInvalida(Integer cantidad) {
        error(HttpStatus.BAD_REQUEST, () -> servicio.agregarProducto("u1", new ItemCarritoRequest("QA001", cantidad), "jwt"));
        error(HttpStatus.BAD_REQUEST, () -> servicio.actualizarCantidad("u1", "QA001", new CantidadRequest(cantidad), "jwt"));
        verify(detalles, never()).save(any());
    }
    @Test void rechazaAcumuladoSuperiorAlMaximo() {
        agregar(999);
        error(HttpStatus.BAD_REQUEST, () -> servicio.agregarProducto("u1", new ItemCarritoRequest("QA001", 1), "jwt"));
        assertEquals(999, items.getFirst().getCantidad());
    }
    @Test void rechazaStockInsuficienteSinCambiarCantidad() {
        agregar(2);
        error(HttpStatus.CONFLICT, () -> servicio.agregarProducto("u1", new ItemCarritoRequest("QA001", 9), "jwt"));
        error(HttpStatus.CONFLICT, () -> servicio.actualizarCantidad("u1", "QA001", new CantidadRequest(11), "jwt"));
        assertEquals(2, items.getFirst().getCantidad()); verify(detalles, never()).save(any());
    }
    @Test void actualizaCantidad() {
        agregar(2);
        assertEquals(3, servicio.actualizarCantidad("u1", "QA001", new CantidadRequest(3), "jwt").cantidadTotal());
    }
    @Test void rechazaModificarOEliminarProductoAusente() {
        error(HttpStatus.NOT_FOUND, () -> servicio.actualizarCantidad("u1", "QA001", new CantidadRequest(2), "jwt"));
        error(HttpStatus.NOT_FOUND, () -> servicio.eliminarProducto("u1", "QA001", "jwt"));
    }
    @Test void eliminaSoloElProductoSolicitado() {
        var item = agregar(2);
        doAnswer(inv -> { items.remove(inv.getArgument(0)); return null; }).when(detalles).delete(any());
        assertTrue(servicio.eliminarProducto("u1", "QA001", "jwt").items().isEmpty());
        verify(detalles).delete(item);
    }
    @Test void vaciaCarritoPropio() {
        servicio.vaciarCarrito("u1"); verify(detalles).deleteByCarrito_Id("c1"); verify(carritos).save(carrito);
    }
    @Test void productoEliminadoSeConservaComoNoDisponible() {
        agregar(2);
        when(productos.obtenerProducto("QA001", "jwt")).thenThrow(new CarritoException(HttpStatus.NOT_FOUND, "No existe"));
        var r = servicio.obtenerCarrito("u1", "jwt");
        assertFalse(r.items().getFirst().disponible()); assertEquals(2, r.cantidadTotal()); assertEquals(BigDecimal.ZERO, r.total());
    }
    @Test void noOcultaErroresDelServicioDeProductos() {
        agregar(2);
        when(productos.obtenerProducto("QA001", "jwt")).thenThrow(new CarritoException(HttpStatus.SERVICE_UNAVAILABLE, "Sin servicio"));
        error(HttpStatus.SERVICE_UNAVAILABLE, () -> servicio.obtenerCarrito("u1", "jwt"));
    }
    @Test void informaStockActualMenorQueCantidadPersistida() {
        agregar(11); assertFalse(servicio.obtenerCarrito("u1", "jwt").items().getFirst().disponible());
    }
    @Test void bloqueaTodasLasMutacionesAntePedidoPendiente() {
        when(pedidos.existsByUsuarioIdAndEstado("u1", "pendiente")).thenReturn(true);
        error(HttpStatus.CONFLICT, () -> servicio.agregarProducto("u1", new ItemCarritoRequest("QA001", 1), "jwt"));
        error(HttpStatus.CONFLICT, () -> servicio.actualizarCantidad("u1", "QA001", new CantidadRequest(1), "jwt"));
        error(HttpStatus.CONFLICT, () -> servicio.eliminarProducto("u1", "QA001", "jwt"));
        error(HttpStatus.CONFLICT, () -> servicio.vaciarCarrito("u1"));
        verifyNoInteractions(detalles, productos);
    }
}
