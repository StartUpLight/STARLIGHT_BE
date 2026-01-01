package starlight.adapter.expert.persistence;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import starlight.domain.expert.entity.Expert;
import java.util.List;
import java.util.Set;

public interface ExpertRepository extends JpaRepository<Expert, Long> {

    @Query("select e.id from Expert e")
    List<Long> findAllIds();

    @Query("select distinct e from Expert e left join fetch e.careers where e.id in :ids")
    List<Expert> fetchExpertsWithCareersByIds(@Param("ids") List<Long> ids);

    @Query("select distinct e from Expert e left join fetch e.tags where e.id in :ids")
    List<Expert> fetchExpertsWithTagsByIds(@Param("ids") List<Long> ids);

    @Query("select distinct e from Expert e left join fetch e.categories where e.id in :ids")
    List<Expert> fetchExpertsWithCategoriesByIds(@Param("ids") List<Long> ids);

    @Query("select distinct e from Expert e where e.id in :expertIds")
    List<Expert> findAllByIds(Set<Long> expertIds);

}
