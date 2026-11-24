package cl.tienda.mscarrito.service;

import cl.tienda.mscarrito.dto.*;
import cl.tienda.mscarrito.exception.CarritoException;
import java.math.BigDecimal;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class PedidoServiceTest {
    final PedidoTransaccionService transacciones = mock(PedidoTransaccionService.class);
    final PedidoService servicio = new PedidoService(transacciones);
    PedidoResponse respuesta(String estado) { return new PedidoResponse("p1", null, "u1", estado, null, null, List.of(), BigDecimal.TEN); }
    @Test void persisteAntesDeConfirmarEnOtraTransaccion() {
        var request = new PedidoRequest(null, null);
        when(transacciones.prepararPedido("u1", request, "jwt")).thenReturn("p1");
        when(transacciones.confirmarPedido("u1", "p1")).thenReturn(respuesta("pagado"));
        assertEquals("pagado", servicio.crearPedido("u1", request, "jwt").estado());
        var orden = inOrder(transacciones); orden.verify(transacciones).prepararPedido("u1", request, "jwt");
        orden.verify(transacciones).confirmarPedido("u1", "p1");
    }
    @Test void convierteStockInsuficienteEn409() {
        when(transacciones.prepararPedido(any(), any(), any())).thenReturn("p1");
        when(transacciones.confirmarPedido("u1", "p1")).thenReturn(respuesta("cancelado"));
        assertEquals(HttpStatus.CONFLICT, assertThrows(CarritoException.class, () -> servicio.crearPedido("u1", new PedidoRequest(null, null), "jwt")).getEstado());
    }
    @Test void delegaConsultasYEstado() {
        when(transacciones.obtenerMisPedidos("u1")).thenReturn(List.of(respuesta("pagado")));
        when(transacciones.obtenerTodos()).thenReturn(List.of(respuesta("pagado")));
        when(transacciones.obtenerDetalle("p1", "u1", "cliente")).thenReturn(respuesta("pagado"));
        when(transacciones.actualizarEstado("p1", "enviado")).thenReturn(respuesta("enviado"));
        assertEquals(1, servicio.obtenerMisPedidos("u1").size()); assertEquals(1, servicio.obtenerTodos().size());
        assertEquals("p1", servicio.obtenerDetalle("p1", "u1", "cliente").id());
        assertEquals("enviado", servicio.actualizarEstado("p1", "enviado").estado());
    }
}
