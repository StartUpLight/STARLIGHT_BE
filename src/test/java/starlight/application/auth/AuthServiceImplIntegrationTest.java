package starlight.application.auth;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import starlight.adapter.auth.security.jwt.dto.TokenResponse;
import starlight.adapter.auth.webapi.dto.request.AuthRequest;
import starlight.adapter.auth.webapi.dto.request.SignInRequest;
import starlight.adapter.auth.webapi.dto.response.MemberResponse;
import starlight.application.auth.required.KeyValueMap;
import starlight.application.auth.required.TokenProvider;
import starlight.application.member.provided.CredentialService;
import starlight.application.member.provided.MemberService;
import starlight.domain.auth.exception.AuthException;
import starlight.domain.member.entity.Credential;
import starlight.domain.member.entity.Member;
import starlight.domain.member.enumerate.MemberType;
import starlight.domain.member.exception.MemberException;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@SpringBootTest
@TestPropertySource(properties = {
        "jwt.token.refresh-expiration-time=3600"
})
class AuthServiceImplIntegrationTest {

    @MockitoBean MemberService memberService;
    @MockitoBean CredentialService credentialService;
    @MockitoBean TokenProvider tokenProvider;
    @MockitoBean KeyValueMap redisClient;

    @Autowired AuthServiceImpl sut;

    @Test
    void signUp_정상_자격증명_생성후_회원생성_리턴() {
        AuthRequest req = mock(AuthRequest.class);
        Credential cred = mock(Credential.class);
        Member member = Member.create("name", "u@ex.com", null, MemberType.FOUNDER, null);

        when(credentialService.createCredential(req)).thenReturn(cred);
        when(memberService.createUser(cred, req)).thenReturn(member);

        MemberResponse res = sut.signUp(req);

        verify(credentialService).createCredential(req);
        verify(memberService).createUser(cred, req);
        assertNotNull(res);
    }

    @Test
    void signIn_정상_토큰생성_리프레시_Redis저장() {
        SignInRequest req = new SignInRequest("a@b.com", "pw");
        Member member = Member.create("test", "a@b.com", null, MemberType.FOUNDER, null);
        TokenResponse token = new TokenResponse("AT", "RT");

        when(memberService.getUserByEmail("a@b.com")).thenReturn(member);
        // 비밀번호 검증은 side-effect만 확인
        doNothing().when(credentialService).checkPassword(member, "pw");
        when(tokenProvider.createToken(member)).thenReturn(token);

        TokenResponse out = sut.signIn(req);

        verify(credentialService).checkPassword(member, "pw");
        verify(redisClient).setValue("a@b.com", "RT", 3600L);
        assertEquals("AT", out.accessToken());
        assertEquals("RT", out.refreshToken());
    }

    @Test
    void signIn_비번오류_전파() {
        SignInRequest req = new SignInRequest("a@b.com", "bad");
        Member member = Member.create("test", "a@b.com", null, MemberType.FOUNDER, null);

        when(memberService.getUserByEmail("a@b.com")).thenReturn(member);
        doThrow(new AuthException(starlight.domain.auth.exception.AuthErrorType.TOKEN_INVALID))
                .when(credentialService).checkPassword(member, "bad");

        assertThrows(AuthException.class, () -> sut.signIn(req));
        verify(tokenProvider, never()).createToken(any());
        verify(redisClient, never()).setValue(any(), any(), anyLong());
    }

    @Test
    void signOut_null토큰이면_TOKEN_NOT_FOUND() {
        assertThrows(AuthException.class, () -> sut.signOut(null, "AT"));
        assertThrows(AuthException.class, () -> sut.signOut("RT", null));
        verify(tokenProvider, never()).invalidateTokens(any(), any());
    }

    @Test
    void signOut_AccessToken_유효성_실패면_TOKEN_INVALID() {
        when(tokenProvider.validateToken("BAD_AT")).thenReturn(false);
        assertThrows(AuthException.class, () -> sut.signOut("RT", "BAD_AT"));
        verify(tokenProvider, never()).invalidateTokens(any(), any());
    }

    @Test
    void signOut_정상_무효화호출() {
        when(tokenProvider.validateToken("GOOD_AT")).thenReturn(true);
        doNothing().when(tokenProvider).invalidateTokens("RT", "GOOD_AT");

        assertDoesNotThrow(() -> sut.signOut("RT", "GOOD_AT"));
        verify(tokenProvider).invalidateTokens("RT", "GOOD_AT");
    }

    @Test
    void recreate_token_null이면_TOKEN_NOT_FOUND() {
        Member member = Member.create("m", "m@ex.com", null, MemberType.FOUNDER, null);
        assertThrows(AuthException.class, () -> sut.recreate(null, member));
    }

    @Test
    void recreate_member_null이면_MEMBER_NOT_FOUND() {
        assertThrows(MemberException.class, () -> sut.recreate("Bearer RT", null));
    }

    @Test
    void recreate_refresh_유효성_실패면_TOKEN_INVALID() {
        when(tokenProvider.validateToken("REAL_RT")).thenReturn(false);
        assertThrows(AuthException.class, () -> sut.recreate("Bearer REAL_RT",
                Member.create("m","m@ex.com", null, MemberType.FOUNDER, null)));
    }

    @Test
    void recreate_Redis저장값과_불일치면_TOKEN_NOT_FOUND() {
        Member member = Member.create("m","m@ex.com", null, MemberType.FOUNDER, null);

        when(tokenProvider.validateToken("REAL_RT")).thenReturn(true);
        when(tokenProvider.getEmail("REAL_RT")).thenReturn("m@ex.com");
        when(redisClient.getValue("m@ex.com")).thenReturn("OTHER_RT"); // 불일치

        assertThrows(AuthException.class, () -> sut.recreate("Bearer REAL_RT", member));
        verify(tokenProvider, never()).recreate(any(), anyString());
    }

    @Test
    void recreate_정상_재발급성공() {
        Member member = Member.create("m","m@ex.com", null, MemberType.FOUNDER, null);
        TokenResponse recreated = new TokenResponse("NEW_AT", "SAME_OR_NEW_RT");

        when(tokenProvider.validateToken("REAL_RT")).thenReturn(true);
        when(tokenProvider.getEmail("REAL_RT")).thenReturn("m@ex.com");
        when(redisClient.getValue("m@ex.com")).thenReturn("REAL_RT");
        when(tokenProvider.recreate(member, "REAL_RT")).thenReturn(recreated);

        TokenResponse out = sut.recreate("Bearer REAL_RT", member);

        assertEquals("NEW_AT", out.accessToken());
        verify(tokenProvider).recreate(member, "REAL_RT");
    }
}
