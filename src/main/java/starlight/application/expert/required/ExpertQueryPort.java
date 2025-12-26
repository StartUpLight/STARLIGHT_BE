package starlight.application.expert.required;

import starlight.domain.expert.entity.Expert;
import starlight.domain.expert.enumerate.TagCategory;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;

public interface ExpertQueryPort {

    Expert findById(Long id);

    Expert findByIdWithDetails(Long id);

    Map<Long, Expert> findExpertMapByIds(Set<Long> expertIds);

    List<Expert> findAllWithDetails();

    List<Expert> findByAllCategories(Collection<TagCategory> categories);
}
