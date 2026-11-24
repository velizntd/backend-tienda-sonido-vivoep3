package cl.tienda.mscarrito.controller;

import cl.tienda.mscarrito.config.SecurityConfig;
import cl.tienda.mscarrito.dto.*;
import cl.tienda.mscarrito.exception.CarritoException;
import cl.tienda.mscarrito.service.*;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.*;
import org.springframework.security.oauth2.jwt.*;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(controllers = {CarritoController.class, PedidoController.class})
@Import(SecurityConfig.class)
class CarritoPedidoControllerTest {
    @Autowired MockMvc mvc;
    @MockitoBean CarritoService carritos;
    @MockitoBean PedidoService pedidos;
    @MockitoBean JwtDecoder decoder;
    final CarritoResponse carrito = new CarritoResponse("c1", "u1", List.of(), 0, BigDecimal.ZERO);
    final String compra = """
        {"comprador":{"nombre":"Ana","apellido":"Prueba","correo":"qa@example.test"},
         "entrega":{"tipo":"retiro","calle":"","numero":"","complemento":"","region":"","comuna":"","indicaciones":""}}
        """;
    PedidoResponse pedido(String estado) { return new PedidoResponse("p1", null, "u1", estado, null, null, List.of(), BigDecimal.TEN); }
    @BeforeEach void configurar() {
        for (String rol : List.of("cliente", "vendedor", "administrador")) {
            when(decoder.decode(rol)).thenReturn(Jwt.withTokenValue(rol).header("alg", "HS256").subject("u1")
                .issuer("sonidovivo").claim("rol", rol).issuedAt(Instant.now()).expiresAt(Instant.now().plusSeconds(900)).build());
        }
    }
    @ParameterizedTest @CsvSource({"GET,/api/carrito", "POST,/api/carrito/items", "PUT,/api/carrito/items/QA001",
        "DELETE,/api/carrito/items/QA001", "DELETE,/api/carrito", "POST,/api/pedidos", "GET,/api/pedidos/mis-pedidos",
        "GET,/api/pedidos/p1", "GET,/api/pedidos", "PATCH,/api/pedidos/p1/estado"})
    void rutasPrivadasExigenJwt(String metodo, String ruta) throws Exception {
        mvc.perform(request(HttpMethod.valueOf(metodo), ruta)).andExpect(status().isUnauthorized());
        verifyNoInteractions(carritos, pedidos);
    }
    @Test void clienteObtieneSuCarritoDesdeSubject() throws Exception {
        when(carritos.obtenerCarrito("u1", "cliente")).thenReturn(carrito);
        mvc.perform(get("/api/carrito").header("Authorization", "Bearer cliente")).andExpect(status().isOk()).andExpect(jsonPath("$.usuarioId").value("u1"));
    }
    @ParameterizedTest @ValueSource(strings = {"vendedor", "administrador"})
    void personalNoCompraNiModificaCarrito(String rol) throws Exception {
        mvc.perform(get("/api/carrito").header("Authorization", "Bearer " + rol)).andExpect(status().isForbidden());
        mvc.perform(post("/api/pedidos").header("Authorization", "Bearer " + rol).contentType(MediaType.APPLICATION_JSON).content(compra)).andExpect(status().isForbidden());
        verifyNoInteractions(carritos, pedidos);
    }
    @Test void clienteNoListaGlobalNiGestionaEstados() throws Exception {
        mvc.perform(get("/api/pedidos").header("Authorization", "Bearer cliente")).andExpect(status().isForbidden());
        mvc.perform(patch("/api/pedidos/p1/estado").header("Authorization", "Bearer cliente").contentType(MediaType.APPLICATION_JSON).content("{\"estado\":\"enviado\"}")).andExpect(status().isForbidden());
    }
    @Test void operacionesCarritoUsanUsuarioDelJwt() throws Exception {
        when(carritos.agregarProducto(eq("u1"), any(), eq("cliente"))).thenReturn(carrito);
        when(carritos.actualizarCantidad(eq("u1"), eq("QA001"), any(), eq("cliente"))).thenReturn(carrito);
        when(carritos.eliminarProducto("u1", "QA001", "cliente")).thenReturn(carrito);
        mvc.perform(post("/api/carrito/items").header("Authorization", "Bearer cliente").contentType(MediaType.APPLICATION_JSON)
            .content("{\"codigo\":\"QA001\",\"cantidad\":2,\"usuarioId\":\"victima\"}")).andExpect(status().isOk());
        verify(carritos).agregarProducto("u1", new ItemCarritoRequest("QA001", 2), "cliente");
        mvc.perform(put("/api/carrito/items/QA001").header("Authorization", "Bearer cliente").contentType(MediaType.APPLICATION_JSON)
            .content("{\"cantidad\":3}")).andExpect(status().isOk());
        verify(carritos).actualizarCantidad("u1", "QA001", new CantidadRequest(3), "cliente");
        mvc.perform(delete("/api/carrito/items/QA001").header("Authorization", "Bearer cliente")).andExpect(status().isOk()).andExpect(jsonPath("$.id").value("c1"));
        mvc.perform(delete("/api/carrito").header("Authorization", "Bearer cliente")).andExpect(status().isNoContent()); verify(carritos).vaciarCarrito("u1");
    }
    @ParameterizedTest @ValueSource(strings = {"{}", "{\"codigo\":\"QA001\",\"cantidad\":0}", "{\"codigo\":\"QA001\",\"cantidad\":1000}"})
    void validaCantidadesAntesDeLlamarServicio(String body) throws Exception {
        mvc.perform(post("/api/carrito/items").header("Authorization", "Bearer cliente").contentType(MediaType.APPLICATION_JSON).content(body))
            .andExpect(status().isBadRequest()).andExpect(jsonPath("$.mensaje").exists()); verifyNoInteractions(carritos);
    }
    @ParameterizedTest @CsvSource({"pagado,201", "pendiente,202"})
    void checkoutDistingueConfirmadoYPendiente(String estado, int codigo) throws Exception {
        when(pedidos.crearPedido(eq("u1"), any(), eq("cliente"))).thenReturn(pedido(estado));
        mvc.perform(post("/api/pedidos").header("Authorization", "Bearer cliente").contentType(MediaType.APPLICATION_JSON).content(compra))
            .andExpect(status().is(codigo)).andExpect(jsonPath("$.estado").value(estado));
    }
    @Test void validaCompradorYLimites() throws Exception {
        for (String body : List.of("{}", compra.replace("qa@example.test", "invalido"), compra.replace("Ana", "A".repeat(51)))) {
            mvc.perform(post("/api/pedidos").header("Authorization", "Bearer cliente").contentType(MediaType.APPLICATION_JSON).content(body))
                .andExpect(status().isBadRequest());
        }
        verifyNoInteractions(pedidos);
    }
    @Test void historialYDetalleUsanJwt() throws Exception {
        when(pedidos.obtenerMisPedidos("u1")).thenReturn(List.of(pedido("pagado")));
        when(pedidos.obtenerDetalle("p1", "u1", "cliente")).thenReturn(pedido("pagado"));
        mvc.perform(get("/api/pedidos/mis-pedidos").header("Authorization", "Bearer cliente")).andExpect(status().isOk()).andExpect(jsonPath("$[0].id").value("p1"));
        mvc.perform(get("/api/pedidos/p1").header("Authorization", "Bearer cliente")).andExpect(status().isOk());
        verify(pedidos).obtenerDetalle("p1", "u1", "cliente");
    }
    @ParameterizedTest @ValueSource(strings = {"vendedor", "administrador"})
    void personalListaPedidosYCambiaEstado(String rol) throws Exception {
        when(pedidos.obtenerTodos()).thenReturn(List.of(pedido("pagado")));
        when(pedidos.actualizarEstado("p1", "enviado")).thenReturn(pedido("enviado"));
        mvc.perform(get("/api/pedidos").header("Authorization", "Bearer " + rol)).andExpect(status().isOk());
        mvc.perform(patch("/api/pedidos/p1/estado").header("Authorization", "Bearer " + rol).contentType(MediaType.APPLICATION_JSON).content("{\"estado\":\"enviado\"}"))
            .andExpect(status().isOk()).andExpect(jsonPath("$.estado").value("enviado"));
    }
    @Test void erroresDeNegocioYDatosTienenMensajeJson() throws Exception {
        when(carritos.obtenerCarrito("u1", "cliente")).thenThrow(new CarritoException(HttpStatus.CONFLICT, "Stock insuficiente"));
        mvc.perform(get("/api/carrito").header("Authorization", "Bearer cliente")).andExpect(status().isConflict()).andExpect(jsonPath("$.mensaje").value("Stock insuficiente"));
        doThrow(new DataIntegrityViolationException("detalle SQL privado")).when(carritos).vaciarCarrito("u1");
        mvc.perform(delete("/api/carrito").header("Authorization", "Bearer cliente")).andExpect(status().isConflict()).andExpect(jsonPath("$.mensaje").value("La operacion genera un conflicto de datos"));
        mvc.perform(post("/api/pedidos").header("Authorization", "Bearer cliente").contentType(MediaType.APPLICATION_JSON).content("no-json"))
            .andExpect(status().isBadRequest()).andExpect(jsonPath("$.mensaje").value("El formato de los datos no es valido"));
    }
}
