package live.databo3.gateway.util;

import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.function.Predicate;

/**
 * 인가가 필요한 경로인지 검사하는 역할을 하는 클래스.
 * @author : 강경훈
 * @version : 1.0.0
 */
@Component
public class RouteValidator {
    /**
     * 인가가 필요하지 않은 경로들의 목록
     * @since 1.0.0
     */
    public static final List<String> NEED_NOT_TOKEN_ENDPOINTS = List.of(
            "/api/auth/login",
            "/api/account/member/register"
    );

    /**
     * NEED_NOT_TOKEN_ENDPOINTS 와 request 의 uri 를 대조하여 인가가 필요한지 여부를 반환한다.
     * @since 1.0.0
     */
    public Predicate<ServerHttpRequest> isSecured =
            request -> NEED_NOT_TOKEN_ENDPOINTS
                    .stream()
                    .noneMatch(uri -> request.getURI().getPath().contains(uri));
}
