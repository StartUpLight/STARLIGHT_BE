package starlight.application.member;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import starlight.domain.member.entity.Credential;
import starlight.domain.member.entity.Member;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = {CredentialService.class, CredentialServiceImplIntegrationTest.TestBeans.class})
class CredentialServiceImplIntegrationTest {

    @TestConfiguration
    static class TestBeans {
        @Bean PasswordEncoder passwordEncoder() { return new BCryptPasswordEncoder(); }
    }

    @Autowired
    CredentialService sut;
    @Autowired PasswordEncoder passwordEncoder;

    @Test
    void createCredential_BCrypt로_해싱되고_DB에_저장된다() {
        Credential created = sut.createCredential("raw-pw");
        assertNotNull(created.getPassword());
        assertTrue(passwordEncoder.matches("raw-pw", created.getPassword()));
    }

    @Test
    void checkPassword_BCrypt_실제검증() {
        // given: 해시를 먼저 만든 Credential
        String hashed = passwordEncoder.encode("pw");
        Credential cred = Credential.create(hashed);
        Member member = mock(Member.class);
        when(member.getCredential()).thenReturn(cred);

        // when & then
        assertDoesNotThrow(() -> sut.checkPassword(member, "pw"));
    }
}
