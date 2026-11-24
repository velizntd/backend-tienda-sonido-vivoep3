package cl.tienda.mscarrito.client;

import java.time.Duration;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientResponseException;
import org.springframework.web.client.RestClientException;

import cl.tienda.mscarrito.dto.ProductoResponse;
import cl.tienda.mscarrito.exception.CarritoException;

@Component
public class ProductoClient {

    private final RestClient restClient;

    public ProductoClient(
            @Value("${app.productos.url}") String url) {

        SimpleClientHttpRequestFactory factory =
                new SimpleClientHttpRequestFactory();

        factory.setConnectTimeout(Duration.ofSeconds(3));
        factory.setReadTimeout(Duration.ofSeconds(5));

        this.restClient = RestClient.builder()
                .baseUrl(url)
                .requestFactory(factory)
                .build();
    }

    public ProductoResponse obtenerProducto(
            String codigo,
            String token) {

        try {

            ProductoResponse producto = restClient.get()
                    .uri("/api/productos/{codigo}", codigo)
                    .headers(headers ->
                            headers.setBearerAuth(token))
                    .retrieve()
                    .body(ProductoResponse.class);

            if (producto == null
                    || producto.codigo() == null
                    || producto.nombre() == null
                    || producto.precio() == null
                    || producto.stock() == null
                    || producto.stock() < 0
                    || producto.precio().signum() < 0) {

                throw new CarritoException(
                        HttpStatus.BAD_GATEWAY,
                        "Respuesta invalida de productos");
            }

            return producto;

        } catch (RestClientResponseException ex) {

            if (ex.getStatusCode().value() == 404) {
                throw new CarritoException(
                        HttpStatus.NOT_FOUND,
                        "Producto no encontrado: " + codigo);
            }

            if (ex.getStatusCode().value() == 401) {
                throw new CarritoException(
                        HttpStatus.UNAUTHORIZED,
                        "La sesion no es valida");
            }

            if (ex.getStatusCode().value() == 403) {
                throw new CarritoException(
                        HttpStatus.FORBIDDEN,
                        "No tienes permisos para consultar productos");
            }

            throw new CarritoException(
                    HttpStatus.BAD_GATEWAY,
                    "No fue posible consultar el producto");

        } catch (RestClientException ex) {

            throw new CarritoException(
                    HttpStatus.SERVICE_UNAVAILABLE,
                    "El servicio de productos no esta disponible");
        }
    }
}