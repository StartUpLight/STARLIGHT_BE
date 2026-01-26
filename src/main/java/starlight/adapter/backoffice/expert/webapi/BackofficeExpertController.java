package starlight.adapter.backoffice.expert.webapi;

import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import starlight.adapter.backoffice.expert.webapi.dto.request.BackofficeExpertActiveStatusUpdateRequest;
import starlight.adapter.backoffice.expert.webapi.dto.request.BackofficeExpertCreateRequest;
import starlight.adapter.backoffice.expert.webapi.dto.request.BackofficeExpertProfileImageUpdateRequest;
import starlight.adapter.backoffice.expert.webapi.dto.request.BackofficeExpertUpdateRequest;
import starlight.adapter.backoffice.expert.webapi.dto.response.BackofficeExpertCreateResponse;
import starlight.adapter.backoffice.expert.webapi.dto.response.BackofficeExpertDetailResponse;
import starlight.adapter.backoffice.expert.webapi.dto.response.BackofficeExpertListResponse;
import starlight.adapter.backoffice.expert.webapi.swagger.BackofficeExpertApiDoc;
import starlight.application.backoffice.expert.provided.BackofficeExpertCommandUseCase;
import starlight.application.backoffice.expert.provided.BackofficeExpertQueryUseCase;
import starlight.application.backoffice.expert.provided.dto.input.BackofficeExpertActiveStatusUpdateInput;
import starlight.application.backoffice.expert.provided.dto.input.BackofficeExpertProfileImageUpdateInput;
import starlight.shared.apiPayload.response.ApiResponse;

import java.util.List;

@RestController
@RequiredArgsConstructor
@SecurityRequirement(name = "backofficeSession")
@RequestMapping("/v1/backoffice/experts")
public class BackofficeExpertController implements BackofficeExpertApiDoc {

    private final BackofficeExpertQueryUseCase backofficeExpertQuery;
    private final BackofficeExpertCommandUseCase backofficeExpertCommand;

    @GetMapping
    public ApiResponse<List<BackofficeExpertListResponse>> searchAll() {
        return ApiResponse.success(BackofficeExpertListResponse.fromAll(
                backofficeExpertQuery.searchAll()
        ));
    }

    @PostMapping
    public ApiResponse<BackofficeExpertCreateResponse> create(
            @Valid @RequestBody BackofficeExpertCreateRequest request
    ) {
        return ApiResponse.success(BackofficeExpertCreateResponse.from(
                backofficeExpertCommand.createExpert(request.toInput())
        ));
    }

    @GetMapping("/{expertId}")
    public ApiResponse<BackofficeExpertDetailResponse> detail(
            @PathVariable Long expertId
    ) {
        return ApiResponse.success(BackofficeExpertDetailResponse.from(
                backofficeExpertQuery.findById(expertId)
        ));
    }

    @PatchMapping("/{expertId}/active-status")
    public ApiResponse<?> updateActiveStatus(
            @PathVariable Long expertId,
            @Valid @RequestBody BackofficeExpertActiveStatusUpdateRequest request
    ) {
        backofficeExpertCommand.updateActiveStatus(
                BackofficeExpertActiveStatusUpdateInput.of(expertId, request.activeStatus())
        );

        return ApiResponse.success();
    }

    @PatchMapping("/{expertId}")
    public ApiResponse<?> update(
            @PathVariable Long expertId,
            @Valid @RequestBody BackofficeExpertUpdateRequest request
    ) {
        backofficeExpertCommand.updateExpert(request.toInput(expertId));
        return ApiResponse.success();
    }

    @DeleteMapping("/{expertId}")
    public ApiResponse<?> delete(
            @PathVariable Long expertId
    ) {
        backofficeExpertCommand.deleteExpert(expertId);
        return ApiResponse.success();
    }

    @PatchMapping("/{expertId}/profile-image")
    public ApiResponse<?> updateProfileImage(
            @PathVariable Long expertId,
            @Valid @RequestBody BackofficeExpertProfileImageUpdateRequest request
    ) {
        backofficeExpertCommand.updateProfileImage(
                BackofficeExpertProfileImageUpdateInput.of(expertId, request.profileImageUrl())
        );
        return ApiResponse.success();
    }

}
