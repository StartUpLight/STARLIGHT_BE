package starlight.adapter.expert.persistence;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.util.ReflectionTestUtils;
import starlight.domain.expert.entity.Expert;
import starlight.domain.expert.entity.ExpertCareer;
import starlight.domain.expert.enumerate.TagCategory;

import jakarta.persistence.EntityManager;
import java.lang.reflect.Constructor;
import java.time.LocalDateTime;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
class ExpertRepositoryTest {

    @Autowired ExpertRepository repository;
    @Autowired EntityManager em;

    @Test
    @DisplayName("findByAllCategories: 전달된 모든 카테고리를 가진 Expert만 조회된다(AND)")
    void findByAllCategories_AND() throws Exception {
        // given
        Expert a = expert("A",
                Set.of(TagCategory.GROWTH_STRATEGY, TagCategory.TEAM_CAPABILITY));
        Expert b = expert("B",
                Set.of(TagCategory.GROWTH_STRATEGY)); // 조건 미충족
        Expert c = expert("C",
                Set.of(TagCategory.GROWTH_STRATEGY, TagCategory.TEAM_CAPABILITY, TagCategory.METRIC_DATA));

        em.persist(a); em.persist(b); em.persist(c);
        em.flush(); em.clear();

        // when
        List<Expert> found = repository.findByAllCategories(
                Set.of(TagCategory.GROWTH_STRATEGY, TagCategory.TEAM_CAPABILITY),
                2L // size
        );

        // then
        assertThat(found).extracting("name").containsExactlyInAnyOrder("A", "C");
    }

    // ---- helpers ----
    private Expert expert(String name, Set<TagCategory> cats) throws Exception {
        Constructor<Expert> ctor = Expert.class.getDeclaredConstructor();
        ctor.setAccessible(true);
        Expert e = ctor.newInstance();
        ReflectionTestUtils.setField(e, "name", name);
        ReflectionTestUtils.setField(e, "email", name.toLowerCase() + "@example.com");
        ReflectionTestUtils.setField(e, "careers", List.of(
                career(e, 1, "career1"),
                career(e, 2, "career2")
        ));
        ReflectionTestUtils.setField(e, "categories", new LinkedHashSet<>(cats));
        return e;
    }

    private ExpertCareer career(Expert expert, int orderIndex, String title) {
        ExpertCareer career = ExpertCareer.of(
                title,
                "desc",
                LocalDateTime.now().minusMonths(1),
                LocalDateTime.now()
        );
        ReflectionTestUtils.setField(career, "expert", expert);
        ReflectionTestUtils.setField(career, "orderIndex", orderIndex);
        return career;
    }
}
