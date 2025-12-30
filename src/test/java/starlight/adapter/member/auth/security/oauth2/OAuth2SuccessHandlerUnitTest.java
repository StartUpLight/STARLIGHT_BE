package starlight.adapter.member.auth.security.oauth2;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.core.Authentication;
import starlight.adapter.member.auth.security.auth.AuthDetails;
import starlight.adapter.member.auth.security.jwt.dto.TokenResponse;
import starlight.application.member.auth.required.KeyValueMap;
import starlight.application.member.auth.required.TokenProvider;
import starlight.domain.member.entity.Member;

import java.nio.charset.StandardCharsets;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

class OAuth2SuccessHandlerUnitTest {

    KeyValueMap keyValueMap = mock(KeyValueMap.class);
    TokenProvider tokenProvider = mock(TokenProvider.class);
    OAuth2SuccessHandler handler;

    @BeforeEach
    void setUp() {
        handler = new OAuth2SuccessHandler(tokenProvider, keyValueMap);
        try {
            var field = OAuth2SuccessHandler.class.getDeclaredField("successRedirectBase");
            field.setAccessible(true);
            field.set(handler, "/redirect");
        } catch (NoSuchFieldException | IllegalAccessException e) {
            throw new RuntimeException("successRedirectBase 필드 접근/설정에 실패했습니다.", e);
        }
    }

    @Test
    void 인증성공시_토큰발급_및_리다이렉트() throws Exception {
        // given
        Member user = Member.create("testuser", "", "", null, null, "img.png");

        AuthDetails authDetails = new AuthDetails(user);

        Authentication authentication = mock(Authentication.class);
        when(authentication.getPrincipal()).thenReturn(authDetails);

        when(tokenProvider.createToken(any(Member.class)))
                .thenReturn(new TokenResponse("access-token", "refresh-token"));

        MockHttpServletRequest request = new MockHttpServletRequest();
        MockHttpServletResponse response = new MockHttpServletResponse();

        handler.onAuthenticationSuccess(request, response, authentication);

        // then
        String redirectedUrl = response.getRedirectedUrl();
        assertThat(redirectedUrl).startsWith("/redirect?access=");
    }
}