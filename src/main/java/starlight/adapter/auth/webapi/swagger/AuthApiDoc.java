package starlight.adapter.auth.webapi.swagger;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.parameters.RequestBody;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import starlight.adapter.auth.security.auth.AuthDetails;
import starlight.adapter.auth.security.jwt.dto.TokenResponse;
import starlight.adapter.auth.webapi.dto.request.AuthRequest;
import starlight.adapter.auth.webapi.dto.request.SignInRequest;
import starlight.adapter.auth.webapi.dto.response.MemberResponse;
import starlight.shared.apiPayload.response.ApiResponse;

@Tag(name = "Auth API", description = "인증 관련 API")
public interface AuthApiDoc {

    @Operation(
            summary = "회원가입",
            description = "사용자 회원가입 기능"
    )
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "회원가입 성공",
                    content = @Content(
                            schema = @Schema(implementation = MemberResponse.class),
                            examples = @ExampleObject(
                                    name = "회원가입 성공",
                                    value = """
                                        {
                                          "result": "SUCCESS",
                                          "data": {
                                            "id": 1,
                                            "email": "starLight@gmail.com",
                                            "phoneNumber": null,
                                            "nickname": "starLight"
                                          },
                                          "error": null
                                        }
                                        """
                            )
                    )
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "400",
                    description = "잘못된 요청",
                    content = @Content(
                            examples = {
                                    @ExampleObject(
                                            name = "이미 존재하는 회원",
                                            value = """
                                            {
                                              "result": "ERROR",
                                              "data": null,
                                              "error": {
                                                "code": "MEMBER_ALREADY_EXISTS",
                                                "message": "이미 존재하는 회원입니다."
                                              }
                                            }
                                            """
                                    )
                            }
                    )
            )
    })
    @PostMapping("/sign-up")
    ApiResponse<MemberResponse> signUp(
            @RequestBody(
                    description = "회원가입 정보",
                    required = true,
                    content = @Content(
                            examples = @ExampleObject(
                                    name = "회원가입 요청",
                                    value = """
                                        {
                                          "email": "starLight@gmail.com",
                                          "phoneNumber": "010-2112-9765",
                                          "password": "password123"
                                        }
                                        """
                            )
                    )
            )
            @org.springframework.web.bind.annotation.RequestBody AuthRequest authRequest
    );

    @Operation(
            summary = "로그인",
            description = "사용자 로그인 기능"
    )
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "로그인 성공",
                    content = @Content(
                            schema = @Schema(implementation = TokenResponse.class),
                            examples = @ExampleObject(
                                    name = "로그인 성공",
                                    value = """
                                            {
                                              "result": "SUCCESS",
                                              "data": {
                                                "accessToken": "eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJzdGFyTGlnaHRAZ21haWwuY29tIiwiaWF0IjoxNzU5Njg3MzAwLCJleHAiOjE3NTk2OTA5MDB9...",
                                                "refreshToken": "eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJzdGFyTGlnaHRAZ21haWwuY29tIiwiaWF0IjoxNzU5Njg3MzAwLCJleHAiOjE3NjAyOTIxMDB9..."
                                              },
                                              "error": null
                                            }
                                            """
                            )
                    )
            )
    })
    @PostMapping("/sign-in")
    ApiResponse<TokenResponse> signIn(
            @RequestBody(
                    description = "로그인 정보",
                    required = true,
                    content = @Content(
                            examples = @ExampleObject(
                                    name = "로그인 요청",
                                    value = """
                                            {
                                              "email": "starLight@gmail.com",
                                              "password": "password123"
                                            }
                                            """
                            )
                    )
            )
            @org.springframework.web.bind.annotation.RequestBody SignInRequest signInRequest
    );

    @Operation(
            summary = "로그아웃",
            description = "사용자 로그아웃 기능"
    )
    @PostMapping("/sign-out")
    ApiResponse<?> signOut(HttpServletRequest request);

    @Operation(
            summary = "토큰 재발급",
            description = "AccessToken 만료 시 RefreshToken으로 AccessToken 재발급"
    )
    @GetMapping("/recreate")
    ApiResponse<TokenResponse> recreate(HttpServletRequest request, @AuthenticationPrincipal AuthDetails authDetails);
}
