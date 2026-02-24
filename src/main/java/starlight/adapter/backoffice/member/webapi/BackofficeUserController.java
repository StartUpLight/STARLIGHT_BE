package starlight.adapter.backoffice.member.webapi;

import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import starlight.adapter.backoffice.member.webapi.dto.response.BackofficeUserBusinessPlanPageResponse;
import starlight.adapter.backoffice.member.webapi.dto.response.BackofficeUserDashboardResponse;
import starlight.adapter.backoffice.member.webapi.dto.response.BackofficeUserPageResponse;
import starlight.adapter.backoffice.member.webapi.dto.response.BackofficeUserPaymentResponse;
import starlight.adapter.backoffice.member.webapi.swagger.BackofficeUserApiDoc;
import starlight.application.backoffice.member.provided.BackofficeUserQueryUseCase;
import starlight.shared.apiPayload.response.ApiResponse;

@RestController
@RequiredArgsConstructor
@SecurityRequirement(name = "backofficeSession")
@RequestMapping("/v1/backoffice/users")
public class BackofficeUserController implements BackofficeUserApiDoc {

    private final BackofficeUserQueryUseCase backofficeUserQueryUseCase;

    @Override
    @GetMapping("/dashboard")
    public ApiResponse<BackofficeUserDashboardResponse> getDashboard() {
        return ApiResponse.success(BackofficeUserDashboardResponse.from(
                backofficeUserQueryUseCase.getDashboard()
        ));
    }

    @Override
    @GetMapping
    public ApiResponse<BackofficeUserPageResponse> findUsers(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false, defaultValue = "0") int page,
            @RequestParam(required = false, defaultValue = "20") int size,
            @RequestParam(required = false, defaultValue = "joinedAt,desc") String sort
    ) {
        return ApiResponse.success(BackofficeUserPageResponse.from(
                backofficeUserQueryUseCase.findUsers(
                        keyword,
                        PageRequest.of(page, size, toUserSort(sort))
                )
        ));
    }

    @Override
    @GetMapping("/{userId}/business-plans")
    public ApiResponse<BackofficeUserBusinessPlanPageResponse> findUserBusinessPlans(
            @PathVariable Long userId,
            @RequestParam(required = false, defaultValue = "all") String scoreFilter,
            @RequestParam(required = false, defaultValue = "0") int page,
            @RequestParam(required = false, defaultValue = "20") int size,
            @RequestParam(required = false, defaultValue = "updatedAt,desc") String sort
    ) {
        return ApiResponse.success(BackofficeUserBusinessPlanPageResponse.from(
                backofficeUserQueryUseCase.findUserBusinessPlans(
                        userId,
                        scoreFilter,
                        PageRequest.of(page, size, toUserBusinessPlanSort(sort))
                )
        ));
    }

    @Override
    @GetMapping("/{userId}/payments")
    public ApiResponse<BackofficeUserPaymentResponse> findUserPayments(
            @PathVariable Long userId,
            @RequestParam(required = false, defaultValue = "0") int page,
            @RequestParam(required = false, defaultValue = "20") int size,
            @RequestParam(required = false, defaultValue = "createdAt,desc") String sort
    ) {
        return ApiResponse.success(BackofficeUserPaymentResponse.from(
                backofficeUserQueryUseCase.findUserPayments(
                        userId,
                        PageRequest.of(page, size, toUserPaymentSort(sort))
                )
        ));
    }

    private Sort toUserSort(String sort) {
        if (sort == null || sort.isBlank()) {
            return Sort.by(Sort.Order.desc("createdAt"));
        }

        String[] tokens = sort.split(",");
        String property = tokens[0].trim();

        if (property.isBlank()) {
            return Sort.by(Sort.Order.desc("createdAt"));
        }

        if ("joinedAt".equals(property)) {
            property = "createdAt";
        } else if ("lastActiveAt".equals(property)) {
            property = "modifiedAt";
        }

        Sort.Direction direction = Sort.Direction.ASC;
        if (tokens.length > 1 && "desc".equalsIgnoreCase(tokens[1].trim())) {
            direction = Sort.Direction.DESC;
        }

        return Sort.by(new Sort.Order(direction, property));
    }

    private Sort toUserBusinessPlanSort(String sort) {
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

    private Sort toUserPaymentSort(String sort) {
        if (sort == null || sort.isBlank()) {
            return Sort.by(Sort.Order.desc("createdAt"));
        }

        String[] tokens = sort.split(",");
        String property = tokens[0].trim();

        if (property.isBlank()) {
            return Sort.by(Sort.Order.desc("createdAt"));
        }

        Sort.Direction direction = Sort.Direction.ASC;
        if (tokens.length > 1 && "desc".equalsIgnoreCase(tokens[1].trim())) {
            direction = Sort.Direction.DESC;
        }

        return Sort.by(new Sort.Order(direction, property));
    }
}
