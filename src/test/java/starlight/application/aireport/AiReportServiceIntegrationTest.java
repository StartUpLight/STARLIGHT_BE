package starlight.application.aireport;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import starlight.adapter.aireport.report.parser.AiReportResponseParser;
import starlight.adapter.aireport.persistence.AiReportJpa;
import starlight.adapter.aireport.persistence.AiReportRepository;
import starlight.adapter.businessplan.persistence.BusinessPlanQueryJpa;
import starlight.adapter.businessplan.persistence.BusinessPlanRepository;
import starlight.application.aireport.provided.dto.AiReportResult;
import starlight.application.aireport.required.AiReportCommandPort;
import starlight.application.aireport.required.AiReportQueryPort;
import starlight.application.aireport.required.OcrProviderPort;
import starlight.application.aireport.required.ReportGraderPort;
import starlight.application.businessplan.required.BusinessPlanCommandPort;
import starlight.application.businessplan.required.BusinessPlanQueryPort;
import starlight.application.aireport.required.BusinessPlanCommandLookupPort;
import starlight.application.aireport.required.BusinessPlanQueryLookupPort;
import starlight.application.aireport.util.BusinessPlanContentExtractor;
import starlight.domain.aireport.entity.AiReport;
import starlight.domain.businessplan.entity.BusinessPlan;
import starlight.domain.businessplan.entity.SubSection;
import starlight.domain.businessplan.enumerate.PlanStatus;
import starlight.domain.businessplan.enumerate.SubSectionType;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.ANY)
@Import({AiReportService.class, AiReportJpa.class, BusinessPlanQueryJpa.class, AiReportServiceIntegrationTest.TestBeans.class})
@DisplayName("AiReportService 통합 테스트")
class AiReportServiceIntegrationTest {

    @Autowired
    AiReportService sut;
    @Autowired
    BusinessPlanRepository businessPlanRepository;
    @Autowired
    AiReportRepository aiReportRepository;
    @Autowired
    EntityManager em;

    @TestConfiguration
    static class TestBeans {
        
        @Bean
        ReportGraderPort aiReportGrader() {
            return new ReportGraderPort() {
                @Override
                public AiReportResult gradeWithSectionAgents(java.util.Map<starlight.shared.enumerate.SectionType, String> sectionContents, String fullContent) {
                    return AiReportResult.fromGradingResult(
                            20, 25, 30, 20,
                            List.of(
                                    new AiReportResult.SectionScoreDetailResponse("PROBLEM_RECOGNITION", "[{\"item\":\"근본 원인 논리 분석\",\"score\":5,\"maxScore\":5}]"),
                                    new AiReportResult.SectionScoreDetailResponse("FEASIBILITY", "[{\"item\":\"로드맵 구체성\",\"score\":6,\"maxScore\":6}]"),
                                    new AiReportResult.SectionScoreDetailResponse("GROWTH_STRATEGY", "[{\"item\":\"BM 9요소 완결·연계성\",\"score\":6,\"maxScore\":6}]"),
                                    new AiReportResult.SectionScoreDetailResponse("TEAM_COMPETENCE", "[{\"item\":\"창업자 전문성·연관성\",\"score\":5,\"maxScore\":5}]")
                            ),
                            List.of(
                                    new AiReportResult.StrengthWeakness("강점1", "내용1"),
                                    new AiReportResult.StrengthWeakness("강점2", "내용2"),
                                    new AiReportResult.StrengthWeakness("강점3", "내용3")
                            ),
                            List.of(
                                    new AiReportResult.StrengthWeakness("약점1", "내용1"),
                                    new AiReportResult.StrengthWeakness("약점2", "내용2"),
                                    new AiReportResult.StrengthWeakness("약점3", "내용3")
                            )
                    );
                }

                @Override
                public AiReportResult gradeWithFullPrompt(String content) {
                    return AiReportResult.fromGradingResult(
                            20, 25, 30, 20,
                            List.of(
                                    new AiReportResult.SectionScoreDetailResponse("PROBLEM_RECOGNITION", "[{\"item\":\"근본 원인 논리 분석\",\"score\":5,\"maxScore\":5}]"),
                                    new AiReportResult.SectionScoreDetailResponse("FEASIBILITY", "[{\"item\":\"로드맵 구체성\",\"score\":6,\"maxScore\":6}]"),
                                    new AiReportResult.SectionScoreDetailResponse("GROWTH_STRATEGY", "[{\"item\":\"BM 9요소 완결·연계성\",\"score\":6,\"maxScore\":6}]"),
                                    new AiReportResult.SectionScoreDetailResponse("TEAM_COMPETENCE", "[{\"item\":\"창업자 전문성·연관성\",\"score\":5,\"maxScore\":5}]")
                            ),
                            List.of(
                                    new AiReportResult.StrengthWeakness("강점1", "내용1"),
                                    new AiReportResult.StrengthWeakness("강점2", "내용2"),
                                    new AiReportResult.StrengthWeakness("강점3", "내용3")
                            ),
                            List.of(
                                    new AiReportResult.StrengthWeakness("약점1", "내용1"),
                                    new AiReportResult.StrengthWeakness("약점2", "내용2"),
                                    new AiReportResult.StrengthWeakness("약점3", "내용3")
                            )
                    );
                }
            };
        }

        @Bean
        ObjectMapper objectMapper() {
            return new ObjectMapper();
        }

        @Bean
        AiReportResponseParser responseParser() {
            return new AiReportResponseParser(new ObjectMapper());
        }

        @Bean
        BusinessPlanCommandPort businessPlanCommandPort(BusinessPlanRepository businessPlanRepository) {
            return new BusinessPlanCommandPort() {
                @Override
                public BusinessPlan save(BusinessPlan businessPlan) {
                    return businessPlanRepository.save(businessPlan);
                }

                @Override
                public void delete(BusinessPlan businessPlan) {
                    businessPlanRepository.delete(businessPlan);
                }
            };
        }

        @Bean
        BusinessPlanQueryPort businessPlanQueryPort(BusinessPlanRepository businessPlanRepository) {
            return new BusinessPlanQueryPort() {
                @Override
                public BusinessPlan findByIdOrThrow(Long id) {
                    return businessPlanRepository.findById(id)
                            .orElseThrow(() -> new RuntimeException("BusinessPlan not found: " + id));
                }

                @Override
                public BusinessPlan findByIdWithAllSubSectionsOrThrow(Long id) {
                    return businessPlanRepository.findByIdWithAllSubSections(id)
                            .orElseThrow(() -> new RuntimeException("BusinessPlan not found: " + id));
                }

                @Override
                public org.springframework.data.domain.Page<BusinessPlan> findPreviewPage(Long memberId, org.springframework.data.domain.Pageable pageable) {
                    return businessPlanRepository.findAllByMemberIdOrderedByLastSavedAt(memberId, pageable);
                }
            };
        }

        @Bean
        AiReportCommandPort aiReportCommandPort(AiReportRepository aiReportRepository) {
            return new AiReportCommandPort() {
                @Override
                public starlight.domain.aireport.entity.AiReport save(starlight.domain.aireport.entity.AiReport aiReport) {
                    return aiReportRepository.save(aiReport);
                }
            };
        }

        @Bean
        AiReportQueryPort aiReportQueryPort(AiReportRepository aiReportRepository) {
            return new AiReportQueryPort() {
                @Override
                public Optional<starlight.domain.aireport.entity.AiReport> findByBusinessPlanId(Long businessPlanId) {
                    return aiReportRepository.findByBusinessPlanId(businessPlanId);
                }
            };
        }

        @Bean
        OcrProviderPort ocrProvider() {
            return new OcrProviderPort() {
                @Override
                public starlight.shared.dto.infrastructure.OcrResponse ocrPdfByUrl(String pdfUrl) {
                    throw new UnsupportedOperationException("Not implemented in test");
                }

                @Override
                public String ocrPdfTextByUrl(String pdfUrl) {
                    return "PDF에서 추출한 텍스트 내용입니다. 이것은 테스트용 OCR 결과입니다.";
                }
            };
        }

        @Bean
        BusinessPlanContentExtractor businessPlanContentExtractor() {
            return new BusinessPlanContentExtractor();
        }

        @Bean
        BusinessPlanCommandLookupPort businessPlanCommandLookupPort(BusinessPlanRepository businessPlanRepository) {
            return new BusinessPlanCommandLookupPort() {
                @Override
                public BusinessPlan save(BusinessPlan plan) {
                    return businessPlanRepository.save(plan);
                }

                @Override
                public Long createBusinessPlanWithPdf(String title, String pdfUrl, Long memberId) {
                    BusinessPlan plan = BusinessPlan.createWithPdf(title, memberId, pdfUrl);
                    BusinessPlan saved = businessPlanRepository.save(plan);
                    return saved.getId();
                }
            };
        }

        @Bean
        BusinessPlanQueryLookupPort businessPlanQueryLookupPort(BusinessPlanRepository businessPlanRepository) {
            return new BusinessPlanQueryLookupPort() {
                @Override
                public BusinessPlan findByIdOrThrow(Long id) {
                    return businessPlanRepository.findById(id)
                            .orElseThrow(() -> new RuntimeException("BusinessPlan not found: " + id));
                }
            };
        }

    }

    /**
     * BusinessPlan에 모든 서브섹션을 생성하여 작성 완료 상태로 만듦
     */
    private void createAllSubSections(BusinessPlan plan) {
        // Overview
        SubSection overviewBasic = SubSection.create(SubSectionType.OVERVIEW_BASIC, "content", "{}", List.of(false, false, false, false, false));
        plan.getOverview().putSubSection(overviewBasic);

        // ProblemRecognition
        SubSection problemBackground = SubSection.create(SubSectionType.PROBLEM_BACKGROUND, "content", "{}", List.of(false, false, false, false, false));
        SubSection problemPurpose = SubSection.create(SubSectionType.PROBLEM_PURPOSE, "content", "{}", List.of(false, false, false, false, false));
        SubSection problemMarket = SubSection.create(SubSectionType.PROBLEM_MARKET, "content", "{}", List.of(false, false, false, false, false));
        plan.getProblemRecognition().putSubSection(problemBackground);
        plan.getProblemRecognition().putSubSection(problemPurpose);
        plan.getProblemRecognition().putSubSection(problemMarket);

        // Feasibility
        SubSection feasibilityStrategy = SubSection.create(SubSectionType.FEASIBILITY_STRATEGY, "content", "{}", List.of(false, false, false, false, false));
        SubSection feasibilityMarket = SubSection.create(SubSectionType.FEASIBILITY_MARKET, "content", "{}", List.of(false, false, false, false, false));
        plan.getFeasibility().putSubSection(feasibilityStrategy);
        plan.getFeasibility().putSubSection(feasibilityMarket);

        // GrowthTactic
        SubSection growthModel = SubSection.create(SubSectionType.GROWTH_MODEL, "content", "{}", List.of(false, false, false, false, false));
        SubSection growthFunding = SubSection.create(SubSectionType.GROWTH_FUNDING, "content", "{}", List.of(false, false, false, false, false));
        SubSection growthEntry = SubSection.create(SubSectionType.GROWTH_ENTRY, "content", "{}", List.of(false, false, false, false, false));
        plan.getGrowthTactic().putSubSection(growthModel);
        plan.getGrowthTactic().putSubSection(growthFunding);
        plan.getGrowthTactic().putSubSection(growthEntry);

        // TeamCompetence
        SubSection teamFounder = SubSection.create(SubSectionType.TEAM_FOUNDER, "content", "{}", List.of(false, false, false, false, false));
        SubSection teamMembers = SubSection.create(SubSectionType.TEAM_MEMBERS, "content", "{}", List.of(false, false, false, false, false));
        plan.getTeamCompetence().putSubSection(teamFounder);
        plan.getTeamCompetence().putSubSection(teamMembers);
    }

    @Test
    @DisplayName("채점 성공 시 새로운 AiReport를 생성하고 저장한다")
    void gradeBusinessPlan_createsNewReport() {
        // given
        Long memberId = 1L;
        BusinessPlan plan = businessPlanRepository.save(BusinessPlan.create("default title", memberId));
        createAllSubSections(plan);
        businessPlanRepository.save(plan);
        em.flush();
        em.clear();

        Long planId = plan.getId();

        // when
        AiReportResult result = sut.gradeBusinessPlan(planId, memberId);

        // then
        assertThat(result).isNotNull();
        assertThat(result.id()).isNotNull();
        assertThat(result.businessPlanId()).isEqualTo(planId);
        assertThat(result.totalScore()).isEqualTo(95);
        assertThat(result.problemRecognitionScore()).isEqualTo(20);
        assertThat(result.feasibilityScore()).isEqualTo(25);
        assertThat(result.growthStrategyScore()).isEqualTo(30);
        assertThat(result.teamCompetenceScore()).isEqualTo(20);
        assertThat(result.strengths()).hasSize(3);
        assertThat(result.weaknesses()).hasSize(3);
        assertThat(result.sectionScores()).hasSize(4);

        // DB에 저장되었는지 확인
        Optional<AiReport> savedReport = aiReportRepository.findByBusinessPlanId(planId);
        assertThat(savedReport).isPresent();
        assertThat(savedReport.get().getBusinessPlanId()).isEqualTo(planId);

        // Plan 상태가 변경되었는지 확인
        BusinessPlan updatedPlan = businessPlanRepository.findById(planId).orElseThrow();
        assertThat(updatedPlan.getPlanStatus()).isEqualTo(PlanStatus.AI_REVIEWED);
    }

    @Test
    @DisplayName("기존 리포트가 있으면 업데이트한다")
    void gradeBusinessPlan_updatesExistingReport() {
        // given
        Long memberId = 1L;
        BusinessPlan plan = businessPlanRepository.save(BusinessPlan.create("default title", memberId));
        createAllSubSections(plan);
        businessPlanRepository.save(plan);
        em.flush();
        em.clear();

        Long planId = plan.getId();

        // 첫 번째 채점
        AiReportResult firstResult = sut.gradeBusinessPlan(planId, memberId);
        em.flush();
        em.clear();

        // 두 번째 채점 (업데이트)
        AiReportResult secondResult = sut.gradeBusinessPlan(planId, memberId);

        // then
        assertThat(secondResult).isNotNull();
        assertThat(secondResult.id()).isEqualTo(firstResult.id()); // 같은 ID
        assertThat(secondResult.businessPlanId()).isEqualTo(planId);

        // DB에 하나만 존재하는지 확인
        List<AiReport> reports = aiReportRepository.findAll();
        assertThat(reports).hasSize(1);
    }

    @Test
    @DisplayName("리포트 조회 성공 시 AiReportResponse를 반환한다")
    void getAiReport_returnsResponse() {
        // given
        Long memberId = 1L;
        BusinessPlan plan = businessPlanRepository.save(BusinessPlan.create("default title", memberId));
        createAllSubSections(plan);
        businessPlanRepository.save(plan);
        em.flush();
        em.clear();

        Long planId = plan.getId();

        // 채점하여 리포트 생성
        sut.gradeBusinessPlan(planId, memberId);
        em.flush();
        em.clear();

        // when
        AiReportResult result = sut.getAiReport(planId, memberId);

        // then
        assertThat(result).isNotNull();
        assertThat(result.id()).isNotNull();
        assertThat(result.businessPlanId()).isEqualTo(planId);
        assertThat(result.totalScore()).isEqualTo(95);
        assertThat(result.strengths()).hasSize(3);
        assertThat(result.weaknesses()).hasSize(3);
        assertThat(result.sectionScores()).hasSize(4);
    }

    @Test
    @DisplayName("convertToJsonNode와 toResponse가 올바르게 동작한다")
    void convertToJsonNode_and_toResponse_workCorrectly() {
        // given
        Long memberId = 1L;
        BusinessPlan plan = businessPlanRepository.save(BusinessPlan.create("default title", memberId));
        createAllSubSections(plan);
        businessPlanRepository.save(plan);
        em.flush();
        em.clear();

        Long planId = plan.getId();

        // 채점하여 리포트 생성
        AiReportResult gradingResult = sut.gradeBusinessPlan(planId, memberId);
        em.flush();
        em.clear();

        // when - 조회
        AiReportResult retrievedResult = sut.getAiReport(planId, memberId);

        // then - 저장된 데이터와 조회된 데이터가 일치하는지 확인
        assertThat(retrievedResult.problemRecognitionScore()).isEqualTo(gradingResult.problemRecognitionScore());
        assertThat(retrievedResult.feasibilityScore()).isEqualTo(gradingResult.feasibilityScore());
        assertThat(retrievedResult.growthStrategyScore()).isEqualTo(gradingResult.growthStrategyScore());
        assertThat(retrievedResult.teamCompetenceScore()).isEqualTo(gradingResult.teamCompetenceScore());
        assertThat(retrievedResult.totalScore()).isEqualTo(gradingResult.totalScore());
        assertThat(retrievedResult.strengths()).hasSize(gradingResult.strengths().size());
        assertThat(retrievedResult.weaknesses()).hasSize(gradingResult.weaknesses().size());
        assertThat(retrievedResult.sectionScores()).hasSize(gradingResult.sectionScores().size());
    }

    @Test
    @DisplayName("PDF URL을 기반으로 사업계획서를 생성하고 AI 리포트를 생성한다")
    void createAndGradePdfBusinessPlan_createsBusinessPlanAndReport() {
        // given
        Long memberId = 1L;
        String title = "테스트 사업계획서";
        String pdfUrl = "https://example.com/test.pdf";

        // when
        AiReportResult result = sut.createAndGradePdfBusinessPlan(title, pdfUrl, memberId);

        // then
        assertThat(result).isNotNull();
        assertThat(result.id()).isNotNull();
        assertThat(result.businessPlanId()).isNotNull();
        assertThat(result.totalScore()).isEqualTo(95);
        assertThat(result.problemRecognitionScore()).isEqualTo(20);
        assertThat(result.feasibilityScore()).isEqualTo(25);
        assertThat(result.growthStrategyScore()).isEqualTo(30);
        assertThat(result.teamCompetenceScore()).isEqualTo(20);
        assertThat(result.strengths()).hasSize(3);
        assertThat(result.weaknesses()).hasSize(3);
        assertThat(result.sectionScores()).hasSize(4);

        // BusinessPlan이 생성되었는지 확인
        BusinessPlan createdPlan = businessPlanRepository.findById(result.businessPlanId()).orElseThrow();
        assertThat(createdPlan.getTitle()).isEqualTo(title);
        assertThat(createdPlan.getPdfUrl()).isEqualTo(pdfUrl);
        assertThat(createdPlan.getMemberId()).isEqualTo(memberId);
        assertThat(createdPlan.getPlanStatus()).isEqualTo(PlanStatus.AI_REVIEWED);

        // AiReport가 생성되었는지 확인
        Optional<AiReport> savedReport = aiReportRepository.findByBusinessPlanId(result.businessPlanId());
        assertThat(savedReport).isPresent();
        assertThat(savedReport.get().getBusinessPlanId()).isEqualTo(result.businessPlanId());
    }

    @Test
    @DisplayName("PDF 기반으로 생성한 사업계획서의 리포트를 조회할 수 있다")
    void createAndGradePdfBusinessPlan_canRetrieveReport() {
        // given
        Long memberId = 1L;
        String title = "테스트 사업계획서";
        String pdfUrl = "https://example.com/test.pdf";

        // when - PDF로 사업계획서 생성 및 채점
        AiReportResult createdResult = sut.createAndGradePdfBusinessPlan(title, pdfUrl, memberId);
        Long planId = createdResult.businessPlanId();
        em.flush();
        em.clear();

        // when - 리포트 조회
        AiReportResult retrievedResult = sut.getAiReport(planId, memberId);

        // then
        assertThat(retrievedResult).isNotNull();
        assertThat(retrievedResult.id()).isEqualTo(createdResult.id());
        assertThat(retrievedResult.businessPlanId()).isEqualTo(planId);
        assertThat(retrievedResult.totalScore()).isEqualTo(95);
        assertThat(retrievedResult.problemRecognitionScore()).isEqualTo(createdResult.problemRecognitionScore());
        assertThat(retrievedResult.feasibilityScore()).isEqualTo(createdResult.feasibilityScore());
        assertThat(retrievedResult.growthStrategyScore()).isEqualTo(createdResult.growthStrategyScore());
        assertThat(retrievedResult.teamCompetenceScore()).isEqualTo(createdResult.teamCompetenceScore());
    }
}

