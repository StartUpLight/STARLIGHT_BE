package starlight.adapter.expert.webapi;

import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import starlight.adapter.auth.security.auth.AuthDetails;
import starlight.application.expert.FeedbackService;

@RestController
@RequestMapping("/v1/feedback")
@RequiredArgsConstructor
public class FeedbackController {

    private final FeedbackService feedbackService;

    @PostMapping(value = "/request/{mentorId}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<Void> requestFeedback(
            @PathVariable Long mentorId,
            @RequestParam Long planId,
            @RequestParam("file") MultipartFile file,
            @AuthenticationPrincipal AuthDetails auth
    ) throws Exception {
        feedbackService.requestFeedback(mentorId, planId, file, auth.getUser().getName());
        return ResponseEntity.ok().build();
    }
}