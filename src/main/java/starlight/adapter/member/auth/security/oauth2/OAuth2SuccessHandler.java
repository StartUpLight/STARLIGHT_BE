package starlight.adapter.member.auth.security.oauth2;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;
import starlight.adapter.member.auth.security.auth.AuthDetails;
import starlight.adapter.member.auth.security.jwt.dto.TokenResponse;
import starlight.application.member.auth.required.KeyValueMap;
import starlight.application.member.auth.required.TokenProvider;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

@Slf4j
@Component
@RequiredArgsConstructor
public class OAuth2SuccessHandler implements AuthenticationSuccessHandler {

    private final TokenProvider tokenProvider;
    private final KeyValueMap redisClient;

    @Value("${app.oauth2.success-redirect:/}")
    private String successRedirectBase;

    @Value("${jwt.token.refresh-expiration-time}")
    private Long refreshTokenExpirationTime;

    @Override
    public void onAuthenticationSuccess(HttpServletRequest req, HttpServletResponse res, Authentication auth) throws IOException {
        AuthDetails principal = (AuthDetails) auth.getPrincipal();

        TokenResponse tokens = tokenProvider.createToken(principal.getUser());

        String access = tokens.accessToken();
        String refresh = tokens.refreshToken();

        String redirect = successRedirectBase
                + "?access="  + URLEncoder.encode(access, StandardCharsets.UTF_8)
                + "&refresh=" + URLEncoder.encode(refresh, StandardCharsets.UTF_8);

        redisClient.setValue(principal.member().getEmail(), tokens.refreshToken(), refreshTokenExpirationTime);

        res.sendRedirect(redirect);
    }
}
