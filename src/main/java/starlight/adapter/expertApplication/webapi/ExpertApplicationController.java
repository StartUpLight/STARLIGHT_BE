package starlight.adapter.expertApplication.webapi;

import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import starlight.adapter.auth.security.auth.AuthDetails;
import starlight.application.expertApplicaiton.ExpertApplicationServiceImpl;
import starlight.application.expertApplicaiton.provided.ExpertApplicationService;
import starlight.application.expertApplicaiton.required.ExpertApplicationQuery;
import starlight.shared.apiPayload.response.ApiResponse;

import java.util.List;

@RestController
@RequiredArgsConstructor
@Tag(name = "전문가", description = "전문가 API")
@RequestMapping("/v1/expert-applications")
public class ExpertApplicationController {

    private final ExpertApplicationQuery finder;
    private final ExpertApplicationService expertApplicationService;

    @GetMapping
    public ApiResponse<List<Long>> search(
            @RequestParam Long businessPlanId
    ) {
        return ApiResponse.success(finder.findRequestedExpertIds(businessPlanId));
    }

    @PostMapping(value = "/{expertId}/feedback", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ApiResponse<String> requestFeedback(
            @PathVariable Long expertId,
            @RequestParam Long businessPlanId,
            @RequestParam("file") MultipartFile file,
            @AuthenticationPrincipal AuthDetails auth
    ) throws Exception {
        expertApplicationService.requestFeedback(expertId, businessPlanId, file, auth.getUser().getName());
        return ApiResponse.success("피드백 요청이 전달되었습니다.");
    }
}
