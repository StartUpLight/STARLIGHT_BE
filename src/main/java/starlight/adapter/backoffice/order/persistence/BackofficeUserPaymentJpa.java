package starlight.adapter.backoffice.order.persistence;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;
import starlight.application.backoffice.member.required.BackofficeUserPaymentLookupPort;
import starlight.application.backoffice.member.required.dto.BackofficeUserPaymentLookupResult;

@Repository
@RequiredArgsConstructor
public class BackofficeUserPaymentJpa implements BackofficeUserPaymentLookupPort {

    private final BackofficeUserPaymentRepository backofficeUserPaymentRepository;

    @Override
    public Page<BackofficeUserPaymentLookupResult> findUserPaymentPage(Long userId, Pageable pageable) {
        return backofficeUserPaymentRepository.findUserPaymentPage(userId, pageable);
    }
}
