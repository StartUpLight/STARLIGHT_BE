package starlight.adapter.backoffice.mail.webapi;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import org.springframework.web.bind.annotation.*;
import starlight.adapter.backoffice.mail.webapi.dto.request.BackofficeMailSendRequest;
import starlight.adapter.backoffice.mail.webapi.dto.request.BackofficeMailTemplateCreateRequest;
import starlight.adapter.backoffice.mail.webapi.dto.response.BackofficeMailTemplateResponse;
import starlight.application.backoffice.mail.provided.BackofficeMailSendUseCase;
import starlight.application.backoffice.mail.provided.BackofficeMailTemplateUseCase;
import starlight.shared.apiPayload.response.ApiResponse;

import java.util.List;

@RestController
@RequiredArgsConstructor
@SecurityRequirement(name = "backofficeSession")
public class BackofficeMailController {

    private final BackofficeMailSendUseCase backofficeMailSendUseCase;
    private final BackofficeMailTemplateUseCase templateUseCase;

    @PostMapping("/v1/backoffice/mail/send")
    public ApiResponse<String> send(
            @Valid @RequestBody BackofficeMailSendRequest request
    ) {
        backofficeMailSendUseCase.send(request.toInput());
        return ApiResponse.success("이메일 전송에 성공하였습니다.");
    }

    @PostMapping("/v1/backoffice/mail/templates")
    public ApiResponse<BackofficeMailTemplateResponse> createTemplate(
            @Valid @RequestBody BackofficeMailTemplateCreateRequest request
    ) {
        BackofficeMailTemplateResponse response = BackofficeMailTemplateResponse.from(templateUseCase.createTemplate(request.toInput()));
        return ApiResponse.success(response);
    }

    @GetMapping("/v1/backoffice/mail/templates")
    public ApiResponse<List<BackofficeMailTemplateResponse>> findTemplates() {
        return ApiResponse.success(templateUseCase.findTemplates().stream()
                .map(BackofficeMailTemplateResponse::from)
                .toList());
    }

    @DeleteMapping("/v1/backoffice/mail/templates/{templateId}")
    public ApiResponse<String> deleteTemplate(
            @PathVariable Long templateId
    ) {
        templateUseCase.deleteTemplate(templateId);
        return ApiResponse.success("템플릿이 삭제되었습니다.");
    }
}
