package starlight.adapter.expert.webapi;

import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import starlight.adapter.expert.webapi.dto.ExpertAiReportBusinessPlanResponse;
import starlight.adapter.expert.webapi.dto.ExpertDetailResponse;
import starlight.adapter.expert.webapi.dto.ExpertListResponse;
import starlight.adapter.expert.webapi.swagger.ExpertApiDoc;
import starlight.application.expert.provided.ExpertAiReportQueryUseCase;
import starlight.application.expert.provided.ExpertDetailQueryUseCase;
import starlight.shared.auth.AuthenticatedMember;
import starlight.shared.apiPayload.response.ApiResponse;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/v1/experts")
public class ExpertController implements ExpertApiDoc {

    private final ExpertDetailQueryUseCase expertDetailQuery;
    private final ExpertAiReportQueryUseCase expertAiReportQuery;

    @GetMapping
    public ApiResponse<List<ExpertListResponse>> search() {
        return ApiResponse.success(ExpertListResponse.fromAll(expertDetailQuery.searchAllActive()));
    }

    @GetMapping("/{expertId}")
    public ApiResponse<ExpertDetailResponse> detail(
            @PathVariable Long expertId
    ) {
        return ApiResponse.success(ExpertDetailResponse.from(expertDetailQuery.findById(expertId)));
    }

    @GetMapping("/{expertId}/business-plans/ai-reports")
    public ApiResponse<List<ExpertAiReportBusinessPlanResponse>> aiReportBusinessPlans(
            @PathVariable Long expertId,
            @AuthenticationPrincipal AuthenticatedMember authenticatedMember
    ) {
        return ApiResponse.success(ExpertAiReportBusinessPlanResponse.fromAll(
                expertAiReportQuery.findAiReportBusinessPlans(expertId, authenticatedMember.getMemberId())
        ));
    }
}
