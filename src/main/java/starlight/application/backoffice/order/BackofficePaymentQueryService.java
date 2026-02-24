package starlight.application.backoffice.order;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import starlight.application.backoffice.order.provided.BackofficePaymentQueryUseCase;
import starlight.application.backoffice.order.provided.dto.result.BackofficePaymentPageResult;
import starlight.application.backoffice.order.provided.dto.result.BackofficePaymentRowResult;
import starlight.application.backoffice.order.required.BackofficeOrderMemberLookupPort;
import starlight.application.backoffice.order.required.BackofficePaymentOrderQueryPort;
import starlight.application.backoffice.order.required.dto.BackofficeOrderMemberLookupResult;
import starlight.application.backoffice.order.required.dto.BackofficePaymentOrderLookupResult;
import starlight.domain.order.enumerate.OrderStatus;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class BackofficePaymentQueryService implements BackofficePaymentQueryUseCase {

    private static final long NO_MATCH_MEMBER_ID = -1L;

    private final BackofficePaymentOrderQueryPort paymentOrderQueryPort;
    private final BackofficeOrderMemberLookupPort memberLookupPort;

    @Override
    public BackofficePaymentPageResult findPayments(OrderStatus status, String keyword, Pageable pageable) {
        String normalizedKeyword = normalizeKeyword(keyword);
        List<Long> memberIds = resolveMemberIds(normalizedKeyword);

        Page<BackofficePaymentOrderLookupResult> paymentPage = paymentOrderQueryPort.findPaymentPage(
                status,
                normalizedKeyword,
                memberIds,
                pageable
        );

        List<Long> userIds = paymentPage.getContent().stream()
                .map(BackofficePaymentOrderLookupResult::userId)
                .distinct()
                .toList();

        Map<Long, BackofficeOrderMemberLookupResult> memberMap = memberLookupPort.findMembersByIds(userIds);

        List<BackofficePaymentRowResult> content = paymentPage.getContent().stream()
                .map(payment -> {
                    BackofficeOrderMemberLookupResult member = memberMap.get(payment.userId());

                    return BackofficePaymentRowResult.of(
                            payment.orderId(),
                            payment.orderCode(),
                            payment.userId(),
                            member != null ? member.name() : null,
                            member != null ? member.email() : null,
                            payment.usageProductCode(),
                            payment.usageCount(),
                            payment.price(),
                            payment.orderStatus(),
                            payment.latestPaymentStatus(),
                            payment.createdAt(),
                            toLocalDateTime(payment.approvedAt()),
                            payment.paymentKey()
                    );
                })
                .toList();

        return BackofficePaymentPageResult.of(
                content,
                paymentPage.getNumber(),
                paymentPage.getSize(),
                paymentPage.getTotalElements(),
                paymentPage.getTotalPages(),
                paymentPage.hasNext()
        );
    }

    private String normalizeKeyword(String keyword) {
        if (!StringUtils.hasText(keyword)) {
            return null;
        }

        return keyword.trim();
    }

    private List<Long> resolveMemberIds(String keyword) {
        if (keyword == null) {
            return List.of(NO_MATCH_MEMBER_ID);
        }

        List<Long> memberIds = memberLookupPort.findMemberIdsByKeyword(keyword);
        if (memberIds == null || memberIds.isEmpty()) {
            return List.of(NO_MATCH_MEMBER_ID);
        }

        return memberIds;
    }

    private LocalDateTime toLocalDateTime(Instant instant) {
        if (instant == null) {
            return null;
        }

        return LocalDateTime.ofInstant(instant, ZoneId.systemDefault());
    }
}
