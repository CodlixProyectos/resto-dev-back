package resto_dev.shared.common;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

/**
 * OpenAPI / Swagger configuration.
 * Accessible at: /swagger-ui/index.html
 * JSON spec at: /v3/api-docs
 */
@Configuration
public class OpenApiConfig {

    private static final String SECURITY_SCHEME_NAME = "Bearer JWT";

    @Bean
    public OpenAPI restoDevOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("Resto Dev API")
                        .description("API para el SaaS multi-tenant de restaurantes. "
                                + "Autenticación vía JWT Bearer token.")
                        .version("v1.0.0")
                        .contact(new Contact()
                                .name("Codlix Team")
                                .email("dev@codlix.com"))
                        .license(new License()
                                .name("Privada")
                                .url("https://codlix.com")))
                .servers(List.of(
                        new Server().url("http://localhost:8080").description("Local")))
                .addSecurityItem(new SecurityRequirement()
                        .addList(SECURITY_SCHEME_NAME))
                .components(new Components()
                        .addSecuritySchemes(SECURITY_SCHEME_NAME,
                                new SecurityScheme()
                                        .name(SECURITY_SCHEME_NAME)
                                        .type(SecurityScheme.Type.HTTP)
                                        .scheme("bearer")
                                        .bearerFormat("JWT")
                                        .description("Ingresa tu JWT token (sin el prefijo 'Bearer ')")));
    }
}
