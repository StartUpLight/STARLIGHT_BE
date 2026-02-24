package starlight.adapter.backoffice.businessplan.persistence;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;
import starlight.application.backoffice.member.required.BackofficeUserBusinessPlanLookupPort;
import starlight.application.backoffice.member.required.dto.BackofficeUserBusinessPlanLookupResult;
import starlight.application.backoffice.member.required.dto.BackofficeUserBusinessPlanMemberLookupResult;

import java.util.Collection;
import java.util.List;

@Repository
@RequiredArgsConstructor
public class BackofficeUserBusinessPlanJpa implements BackofficeUserBusinessPlanLookupPort {

    private final BackofficeUserBusinessPlanRepository backofficeUserBusinessPlanRepository;

    @Override
    public List<BackofficeUserBusinessPlanMemberLookupResult> findBusinessPlansByUserIds(Collection<Long> userIds) {
        if (userIds == null || userIds.isEmpty()) {
            return List.of();
        }

        return backofficeUserBusinessPlanRepository.findBusinessPlansByUserIds(userIds);
    }

    @Override
    public Page<BackofficeUserBusinessPlanLookupResult> findUserBusinessPlanPage(Long userId, Boolean scored, Pageable pageable) {
        return backofficeUserBusinessPlanRepository.findUserBusinessPlanPage(userId, scored, pageable);
    }
}
