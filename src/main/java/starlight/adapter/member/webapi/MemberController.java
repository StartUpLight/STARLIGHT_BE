package starlight.adapter.member.webapi;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import starlight.adapter.auth.security.auth.AuthDetails;
import starlight.adapter.member.webapi.dto.MemberDetailResponse;
import starlight.shared.apiPayload.response.ApiResponse;

@Slf4j
@RestController
@RequiredArgsConstructor
@Tag(name = "사용자", description = "사용자 관련 API")
@RequestMapping("/v1/members")
public class MemberController {

    @GetMapping
    @Operation(summary = "멤버 정보를 조회합니다.")
    public ApiResponse<MemberDetailResponse> getMemberDetail(
            @AuthenticationPrincipal AuthDetails authDetails
    ) {
        return ApiResponse.success(MemberDetailResponse.from(authDetails.getUser()));
    }
}
