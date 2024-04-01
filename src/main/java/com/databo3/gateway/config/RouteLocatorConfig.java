package com.databo3.gateway.config;

import com.databo3.gateway.filter.JwtAuthorizationHeaderFilter;
import lombok.RequiredArgsConstructor;
import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@RequiredArgsConstructor
public class RouteLocatorConfig {
    private final JwtAuthorizationHeaderFilter jwtAuthorizationHeaderFilter;

    @Bean
    public RouteLocator myRoute(RouteLocatorBuilder builder) {
        return builder.routes()
                .route("auth-service",
                        p->p.path("/api/auth/**")
                                .filters(f -> f.filter(jwtAuthorizationHeaderFilter.apply(new JwtAuthorizationHeaderFilter.Config())))
                                .uri("lb://auth-service")
                )
                .route("account-service",
                        p->p.path("/api/account/**")
                                .filters(f -> f.filter(jwtAuthorizationHeaderFilter.apply(new JwtAuthorizationHeaderFilter.Config())))
                                .uri("lb://account-service")
                )
                .build();
    }
}