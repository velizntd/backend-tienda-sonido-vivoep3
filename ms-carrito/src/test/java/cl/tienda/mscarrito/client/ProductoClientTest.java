package cl.tienda.mscarrito.client;

import cl.tienda.mscarrito.exception.CarritoException;
import java.math.BigDecimal;
import java.net.SocketTimeoutException;
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

class ProductoClientTest {
    ProductoClient cliente;
    MockRestServiceServer servidor;
    @BeforeEach void configurar() {
        cliente = new ProductoClient("http://productos.test");
        var builder = RestClient.builder().baseUrl("http://productos.test");
        servidor = MockRestServiceServer.bindTo(builder).build();
        ReflectionTestUtils.setField(cliente, "restClient", builder.build());
    }
    @AfterEach void verificar() { servidor.verify(); }
    @Test void consultaProductoConBearer() {
        servidor.expect(requestTo("http://productos.test/api/productos/QA001"))
            .andExpect(method(HttpMethod.GET)).andExpect(header("Authorization", "Bearer jwt"))
            .andRespond(withSuccess("{\"codigo\":\"QA001\",\"nombre\":\"Guitarra\",\"precio\":129990,\"stock\":5}", MediaType.APPLICATION_JSON));
        var p = cliente.obtenerProducto("QA001", "jwt");
        assertEquals(new BigDecimal("129990"), p.precio()); assertEquals(5, p.stock());
    }
    @ParameterizedTest @CsvSource({"404,404", "401,401", "403,403", "500,502"})
    void traduceErroresHttp(int remoto, int esperado) {
        servidor.expect(requestTo("http://productos.test/api/productos/QA001")).andRespond(withStatus(HttpStatus.valueOf(remoto)));
        assertEquals(esperado, assertThrows(CarritoException.class, () -> cliente.obtenerProducto("QA001", "jwt")).getEstado().value());
    }
    @ParameterizedTest @ValueSource(strings = {
        "null", "{}", "{\"codigo\":\"A\"}", "{\"codigo\":\"A\",\"nombre\":\"A\"}",
        "{\"codigo\":\"A\",\"nombre\":\"A\",\"precio\":1}",
        "{\"codigo\":\"A\",\"nombre\":\"A\",\"precio\":1,\"stock\":-1}",
        "{\"codigo\":\"A\",\"nombre\":\"A\",\"precio\":-1,\"stock\":1}"})
    void rechazaDatosIncompletosONegativos(String json) {
        servidor.expect(requestTo("http://productos.test/api/productos/QA001")).andRespond(withSuccess(json, MediaType.APPLICATION_JSON));
        assertEquals(HttpStatus.BAD_GATEWAY, assertThrows(CarritoException.class, () -> cliente.obtenerProducto("QA001", "jwt")).getEstado());
    }
    @Test void convierteTimeoutEn503() {
        servidor.expect(requestTo("http://productos.test/api/productos/QA001")).andRespond(withException(new SocketTimeoutException("Timeout simulado")));
        assertEquals(HttpStatus.SERVICE_UNAVAILABLE, assertThrows(CarritoException.class, () -> cliente.obtenerProducto("QA001", "jwt")).getEstado());
    }
}
