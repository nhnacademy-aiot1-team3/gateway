package live.databo3.gateway.filter;

import live.databo3.gateway.util.JwtUtil;
import live.databo3.gateway.util.RouteValidator;
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

/**
 * jwt 토큰을 검증하는 필터
 * @author : 강경훈
 * @version : 1.0.0
 */
@Component
@Slf4j
public class JwtAuthorizationHeaderFilter extends AbstractGatewayFilterFactory<JwtAuthorizationHeaderFilter.Config> {
    private final RouteValidator routeValidator;
    private final RedisTemplate<String, Object> redisTemplate;
    private static final String BLACKLIST_PREFIX = "blacklist:";


    /**
     * Config 와 컴포넌트들을 주입받기 위한 생성자
     * @param routeValidator routeValidator 컴포넌트, 인가가 필요한 경로인지 검사하는 역할을 한다.
     * @param redisTemplate redis 서버와 통신하기 위한 빈
     * @since 1.0.0
     */
    public JwtAuthorizationHeaderFilter(RouteValidator routeValidator, RedisTemplate<String, Object> redisTemplate) {
        super(Config.class);
        this.routeValidator = routeValidator;
        this.redisTemplate = redisTemplate;
    }

    @Getter
    public static class Config {
    }

    /**
     * jwt 토큰을 검증하는 아래의 로직들을 포함한다.
     * 1. routeValidator 가 인가가 필요한 경로인지 검사한다.
     * 2. 토큰의 유효성 검사를 실시한다.(토큰 시그니처 검증, 토큰 만료 검증)
     * 3. 토큰이 로그아웃 이력이 있는 토큰인지 검증한다.
     * 위 검증들 중 하나라도 통과하지 못한다면 UNAUTHORIZED 오류를 반환한다.
     *
     * @param config 설정을 위한 파라미터
     * @throws ResponseStatusException 요청이 원래 왔던 곳으로 error code 를 포함하여 응답한다.
     * @return exception 이 발생하지 않는다면 다음 필터로 위임한다.
     * @since 1.0.0
     */
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

    /**
     * Authorization 헤더를 분석하여 member_id를 추출해 X-USER-ID 헤더를 추가하는 메서드.
     * @param exchange ServerWebExchange
     * @param claims jwt 토큰의 내용
     * @since 1.0.0
     */
    public void updateRequest(ServerWebExchange exchange, Claims claims) {
        String memberId = claims.get("memberId", String.class);
        log.debug("member_id: {}", memberId);

        exchange.mutate().request(builder -> builder.header("X-USER-ID", memberId));
    }

    /**
     * redis 서버에서 블랙리스트를 탐색하여 로그아웃 이력이 있는지 검사하는 메서드.
     * @param accessToken jwt 토큰
     * @return accessToken 이 블랙리스트에 등록되어있다면 true, 그렇지 않다면 false 를 반환한다.
     * @since 1.0.0
     */
    public boolean isInBlackList(String accessToken) {
        return (Boolean.TRUE.equals(redisTemplate.hasKey(BLACKLIST_PREFIX + accessToken)));
    }
}

