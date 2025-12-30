package starlight.adapter.member.auth.webapi;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class AuthTokenResolver {

    private static final String BEARER_PREFIX = "Bearer ";

    private final String authHeaderName;

    public AuthTokenResolver(@Value("${jwt.header}") String authHeaderName) {
        this.authHeaderName = authHeaderName;
    }

    public String resolveAccessToken(HttpServletRequest request) {
        return extractToken(request.getHeader(authHeaderName));
    }

    public String resolveRefreshToken(HttpServletRequest request) {
        return extractToken(request.getHeader(authHeaderName));
    }

    private String extractToken(String raw) {
        if (raw == null) {
            return null;
        }
        String trimmed = raw.trim();
        if (trimmed.startsWith(BEARER_PREFIX)) {
            String token = trimmed.substring(BEARER_PREFIX.length()).trim();
            return token.isEmpty() ? null : token;
        }
        return trimmed.isEmpty() ? null : trimmed;
    }
}
