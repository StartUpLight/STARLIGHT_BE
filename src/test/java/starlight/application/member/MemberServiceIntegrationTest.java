package starlight.application.member;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import starlight.adapter.member.persistence.MemberJpa;
import starlight.adapter.member.persistence.MemberRepository;
import starlight.domain.member.entity.Credential;
import starlight.domain.member.entity.Member;
import starlight.domain.member.enumerate.MemberType;
import starlight.domain.member.exception.MemberException;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.ANY)
@Import({MemberService.class, MemberJpa.class})
class MemberServiceIntegrationTest {

    @Autowired
    MemberService sut;
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

        Credential newCredential = Credential.create("anotherHashedPassword");
        assertThrows(MemberException.class,
                () -> sut.createUser(newCredential, "dup", "dup@ex.com", "010-0000-0000"));
    }

    @Test
    void createUser_정상저장_DB반영() {
        Credential cred = Credential.create("hashedPassword");
        Member saved = sut.createUser(cred, "ok", "ok@ex.com", "010-0000-0000");

        Optional<Member> found = memberRepository.findByEmail("ok@ex.com");
        assertTrue(found.isPresent());
        assertEquals(saved.getId(), found.get().getId());
    }
}
