package starlight.adapter.backoffice.mail.webapi;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import starlight.adapter.backoffice.mail.webapi.dto.request.BackofficeMailSendRequest;
import starlight.adapter.backoffice.mail.webapi.dto.response.BackofficeMailSendLogResponse;
import starlight.adapter.backoffice.mail.webapi.dto.request.BackofficeMailTemplateCreateRequest;
import starlight.adapter.backoffice.mail.webapi.dto.response.BackofficeMailTemplateResponse;
import starlight.application.backoffice.mail.provided.BackofficeMailSendUseCase;
import starlight.application.backoffice.mail.provided.BackofficeMailTemplateUseCase;
import starlight.shared.apiPayload.response.ApiResponse;

import java.util.List;

@RestController
@RequiredArgsConstructor
public class BackofficeMailController {

    private final BackofficeMailSendUseCase backofficeMailSendUseCase;
    private final BackofficeMailTemplateUseCase templateUseCase;

    @PostMapping("/v1/backoffice/mail/send")
    public ApiResponse<BackofficeMailSendLogResponse> send(@Valid @RequestBody BackofficeMailSendRequest request) {
        return ApiResponse.success(BackofficeMailSendLogResponse.from(
                backofficeMailSendUseCase.send(request.toInput())
        ));
    }

    @PostMapping("/v1/backoffice/mail/templates")
    public ApiResponse<BackofficeMailTemplateResponse> createTemplate(
            @Valid @RequestBody BackofficeMailTemplateCreateRequest request
    ) {
        return ApiResponse.success(BackofficeMailTemplateResponse.from(
                templateUseCase.createTemplate(request.toInput())
        ));
    }

    @GetMapping("/v1/backoffice/mail/templates")
    public ApiResponse<List<BackofficeMailTemplateResponse>> findTemplates() {
        return ApiResponse.success(templateUseCase.findTemplates().stream()
                .map(BackofficeMailTemplateResponse::from)
                .toList());
    }

    @DeleteMapping("/v1/backoffice/mail/templates/{templateId}")
    public ApiResponse<Void> deleteTemplate(@PathVariable Long templateId) {
        templateUseCase.deleteTemplate(templateId);
        return ApiResponse.success(null);
    }

}
