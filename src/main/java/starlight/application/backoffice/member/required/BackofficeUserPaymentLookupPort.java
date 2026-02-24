package starlight.application.backoffice.member.required;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import starlight.application.backoffice.member.required.dto.BackofficeUserPaymentLookupResult;

public interface BackofficeUserPaymentLookupPort {

    Page<BackofficeUserPaymentLookupResult> findUserPaymentPage(Long userId, Pageable pageable);
}
