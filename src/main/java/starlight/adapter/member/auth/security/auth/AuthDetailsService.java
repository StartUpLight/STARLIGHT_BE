package starlight.adapter.member.auth.security.auth;

import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import starlight.application.member.required.MemberQueryPort;
import starlight.domain.member.entity.Member;
import starlight.domain.member.exception.MemberErrorType;
import starlight.domain.member.exception.MemberException;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class AuthDetailsService implements UserDetailsService {

    private final MemberQueryPort memberQueryPort;

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        Member member = memberQueryPort.findByEmail(email).orElseThrow(
                () -> new MemberException(MemberErrorType.MEMBER_NOT_FOUND)
        );

        return new AuthDetails(member);
    }
}
