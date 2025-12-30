package starlight.adapter.auth.webapi;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import starlight.adapter.auth.security.auth.AuthDetails;
import starlight.adapter.auth.security.jwt.dto.TokenResponse;
import starlight.adapter.auth.webapi.dto.request.AuthRequest;
import starlight.adapter.auth.webapi.dto.request.SignInRequest;
import starlight.adapter.auth.webapi.dto.response.MemberResponse;
import starlight.adapter.auth.webapi.swagger.AuthApiDoc;
import starlight.application.auth.provided.AuthUseCase;
import starlight.application.auth.required.TokenProvider;
import starlight.shared.apiPayload.response.ApiResponse;

@RestController
@RequestMapping("/v1/auth")
@RequiredArgsConstructor
public class AuthController implements AuthApiDoc {

    @Value("${jwt.header}")
    private String tokenHeader;

    private final AuthUseCase authUseCase;
    private final TokenProvider tokenProvider;

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
        String refreshToken = tokenProvider.resolveRefreshToken(request);
        String accessToken = tokenProvider.resolveAccessToken(request);

        authUseCase.signOut(refreshToken, accessToken);
        return ApiResponse.success("로그아웃 성공");
    }

    @GetMapping("/recreate")
    public ApiResponse<TokenResponse> recreate(HttpServletRequest request, @AuthenticationPrincipal AuthDetails authDetails) {
        String token = request.getHeader(tokenHeader);
        return ApiResponse.success(authUseCase.recreate(token, authDetails.getUser()));
    }
}
