package starlight.application.expert.required;

import starlight.domain.expert.entity.Expert;
import java.util.List;

public interface ExpertQueryPort {

    Expert findByIdWithCareersAndTags(Long id);

    List<Expert> findAllWithCareersTagsCategories();

}
