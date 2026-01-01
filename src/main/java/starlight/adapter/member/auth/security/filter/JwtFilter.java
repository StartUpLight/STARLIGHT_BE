package starlight.adapter.member.auth.security.filter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;
import starlight.adapter.member.auth.security.auth.AuthDetailsService;
import starlight.adapter.member.auth.webapi.AuthTokenResolver;
import starlight.application.member.auth.required.KeyValueMap;
import starlight.application.member.auth.required.TokenProvider;

import java.io.IOException;

@Slf4j
@Component
@RequiredArgsConstructor
public class JwtFilter extends OncePerRequestFilter {

    private final TokenProvider tokenProvider;
    private final KeyValueMap redisClient;
    private final AuthDetailsService authDetailsService;
    private final AuthTokenResolver tokenResolver;

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        String token = tokenResolver.resolveAccessToken(request);
        boolean hasToken = StringUtils.hasText(token);
        String redisValue = hasToken ? redisClient.getValue(token) : null;
        boolean isBlacklisted = hasToken && redisValue != null;
        boolean isValid = hasToken && tokenProvider.validateToken(token);

        if (hasToken && !isBlacklisted && isValid) {
            String email = tokenProvider.getEmail(token);
            UserDetails userDetails = authDetailsService.loadUserByUsername(email);

            if (userDetails != null) {
                UsernamePasswordAuthenticationToken authentication =
                        new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
                SecurityContextHolder.getContext().setAuthentication(authentication);
            }
        }
        filterChain.doFilter(request, response);
    }
}
