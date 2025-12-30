package starlight.adapter.member.auth.webapi;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import starlight.adapter.member.auth.security.auth.AuthDetails;
import starlight.adapter.member.auth.security.jwt.dto.TokenResponse;
import starlight.adapter.member.auth.webapi.dto.request.AuthRequest;
import starlight.adapter.member.auth.webapi.dto.request.SignInRequest;
import starlight.adapter.member.auth.webapi.dto.response.MemberResponse;
import starlight.adapter.member.auth.webapi.swagger.AuthApiDoc;
import starlight.application.member.auth.provided.AuthUseCase;
import starlight.shared.apiPayload.response.ApiResponse;

@RestController
@RequestMapping("/v1/auth")
@RequiredArgsConstructor
public class AuthController implements AuthApiDoc {

    private final AuthUseCase authUseCase;
    private final AuthTokenResolver tokenResolver;

    @PostMapping("/sign-up")
    public ApiResponse<MemberResponse> signUp(@Validated @RequestBody AuthRequest authRequest) {
        return ApiResponse.success(authUseCase.signUp(authRequest));
    }

    @PostMapping("/sign-in")
    public ApiResponse<TokenResponse> signIn(@Validated @RequestBody SignInRequest signInRequest) {
        return ApiResponse.success(authUseCase.signIn(signInRequest));
    }

    @PostMapping("/sign-out")
    public ApiResponse<?> signOut(HttpServletRequest request) {
        String refreshToken = tokenResolver.resolveRefreshToken(request);
        String accessToken = tokenResolver.resolveAccessToken(request);

        authUseCase.signOut(refreshToken, accessToken);
        return ApiResponse.success("로그아웃 성공");
    }

    @GetMapping("/recreate")
    public ApiResponse<TokenResponse> reissue(HttpServletRequest request, @AuthenticationPrincipal AuthDetails authDetails) {
        String token = tokenResolver.resolveRefreshToken(request);
        return ApiResponse.success(authUseCase.reissue(token, authDetails.getUser()));
    }
}
