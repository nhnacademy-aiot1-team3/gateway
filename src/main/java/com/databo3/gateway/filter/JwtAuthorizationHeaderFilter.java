package com.databo3.gateway.filter;

import com.databo3.gateway.util.JwtUtil;
import com.databo3.gateway.util.RouteValidator;
import io.jsonwebtoken.*;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.factory.AbstractGatewayFilterFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.server.ServerWebExchange;

import java.util.Objects;

@Component
@Slf4j
public class JwtAuthorizationHeaderFilter extends AbstractGatewayFilterFactory<JwtAuthorizationHeaderFilter.Config> {
    private final RouteValidator routeValidator;
    private final RedisTemplate<String, Object> redisTemplate;
    private static final String BLACKLIST_PREFIX = "blacklist:";


    public JwtAuthorizationHeaderFilter(RouteValidator routeValidator, RedisTemplate<String, Object> redisTemplate) {
        super(Config.class);
        this.routeValidator = routeValidator;
        this.redisTemplate = redisTemplate;
    }

    @Getter
    public static class Config {
    }

    @Override
    public GatewayFilter apply(Config config) {
        return (exchange, chain) -> {
            log.debug("jwt-validation-filter");
            ServerHttpRequest request = exchange.getRequest();

            if (routeValidator.isSecured.test(request)) {
                if (!request.getHeaders().containsKey(HttpHeaders.AUTHORIZATION)) {
                    log.debug("No Header Authorization");
                    throw new ResponseStatusException(HttpStatus.UNAUTHORIZED);
                }
                String jwtToken = Objects.requireNonNull(request.getHeaders().get(HttpHeaders.AUTHORIZATION)).get(0);

                log.debug("accessToken:{}", jwtToken);

                if (!JwtUtil.isValidToken(jwtToken)) {
                    log.debug("invalidToken: " + jwtToken);
                    throw new ResponseStatusException(HttpStatus.UNAUTHORIZED);
                }

                if (isInBlackList(jwtToken)) {
                    log.debug("user in blacklist: " + jwtToken);
                    throw new ResponseStatusException(HttpStatus.UNAUTHORIZED);
                }

                Claims claims = JwtUtil.parseClaims(jwtToken);

                updateRequest(exchange, claims);
            }
            return chain.filter(exchange);
        };
    }

    public void updateRequest(ServerWebExchange exchange, Claims claims) {
        String memberId = claims.get("memberId", String.class);
        log.debug("member_id: {}", memberId);

        exchange.mutate().request(builder -> builder.header("X-USER-ID", memberId));
    }

    public boolean isInBlackList(String accessToken) {
        return (Boolean.TRUE.equals(redisTemplate.hasKey(BLACKLIST_PREFIX + accessToken)));
    }
}

