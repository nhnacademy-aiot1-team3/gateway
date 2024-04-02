package live.databo3.gateway.config;

import live.databo3.gateway.filter.JwtAuthorizationHeaderFilter;
import lombok.RequiredArgsConstructor;
import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * path 별로 route 와 라운드로빈을 해주는 클래스
 * @author : 강경훈
 * @version : 1.0.0
 */
@Configuration
@RequiredArgsConstructor
public class RouteLocatorConfig {
    /**
     * 인가를 위해 토큰을 검증하는 필터
     * @since 1.0.0
     */
    private final JwtAuthorizationHeaderFilter jwtAuthorizationHeaderFilter;

    /**
     * path 에 맞게 요청을 알맞은 서버로 route 및 라운드로빈 해주는 메서드, 조건에 따라 특정 필터를 적용시킬 수 있다.
     * @param builder RouteLocator 를 구성하는데 필요한 빌더 파라미터
     * @return build 완료된 RouteLocator
     */
    @Bean
    public RouteLocator myRoute(RouteLocatorBuilder builder) {
        return builder.routes()
                .route("auth-service",
                        p->p.path("/auth/**")
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