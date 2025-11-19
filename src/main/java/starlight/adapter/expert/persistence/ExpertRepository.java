package starlight.adapter.expert.persistence;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import starlight.domain.expert.entity.Expert;
import starlight.domain.expert.enumerate.TagCategory;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.Set;

public interface ExpertRepository extends JpaRepository<Expert, Long> {

    @Query("select distinct e from Expert e")
    @EntityGraph(attributePaths = {"categories", "careers", "tags"})
    List<Expert> findAllWithDetails();

    @Query("select distinct e from Expert e where e.id in :expertIds")
    @EntityGraph(attributePaths = {"categories", "careers", "tags"})
    List<Expert> findAllWithDetailsByIds(Set<Long> expertIds);

    @Query("""
    select distinct e from Expert e where e.id in (
        select e2.id
        from Expert e2
        join e2.categories c2
        where c2 in :cats
        group by e2.id
        having count(distinct c2) = :size)
    """)
    @EntityGraph(attributePaths = {"categories", "careers", "tags"})
    List<Expert> findByAllCategories(@Param("cats") Collection<TagCategory> cats,
                                     @Param("size") long size);

    @Query("""
        select e from Expert e
        left join fetch e.categories
        left join fetch e.careers
        left join fetch e.tags
        where e.id = :id
    """)
    Optional<Expert> findByIdWithDetails(@Param("id") Long id);
}
