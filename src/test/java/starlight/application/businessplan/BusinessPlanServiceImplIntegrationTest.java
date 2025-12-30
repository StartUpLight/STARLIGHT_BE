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
import starlight.application.member.required.MemberQueryPort;
import starlight.domain.businessplan.entity.BusinessPlan;
import starlight.domain.businessplan.entity.SubSection;
import starlight.domain.businessplan.enumerate.SubSectionType;
import starlight.domain.member.entity.Member;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.ANY)
@Import({ BusinessPlanServiceImpl.class, BusinessPlanJpa.class,
        BusinessPlanServiceImplIntegrationTest.TestBeans.class })
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

        @Bean
        MemberQueryPort memberQuery() {
            return new MemberQueryPort() {
                @Override
                public Member getOrThrow(Long memberId) {
                    Member m = mock(Member.class);
                    when(m.getName()).thenReturn("tester");
                    return m;
                }

                @Override
                public java.util.Optional<Member> findByEmail(String email) {
                    return java.util.Optional.empty();
                }
            };
        }
    }

    @Test
    void create_and_update_title_and_delete_with_subsections_cleanup() {
        // create
        var createdPreview = sut.createBusinessPlan(1L);
        Long planId = createdPreview.businessPlanId();
        assertThat(planId).isNotNull();

        // attach a subsection to overview
        SubSection s1 = SubSection.create(SubSectionType.OVERVIEW_BASIC, "c", "{}",
                List.of(false, false, false, false, false));
        BusinessPlan createdEntity = businessPlanRepository.findById(planId).orElseThrow();
        createdEntity.getOverview().putSubSection(s1);
        businessPlanRepository.save(createdEntity);
        em.flush();
        em.clear();

        // sanity: persisted - Overview를 통해 SubSection이 있는지 확인
        BusinessPlan reloaded = businessPlanRepository.findById(planId).orElseThrow();
        assertThat(reloaded.getOverview().getSubSectionByType(SubSectionType.OVERVIEW_BASIC)).isNotNull();

        // update title
        String updatedTitle = sut.updateBusinessPlanTitle(planId, "new-title", createdEntity.getMemberId());
        assertThat(updatedTitle).isEqualTo("new-title");

        // delete plan -> cascade로 subsections도 함께 삭제
        sut.deleteBusinessPlan(planId, createdEntity.getMemberId());

        // SubSection이 cascade로 삭제되었는지 확인
        BusinessPlan afterDelete = businessPlanRepository.findById(planId).orElse(null);
        assertThat(afterDelete).isNull();
    }

    @Test
    void createBusinessPlanWithPdf_createsPlanWithPdfInfo() {
        // given
        String title = "PDF 사업계획서";
        String pdfUrl = "https://example.com/test.pdf";
        Long memberId = 1L;

        // when
        var createdResult = sut.createBusinessPlanWithPdf(title, pdfUrl, memberId);
        Long planId = createdResult.businessPlanId();

        // then
        assertThat(planId).isNotNull();
        assertThat(createdResult.title()).isEqualTo(title);

        BusinessPlan createdPlan = businessPlanRepository.findById(planId).orElseThrow();
        assertThat(createdPlan.getTitle()).isEqualTo(title);
        assertThat(createdPlan.getPdfUrl()).isEqualTo(pdfUrl);
        assertThat(createdPlan.getMemberId()).isEqualTo(memberId);
        assertThat(createdPlan.getPlanStatus())
                .isEqualTo(starlight.domain.businessplan.enumerate.PlanStatus.WRITTEN_COMPLETED);
    }
}
