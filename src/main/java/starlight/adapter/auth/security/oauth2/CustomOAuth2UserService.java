package starlight.adapter.auth.security.oauth2;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Primary;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserService;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import starlight.application.member.required.MemberRepository;
import starlight.adapter.auth.security.auth.AuthDetails;
import starlight.domain.member.entity.Member;
import starlight.domain.member.enumerate.MemberType;

import java.util.Optional;

@Service
public class CustomOAuth2UserService implements OAuth2UserService<OAuth2UserRequest, OAuth2User> {

    private final MemberRepository memberRepository;
    private final OAuth2UserService<OAuth2UserRequest, OAuth2User> delegate;

    public CustomOAuth2UserService(MemberRepository memberRepository) {
        this.memberRepository = memberRepository;
        this.delegate = new DefaultOAuth2UserService();
    }

    @Override
    @Transactional
    public OAuth2User loadUser(OAuth2UserRequest request) throws OAuth2AuthenticationException {
        OAuth2User oAuth2User = delegate.loadUser(request);
        OAuth2Attributes.Parsed parsed = OAuth2Attributes.parse(request, oAuth2User);

        Optional<Member> found = memberRepository.findByProviderAndProviderId(parsed.provider(), parsed.providerId());
        if (found.isEmpty() && parsed.email() != null) {
            found = memberRepository.findByEmail(parsed.email());
        }

        Member member = found.orElseGet(() ->
                memberRepository.save(Member.newSocial(parsed.name(), parsed.email(), parsed.provider(), parsed.providerId(), null, MemberType.WRITER))
        );

        return AuthDetails.of(member, oAuth2User.getAttributes(), parsed.nameAttributeKey());
    }
}
