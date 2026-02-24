package starlight.application.backoffice.expertapplication.required;

import java.util.Collection;
import java.util.Map;

public interface ExpertLookupPort {

    Map<Long, String> findExpertNamesByIds(Collection<Long> expertIds);
}
