package live.databo3.gateway.util;

import live.databo3.gateway.properties.JwtProperties;
import io.jsonwebtoken.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import javax.crypto.spec.SecretKeySpec;
import java.security.Key;
import java.util.Date;

/**
 * jwt 관련 동작을 위한 유틸클래스
 * @author : 강경훈
 * @version : 1.0.0
 */
@Component
@RequiredArgsConstructor
public class JwtUtil {
    private final JwtProperties jwtProperties;
    private static String secret;

    /**
     * 컴포넌트가 생성될 때 jwtProperties 에서 secret 을 가져온다.
     * @since 1.0.0
     */
    @PostConstruct
    private void init() {
        secret = jwtProperties.getSecret();
    }

    /**
     * 토큰의 유효성 검사를 하는 메서드
     * 1. 토큰의 시그니처를 검증한다.
     * 2. 토큰의 만료 여부를 검증한다.
     * @param token jwt 토큰
     * @return token 의 시그니처가 유효하고, 토큰의 만료기간이 지나지 않았다면 true, 그렇지 않다면 false 를 반환한다.
     * @since 1.0.0
     */
    public static boolean isValidToken(String token) {
        try {
            Key key = new SecretKeySpec(secret.getBytes(),SignatureAlgorithm.HS256.getJcaName());
            Jws<Claims> claims = Jwts.parserBuilder()
                    .setSigningKey(key)
                    .build()
                    .parseClaimsJws(token);
            Date now = new Date();
            Date expiration = claims.getBody().getExpiration();
            return !expiration.before(now);
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * token 을 분석한 Claims 를 반환하는 메서드.
     * @param token jwt 토큰
     * @return token 을 분석한 Claims 를 반환한다.
     * @since 1.0.0
     */
    public static Claims parseClaims(String token) {
        Key key = new SecretKeySpec(secret.getBytes(),SignatureAlgorithm.HS256.getJcaName());
        Jws<Claims> claimsJws = Jwts.parserBuilder().setSigningKey(key).build().parseClaimsJws(token);
        return claimsJws.getBody();
    }
}
