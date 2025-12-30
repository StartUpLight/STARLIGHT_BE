package starlight.adapter.member.webapi;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import starlight.adapter.member.auth.security.auth.AuthDetails;
import starlight.adapter.member.webapi.swagger.MemberApiDoc;
import starlight.adapter.member.webapi.dto.MemberDetailResponse;
import starlight.shared.apiPayload.response.ApiResponse;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/v1/members")
public class MemberController implements MemberApiDoc {

    @GetMapping
    public ApiResponse<MemberDetailResponse> getMemberDetail(
            @AuthenticationPrincipal AuthDetails authDetails
    ) {
        return ApiResponse.success(MemberDetailResponse.from(authDetails.getUser()));
    }
}
