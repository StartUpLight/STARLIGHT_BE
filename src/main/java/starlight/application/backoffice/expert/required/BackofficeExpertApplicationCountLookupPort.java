package starlight.application.backoffice.expert.required;

import java.util.List;
import java.util.Map;

public interface BackofficeExpertApplicationCountLookupPort {

    Map<Long, Long> countByExpertIds(List<Long> expertIds);
}
