package starlight.adapter.expertReport.webapi;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import starlight.adapter.expertReport.webapi.dto.ExpertReportResponse;
import starlight.adapter.expertReport.webapi.dto.UpsertExpertReportRequest;
import starlight.adapter.expertReport.webapi.mapper.ExpertReportMapper;
import starlight.adapter.expertReport.webapi.swagger.ExpertReportApiDoc;
import starlight.application.expertReport.provided.ExpertReportServiceUseCase;
import starlight.application.expertReport.provided.dto.ExpertReportWithExpertResult;
import starlight.domain.expertReport.entity.ExpertReport;
import starlight.domain.expertReport.entity.ExpertReportComment;
import starlight.shared.apiPayload.response.ApiResponse;

import java.util.List;

@RestController
@RequestMapping("/v1/expert-reports")
@RequiredArgsConstructor
public class ExpertReportController implements ExpertReportApiDoc {

    private final ExpertReportMapper mapper;
    private final ExpertReportServiceUseCase expertReportService;

    @GetMapping
    public ApiResponse<List<ExpertReportResponse>> getExpertReports(
            @RequestParam Long businessPlanId
    ) {
        List<ExpertReportWithExpertResult> dtos = expertReportService
                .getExpertReportsWithExpertByBusinessPlanId(businessPlanId);

        List<ExpertReportResponse> responses = dtos.stream()
                .map(dto -> ExpertReportResponse.fromEntities(
                        dto.report(),
                        dto.expert()
                ))
                .toList();

        return ApiResponse.success(responses);
    }

    @GetMapping("/{token}")
    public ApiResponse<ExpertReportResponse> getExpertReport(
            @PathVariable String token
    ) {
        ExpertReportWithExpertResult dto = expertReportService.getExpertReportWithExpert(token);

        ExpertReportResponse response = ExpertReportResponse.fromEntities(
                dto.report(),
                dto.expert()
        );

        return ApiResponse.success(response);
    }

    @PostMapping("/{token}")
    public ApiResponse<ExpertReportResponse> save(
            @PathVariable String token,
            @Valid @RequestBody UpsertExpertReportRequest request
    ) {
        List<ExpertReportComment> comments = mapper.toEntityList(request.comments());

        ExpertReport report = expertReportService.saveReport(
                token,
                request.overallComment(),
                comments,
                request.saveType()
        );

        return ApiResponse.success(ExpertReportResponse.from(report));
    }
}
