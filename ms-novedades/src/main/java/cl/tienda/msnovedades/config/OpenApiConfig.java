package cl.tienda.msnovedades.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.security.SecurityRequirement;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI novedadesOpenAPI() {

        return new OpenAPI()
                .info(new Info()
                        .title("Sonido Vivo - Microservicio Novedades")
                        .version("1.0.0")
                        .description(
                            "API REST para la gestión de novedades y productos destacados"
                        ))
                .components(new Components()
                        .addSecuritySchemes("bearerAuth",
                            new SecurityScheme()
                                .type(SecurityScheme.Type.HTTP)
                                .scheme("bearer")
                                .bearerFormat("JWT")))
                .addSecurityItem(
                    new SecurityRequirement().addList("bearerAuth")
                );
    }
}
