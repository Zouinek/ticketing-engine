package com.ticketmaster.event.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.Contact;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Swagger/OpenAPI Configuration
 * Access at: http://localhost:8082/swagger-ui/index.html
 */
@Configuration
public class SwaggerConfig {

    @Bean
    public OpenAPI eventServiceOpenAPI() {
        return new OpenAPI()
            .info(new Info()
                .title("Event Service API")
                .description("Manages events, concerts, and ticket availability")
                .version("1.0.0")
                .contact(new Contact()
                    .name("Ticketing Engine Team")
                    .email("supportv1@ticketmaster.com")));
    }
}
