package starlight.application.expertReport.required;

import java.util.List;
import java.util.Map;

public interface ExpertApplicationCountLookupPort {

    Map<Long, Long> countByExpertIds(List<Long> expertIds);
}
