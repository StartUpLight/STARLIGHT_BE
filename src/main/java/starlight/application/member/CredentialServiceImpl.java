package starlight.application.member;

import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import starlight.application.member.provided.CredentialService;
import starlight.domain.auth.exception.AuthErrorType;
import starlight.domain.auth.exception.AuthException;
import starlight.domain.member.entity.Credential;
import starlight.domain.member.entity.Member;

@Service
@RequiredArgsConstructor
public class CredentialServiceImpl implements CredentialService {

    private final PasswordEncoder passwordEncoder;

    /**
     * Credential을 생성하고 저장하는 메서드
     * @param rawPassword
     * @return Credential
     */
    public Credential createCredential(String rawPassword) {

        String hashedPassword = passwordEncoder.encode(rawPassword);
        return Credential.create(hashedPassword);
    }

    /**
     * 비밀번호를 확인하는 메서드
     * @param member
     * @param password
     */
    public void checkPassword(Member member, String password) {
        Credential credential = member.getCredential();

        if(!passwordEncoder.matches(password, credential.getPassword())) {
            throw new AuthException(AuthErrorType.PASSWORD_MISMATCH);
        }
    }
}
