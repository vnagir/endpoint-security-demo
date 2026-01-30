package com.endpoint.security.api.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    private static final String API_KEY = "apiKey";
    private static final String BEARER = "bearerAuth";

    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
            .info(new Info()
                .title("Endpoint Security Analytics API")
                .version("1.0")
                .description("Read-only API for event summaries and alerts. Requires Analyst or Admin role (API key / Bearer token)."))
            .addSecurityItem(new SecurityRequirement().addList(API_KEY))
            .addSecurityItem(new SecurityRequirement().addList(BEARER))
            .components(new Components()
                .addSecuritySchemes(API_KEY,
                    new SecurityScheme()
                        .type(SecurityScheme.Type.APIKEY)
                        .in(SecurityScheme.In.HEADER)
                        .name("X-API-Key")
                        .description("API key (e.g. analyst-token-67890 or admin-token-12345)"))
                .addSecuritySchemes(BEARER,
                    new SecurityScheme()
                        .type(SecurityScheme.Type.HTTP)
                        .scheme("bearer")
                        .bearerFormat("token")
                        .description("Bearer token (same value as X-API-Key)")));
    }
}
