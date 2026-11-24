package cl.tienda.mscarrito.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityScheme;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI carritoOpenAPI() {

        return new OpenAPI()

                .info(new Info()
                        .title("Sonido Vivo - API Carrito y Pedidos")
                        .description(
                                "Gestion de carritos y pedidos "
                                + "mediante autenticacion JWT")
                        .version("1.0.0"))

                .components(new Components()
                        .addSecuritySchemes(
                                "bearerAuth",
                                new SecurityScheme()
                                        .type(
                                                SecurityScheme.Type.HTTP)
                                        .scheme("bearer")
                                        .bearerFormat("JWT")));
    }
}