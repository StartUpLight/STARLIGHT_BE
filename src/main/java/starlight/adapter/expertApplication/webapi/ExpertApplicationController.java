package starlight.adapter.expertApplication.webapi;

import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import starlight.adapter.auth.security.auth.AuthDetails;
import starlight.application.expert.FeedbackService;
import starlight.application.expertApplicaiton.provided.ExpertApplicationFinder;
import starlight.shared.apiPayload.response.ApiResponse;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/v1/expert-applications")
public class ExpertApplicationController {

    private final ExpertApplicationFinder finder;
    private final FeedbackService feedbackService;

    @GetMapping
    public ApiResponse<List<Long>> search(
            @RequestParam Long businessPlanId
    ) {
        return ApiResponse.success(finder.findRequestedExpertIds(businessPlanId));
    }

    @PostMapping(value = "/{expertId}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ApiResponse<String> requestFeedback(
            @PathVariable Long expertId,
            @RequestParam Long businessPlanId,
            @RequestParam("file") MultipartFile file,
            @AuthenticationPrincipal AuthDetails auth
    ) throws Exception {
        feedbackService.requestFeedback(expertId, businessPlanId, file, auth.getUser().getName());
        return ApiResponse.success("피드백 요청이 전달되었습니다.");
    }
}
