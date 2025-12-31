package starlight.adapter.member.webapi.swagger;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import starlight.adapter.member.webapi.dto.MemberDetailResponse;
import starlight.shared.auth.AuthenticatedMember;
import starlight.shared.apiPayload.response.ApiResponse;

@Tag(name = "사용자", description = "사용자 관련 API")
public interface MemberApiDoc {

    @Operation(summary = "멤버 정보를 조회합니다.", security = @SecurityRequirement(name = "Bearer Authentication"))
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "성공",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = MemberDetailResponse.class),
                            examples = @ExampleObject(
                                    name = "성공 예시",
                                    value = """
                        {
                          "result": "SUCCESS",
                          "data": {
                            "id": 1,
                            "name": "홍길동",
                            "email": "hong@example.com",
                            "phoneNumber": "010-1234-5678",
                            "provider": "KAKAO",
                            "profileImageUrl": "https://cdn.example.com/profile/1.png"
                          },
                          "error": null
                        }
                        """
                            )
                    )
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "404",
                    description = "멤버 없음",
                    content = @Content(
                            mediaType = "application/json",
                            examples = @ExampleObject(
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
    @GetMapping
    ApiResponse<MemberDetailResponse> getMemberDetail(
            @AuthenticationPrincipal AuthenticatedMember authDetails
    );
}
