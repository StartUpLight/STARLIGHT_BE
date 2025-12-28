package starlight.application.expert.required;

import starlight.domain.expert.entity.Expert;
import starlight.domain.expert.enumerate.TagCategory;

import java.util.Collection;
import java.util.List;

public interface ExpertQueryPort {

    Expert findByIdWithCareersAndTags(Long id);

    List<Expert> findAllWithCareersTagsCategories();

    List<Expert> findByAllCategories(Collection<TagCategory> categories);
}
