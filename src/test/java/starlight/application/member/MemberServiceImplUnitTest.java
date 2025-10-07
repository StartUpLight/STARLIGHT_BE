package starlight.application.member;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import starlight.adapter.auth.webapi.dto.request.AuthRequest;
import starlight.application.member.required.MemberRepository;
import starlight.domain.member.entity.Credential;
import starlight.domain.member.entity.Member;
import starlight.domain.member.exception.MemberException;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class MemberServiceImplUnitTest {

    @Mock MemberRepository memberRepository;
    @InjectMocks MemberServiceImpl sut;

    @Test
    void createUser_중복이메일이면_예외() {
        AuthRequest req = mock(AuthRequest.class);
        when(req.email()).thenReturn("dup@ex.com");
        when(memberRepository.findByEmail("dup@ex.com"))
                .thenReturn(Optional.of(mock(Member.class)));

        assertThrows(MemberException.class,
                () -> sut.createUser(mock(Credential.class), req));
        verify(memberRepository, never()).save(any());
    }

    @Test
    void createUser_정상저장() {
        AuthRequest req = mock(AuthRequest.class);
        Credential cred = mock(Credential.class);
        Member mapped = mock(Member.class);
        Member saved  = mock(Member.class);

        when(req.email()).thenReturn("ok@ex.com");
        when(memberRepository.findByEmail("ok@ex.com"))
                .thenReturn(Optional.empty());
        when(req.toMember(cred)).thenReturn(mapped);
        when(memberRepository.save(mapped)).thenReturn(saved);

        Member result = sut.createUser(cred, req);

        verify(memberRepository).save(mapped);
        assertSame(saved, result);
    }

    @Test
    void getUserByEmail_없으면_예외() {
        when(memberRepository.findByEmail("none@ex.com")).thenReturn(Optional.empty());
        assertThrows(MemberException.class, () -> sut.getUserByEmail("none@ex.com"));
    }

    @Test
    void getUserByEmail_정상반환() {
        Member m = mock(Member.class);
        when(memberRepository.findByEmail("hit@ex.com")).thenReturn(Optional.of(m));
        assertSame(m, sut.getUserByEmail("hit@ex.com"));
    }
}
