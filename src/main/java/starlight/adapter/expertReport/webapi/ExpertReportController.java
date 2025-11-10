package starlight.adapter.expertReport.webapi;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import starlight.adapter.expertReport.webapi.dto.ExpertReportResponse;
import starlight.adapter.expertReport.webapi.dto.SaveExpertReportRequest;
import starlight.adapter.expertReport.webapi.mapper.ExpertReportMapper;
import starlight.application.expertReport.provided.ExpertReportService;
import starlight.domain.expertReport.entity.ExpertReport;
import starlight.domain.expertReport.entity.ExpertReportDetail;
import starlight.shared.apiPayload.response.ApiResponse;

import java.util.List;

@RestController
@RequestMapping("/api/expert-reports")
@RequiredArgsConstructor
public class ExpertReportController {

    private final ExpertReportMapper mapper;
    private final ExpertReportService expertReportService;

    @GetMapping("/{token}")
    public ApiResponse<ExpertReportResponse> getExpertReport(
            @PathVariable String token
    ) {
        ExpertReport report = expertReportService.getExpertReport(token);

        return ApiResponse.success(ExpertReportResponse.from(report));
    }

    @PostMapping("/{token}/save")
    public ApiResponse<?> save(
            @PathVariable String token,
            @Valid @RequestBody SaveExpertReportRequest request
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