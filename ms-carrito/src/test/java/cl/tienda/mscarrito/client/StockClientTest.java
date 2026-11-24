package cl.tienda.mscarrito.client;

import cl.tienda.mscarrito.dto.*;
import java.net.*;
import java.util.List;
import org.junit.jupiter.api.*;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.*;
import org.springframework.http.*;
import org.springframework.http.MediaType;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestClient;
import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.*;
import static org.springframework.test.web.client.response.MockRestResponseCreators.*;

class StockClientTest {
    StockClient cliente;
    MockRestServiceServer servidor;
    final StockDescuentoRequest solicitud = new StockDescuentoRequest("550e8400-e29b-41d4-a716-446655440000", List.of(new StockItemRequest("QA001", 2)));
    @BeforeEach void configurar() {
        cliente = new StockClient("http://productos.test", "clave-unitaria");
        var builder = RestClient.builder().baseUrl("http://productos.test");
        servidor = MockRestServiceServer.bindTo(builder).build();
        ReflectionTestUtils.setField(cliente, "restClient", builder.build());
    }
    @AfterEach void verificar() { servidor.verify(); }
    @Test void enviaUuidItemsYClaveInterna() {
        servidor.expect(requestTo("http://productos.test/api/productos/stock/descontar"))
            .andExpect(method(HttpMethod.POST)).andExpect(header("X-Internal-Service-Key", "clave-unitaria"))
            .andExpect(content().json("{\"pedidoId\":\"" + solicitud.pedidoId() + "\",\"items\":[{\"codigo\":\"QA001\",\"cantidad\":2}]}"))
            .andRespond(withSuccess("{\"pedidoId\":\"" + solicitud.pedidoId() + "\",\"estado\":\"DESCONTADO\"}", MediaType.APPLICATION_JSON));
        assertEquals(StockClient.Resultado.DESCONTADO, cliente.descontar(solicitud));
    }
    @ParameterizedTest @CsvSource({"STOCK_INSUFICIENTE,STOCK_INSUFICIENTE", "CONFLICTO_IDEMPOTENCIA,CONFLICTO_IDEMPOTENCIA", "OTRO,NO_CONFIRMADO"})
    void distingueConflictosSinAsumirExito(String codigo, StockClient.Resultado esperado) {
        servidor.expect(requestTo("http://productos.test/api/productos/stock/descontar"))
            .andRespond(withStatus(HttpStatus.CONFLICT).contentType(MediaType.APPLICATION_JSON).body("{\"codigo\":\"" + codigo + "\"}"));
        assertEquals(esperado, cliente.descontar(solicitud));
    }
    @ParameterizedTest @ValueSource(strings = {"null", "no-json", "{}"})
    void conflictoSinContratoEsIncierto(String cuerpo) {
        servidor.expect(requestTo("http://productos.test/api/productos/stock/descontar"))
            .andRespond(withStatus(HttpStatus.CONFLICT).contentType(MediaType.APPLICATION_JSON).body(cuerpo));
        assertEquals(StockClient.Resultado.NO_CONFIRMADO, cliente.descontar(solicitud));
    }
    @ParameterizedTest @ValueSource(strings = {"null", "{}", "{\"pedidoId\":\"otro\",\"estado\":\"DESCONTADO\"}",
        "{\"pedidoId\":\"550e8400-e29b-41d4-a716-446655440000\",\"estado\":\"PENDIENTE\"}"})
    void respuestaInvalidaNoConfirmaStock(String cuerpo) {
        servidor.expect(requestTo("http://productos.test/api/productos/stock/descontar")).andRespond(withSuccess(cuerpo, MediaType.APPLICATION_JSON));
        assertEquals(StockClient.Resultado.NO_CONFIRMADO, cliente.descontar(solicitud));
    }
    @Test void errorServidorNoCancelaPedido() {
        servidor.expect(requestTo("http://productos.test/api/productos/stock/descontar")).andRespond(withServerError());
        assertEquals(StockClient.Resultado.NO_CONFIRMADO, cliente.descontar(solicitud));
    }
    @Test void timeoutNoConfirmaNiCancela() {
        servidor.expect(requestTo("http://productos.test/api/productos/stock/descontar")).andRespond(withException(new SocketTimeoutException("Timeout simulado")));
        assertEquals(StockClient.Resultado.NO_CONFIRMADO, cliente.descontar(solicitud));
    }
    @Test void conexionCaidaNoConfirmaNiCancela() {
        servidor.expect(requestTo("http://productos.test/api/productos/stock/descontar")).andRespond(withException(new ConnectException("Conexión simulada")));
        assertEquals(StockClient.Resultado.NO_CONFIRMADO, cliente.descontar(solicitud));
    }
    @ParameterizedTest @NullAndEmptySource @ValueSource(strings = {" "})
    void sinClaveNoEnviaPeticiones(String clave) {
        assertEquals(StockClient.Resultado.NO_CONFIRMADO, new StockClient("http://productos.test", clave).descontar(solicitud));
    }
}
