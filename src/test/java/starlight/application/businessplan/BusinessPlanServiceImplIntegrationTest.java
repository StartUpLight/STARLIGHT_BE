package starlight.application.businessplan;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import starlight.adapter.businessplan.persistence.BusinessPlanJpa;
import starlight.adapter.businessplan.persistence.BusinessPlanRepository;
import starlight.application.businessplan.required.ChecklistGrader;
import starlight.domain.businessplan.entity.BusinessPlan;
import starlight.domain.businessplan.entity.SubSection;
import starlight.domain.businessplan.enumerate.SubSectionType;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.ANY)
@Import({ BusinessPlanServiceImpl.class, BusinessPlanJpa.class, BusinessPlanServiceImplIntegrationTest.TestBeans.class })
class BusinessPlanServiceImplIntegrationTest {

    @Autowired
    BusinessPlanServiceImpl sut;
    @Autowired
    BusinessPlanRepository businessPlanRepository;
    @Autowired
    EntityManager em;

    @TestConfiguration
    static class TestBeans {
        @Bean
        ChecklistGrader checklistGrader() {
            return (subSectionType, content) -> List.of(false, false, false, false, false);
        }

        @Bean
        ObjectMapper objectMapper() {
            return new ObjectMapper();
        }
    }

    @Test
    void create_and_update_title_and_delete_with_subsections_cleanup() {
        // create
        BusinessPlan created = sut.createBusinessPlan(1L);
        Long planId = created.getId();
        assertThat(planId).isNotNull();

        // attach a subsection to overview
        SubSection s1 = SubSection.create(SubSectionType.OVERVIEW_BASIC, "c", "{}", List.of(false, false, false, false, false));
        created.getOverview().putSubSection(s1);
        businessPlanRepository.save(created);
        em.flush();
        em.clear();

        // sanity: persisted - Overview를 통해 SubSection이 있는지 확인
        BusinessPlan reloaded = businessPlanRepository.findById(planId).orElseThrow();
        assertThat(reloaded.getOverview().getSubSectionByType(SubSectionType.OVERVIEW_BASIC)).isNotNull();

        // update title
        BusinessPlan updated = sut.updateBusinessPlanTitle(planId, created.getMemberId(), "new-title");
        assertThat(updated.getTitle()).isEqualTo("new-title");

        // delete plan -> cascade로 subsections도 함께 삭제
        sut.deleteBusinessPlan(planId, created.getMemberId());

        // SubSection이 cascade로 삭제되었는지 확인
        BusinessPlan afterDelete = businessPlanRepository.findById(planId).orElse(null);
        assertThat(afterDelete).isNull();
    }
}
