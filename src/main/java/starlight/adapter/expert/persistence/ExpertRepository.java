package starlight.adapter.expert.persistence;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import starlight.domain.expert.entity.Expert;
import starlight.domain.expert.enumerate.TagCategory;

import java.util.Collection;
import java.util.List;

public interface ExpertRepository extends JpaRepository<Expert, Long> {

    @Query("select distinct e from Expert e")
    @EntityGraph(attributePaths = {"categories", "careers"})
    List<Expert> findAllWithDetails();

    @Query("""
    select distinct e from Expert e where e.id in (
        select e2.id
        from Expert e2
        join e2.categories c2
        where c2 in :cats
        group by e2.id
        having count(distinct c2) = :size)
    """)
    @EntityGraph(attributePaths = {"categories", "careers"})
    List<Expert> findByAllCategories(@Param("cats") Collection<TagCategory> cats,
                                     @Param("size") long size);
}
