package live.databo3.gateway.filter;

import live.databo3.gateway.util.RouteValidator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.HttpHeaders;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.test.web.reactive.server.WebTestClient;

import java.util.function.Predicate;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class JwtAuthorizationHeaderFilterTest {

    @Autowired
    private WebTestClient webTestClient;

    @MockBean
    private RouteValidator routeValidator;

    @MockBean
    private RedisTemplate<String, Object> redisTemplate;

    @MockBean
    private Predicate<ServerHttpRequest> isSecuredPredicate;

    @BeforeEach
    public void setUp() {
        routeValidator.isSecured = isSecuredPredicate;
        when(isSecuredPredicate.test(any())).thenReturn(true);
    }
    @Test
    void testUnAuthorizedRequest() {
        webTestClient.get()
                .uri("/api/account/needAuthorization")
                .exchange()
                .expectStatus().isUnauthorized();
    }

    @Test
    void testInvalidTokenRequest() {
        String invalidToken = "invalidToken";

        webTestClient.get()
                .uri("/api/account/needAuthorization")
                .header(HttpHeaders.AUTHORIZATION, invalidToken)
                .exchange()
                .expectStatus().isUnauthorized();
    }

    @Test
    void testTokenInBlackListRequest() {
        String validToken = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiIxMjM0NTY3ODkwIiwibmFtZSI6IkpvaG4gRG9lIiwiZXhwIjo1MDE2MjM5MDIyfQ.Mgyr4d8tKSVNTfiRCjfLGD8fQGMRF_hamYuZ-XopoYg";
        when(redisTemplate.hasKey(anyString())).thenReturn(true);

        webTestClient.get()
                .uri("/api/account/needAuthorization")
                .header(HttpHeaders.AUTHORIZATION, validToken)
                .exchange()
                .expectStatus().isUnauthorized();
    }

    @Test
    void testExchangeSuccess() {
        String validToken = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiIxMjM0NTY3ODkwIiwibmFtZSI6IkpvaG4gRG9lIiwiZXhwIjo1MDE2MjM5MDIyLCJtZW1iZXJJZCI6InVzZXIifQ.gOC0DPGlKsxXFMDvxBsj1paULvdfAKj-8IqmDK5n_w0";
        when(redisTemplate.hasKey(anyString())).thenReturn(false);

        webTestClient.get()
                .uri("/api/account/needAuthorization")
                .header(HttpHeaders.AUTHORIZATION, validToken)
                .exchange()
                .expectStatus().is5xxServerError();
    }
}