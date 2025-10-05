package starlight.adapter.auth.security.oauth2;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;
import starlight.adapter.auth.security.auth.AuthDetails;
import starlight.adapter.auth.security.jwt.JwtTokenProvider;
import starlight.adapter.auth.security.jwt.dto.TokenResponse;
import starlight.application.auth.required.TokenProvider;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

@Slf4j
@Component
@RequiredArgsConstructor
public class OAuth2SuccessHandler implements AuthenticationSuccessHandler {

    private final TokenProvider tokenProvider;

    @Value("${app.oauth2.success-redirect:/}")
    private String successRedirectBase;

    @Override
    public void onAuthenticationSuccess(HttpServletRequest req, HttpServletResponse res, Authentication auth) throws IOException {
        AuthDetails principal = (AuthDetails) auth.getPrincipal();

        TokenResponse tokens = tokenProvider.createToken(principal.getUser());

        String access = tokens.accessToken();
        String refresh = tokens.refreshToken();

        String redirect = successRedirectBase
                + "?access="  + URLEncoder.encode(access, StandardCharsets.UTF_8)
                + "&refresh=" + URLEncoder.encode(refresh, StandardCharsets.UTF_8);

        res.sendRedirect(redirect);
    }
}
