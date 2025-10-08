package starlight.application.auth;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;
import starlight.adapter.auth.security.jwt.dto.TokenResponse;
import starlight.adapter.auth.webapi.dto.request.SignInRequest;
import starlight.application.auth.required.KeyValueMap;
import starlight.application.auth.required.TokenProvider;
import starlight.application.member.provided.CredentialService;
import starlight.application.member.provided.MemberService;
import starlight.domain.auth.exception.AuthException;
import starlight.domain.member.entity.Member;
import starlight.domain.member.enumerate.MemberType;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthServiceImplUnitTest {

    @Mock MemberService memberService;
    @Mock CredentialService credentialService;
    @Mock TokenProvider tokenProvider;
    @Mock KeyValueMap redisClient;

    @InjectMocks AuthServiceImpl sut;

    @BeforeEach
    void init() {
        ReflectionTestUtils.setField(sut, "refreshTokenExpirationTime", 3600L); // @Value 대체
    }

    @Test
    void signIn_정상() {
        SignInRequest req = new SignInRequest("a@b.com", "pw");
        Member member = Member.create("testName", "a@b.com", null, MemberType.FOUNDER, null);
        TokenResponse token = new TokenResponse("AT", "RT");
        when(memberService.getUserByEmail("a@b.com")).thenReturn(member);
        when(tokenProvider.createToken(member)).thenReturn(token);

        TokenResponse res = sut.signIn(req);

        verify(credentialService).checkPassword(member, "pw");
        verify(redisClient).setValue("a@b.com", "RT", 3600L);
        assertEquals("AT", res.accessToken());
    }

    @Test
    void signOut_AccessToken_유효성_실패면_예외() {
        when(tokenProvider.validateToken("bad")).thenReturn(false);
        assertThrows(AuthException.class, () -> sut.signOut("r", "bad"));
        verify(tokenProvider, never()).invalidateTokens(any(), any());
    }

    @Test
    void recreate_저장된_리프레시와_불일치면_예외() {
        Member member = Member.create("testName", "a@b.com", null, MemberType.FOUNDER, null);
        when(tokenProvider.validateToken("REAL_RT")).thenReturn(true);
        when(tokenProvider.getEmail("REAL_RT")).thenReturn("a@b.com");
        when(redisClient.getValue("a@b.com")).thenReturn("OTHER_RT");

        assertThrows(AuthException.class,
                () -> sut.recreate("Bearer REAL_RT", member));
    }
}