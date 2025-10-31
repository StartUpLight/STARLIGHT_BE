package starlight.application.expert.provided;

import starlight.domain.expert.entity.Expert;
import starlight.domain.expert.enumerate.TagCategory;

import java.util.Collection;
import java.util.List;

public interface ExpertFinder {

    List<Expert> loadAll();

    List<Expert> findByAllCategories(Collection<TagCategory> categories);
}
