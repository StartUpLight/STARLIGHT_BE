package starlight.application.backoffice.member.required;

import starlight.application.backoffice.member.required.dto.BackofficeUserWalletLookupResult;

import java.util.Optional;

public interface BackofficeUserWalletLookupPort {

    Optional<BackofficeUserWalletLookupResult> findWalletByUserId(Long userId);
}
