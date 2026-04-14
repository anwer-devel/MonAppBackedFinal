package com.app.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * OpenAPI 3.0 (Swagger) Configuration
 *
 * Accessible at: http://localhost:8080/swagger-ui.html
 * OpenAPI JSON: http://localhost:8080/v3/api-docs
 * OpenAPI YAML: http://localhost:8080/v3/api-docs.yaml
 */
@Configuration
public class SwaggerConfig {

    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("Monolith App API")
                        .version("1.0.0")
                        .description("Production-Grade Modular Monolith API Documentation\n\n" +
                                "Complete REST API with JWT Authentication, PostgreSQL, Redis, and Clean Architecture.\n\n" +
                                "**Features:**\n" +
                                "- Zone Management (Admin Only)\n" +
                                "- Partner Management (CRUD)\n" +
                                "- Location Management\n" +
                                "- Asset Upload & Management\n" +
                                "- JWT Authentication with Refresh Tokens\n" +
                                "- Role-Based Access Control\n" +
                                "- Redis Caching\n" +
                                "- Soft Delete Support\n\n" +
                                "**Default Credentials (Dev Profile):**\n" +
                                "- Admin: `admin@example.com` / `AdminPassword123`\n" +
                                "- Partner: `partner@example.com` / `PartnerPassword123`\n" +
                                "- User: `user@example.com` / `UserPassword123`")
                        .contact(new Contact()
                                .name("Development Team")
                                .email("dev@example.com")
                                .url("https://example.com"))
                        .license(new License()
                                .name("MIT License")
                                .url("https://opensource.org/licenses/MIT")))
                .addSecurityItem(new SecurityRequirement().addList("BearerAuth"))
                .components(new io.swagger.v3.oas.models.Components()
                        .addSecuritySchemes("BearerAuth", new SecurityScheme()
                                .type(SecurityScheme.Type.HTTP)
                                .scheme("bearer")
                                .bearerFormat("JWT")
                                .description("JWT Access Token")));
    }
}

