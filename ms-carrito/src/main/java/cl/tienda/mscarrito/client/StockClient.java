package cl.tienda.mscarrito.client;

import java.time.Duration;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestClientResponseException;

import cl.tienda.mscarrito.dto.StockDescuentoRequest;
import cl.tienda.mscarrito.dto.StockDescuentoResponse;
import cl.tienda.mscarrito.dto.StockErrorResponse;

@Component
public class StockClient {

    public enum Resultado {

        DESCONTADO,
        STOCK_INSUFICIENTE,
        CONFLICTO_IDEMPOTENCIA,
        NO_CONFIRMADO

    }

    private final RestClient restClient;

    private final String claveInterna;

    public StockClient(

            @Value("${app.productos.internal-url}")
            String url,

            @Value("${app.internal-service-key}")
            String claveInterna) {

        this.claveInterna = claveInterna;

        SimpleClientHttpRequestFactory factory =
                new SimpleClientHttpRequestFactory();

        factory.setConnectTimeout(Duration.ofSeconds(3));

        factory.setReadTimeout(Duration.ofSeconds(10));

        this.restClient = RestClient.builder()
                .baseUrl(url)
                .requestFactory(factory)
                .build();
    }

    public Resultado descontar(
            StockDescuentoRequest request) {

        if (claveInterna == null
                || claveInterna.isBlank()) {

            return Resultado.NO_CONFIRMADO;
        }

        try {

            StockDescuentoResponse respuesta =
                    restClient.post()

                    .uri("/api/productos/stock/descontar")

                    .header(
                            "X-Internal-Service-Key",
                            claveInterna)

                    .contentType(MediaType.APPLICATION_JSON)

                    .body(request)

                    .retrieve()

                    .body(StockDescuentoResponse.class);

            if (respuesta != null

                    && request.pedidoId()
                            .equals(respuesta.pedidoId())

                    && "DESCONTADO"
                            .equals(respuesta.estado())) {

                return Resultado.DESCONTADO;
            }

            return Resultado.NO_CONFIRMADO;

        } catch (RestClientResponseException ex) {

            if (ex.getStatusCode().value() == 409) {

                try {

                    StockErrorResponse error =
                            ex.getResponseBodyAs(
                                    StockErrorResponse.class);

                    if (error != null) {

                        if ("STOCK_INSUFICIENTE"
                                .equals(error.codigo())) {

                            return Resultado.STOCK_INSUFICIENTE;
                        }

                        if ("CONFLICTO_IDEMPOTENCIA"
                                .equals(error.codigo())) {

                            return Resultado.CONFLICTO_IDEMPOTENCIA;
                        }
                    }

                } catch (RuntimeException errorJson) {

                    return Resultado.NO_CONFIRMADO;
                }
            }

            return Resultado.NO_CONFIRMADO;

        } catch (RestClientException ex) {

            return Resultado.NO_CONFIRMADO;
        }
    }
}