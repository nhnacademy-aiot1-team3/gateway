package com.databo3.gateway.config;

import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RouteLocatorConfig {

    @Bean
    public RouteLocator myRoute(RouteLocatorBuilder builder) {
        return builder.routes()
                .route("auth-service",
                        p->p.path("/api/auth/**").and()
                                .uri("lb://auth-service")
                )
                .route("account-service",
                        p->p.path("/api/account/**").and()
                                .uri("lb://account-service")
                )
                .build();
    }
}