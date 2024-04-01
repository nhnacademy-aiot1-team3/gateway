package com.databo3.gateway.util;

import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.function.Predicate;

@Component
public class RouteValidator {
    public static final List<String> needNotTokenEndPoints = List.of(
            "/api/auth/login",
            "/api/account/member/register"
    );

    public Predicate<ServerHttpRequest> isSecured =
            request -> needNotTokenEndPoints
                    .stream()
                    .noneMatch(uri -> request.getURI().getPath().contains(uri));
}
