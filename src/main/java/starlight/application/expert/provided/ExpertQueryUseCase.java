package starlight.application.expert.provided;

import starlight.domain.expert.entity.Expert;
import starlight.domain.expert.enumerate.TagCategory;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;

public interface ExpertQueryUseCase {

    Expert findById(Long id);

    Expert findByIdWithDetails(Long id);

//    List<Expert> loadAll();
//
//    List<Expert> findByAllCategories(Collection<TagCategory> categories);

    Map<Long, Expert> findByIds(Set<Long> expertIds);
}
