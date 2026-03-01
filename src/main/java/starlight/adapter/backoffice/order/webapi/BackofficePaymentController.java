package starlight.adapter.backoffice.order.webapi;

import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import starlight.adapter.backoffice.order.webapi.dto.response.BackofficePaymentPageResponse;
import starlight.adapter.backoffice.order.webapi.swagger.BackofficePaymentApiDoc;
import starlight.application.backoffice.order.provided.BackofficePaymentQueryUseCase;
import starlight.application.backoffice.order.provided.dto.result.BackofficePaymentPageResult;
import starlight.domain.order.enumerate.OrderStatus;
import starlight.shared.apiPayload.response.ApiResponse;

@RestController
@RequiredArgsConstructor
@SecurityRequirement(name = "backofficeSession")
@RequestMapping("/v1/backoffice/payments")
public class BackofficePaymentController implements BackofficePaymentApiDoc {

    private final BackofficePaymentQueryUseCase backofficePaymentQueryUseCase;

    @Override
    @GetMapping
    public ApiResponse<BackofficePaymentPageResponse> findPayments(
            @RequestParam(required = false) OrderStatus status,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false, defaultValue = "0") int page,
            @RequestParam(required = false, defaultValue = "20") int size,
            @RequestParam(required = false, defaultValue = "createdAt,desc") String sort
    ) {
        BackofficePaymentPageResult result = backofficePaymentQueryUseCase.findPayments(
                status,
                keyword,
                PageRequest.of(page, size, toSort(sort))
        );

        return ApiResponse.success(BackofficePaymentPageResponse.from(result));
    }

    private Sort toSort(String sort) {
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
