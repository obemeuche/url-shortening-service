package com.roadmap.urlshorteningservice.config;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.info.Info;
import org.springframework.context.annotation.Configuration;

@OpenAPIDefinition(
        info = @Info(
                title = "URL Shortening Service API",
                description = "API for creating, retrieving, updating, deleting, and tracking shortened URLs",
                version = "1.0.0"
        )
)
@Configuration
public class OpenApiConfig {
}
