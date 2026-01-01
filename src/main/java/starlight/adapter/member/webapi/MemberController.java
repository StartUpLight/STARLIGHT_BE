package starlight.adapter.member.webapi;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import starlight.adapter.member.webapi.swagger.MemberApiDoc;
import starlight.adapter.member.webapi.dto.MemberDetailResponse;
import starlight.application.member.provided.MemberQueryUseCase;
import starlight.shared.auth.AuthenticatedMember;
import starlight.shared.apiPayload.response.ApiResponse;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/v1/members")
public class MemberController implements MemberApiDoc {

    private final MemberQueryUseCase memberQueryUseCase;

    @GetMapping
    public ApiResponse<MemberDetailResponse> getMemberDetail(
            @AuthenticationPrincipal AuthenticatedMember authenticatedMember
    ) {
        return ApiResponse.success(MemberDetailResponse.fromMember(
                memberQueryUseCase.getUserById(authenticatedMember.getMemberId())
        ));
    }
}
