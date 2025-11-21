package starlight.application.member;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import starlight.adapter.auth.webapi.dto.request.AuthRequest;
import starlight.adapter.member.persistence.MemberRepository;
import starlight.domain.member.entity.Credential;
import starlight.domain.member.entity.Member;
import starlight.domain.member.enumerate.MemberType;
import starlight.domain.member.exception.MemberException;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.ANY)
@Import(MemberServiceImpl.class)
class MemberServiceImplIntegrationTest {

    @Autowired MemberServiceImpl sut;
    @Autowired MemberRepository memberRepository;

    @Test
    void getUserByEmail_DB저장후_정상조회() {
        Member preSaved = Member.create("name", "hit@ex.com", null, MemberType.FOUNDER, null, "img.png");
        memberRepository.save(preSaved);

        Member found = sut.getUserByEmail("hit@ex.com");
        assertEquals("hit@ex.com", found.getEmail());
    }

    @Test
    void getUserByEmail_DB에없으면_예외() {
        assertThrows(MemberException.class, () -> sut.getUserByEmail("none@ex.com"));
    }

    @Test
    void createUser_중복이면_예외_바로발생() {
        // 중복 방지 로직은 DB 유니크 제약과 별개로 서비스가 findByEmail로 막음
        memberRepository.save(Member.create("dup", "dup@ex.com", null, MemberType.FOUNDER, null, "img.png"));

        AuthRequest req = mock(AuthRequest.class);
        when(req.email()).thenReturn("dup@ex.com");

        Credential newCredential = Credential.create("anotherHashedPassword");
        assertThrows(MemberException.class,
                () -> sut.createUser(newCredential, req));
    }

    @Test
    void createUser_정상저장_DB반영() {
        Credential cred = Credential.create("hashedPassword");
        AuthRequest req = mock(AuthRequest.class);
        when(req.email()).thenReturn("ok@ex.com");
        when(req.toMember(cred)).thenReturn(Member.create("ok", "ok@ex.com", null, MemberType.FOUNDER, null, "img.png"));

        Member saved = sut.createUser(cred, req);

        Optional<Member> found = memberRepository.findByEmail("ok@ex.com");
        assertTrue(found.isPresent());
        assertEquals(saved.getId(), found.get().getId());
    }
}
