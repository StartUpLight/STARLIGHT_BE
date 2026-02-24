package starlight.application.backoffice.businessplan.required;

import starlight.application.backoffice.businessplan.required.dto.BackofficeBusinessPlanMemberLookupResult;

import java.util.Collection;
import java.util.List;
import java.util.Map;

public interface BackofficeBusinessPlanMemberLookupPort {

    BackofficeBusinessPlanMemberLookupResult findBusinessPlanMemberById(Long memberId);

    Map<Long, BackofficeBusinessPlanMemberLookupResult> findBusinessPlanMembersByIds(Collection<Long> memberIds);

    List<Long> findMemberIdsByKeyword(String keyword);
}
