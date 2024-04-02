package live.databo3.gateway.properties;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * jwt secret 을 불러오기 위한 properties 클래스
 * @author : 강경훈
 * @version : 1.0.0
 */
@Setter
@Getter
@ConfigurationProperties(prefix="jwt")
public class JwtProperties {
    private String secret;
}
