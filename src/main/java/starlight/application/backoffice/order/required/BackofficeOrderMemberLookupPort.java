package starlight.application.backoffice.order.required;

import starlight.application.backoffice.order.required.dto.BackofficeOrderMemberLookupResult;

import java.util.Collection;
import java.util.List;
import java.util.Map;

public interface BackofficeOrderMemberLookupPort {

    Map<Long, BackofficeOrderMemberLookupResult> findMembersByIds(Collection<Long> memberIds);

    List<Long> findMemberIdsByKeyword(String keyword);
}
