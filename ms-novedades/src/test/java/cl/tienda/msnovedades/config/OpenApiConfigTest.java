package cl.tienda.msnovedades.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.security.SecurityScheme;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class OpenApiConfigTest {

    @Test
    void verificarInformacionApi() {

        OpenApiConfig config = new OpenApiConfig();

        OpenAPI api = config.novedadesOpenAPI();

        assertEquals(
                "Sonido Vivo - Microservicio Novedades",
                api.getInfo().getTitle()
        );

        assertEquals("1.0.0", api.getInfo().getVersion());
    }

    @Test
    void verificarConfiguracionJwt() {

        OpenApiConfig config = new OpenApiConfig();

        OpenAPI api = config.novedadesOpenAPI();

        SecurityScheme seguridad = api.getComponents()
                .getSecuritySchemes()
                .get("bearerAuth");

        assertNotNull(seguridad);
        assertEquals(SecurityScheme.Type.HTTP, seguridad.getType());
        assertEquals("bearer", seguridad.getScheme());
        assertEquals("JWT", seguridad.getBearerFormat());

        assertTrue(
                api.getSecurity().get(0).containsKey("bearerAuth")
        );
    }
}