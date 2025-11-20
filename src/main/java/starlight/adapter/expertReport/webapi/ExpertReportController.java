package starlight.adapter.expertReport.webapi;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import starlight.adapter.expertReport.webapi.dto.ExpertReportResponse;
import starlight.adapter.expertReport.webapi.dto.UpsertExpertReportRequest;
import starlight.adapter.expertReport.webapi.mapper.ExpertReportMapper;
import starlight.application.expertReport.provided.ExpertReportService;
import starlight.application.expertReport.provided.dto.ExpertReportWithExpertDto;
import starlight.domain.expertReport.entity.ExpertReport;
import starlight.domain.expertReport.entity.ExpertReportDetail;
import starlight.shared.apiPayload.response.ApiResponse;

import java.util.List;

@RestController
@RequestMapping("/v1/expert-reports")
@RequiredArgsConstructor
@Tag(name = "전문가", description = "전문가 관련 API")
public class ExpertReportController {

    private final ExpertReportMapper mapper;
    private final ExpertReportService expertReportService;

    @Operation(summary = "전문가 리포트 목록을 조회합니다. (사용자 사용)")
    @GetMapping
    public ApiResponse<List<ExpertReportResponse>> getExpertReports(
            @RequestParam Long businessPlanId
    ) {
        List<ExpertReportWithExpertDto> dtos = expertReportService
                .getExpertReportsWithExpertByBusinessPlanId(businessPlanId);

        List<ExpertReportResponse> responses = dtos.stream()
                .map(dto -> ExpertReportResponse.fromEntities(
                        dto.report(),
                        dto.expert()
                ))
                .toList();

        return ApiResponse.success(responses);
    }

    @Operation(summary = "전문가 리포트를 조회합니다. (전문가 사용)")
    @GetMapping("/{token}")
    public ApiResponse<ExpertReportResponse> getExpertReport(
            @PathVariable String token
    ) {
        ExpertReportWithExpertDto dto = expertReportService.getExpertReportWithExpert(token);

        ExpertReportResponse response = ExpertReportResponse.fromEntities(
                dto.report(),
                dto.expert()
        );

        return ApiResponse.success(response);
    }

    @Operation(summary = "전문가 리포트를 저장합니다 (전문가 사용)")
    @PostMapping("/{token}")
    public ApiResponse<?> save(
            @PathVariable String token,
            @Valid @RequestBody UpsertExpertReportRequest request
    ) {
        List<ExpertReportDetail> details = mapper.toEntityList(request.details());

        ExpertReport report = expertReportService.saveReport(
                token,
                request.overallComment(),
                details,
                request.saveType()
        );

        return ApiResponse.success(ExpertReportResponse.from(report));
    }
}