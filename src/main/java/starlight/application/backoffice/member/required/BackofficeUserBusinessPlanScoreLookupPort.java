package starlight.application.backoffice.member.required;

import java.util.Collection;
import java.util.Map;

public interface BackofficeUserBusinessPlanScoreLookupPort {

    Map<Long, Integer> findScoresByBusinessPlanIds(Collection<Long> businessPlanIds);
}
