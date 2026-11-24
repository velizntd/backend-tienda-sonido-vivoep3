package cl.tienda.mscarrito.service;

import cl.tienda.mscarrito.client.*;
import cl.tienda.mscarrito.dto.*;
import cl.tienda.mscarrito.exception.CarritoException;
import cl.tienda.mscarrito.model.*;
import cl.tienda.mscarrito.repository.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Stream;
import org.junit.jupiter.api.*;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.*;
import org.mockito.ArgumentCaptor;
import org.springframework.http.HttpStatus;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class PedidoTransaccionServiceTest {
    CarritoRepository carritos;
    DetalleCarritoRepository detallesCarrito;
    PedidoRepository pedidos;
    DetallePedidoRepository detallesPedido;
    ProductoClient productos;
    StockClient stock;
    PedidoTransaccionService servicio;
    Carrito carrito;
    Pedido pedido;
    DetallePedido detalle;
    final CompradorRequest comprador = new CompradorRequest("Ana", "Prueba", "qa@example.test");
    final EntregaRequest retiro = new EntregaRequest("retiro", "", "", "", "", "", "");
    final PedidoRequest solicitud = new PedidoRequest(comprador, retiro);

    @BeforeEach void configurar() {
        carritos = mock(CarritoRepository.class); detallesCarrito = mock(DetalleCarritoRepository.class);
        pedidos = mock(PedidoRepository.class); detallesPedido = mock(DetallePedidoRepository.class);
        productos = mock(ProductoClient.class); stock = mock(StockClient.class);
        servicio = new PedidoTransaccionService(carritos, detallesCarrito, pedidos, detallesPedido, productos, stock);
        carrito = new Carrito(); carrito.setId("c1"); carrito.setUsuarioId("u1");
        when(carritos.findByUsuarioId("u1")).thenReturn(Optional.of(carrito));
        pedido = new Pedido(); pedido.setId("550e8400-e29b-41d4-a716-446655440000"); pedido.setUsuarioId("u1");
        pedido.setEstado("pendiente"); pedido.setFecha(LocalDateTime.now()); pedido.setTotal(new BigDecimal("2000"));
        pedido.setNombreComprador(comprador.nombre()); pedido.setApellidoComprador(comprador.apellido());
        pedido.setCorreoComprador(comprador.correo()); pedido.setTipoEntrega("retiro");
        pedido.setCalle(""); pedido.setNumero(""); pedido.setComplemento(""); pedido.setRegion(""); pedido.setComuna(""); pedido.setIndicaciones("");
        detalle = new DetallePedido(); detalle.setId("d1"); detalle.setPedido(pedido);
        detalle.setCodigoProducto("QA001"); detalle.setNombreProducto("Nombre histórico");
        detalle.setCantidad(2); detalle.setPrecioUnitario(new BigDecimal("1000"));
        when(pedidos.findOneById(pedido.getId())).thenReturn(Optional.of(pedido));
        when(detallesPedido.findByPedido_Id(pedido.getId())).thenReturn(List.of(detalle));
    }
    void carritoConProducto(int cantidad, int disponible) {
        var item = new DetalleCarrito(); item.setCodigoProducto("QA001"); item.setCantidad(cantidad); item.setCarrito(carrito);
        when(detallesCarrito.findByCarrito_Id("c1")).thenReturn(List.of(item));
        when(productos.obtenerProducto("QA001", "jwt")).thenReturn(new ProductoResponse("QA001", "Nombre actual", disponible, new BigDecimal("1500"), null));
    }
    void error(HttpStatus estado, org.junit.jupiter.api.function.Executable accion) {
        assertEquals(estado, assertThrows(CarritoException.class, accion).getEstado());
    }

    @Test void rechazaCarritoInexistente() {
        when(carritos.findByUsuarioId("u1")).thenReturn(Optional.empty());
        error(HttpStatus.BAD_REQUEST, () -> servicio.prepararPedido("u1", solicitud, "jwt"));
    }
    @Test void rechazaCarritoVacio() {
        error(HttpStatus.BAD_REQUEST, () -> servicio.prepararPedido("u1", solicitud, "jwt"));
        verify(pedidos, never()).save(any());
    }
    @Test void creaPendienteConUuidYPreciosHistoricosDelBackend() {
        carritoConProducto(2, 5);
        String id = servicio.prepararPedido("u1", solicitud, "jwt");
        assertDoesNotThrow(() -> UUID.fromString(id));
        var captor = ArgumentCaptor.forClass(Pedido.class); verify(pedidos).save(captor.capture());
        var p = captor.getValue(); assertEquals("pendiente", p.getEstado()); assertEquals("u1", p.getUsuarioId());
        assertEquals(new BigDecimal("3000"), p.getTotal()); assertEquals("retiro", p.getTipoEntrega());
        verify(detallesPedido).saveAll(argThat(items -> {
            var d = items.iterator().next();
            return d.getNombreProducto().equals("Nombre actual") && d.getCantidad() == 2 && d.getPrecioUnitario().equals(new BigDecimal("1500"));
        }));
        verifyNoInteractions(stock);
    }
    @Test void rechazaStockInsuficienteAntesDeGuardar() {
        carritoConProducto(2, 1);
        error(HttpStatus.CONFLICT, () -> servicio.prepararPedido("u1", solicitud, "jwt")); verify(pedidos, never()).save(any());
    }
    @ParameterizedTest @CsvSource({"calle", "numero", "region", "comuna"})
    void exigeCadaDatoDeDespacho(String campo) {
        var entrega = new EntregaRequest("despacho", campo.equals("calle") ? null : "Calle", campo.equals("numero") ? " " : "1", "",
            campo.equals("region") ? "" : "Valparaíso", campo.equals("comuna") ? "" : "Viña", "");
        error(HttpStatus.BAD_REQUEST, () -> servicio.prepararPedido("u1", new PedidoRequest(comprador, entrega), "jwt"));
    }
    @Test void permiteDespachoCompleto() {
        carritoConProducto(1, 5);
        var entrega = new EntregaRequest("despacho", "Calle", "1", "Depto", "Región", "Comuna", "Portería");
        assertNotNull(servicio.prepararPedido("u1", new PedidoRequest(comprador, entrega), "jwt"));
    }
    @Test void rechazaTipoEntregaDesconocido() {
        error(HttpStatus.BAD_REQUEST, () -> servicio.prepararPedido("u1", new PedidoRequest(comprador,
            new EntregaRequest("otro", "", "", "", "", "", "")), "jwt"));
    }
    @Test void recuperaMismoUuidSinConsultarNuevosPrecios() {
        when(pedidos.findFirstByUsuarioIdAndEstadoOrderByFechaDesc("u1", "pendiente")).thenReturn(Optional.of(pedido));
        assertEquals(pedido.getId(), servicio.prepararPedido("u1", solicitud, "jwt"));
        verifyNoInteractions(productos, stock, detallesCarrito); verify(pedidos, never()).save(any());
    }
    @Test void noAceptaCambiarCompradorNiEntregaDelPendiente() {
        when(pedidos.findFirstByUsuarioIdAndEstadoOrderByFechaDesc("u1", "pendiente")).thenReturn(Optional.of(pedido));
        error(HttpStatus.CONFLICT, () -> servicio.prepararPedido("u1", new PedidoRequest(new CompradorRequest("Otro", "Prueba", comprador.correo()), retiro), "jwt"));
        error(HttpStatus.CONFLICT, () -> servicio.prepararPedido("u1", new PedidoRequest(comprador, new EntregaRequest("despacho", "C", "1", "", "R", "C", "")), "jwt"));
    }
    @Test void confirmaConItemsPersistidosYVaciaCarrito() {
        when(stock.descontar(any())).thenReturn(StockClient.Resultado.DESCONTADO);
        var r = servicio.confirmarPedido("u1", pedido.getId());
        assertEquals("pagado", r.estado()); assertEquals("Nombre histórico", r.items().getFirst().nombre());
        verify(stock).descontar(new StockDescuentoRequest(pedido.getId(), List.of(new StockItemRequest("QA001", 2))));
        verify(detallesCarrito).deleteByCarrito_Id("c1"); verify(pedidos).save(pedido);
        verifyNoInteractions(productos);
    }
    @Test void cancelaPorStockDefinitivoYConservaCarrito() {
        when(stock.descontar(any())).thenReturn(StockClient.Resultado.STOCK_INSUFICIENTE);
        assertEquals("cancelado", servicio.confirmarPedido("u1", pedido.getId()).estado());
        verify(detallesCarrito, never()).deleteByCarrito_Id(anyString());
    }
    @ParameterizedTest @EnumSource(value = StockClient.Resultado.class, names = {"NO_CONFIRMADO", "CONFLICTO_IDEMPOTENCIA"})
    void conservaPendienteYUuidAnteIncertidumbre(StockClient.Resultado resultado) {
        when(stock.descontar(any())).thenReturn(resultado, StockClient.Resultado.DESCONTADO);
        assertEquals("pendiente", servicio.confirmarPedido("u1", pedido.getId()).estado());
        verify(detallesCarrito, never()).deleteByCarrito_Id(anyString());
        assertEquals("pagado", servicio.confirmarPedido("u1", pedido.getId()).estado());
        verify(stock, times(2)).descontar(new StockDescuentoRequest(pedido.getId(), List.of(new StockItemRequest("QA001", 2))));
    }
    @Test void confirmacionYaResueltaNoDescuentaOtraVez() {
        pedido.setEstado("pagado"); assertEquals("pagado", servicio.confirmarPedido("u1", pedido.getId()).estado());
        verifyNoInteractions(stock, detallesCarrito);
    }
    @Test void rechazaConfirmacionDePedidoAjenoOInexistente() {
        pedido.setUsuarioId("otro"); error(HttpStatus.NOT_FOUND, () -> servicio.confirmarPedido("u1", pedido.getId()));
        error(HttpStatus.NOT_FOUND, () -> servicio.confirmarPedido("u1", "ausente")); verifyNoInteractions(stock);
    }
    @Test void rechazaConfirmacionSinCarrito() {
        when(carritos.findByUsuarioId("u1")).thenReturn(Optional.empty());
        error(HttpStatus.NOT_FOUND, () -> servicio.confirmarPedido("u1", pedido.getId()));
    }
    @Test void historialFiltraPorUsuarioYConservaTotales() {
        when(pedidos.findByUsuarioIdOrderByFechaDesc("u1")).thenReturn(List.of(pedido));
        assertEquals(new BigDecimal("2000"), servicio.obtenerMisPedidos("u1").getFirst().total());
        when(pedidos.findAllByOrderByFechaDesc()).thenReturn(List.of(pedido));
        assertEquals(1, servicio.obtenerTodos().size());
    }
    @ParameterizedTest @ValueSource(strings = {"administrador", "vendedor"})
    void personalConsultaDetalle(String rol) {
        when(pedidos.findById(pedido.getId())).thenReturn(Optional.of(pedido));
        assertEquals(pedido.getId(), servicio.obtenerDetalle(pedido.getId(), "personal", rol).id());
        error(HttpStatus.NOT_FOUND, () -> servicio.obtenerDetalle("ausente", "personal", rol));
    }
    @Test void clienteSoloConsultaDetallePropio() {
        when(pedidos.findByIdAndUsuarioId(pedido.getId(), "u1")).thenReturn(Optional.of(pedido));
        assertEquals("u1", servicio.obtenerDetalle(pedido.getId(), "u1", "cliente").usuarioId());
        error(HttpStatus.NOT_FOUND, () -> servicio.obtenerDetalle(pedido.getId(), "otro", "cliente"));
        error(HttpStatus.FORBIDDEN, () -> servicio.obtenerDetalle(pedido.getId(), "u1", "invitado"));
    }
    static Stream<Arguments> transiciones() {
        var estados = List.of("pendiente", "pagado", "enviado", "entregado", "cancelado");
        return estados.stream().flatMap(desde -> estados.stream().map(hasta -> Arguments.of(desde, hasta,
            (desde.equals("pagado") && (hasta.equals("enviado") || hasta.equals("cancelado"))) ||
            (desde.equals("enviado") && hasta.equals("entregado")))));
    }
    @ParameterizedTest @MethodSource("transiciones")
    void validaTodasLasTransiciones(String desde, String hasta, boolean permitida) {
        pedido.setEstado(desde);
        if (permitida) assertEquals(hasta, servicio.actualizarEstado(pedido.getId(), hasta).estado());
        else { error(HttpStatus.CONFLICT, () -> servicio.actualizarEstado(pedido.getId(), hasta)); assertEquals(desde, pedido.getEstado()); }
        verifyNoInteractions(stock); // Cancelar no repone inventario por contrato del equipo.
    }
    @Test void noActualizaPedidoInexistente() { error(HttpStatus.NOT_FOUND, () -> servicio.actualizarEstado("ausente", "enviado")); }
}
