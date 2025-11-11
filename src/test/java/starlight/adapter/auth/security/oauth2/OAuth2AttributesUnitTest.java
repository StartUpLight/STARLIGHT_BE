package starlight.adapter.auth.security.oauth2;

import org.junit.jupiter.api.Test;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.client.registration.ClientRegistration;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.AuthorizationGrantType;
import org.springframework.security.oauth2.core.OAuth2AccessToken;
import org.springframework.security.oauth2.core.user.DefaultOAuth2User;

import java.time.Instant;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

class OAuth2AttributesUnitTest {

    private ClientRegistration naverRegistration() {
        return ClientRegistration.withRegistrationId("naver")
                .clientId("test-id")
                .clientSecret("test-secret")
                .authorizationGrantType(AuthorizationGrantType.AUTHORIZATION_CODE)
                .redirectUri("{baseUrl}/login/oauth2/code/{registrationId}")
                .authorizationUri("https://example/authorize")
                .tokenUri("https://example/token")
                .userInfoUri("https://example/me")
                .userNameAttributeName("response")
                .build();
    }

    private OAuth2UserRequest naverRequest() {
        return new OAuth2UserRequest(
                naverRegistration(),
                new OAuth2AccessToken(
                        OAuth2AccessToken.TokenType.BEARER, "access-token",
                        Instant.now(), Instant.now().plusSeconds(3600)
                )
        );
    }

    @Test
    void parse_naver_response_wrapped() {
        Map<String, Object> resp = Map.of(
                "id", "nid-123",
                "email", "user@naver.com",
                "name", "홍길동",
                "profile_image", "http://img/naver.jpg"
        );
        Map<String, Object> attrs = Map.of("response", resp);

        var user = new DefaultOAuth2User(
                List.of(new SimpleGrantedAuthority("ROLE_USER")),
                attrs,
                "response"
        );

        var parsed = OAuth2Attributes.parse(naverRequest(), user);


        assertThat(parsed.provider()).isEqualTo("naver");
        assertThat(parsed.providerId()).isEqualTo("nid-123");
        assertThat(parsed.email()).isEqualTo("user@naver.com");
        assertThat(parsed.name()).isEqualTo("홍길동");
        assertThat(parsed.profileImageUrl()).isEqualTo("http://img/naver.jpg");
        assertThat(parsed.nameAttributeKey()).isEqualTo("id");
    }
}
