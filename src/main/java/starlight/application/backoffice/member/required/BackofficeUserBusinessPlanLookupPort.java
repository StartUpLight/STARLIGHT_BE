package starlight.application.backoffice.member.required;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import starlight.application.backoffice.member.required.dto.BackofficeUserBusinessPlanLookupResult;
import starlight.application.backoffice.member.required.dto.BackofficeUserBusinessPlanMemberLookupResult;

import java.util.Collection;
import java.util.List;

public interface BackofficeUserBusinessPlanLookupPort {

    List<BackofficeUserBusinessPlanMemberLookupResult> findBusinessPlansByUserIds(Collection<Long> userIds);

    Page<BackofficeUserBusinessPlanLookupResult> findUserBusinessPlanPage(Long userId, Boolean scored, Pageable pageable);
}
