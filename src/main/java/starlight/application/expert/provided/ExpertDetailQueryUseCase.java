package starlight.application.expert.provided;

import starlight.domain.expert.enumerate.TagCategory;
import starlight.application.expert.provided.dto.ExpertDetailResult;

import java.util.List;
import java.util.Set;

public interface ExpertDetailQueryUseCase {

    List<ExpertDetailResult> search(Set<TagCategory> categories);
}
