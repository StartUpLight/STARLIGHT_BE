package starlight.adapter.member.auth.security.oauth2;

import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.client.registration.ClientRegistration;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.AuthorizationGrantType;
import org.springframework.security.oauth2.core.OAuth2AccessToken;
import org.springframework.security.oauth2.core.user.DefaultOAuth2User;

import java.time.Instant;
import java.util.List;
import java.util.Map;

public class TestOAuth2Objects {

    public ClientRegistration naverRegistration() {
        return ClientRegistration.withRegistrationId("naver")
                .clientId("test-id")
                .clientSecret("test-secret")
                .authorizationGrantType(AuthorizationGrantType.AUTHORIZATION_CODE)
                .redirectUri("{baseUrl}/login/oauth2/code/{registrationId}")
                .authorizationUri("https://nid.naver.com/oauth2.0/authorize")
                .tokenUri("https://nid.naver.com/oauth2.0/token")
                .userInfoUri("https://openapi.naver.com/v1/nid/me")
                .userNameAttributeName("response")
                .build();
    }

    public OAuth2UserRequest naverRequest() {
        return new OAuth2UserRequest(
                naverRegistration(),
                new OAuth2AccessToken(
                        OAuth2AccessToken.TokenType.BEARER,
                        "access-token",
                        Instant.now(),
                        Instant.now().plusSeconds(3600)
                )
        );
    }

    public DefaultOAuth2User naverUser(String id, String email, String name, String profileImageUrl) {
        Map<String, Object> response = new java.util.HashMap<>();
        response.put("id", id);
        if (email != null) response.put("email", email);
        if (name != null) response.put("name", name);
        if (profileImageUrl != null) response.put("profile_image", profileImageUrl);

        Map<String, Object> attributes = Map.of("response", response);

        return new DefaultOAuth2User(
                List.of(new SimpleGrantedAuthority("ROLE_USER")),
                attributes,
                "response"
        );
    }
}
