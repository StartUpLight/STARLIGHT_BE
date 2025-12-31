package starlight.application.member;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import starlight.application.member.required.MemberCommandPort;
import starlight.application.member.required.MemberQueryPort;
import starlight.domain.member.entity.Credential;
import starlight.domain.member.entity.Member;
import starlight.domain.member.exception.MemberException;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class MemberQueryServiceUnitTest {

    @Mock MemberQueryPort memberQueryPort;
    @Mock MemberCommandPort memberCommandPort;
    @InjectMocks MemberQueryService sut;

    @Test
    void createUser_중복이메일이면_예외() {
        when(memberQueryPort.findByEmail("dup@ex.com"))
                .thenReturn(Optional.of(mock(Member.class)));

        assertThrows(MemberException.class,
                () -> sut.createUser(mock(Credential.class), "name", "dup@ex.com", "010-0000-0000"));
        verify(memberCommandPort, never()).save(any());
    }

    @Test
    void createUser_정상저장() {
        Credential cred = mock(Credential.class);
        Member saved  = mock(Member.class);

        when(memberQueryPort.findByEmail("ok@ex.com"))
                .thenReturn(Optional.empty());
        when(memberCommandPort.save(any(Member.class))).thenReturn(saved);

        Member result = sut.createUser(cred, "name", "ok@ex.com", "010-0000-0000");

        verify(memberCommandPort).save(any(Member.class));
        assertSame(saved, result);
    }

    @Test
    void getUserByEmail_없으면_예외() {
        when(memberQueryPort.findByEmail("none@ex.com")).thenReturn(Optional.empty());
        assertThrows(MemberException.class, () -> sut.getUserByEmail("none@ex.com"));
    }

    @Test
    void getUserByEmail_정상반환() {
        Member m = mock(Member.class);
        when(memberQueryPort.findByEmail("hit@ex.com")).thenReturn(Optional.of(m));
        assertSame(m, sut.getUserByEmail("hit@ex.com"));
    }
}
