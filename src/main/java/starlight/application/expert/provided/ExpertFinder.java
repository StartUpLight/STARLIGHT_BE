package starlight.application.expert.provided;

import starlight.domain.expert.entity.Expert;
import starlight.domain.expert.enumerate.TagCategory;

import java.util.Collection;
import java.util.List;
import java.util.Map;

public interface ExpertFinder {
    Expert findExpert(Long id);

    List<Expert> loadAll();

    List<Expert> findByAllCategories(Collection<TagCategory> categories);

    Map<Long, Expert> findByIds(List<Long> expertIds);
}
