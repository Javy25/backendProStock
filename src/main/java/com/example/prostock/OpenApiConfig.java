package com.example.prostock;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springdoc.core.models.GroupedOpenApi;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/** Configura la documentación OpenAPI de las rutas versionadas del backend. */
@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI prostockOpenApi() {
        return new OpenAPI()
                .info(new Info()
                        .title("ProStock API")
                        .version("v1")
                        .description("API REST para catálogo, clientes, carritos, compras y mensajes de ProStock. "
                                + "Las operaciones privadas aceptan tokens propios como Bearer JWT."))
                .components(new Components().addSecuritySchemes("BearerAuth",
                        new SecurityScheme()
                                .type(SecurityScheme.Type.HTTP)
                                .scheme("bearer")
                                .bearerFormat("JWT")));
    }

    @Bean
    public GroupedOpenApi apiV1() {
        return GroupedOpenApi.builder()
                .group("v1")
                .pathsToMatch("/api/v1/**")
                .build();
    }
}
