package starlight.application.backoffice.member.required;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import starlight.application.backoffice.member.required.dto.BackofficeUserMemberLookupResult;
import starlight.application.backoffice.member.required.dto.BackofficeUserSignupLookupResult;

import java.time.LocalDateTime;
import java.util.List;

public interface BackofficeUserMemberLookupPort {

    boolean existsById(Long userId);

    Page<BackofficeUserMemberLookupResult> findUserPage(String keyword, Pageable pageable);

    long countUsers();

    long countActiveUsersSince(LocalDateTime since);

    long countNewUsersBetween(LocalDateTime fromInclusive, LocalDateTime toExclusive);

    long countChurnedUsersBetween(LocalDateTime fromInclusive, LocalDateTime toExclusive);

    List<BackofficeUserSignupLookupResult> findSignupsBetween(LocalDateTime fromInclusive, LocalDateTime toExclusive);
}
