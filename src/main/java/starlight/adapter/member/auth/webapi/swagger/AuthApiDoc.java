package starlight.adapter.member.auth.webapi.swagger;

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
import starlight.adapter.member.auth.security.auth.AuthDetails;
import starlight.adapter.member.auth.security.jwt.dto.TokenResponse;
import starlight.adapter.member.auth.webapi.dto.request.AuthRequest;
import starlight.adapter.member.auth.webapi.dto.request.SignInRequest;
import starlight.adapter.member.auth.webapi.dto.response.MemberResponse;
import starlight.shared.apiPayload.response.ApiResponse;

@Tag(name = "사용자", description = "사용자 관련 API")
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
                            mediaType = "application/json",
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
                                          "name": "박나리",
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
                            mediaType = "application/json",
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
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "401",
                    description = "비밀번호 불일치",
                    content = @Content(
                            mediaType = "application/json",
                            examples = @ExampleObject(
                                    name = "비밀번호 불일치",
                                    value = """
                                            {
                                              "result": "ERROR",
                                              "data": null,
                                              "error": {
                                                "code": "PASSWORD_MISMATCH",
                                                "message": "비밀번호가 일치하지 않습니다."
                                              }
                                            }
                                            """
                            )
                    )
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "404",
                    description = "사용자 없음",
                    content = @Content(
                            mediaType = "application/json",
                            examples = @ExampleObject(
                                    name = "사용자 없음",
                                    value = """
                                            {
                                              "result": "ERROR",
                                              "data": null,
                                              "error": {
                                                "code": "MEMBER_NOT_FOUND",
                                                "message": "존재하지 않는 사용자입니다."
                                              }
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
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "로그아웃 성공",
                    content = @Content(
                            mediaType = "application/json",
                            examples = @ExampleObject(
                                    name = "로그아웃 성공",
                                    value = """
                                            {
                                              "result": "SUCCESS",
                                              "data": "로그아웃 성공",
                                              "error": null
                                            }
                                            """
                            )
                    )
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "400",
                    description = "토큰 오류",
                    content = @Content(
                            mediaType = "application/json",
                            examples = @ExampleObject(
                                    name = "토큰 유효하지 않음",
                                    value = """
                                            {
                                              "result": "ERROR",
                                              "data": null,
                                              "error": {
                                                "code": "TOKEN_INVALID",
                                                "message": "토큰이 유효하지 않습니다."
                                              }
                                            }
                                            """
                            )
                    )
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "404",
                    description = "토큰 없음",
                    content = @Content(
                            mediaType = "application/json",
                            examples = @ExampleObject(
                                    name = "토큰 없음",
                                    value = """
                                            {
                                              "result": "ERROR",
                                              "data": null,
                                              "error": {
                                                "code": "TOKEN_NOT_FOUND",
                                                "message": "토큰이 존재하지 않습니다."
                                              }
                                            }
                                            """
                            )
                    )
            )
    })
    @PostMapping("/sign-out")
    ApiResponse<?> signOut(HttpServletRequest request);

    @Operation(
            summary = "토큰 재발급",
            description = "AccessToken 만료 시 RefreshToken으로 AccessToken 재발급"
    )
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "재발급 성공",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = TokenResponse.class),
                            examples = @ExampleObject(
                                    name = "재발급 성공",
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
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "400",
                    description = "토큰 오류",
                    content = @Content(
                            mediaType = "application/json",
                            examples = @ExampleObject(
                                    name = "토큰 유효하지 않음",
                                    value = """
                                            {
                                              "result": "ERROR",
                                              "data": null,
                                              "error": {
                                                "code": "TOKEN_INVALID",
                                                "message": "토큰이 유효하지 않습니다."
                                              }
                                            }
                                            """
                            )
                    )
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "404",
                    description = "토큰/사용자 없음",
                    content = @Content(
                            mediaType = "application/json",
                            examples = {
                                    @ExampleObject(
                                            name = "토큰 없음",
                                            value = """
                                            {
                                              "result": "ERROR",
                                              "data": null,
                                              "error": {
                                                "code": "TOKEN_NOT_FOUND",
                                                "message": "토큰이 존재하지 않습니다."
                                              }
                                            }
                                            """
                                    ),
                                    @ExampleObject(
                                            name = "사용자 없음",
                                            value = """
                                            {
                                              "result": "ERROR",
                                              "data": null,
                                              "error": {
                                                "code": "MEMBER_NOT_FOUND",
                                                "message": "존재하지 않는 사용자입니다."
                                              }
                                            }
                                            """
                                    )
                            }
                    )
            )
    })
    @GetMapping("/recreate")
    ApiResponse<TokenResponse> reissue(HttpServletRequest request, @AuthenticationPrincipal AuthDetails authDetails);
}
