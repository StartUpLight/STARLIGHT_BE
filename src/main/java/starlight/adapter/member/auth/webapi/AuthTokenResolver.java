package starlight.adapter.member.auth.webapi;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class AuthTokenResolver {

    private final String authHeaderName;
    private final String bearerPrefix;

    public AuthTokenResolver(@Value("${jwt.header}") String authHeaderName,
                             @Value("${jwt.prefix:Bearer}") String prefix) {
        this.authHeaderName = authHeaderName;
        this.bearerPrefix = normalizePrefix(prefix);
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
        if (trimmed.toLowerCase().startsWith(bearerPrefix)) {
            String token = trimmed.substring(bearerPrefix.length()).trim();
            return token.isEmpty() ? null : token;
        }
        return trimmed.isEmpty() ? null : trimmed;
    }

    private String normalizePrefix(String prefix) {
        if (prefix == null || prefix.isBlank()) {
            return "bearer ";
        }
        String normalized = prefix.trim().toLowerCase();
        return normalized.endsWith(" ") ? normalized : normalized + " ";
    }
}
