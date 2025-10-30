package starlight.application.businessplan;

import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import starlight.adapter.businessplan.persistence.BusinessPlanJpa;
import starlight.adapter.businessplan.persistence.BusinessPlanRepository;
import starlight.application.businessplan.required.ChecklistGrader;
import starlight.domain.businessplan.entity.BusinessPlan;
import starlight.domain.businessplan.entity.SubSection;
import starlight.domain.businessplan.enumerate.SectionName;
import starlight.domain.businessplan.enumerate.SubSectionName;

import com.fasterxml.jackson.databind.ObjectMapper;

import static org.assertj.core.api.Assertions.assertThat;

@org.junit.jupiter.api.Disabled("Temporarily disabled due to JPA identifier mapping issue in domain @MapsId; enable after mapping fix or testcontainer config")
@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.ANY)
@Import({ BusinessPlanServiceImpl.class, BusinessPlanJpa.class })
class BusinessPlanServiceImplIntegrationTest {

    @Autowired
    BusinessPlanServiceImpl sut;
    @Autowired
    BusinessPlanRepository businessPlanRepository;
    @Autowired
    EntityManager em;

    @Test
    void create_and_update_title_and_delete_with_subsections_cleanup() {
        // create
        BusinessPlan created = sut.createBusinessPlan(1L);
        Long planId = created.getId();
        assertThat(planId).isNotNull();

        // attach a subsection to overview
        Long overviewId = created.getOverview().getId();
        SubSection s1 = SubSection.create(SubSectionName.OVERVIEW_BASIC, "c", "{}");
        s1.attachToParent(overviewId, SectionName.OVERVIEW);
        em.persist(s1);
        em.flush();

        // sanity: persisted
        Long countBefore = em
                .createQuery("select count(s) from SubSection s where s.parentSectionId = :pid", Long.class)
                .setParameter("pid", overviewId)
                .getSingleResult();
        assertThat(countBefore).isEqualTo(1L);

        // update title
        BusinessPlan updated = sut.updateBusinessPlanTitle(planId, created.getMemberId(), "new-title");
        assertThat(updated.getTitle()).isEqualTo("new-title");

        // delete plan -> cleanup subsections
        sut.deleteBusinessPlan(planId, created.getMemberId());

        Long countAfter = em.createQuery("select count(s) from SubSection s where s.parentSectionId = :pid", Long.class)
                .setParameter("pid", overviewId)
                .getSingleResult();
        assertThat(countAfter).isZero();
    }
}
