package starlight.adapter.backoffice.expert.webapi;

import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import starlight.adapter.backoffice.expert.webapi.dto.response.BackofficeExpertListResponse;
import starlight.application.backoffice.expert.provided.BackofficeExpertQueryUseCase;
import starlight.shared.apiPayload.response.ApiResponse;

import java.util.List;

@RestController
@RequiredArgsConstructor
@SecurityRequirement(name = "backofficeSession")
@RequestMapping("/v1/backoffice/experts")
public class BackofficeExpertController {

    private final BackofficeExpertQueryUseCase backofficeExpertQuery;

    @GetMapping
    public ApiResponse<List<BackofficeExpertListResponse>> searchAll() {
        return ApiResponse.success(BackofficeExpertListResponse.fromAll(backofficeExpertQuery.searchAll()));
    }
}
