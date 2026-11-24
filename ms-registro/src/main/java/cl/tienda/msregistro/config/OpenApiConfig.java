package cl.tienda.msregistro.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityScheme;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI registroOpenAPI() {

        return new OpenAPI()

                .info(new Info()
                        .title("Sonido Vivo - API Autenticacion")
                        .version("1.0")
                        .description(
                                "Microservicio de registro, inicio de sesion y autenticacion JWT"))

                .components(new Components()
                        .addSecuritySchemes(
                                "bearerAuth",
                                new SecurityScheme()
                                        .type(SecurityScheme.Type.HTTP)
                                        .scheme("bearer")
                                        .bearerFormat("JWT")));
    }
}