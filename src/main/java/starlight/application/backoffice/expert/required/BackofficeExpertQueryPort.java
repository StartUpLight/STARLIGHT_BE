package starlight.application.backoffice.expert.required;

import starlight.domain.expert.entity.Expert;

import java.util.List;

public interface BackofficeExpertQueryPort {

    List<Expert> findAllWithCareersTagsCategories();
}
