package starlight.application.member;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;
import starlight.adapter.auth.webapi.dto.request.AuthRequest;
import starlight.application.member.required.CredentialRepository;
import starlight.domain.auth.exception.AuthException;
import starlight.domain.member.entity.Credential;
import starlight.domain.member.entity.Member;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CredentialServiceImplUnitTest {

    @Mock PasswordEncoder passwordEncoder;
    @Mock CredentialRepository credentialRepository;

    @InjectMocks CredentialServiceImpl sut;

    @Test
    void createCredential_정상_해싱후_저장() {
        AuthRequest req = mock(AuthRequest.class);
        when(req.password()).thenReturn("raw-pw");
        when(passwordEncoder.encode("raw-pw")).thenReturn("HASHED");
        when(credentialRepository.save(any(Credential.class)))
                .thenAnswer(inv -> inv.getArgument(0));

        Credential created = sut.createCredential(req);

        verify(passwordEncoder).encode("raw-pw");
        verify(credentialRepository).save(any(Credential.class));
        assertNotNull(created);
    }

    @Test
    void checkPassword_일치하면_예외없음() {
        Member member = mock(Member.class);
        Credential cred = mock(Credential.class);
        when(member.getCredential()).thenReturn(cred);
        when(cred.getPassword()).thenReturn("HASHED");
        when(passwordEncoder.matches("pw", "HASHED")).thenReturn(true);

        assertDoesNotThrow(() -> sut.checkPassword(member, "pw"));
        verify(passwordEncoder).matches("pw", "HASHED");
    }

    @Test
    void checkPassword_불일치면_AuthException() {
        Member member = mock(Member.class);
        Credential cred = mock(Credential.class);
        when(member.getCredential()).thenReturn(cred);
        when(cred.getPassword()).thenReturn("HASHED");
        when(passwordEncoder.matches("bad", "HASHED")).thenReturn(false);

        assertThrows(AuthException.class, () -> sut.checkPassword(member, "bad"));
    }
}
