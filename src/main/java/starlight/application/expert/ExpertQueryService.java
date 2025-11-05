package starlight.application.expert;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import starlight.application.expert.provided.ExpertFinder;
import starlight.application.expert.required.ExpertQuery;
import starlight.domain.expert.entity.Expert;
import starlight.domain.expert.enumerate.TagCategory;

import java.util.Collection;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ExpertQueryService implements ExpertFinder {

    private final ExpertQuery expertQuery;

    @Override
    public List<Expert> loadAll() {
        return expertQuery.findAllWithDetails();
    }

    @Override
    public List<Expert> findByAllCategories(Collection<TagCategory> categories) {
        return expertQuery.findByAllCategories(categories);
    }
}
