package com.databo3.gateway.util;

import com.databo3.gateway.properties.JwtProperties;
import io.jsonwebtoken.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.util.Date;

@Component
@RequiredArgsConstructor
public class JwtUtil {
    private final JwtProperties jwtProperties;
    private static String secret;

    @PostConstruct
    private void init() {
        secret = jwtProperties.getSecret();
    }

    public static boolean isValidToken(String token) {
        try {
            Jws<Claims> claims = Jwts.parserBuilder()
                    .setSigningKey(secret)
                    .build()
                    .parseClaimsJws(token);
            Date now = new Date();
            Date expiration = claims.getBody().getExpiration();
            return !expiration.before(now);
        } catch (Exception e) {
            return false;
        }
    }

    public static Claims parseClaims(String token) {
        Jws<Claims> claimsJws = Jwts.parserBuilder().setSigningKey(secret).build().parseClaimsJws(token);
        return claimsJws.getBody();
    }
}
