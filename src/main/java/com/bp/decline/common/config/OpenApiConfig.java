package com.bp.decline.common.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    OpenAPI openAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("Well Production Decline API")
                        .description("""
                                REST API for Arps Decline Curve Analysis of oil & gas wells.
                                Supports exponential, hyperbolic, and harmonic decline models
                                to forecast production rates and compute Estimated Ultimate Recovery (EUR).
                                
                                Based on Arps (1945): q(t) = qi / (1 + b·Di·t)^(1/b)
                                """)
                        .version("v1.0.0")
                        .contact(new Contact()
                                .name("IIT ISM Dhanbad — Petroleum Engineering")
                                .email("pe-decline-api@iitism.ac.in")));
    }
}
