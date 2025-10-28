package starlight.adapter.auth.security.oauth2;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.client.registration.ClientRegistration;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserService;
import org.springframework.security.oauth2.core.AuthorizationGrantType;
import org.springframework.security.oauth2.core.OAuth2AccessToken;
import org.springframework.security.oauth2.core.user.DefaultOAuth2User;
import org.springframework.test.util.ReflectionTestUtils;
import starlight.adapter.auth.security.auth.AuthDetails;
import starlight.adapter.member.persistence.MemberRepository;
import starlight.domain.member.entity.Member;
import starlight.domain.member.enumerate.MemberType;

import java.time.Instant;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

class CustomOAuth2UserServiceUnitTest {

    @Mock MemberRepository memberRepository;
    @Mock OAuth2UserService<OAuth2UserRequest, org.springframework.security.oauth2.core.user.OAuth2User> delegate;

    @InjectMocks CustomOAuth2UserService sut;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        sut = new CustomOAuth2UserService(memberRepository);
        ReflectionTestUtils.setField(sut, "delegate", delegate);
    }

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

    private OAuth2UserRequest naverReq = naverRequest();

    private DefaultOAuth2User naverUser(String id, String email, String name) {
        Map<String, Object> resp = new HashMap<>();
        resp.put("id", id);
        if (email != null) resp.put("email", email);
        resp.put("name", name);
        Map<String, Object> attrs = Map.of("response", resp);

        return new DefaultOAuth2User(
                List.of(new SimpleGrantedAuthority("ROLE_USER")),
                attrs,
                "response"
        );
    }

    @Test
    void when_providerId_exists_then_load_existing_member() {
        var oau = naverUser("nid-1", "a@b.com", "홍길동");
        when(delegate.loadUser(any())).thenReturn(oau);

        var existing = Member.newSocial("홍길동", "a@b.com", "naver", "nid-1", null, MemberType.FOUNDER);
        when(memberRepository.findByProviderAndProviderId("naver", "nid-1"))
                .thenReturn(Optional.of(existing));

        var result = sut.loadUser(naverReq);

        verify(memberRepository, never()).save(any());
        assertThat(result).isInstanceOf(AuthDetails.class);
        var details = (AuthDetails) result;
        assertThat(details.member().getProvider()).isEqualTo("naver");
        assertThat(details.member().getProviderId()).isEqualTo("nid-1");
    }

    @Test
    void when_not_found_by_providerId_but_email_matches_then_bind_email() {
        var oau = naverUser("nid-2", "c@d.com", "아무개");
        when(delegate.loadUser(any())).thenReturn(oau);

        when(memberRepository.findByProviderAndProviderId("naver", "nid-2"))
                .thenReturn(Optional.empty());

        var byEmail = Member.newSocial("기존이름", "c@d.com", "kakao", "kid-9", null, MemberType.FOUNDER);
        when(memberRepository.findByEmail("c@d.com")).thenReturn(Optional.of(byEmail));

        var result = sut.loadUser(naverReq);

        verify(memberRepository, never()).save(any());
        var details = (AuthDetails) result;
        // 정책에 따라: 기존 계정에 naver 연결 or 그냥 로그인만
        assertThat(details.member().getEmail()).isEqualTo("c@d.com");
    }

    @Test
    void when_no_match_then_create_new_member() {
        var oau = naverUser("nid-3", null, "신규유저"); // 이메일 동의 안 한 케이스
        when(delegate.loadUser(any())).thenReturn(oau);

        when(memberRepository.findByProviderAndProviderId("naver", "nid-3"))
                .thenReturn(Optional.empty());

        var saved = Member.newSocial("신규유저", null, "naver", "nid-3", null, MemberType.FOUNDER);
        when(memberRepository.save(any(Member.class))).thenReturn(saved);

        var result = sut.loadUser(naverReq);

        verify(memberRepository).save(any(Member.class));
        var details = (AuthDetails) result;
        assertThat(details.member().getProviderId()).isEqualTo("nid-3");
    }
}
