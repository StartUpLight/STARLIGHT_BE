package starlight.application.backoffice.businessplan.required;

import java.util.Collection;
import java.util.Map;

public interface BackofficeBusinessPlanScoreLookupPort {

    Map<Long, Integer> findScoresByBusinessPlanIds(Collection<Long> businessPlanIds);
}
