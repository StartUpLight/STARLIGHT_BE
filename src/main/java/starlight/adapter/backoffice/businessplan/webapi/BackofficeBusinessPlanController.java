package starlight.adapter.backoffice.businessplan.webapi;

import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import starlight.adapter.backoffice.businessplan.webapi.dto.response.BackofficeBusinessPlanDetailResponse;
import starlight.adapter.backoffice.businessplan.webapi.dto.response.BackofficeBusinessPlanDashboardResponse;
import starlight.adapter.backoffice.businessplan.webapi.dto.response.BackofficeBusinessPlanPageResponse;
import starlight.adapter.backoffice.businessplan.webapi.swagger.BackofficeBusinessPlanApiDoc;
import starlight.application.backoffice.expertapplication.provided.BackofficeExpertApplicationQueryUseCase;
import starlight.application.backoffice.expertapplication.provided.dto.result.BackofficeBusinessPlanExpertApplicationsResult;
import starlight.application.backoffice.businessplan.provided.BackofficeBusinessPlanQueryUseCase;
import starlight.application.backoffice.businessplan.provided.dto.result.BackofficeBusinessPlanDashboardResult;
import starlight.application.backoffice.businessplan.provided.dto.result.BackofficeBusinessPlanDetailResult;
import starlight.application.backoffice.businessplan.provided.dto.result.BackofficeBusinessPlanPageResult;
import starlight.domain.businessplan.enumerate.PlanStatus;
import starlight.shared.apiPayload.response.ApiResponse;

import java.time.LocalDate;

@RestController
@RequiredArgsConstructor
@SecurityRequirement(name = "backofficeSession")
public class BackofficeBusinessPlanController implements BackofficeBusinessPlanApiDoc {

    private final BackofficeBusinessPlanQueryUseCase backofficeBusinessPlanQueryUseCase;
    private final BackofficeExpertApplicationQueryUseCase backofficeExpertApplicationQueryUseCase;

    @Override
    @GetMapping("/v1/backoffice/business-plans/{planId}")
    public ApiResponse<BackofficeBusinessPlanDetailResponse> findBusinessPlanDetail(@PathVariable Long planId) {
        BackofficeBusinessPlanDetailResult result = backofficeBusinessPlanQueryUseCase.findBusinessPlanDetail(planId);
        BackofficeBusinessPlanExpertApplicationsResult expertApplicationsResult =
                backofficeExpertApplicationQueryUseCase.findByBusinessPlanId(planId);
        return ApiResponse.success(BackofficeBusinessPlanDetailResponse.from(result, expertApplicationsResult));
    }   

    @Override
    @GetMapping("/v1/backoffice/business-plans")
    public ApiResponse<BackofficeBusinessPlanPageResponse> findBusinessPlans(
            @RequestParam(required = false) PlanStatus status,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false, defaultValue = "0") int page,
            @RequestParam(required = false, defaultValue = "20") int size,
            @RequestParam(required = false, defaultValue = "updatedAt,desc") String sort
    ) {
        BackofficeBusinessPlanPageResult result = backofficeBusinessPlanQueryUseCase.findBusinessPlans(
                status,
                keyword,
                PageRequest.of(page, size, toSort(sort))
        );

        return ApiResponse.success(BackofficeBusinessPlanPageResponse.from(result));
    }

    @Override
    @GetMapping("/v1/backoffice/business-plans/dashboard")
    public ApiResponse<BackofficeBusinessPlanDashboardResponse> getDashboard(
            @RequestParam(required = false) PlanStatus status,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dateFrom,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dateTo
    ) {
        BackofficeBusinessPlanDashboardResult result = backofficeBusinessPlanQueryUseCase.getDashboard(
                status,
                keyword,
                dateFrom,
                dateTo
        );

        return ApiResponse.success(BackofficeBusinessPlanDashboardResponse.from(result));
    }

    private Sort toSort(String sort) {
        if (sort == null || sort.isBlank()) {
            return Sort.by(Sort.Order.desc("modifiedAt"));
        }

        String[] tokens = sort.split(",");
        String property = tokens[0].trim();
        if (property.isBlank()) {
            return Sort.by(Sort.Order.desc("modifiedAt"));
        }

        if ("updatedAt".equals(property)) {
            property = "modifiedAt";
        }

        Sort.Direction direction = Sort.Direction.ASC;
        if (tokens.length > 1 && "desc".equalsIgnoreCase(tokens[1].trim())) {
            direction = Sort.Direction.DESC;
        }

        return Sort.by(new Sort.Order(direction, property));
    }
}
