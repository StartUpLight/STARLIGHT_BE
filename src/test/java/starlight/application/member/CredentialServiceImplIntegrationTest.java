package starlight.application.member;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import starlight.adapter.auth.webapi.dto.request.AuthRequest;
import starlight.adapter.member.persistence.CredentialRepository;
import starlight.domain.member.entity.Credential;
import starlight.domain.member.entity.Member;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.ANY)
@Import({CredentialServiceImpl.class, CredentialServiceImplIntegrationTest.TestBeans.class})
class CredentialServiceImplIntegrationTest {

    @TestConfiguration
    static class TestBeans {
        @Bean PasswordEncoder passwordEncoder() { return new BCryptPasswordEncoder(); }
    }

    @Autowired CredentialServiceImpl sut;
    @Autowired CredentialRepository credentialRepository;
    @Autowired PasswordEncoder passwordEncoder;

    @Test
    void createCredential_BCrypt로_해싱되고_DB에_저장된다() {
        AuthRequest req = mock(AuthRequest.class);
        when(req.password()).thenReturn("raw-pw");

        Credential created = sut.createCredential(req);
        assertNotNull(created.getPassword());
        assertTrue(passwordEncoder.matches("raw-pw", created.getPassword()));

        // 실제 DB에도 들어갔는지 확인 (id 존재 등)
        assertNotNull(created.getId());
        assertTrue(credentialRepository.findById(created.getId()).isPresent());
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
