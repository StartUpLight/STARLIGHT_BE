package starlight.application.businessplan;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import starlight.application.businessplan.provided.dto.BusinessPlanResult;
import starlight.application.businessplan.provided.dto.SubSectionResult;
import starlight.application.businessplan.required.BusinessPlanQueryPort;
import starlight.application.businessplan.required.ChecklistGraderPort;
import starlight.domain.businessplan.entity.BusinessPlan;
import starlight.domain.businessplan.entity.Overview;
import starlight.domain.businessplan.entity.SubSection;
import starlight.domain.businessplan.entity.BaseSection;
import starlight.domain.businessplan.enumerate.SubSectionType;
import starlight.domain.businessplan.exception.BusinessPlanException;
import starlight.shared.enumerate.SectionType;
import starlight.application.member.required.MemberQueryPort;
import starlight.domain.member.entity.Member;

import java.util.List;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@org.mockito.junit.jupiter.MockitoSettings(strictness = org.mockito.quality.Strictness.LENIENT)
class BusinessPlanServiceImplUnitTest {

    @Mock
    private BusinessPlanQueryPort businessPlanQuery;

    @Mock
    private ChecklistGraderPort checklistGrader;

    @Mock
    private ObjectMapper objectMapper;

    @Mock
    private MemberQueryPort memberQuery;

    @InjectMocks
    private BusinessPlanService sut;

    private BusinessPlan buildPlanWithSections(Long memberId) {
        return BusinessPlan.create("default title", memberId);
    }

    @BeforeEach
    void setup() {
        when(objectMapper.valueToTree(any()))
                .thenReturn(new com.fasterxml.jackson.databind.ObjectMapper().createObjectNode());
        try {
            when(objectMapper.writeValueAsString(any())).thenReturn("{}");
        } catch (Exception ignored) {
        }
        // memberQuery 기본 스텁
        Member stubMember = mock(Member.class);
        when(stubMember.getName()).thenReturn("tester");
        when(memberQuery.findByIdOrThrow(anyLong())).thenReturn(stubMember);
    }

    @Test
    @DisplayName("사업계획서 생성 시 루트가 저장된다")
    void createBusinessPlan_savesRoot() {
        when(businessPlanQuery.save(any(BusinessPlan.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        BusinessPlanResult.Result created = sut.createBusinessPlan(1L);

        assertThat(created).isNotNull();
        assertThat(created.message()).isEqualTo("Business plan created");
        verify(businessPlanQuery).save(any(BusinessPlan.class));
    }

    @Test
    @DisplayName("PDF URL을 기반으로 사업계획서를 생성하면 저장된다")
    void createBusinessPlanWithPdf_savesRoot() {
        when(businessPlanQuery.save(any(BusinessPlan.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        String title = "테스트 사업계획서";
        String pdfUrl = "https://example.com/test.pdf";
        Long memberId = 1L;

        BusinessPlanResult.Result created = sut.createBusinessPlanWithPdf(title, pdfUrl, memberId);

        assertThat(created).isNotNull();
        assertThat(created.message()).isEqualTo("PDF Business plan created");
        assertThat(created.title()).isEqualTo(title);
        verify(businessPlanQuery).save(any(BusinessPlan.class));
    }

    @Test
    @DisplayName("사업계획서 제목 수정은 소유자 검증 후 저장한다")
    void updateTitle_checksOwnership_thenSaves() {
        BusinessPlan plan = spy(buildPlanWithSections(10L));
        doReturn(true).when(plan).isOwnedBy(10L);
        when(businessPlanQuery.findByIdOrThrow(100L)).thenReturn(plan);
        when(businessPlanQuery.save(any(BusinessPlan.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        String updatedTitle = sut.updateBusinessPlanTitle(100L, "new-title", 10L);

        assertThat(updatedTitle).isEqualTo("new-title");
        verify(businessPlanQuery).save(plan);
    }

    @Test
    @DisplayName("제목 수정 - 소유자 아님이면 예외")
    void updateTitle_unauthorized_throws() {
        BusinessPlan plan = spy(buildPlanWithSections(20L));
        doReturn(false).when(plan).isOwnedBy(10L);
        when(businessPlanQuery.findByIdOrThrow(100L)).thenReturn(plan);

        org.junit.jupiter.api.Assertions.assertThrows(BusinessPlanException.class,
                () -> sut.updateBusinessPlanTitle(100L, "title", 10L));
    }

    @Test
    @DisplayName("사업계획서 삭제 시 cascade로 SubSection이 함께 삭제된다")
    void deleteBusinessPlan_cascadeDeletesSubSections() {
        BusinessPlan plan = mock(BusinessPlan.class);
        when(plan.isOwnedBy(10L)).thenReturn(true);
        when(plan.getId()).thenReturn(100L);
        when(businessPlanQuery.findByIdOrThrow(100L)).thenReturn(plan);

        BusinessPlanResult.Result deleted = sut.deleteBusinessPlan(100L, 10L);

        assertThat(deleted).isNotNull();
        assertThat(deleted.businessPlanId()).isEqualTo(100L);
        assertThat(deleted.message()).isEqualTo("Business plan deleted");

        verify(businessPlanQuery).delete(plan);
    }

    @Test
    @DisplayName("서브섹션 생성: 없으면 신규 생성 후 부모 섹션에 연결하여 저장")
    void upsertSubSection_creates_whenNotExists() {
        // given
        BusinessPlan plan = buildPlanWithSections(10L);
        Overview overview = plan.getOverview();

        when(businessPlanQuery.findByIdOrThrow(1L)).thenReturn(plan);
        when(businessPlanQuery.save(any(BusinessPlan.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        com.fasterxml.jackson.databind.node.ObjectNode jsonNode = new com.fasterxml.jackson.databind.ObjectMapper()
                .createObjectNode();
        jsonNode.putArray("content");
        when(objectMapper.valueToTree(any())).thenReturn(jsonNode);
        try {
            when(objectMapper.writeValueAsString(eq(jsonNode))).thenReturn("{}");
        } catch (Exception ignored) {
        }

        // when
        List<Boolean> checks = List.of(false, false, false, false, false);
        SubSectionResult.Result res = sut.upsertSubSection(1L, jsonNode, checks,
                SubSectionType.OVERVIEW_BASIC, 10L);

        // then
        assertThat(res).isNotNull();
        assertThat(res.message()).isEqualTo("Subsection created");
        assertThat(res.subSectionType()).isEqualTo(SubSectionType.OVERVIEW_BASIC);
        assertThat(overview.getSubSectionByType(SubSectionType.OVERVIEW_BASIC)).isNotNull();
        assertThat(overview.getSubSectionByType(SubSectionType.OVERVIEW_BASIC).getSubSectionType())
                .isEqualTo(SubSectionType.OVERVIEW_BASIC);
        verify(businessPlanQuery).save(plan);
    }

    @Test
    @DisplayName("서브섹션 생성: 기존 존재하면 업데이트 경로")
    void upsertSubSection_updates_whenExists() {
        BusinessPlan plan = buildPlanWithSections(10L);
        Overview overview = plan.getOverview();

        // 기존 SubSection 생성 및 설정
        SubSection existing = SubSection.create(SubSectionType.OVERVIEW_BASIC, "old", "{}",
                List.of(false, false, false, false, false));
        overview.putSubSection(existing);

        when(businessPlanQuery.findByIdOrThrow(1L)).thenReturn(plan);
        when(businessPlanQuery.save(any(BusinessPlan.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        com.fasterxml.jackson.databind.node.ObjectNode jsonNode = new com.fasterxml.jackson.databind.ObjectMapper()
                .createObjectNode();
        jsonNode.putArray("content");
        when(objectMapper.valueToTree(any())).thenReturn(jsonNode);
        try {
            when(objectMapper.writeValueAsString(eq(jsonNode))).thenReturn("{}");
        } catch (Exception ignored) {
        }

        List<Boolean> checks = List.of(false, false, false, false, false);
        SubSectionResult.Result res = sut.upsertSubSection(1L, jsonNode, checks,
                SubSectionType.OVERVIEW_BASIC, 10L);

        assertThat(res.message()).isEqualTo("Subsection updated");
        verify(businessPlanQuery).save(plan);
    }

    @Test
    @DisplayName("서브섹션 생성: 소유자 아님이면 예외")
    void upsertSubSection_unauthorized_throws() {
        BusinessPlan plan = mock(BusinessPlan.class);
        when(plan.isOwnedBy(10L)).thenReturn(false);
        when(businessPlanQuery.findByIdOrThrow(1L)).thenReturn(plan);

        com.fasterxml.jackson.databind.node.ObjectNode jsonNode = new com.fasterxml.jackson.databind.ObjectMapper()
                .createObjectNode();
        jsonNode.putArray("content");
        List<Boolean> checks = List.of(false, false, false, false, false);

        org.junit.jupiter.api.Assertions.assertThrows(BusinessPlanException.class,
                () -> sut.upsertSubSection(1L, jsonNode, checks, SubSectionType.OVERVIEW_BASIC, 10L));
    }

    @Test
    @DisplayName("서브섹션 조회: 상세 정보를 반환한다")
    void getSubSectionDetail_returnsContent() {
        BusinessPlan plan = buildPlanWithSections(10L);
        Overview overview = plan.getOverview();

        SubSection sub = SubSection.create(SubSectionType.OVERVIEW_BASIC, "content", "{}",
                List.of(true, false, true, false, true));
        overview.putSubSection(sub);

        when(businessPlanQuery.findByIdOrThrow(1L)).thenReturn(plan);

        SubSectionResult.Detail detail = sut.getSubSectionDetail(1L, SubSectionType.OVERVIEW_BASIC, 10L);

        assertThat(detail).isNotNull();
        assertThat(detail.subSectionType()).isEqualTo(SubSectionType.OVERVIEW_BASIC);
        assertThat(detail.content()).isNotNull();
    }

    @Test
    @DisplayName("서브섹션 조회: 없으면 예외")
    void getSubSectionDetail_notFound_throws() {
        BusinessPlan plan = buildPlanWithSections(10L);
        when(businessPlanQuery.findByIdOrThrow(1L)).thenReturn(plan);

        org.junit.jupiter.api.Assertions.assertThrows(BusinessPlanException.class,
                () -> sut.getSubSectionDetail(1L, SubSectionType.OVERVIEW_BASIC, 10L));
    }

    @Test
    @DisplayName("서브섹션 조회: 소유자 아님이면 예외")
    void getSubSectionDetail_unauthorized_throws() {
        BusinessPlan plan = mock(BusinessPlan.class);
        when(plan.isOwnedBy(10L)).thenReturn(false);
        when(businessPlanQuery.findByIdOrThrow(1L)).thenReturn(plan);

        org.junit.jupiter.api.Assertions.assertThrows(BusinessPlanException.class,
                () -> sut.getSubSectionDetail(1L, SubSectionType.OVERVIEW_BASIC, 10L));
    }

    @Test
    @DisplayName("서브섹션 삭제: 정상 삭제")
    void deleteSubSection_success() {
        BusinessPlan plan = buildPlanWithSections(10L);
        Overview overview = plan.getOverview();

        SubSection sub = SubSection.create(SubSectionType.OVERVIEW_BASIC, "content", "{}",
                List.of(false, false, false, false, false));
        overview.putSubSection(sub);

        when(businessPlanQuery.findByIdOrThrow(1L)).thenReturn(plan);
        when(businessPlanQuery.save(any(BusinessPlan.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        SubSectionResult.Result res = sut.deleteSubSection(1L, SubSectionType.OVERVIEW_BASIC, 10L);

        assertThat(res).isNotNull();
        assertThat(res.subSectionType()).isEqualTo(SubSectionType.OVERVIEW_BASIC);
        assertThat(res.subSectionId()).isNull();
        assertThat(res.message()).isEqualTo("Subsection deleted");
        assertThat(overview.getSubSectionByType(SubSectionType.OVERVIEW_BASIC)).isNull();
        verify(businessPlanQuery).save(plan);
    }

    @Test
    @DisplayName("서브섹션 삭제: 소유자 아님이면 예외")
    void deleteSubSection_unauthorized_throws() {
        BusinessPlan plan = mock(BusinessPlan.class);
        when(plan.isOwnedBy(10L)).thenReturn(false);
        when(businessPlanQuery.findByIdOrThrow(1L)).thenReturn(plan);

        org.junit.jupiter.api.Assertions.assertThrows(BusinessPlanException.class,
                () -> sut.deleteSubSection(1L, SubSectionType.OVERVIEW_BASIC, 10L));
    }

    @Test
    @DisplayName("사업계획서 목록 조회(PreviewPage): 매핑 필드를 올바르게 반환한다")
    void getBusinessPlanList_returnsPreviewPage() {
        // given
        BusinessPlan plan = buildPlanWithSections(1L);
        Pageable pageable = PageRequest.of(1, 3); // 내부 0-base 가정, 여기선 1페이지(=두번째) 요청
        when(businessPlanQuery.findPreviewPage(any(Long.class), any(Pageable.class)))
                .thenReturn(new PageImpl<>(List.of(plan), pageable, 7));

        // when
        BusinessPlanResult.PreviewPage res = sut.getBusinessPlanList(1L, pageable);

        // then
        assertThat(res.totalElements()).isEqualTo(7);
        assertThat(res.size()).isEqualTo(3);
        assertThat(res.page()).isEqualTo(pageable.getPageNumber() + 1); // 1-base
        assertThat(res.totalPages()).isEqualTo((int) Math.ceil(7 / 3.0));
        assertThat(res.numberOfElements()).isEqualTo(1);
        assertThat(res.content()).hasSize(1);
        assertThat(res.content().get(0).businessPlanId()).isEqualTo(plan.getId());
        verify(businessPlanQuery).findPreviewPage(any(Long.class), any(Pageable.class));
    }

    @Test
    @DisplayName("사업계획서 전체 서브섹션을 조회하면 존재하는 서브섹션만 반환한다")
    void getBusinessPlanSubSections_returnsExistingSubSectionList() {
        BusinessPlan plan = buildPlanWithSections(10L);

        SubSection overview = SubSection.create(SubSectionType.OVERVIEW_BASIC, "overview", "{\"text\":\"overview\"}",
                List.of(false, false, false, false, false));
        plan.getOverview().putSubSection(overview);

        SubSection problem = SubSection.create(SubSectionType.PROBLEM_BACKGROUND, "problem", "{\"text\":\"problem\"}",
                List.of(false, false, false, false, false));
        plan.getProblemRecognition().putSubSection(problem);

        when(businessPlanQuery.findWithAllSubSectionsOrThrow(1L)).thenReturn(plan);

        BusinessPlanResult.Detail detail = sut.getBusinessPlanDetail(1L, 10L);

        assertThat(detail.title()).isEqualTo(plan.getTitle());
        assertThat(detail.subSectionDetailList()).hasSize(2);
        assertThat(detail.subSectionDetailList())
                .extracting(SubSectionResult.Detail::subSectionType)
                .containsExactly(SubSectionType.OVERVIEW_BASIC, SubSectionType.PROBLEM_BACKGROUND);
        assertThat(detail.subSectionDetailList().get(0).content().path("text").asText()).isEqualTo("overview");
        assertThat(detail.subSectionDetailList().get(1).content().path("text").asText()).isEqualTo("problem");
    }

    @Test
    @DisplayName("사업계획서 전체 서브섹션 조회: 소유자 아님이면 예외")
    void getBusinessPlanDetail_unauthorized_throws() {
        BusinessPlan plan = mock(BusinessPlan.class);
        when(plan.isOwnedBy(10L)).thenReturn(false);
        when(businessPlanQuery.findWithAllSubSectionsOrThrow(1L)).thenReturn(plan);

        org.junit.jupiter.api.Assertions.assertThrows(BusinessPlanException.class,
                () -> sut.getBusinessPlanDetail(1L, 10L));
    }

    @Test
    @DisplayName("서브섹션 체크: 체크리스트가 저장된다")
    void checkAndUpdateSubSection_savesChecks() {
        BusinessPlan plan = buildPlanWithSections(10L);
        Overview overview = plan.getOverview();

        List<Boolean> previousChecks = List.of(false, false, false, false, false);
        SubSection sub = SubSection.create(SubSectionType.OVERVIEW_BASIC, "previous-content", "{}", previousChecks);
        overview.putSubSection(sub);

        when(businessPlanQuery.findByIdOrThrow(1L)).thenReturn(plan);
        when(businessPlanQuery.save(any(BusinessPlan.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        List<Boolean> updatedChecks = List.of(true, true, true, true, true);
        when(checklistGrader.check(
                eq(SubSectionType.OVERVIEW_BASIC),
                eq("updated content"))).thenReturn(updatedChecks);

        com.fasterxml.jackson.databind.ObjectMapper realObjectMapper = new com.fasterxml.jackson.databind.ObjectMapper();
        com.fasterxml.jackson.databind.node.ObjectNode jsonNode = realObjectMapper.createObjectNode();
        jsonNode.putArray("content")
                .addObject()
                .put("type", "text")
                .put("value", "updated content");
        jsonNode.putArray("checks")
                .add(false)
                .add(false)
                .add(false)
                .add(false)
                .add(false);

        when(objectMapper.valueToTree(any())).thenReturn(jsonNode);
        try {
            when(objectMapper.writeValueAsString(any())).thenReturn(jsonNode.toString());
        } catch (Exception ignored) {
        }

        List<Boolean> result = sut.checkAndUpdateSubSection(1L, jsonNode, SubSectionType.OVERVIEW_BASIC, 10L);

        assertThat(result).containsExactlyElementsOf(updatedChecks);
        assertThat(sub.getChecks()).containsExactlyElementsOf(updatedChecks);
        assertThat(sub.getContent()).isEqualTo("updated content");
        verify(businessPlanQuery).save(plan);
    }

    @Test
    @DisplayName("서브섹션 체크: 없으면 예외")
    void checkAndUpdateSubSection_notFound_throws() {
        BusinessPlan plan = buildPlanWithSections(10L);
        when(businessPlanQuery.findByIdOrThrow(1L)).thenReturn(plan);

        JsonNode node = mock(JsonNode.class);
        org.junit.jupiter.api.Assertions.assertThrows(BusinessPlanException.class,
                () -> sut.checkAndUpdateSubSection(1L, node, SubSectionType.OVERVIEW_BASIC, 10L));
    }

    @Test
    @DisplayName("서브섹션 체크: 소유자 아님이면 예외")
    void checkAndUpdateSubSection_unauthorized_throws() {
        BusinessPlan plan = mock(BusinessPlan.class);
        when(plan.isOwnedBy(10L)).thenReturn(false);
        when(businessPlanQuery.findByIdOrThrow(1L)).thenReturn(plan);

        JsonNode node = mock(JsonNode.class);
        org.junit.jupiter.api.Assertions.assertThrows(BusinessPlanException.class,
                () -> sut.checkAndUpdateSubSection(1L, node, SubSectionType.OVERVIEW_BASIC, 10L));
    }

    @Test
    @DisplayName("섹션 매핑: 각 Section 타입별로 올바르게 SubSection이 저장된다")
    void createSubSection_forEachSectionType() {
        BusinessPlan plan = buildPlanWithSections(10L);
        when(businessPlanQuery.findByIdOrThrow(1L)).thenReturn(plan);
        when(businessPlanQuery.save(any(BusinessPlan.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        com.fasterxml.jackson.databind.node.ObjectNode jsonNode = new com.fasterxml.jackson.databind.ObjectMapper()
                .createObjectNode();
        jsonNode.putArray("content");
        when(objectMapper.valueToTree(any())).thenReturn(jsonNode);
        try {
            when(objectMapper.writeValueAsString(eq(jsonNode))).thenReturn("{}");
        } catch (Exception ignored) {
        }

        List<Boolean> checks = List.of(false, false, false, false, false);
        SubSectionResult.Result r1 = sut.upsertSubSection(1L, jsonNode, checks,
                SubSectionType.PROBLEM_BACKGROUND, 10L);
        SubSectionResult.Result r2 = sut.upsertSubSection(1L, jsonNode, checks,
                SubSectionType.FEASIBILITY_STRATEGY, 10L);
        SubSectionResult.Result r3 = sut.upsertSubSection(1L, jsonNode, checks, SubSectionType.GROWTH_MODEL,
                10L);
        SubSectionResult.Result r4 = sut.upsertSubSection(1L, jsonNode, checks, SubSectionType.TEAM_FOUNDER,
                10L);

        assertThat(r1.message()).isEqualTo("Subsection created");
        assertThat(r2.message()).isEqualTo("Subsection created");
        assertThat(r3.message()).isEqualTo("Subsection created");
        assertThat(r4.message()).isEqualTo("Subsection created");
    }

    @Test
    @DisplayName("서브섹션 생성: 모든 서브섹션이 생성되면 상태가 DRAFTED로 변경된다")
    void upsertSubSection_allSubSectionsCreated_updatesStatusToDrafted() {
        // given
        BusinessPlan plan = spy(buildPlanWithSections(10L));
        doReturn(true).when(plan).isOwnedBy(10L);

        // 모든 서브섹션을 생성 (마지막 하나만 남음)
        List<SubSectionType> allTypes = List.of(
                SubSectionType.OVERVIEW_BASIC,
                SubSectionType.PROBLEM_BACKGROUND, SubSectionType.PROBLEM_PURPOSE, SubSectionType.PROBLEM_MARKET,
                SubSectionType.FEASIBILITY_STRATEGY, SubSectionType.FEASIBILITY_MARKET,
                SubSectionType.GROWTH_MODEL, SubSectionType.GROWTH_FUNDING, SubSectionType.GROWTH_ENTRY,
                SubSectionType.TEAM_FOUNDER);

        for (SubSectionType type : allTypes) {
            SubSection sub = SubSection.create(type, "content", "{}", List.of(false, false, false, false, false));
            getSectionByPlanAndType(plan, type.getSectionType()).putSubSection(sub);
        }

        when(businessPlanQuery.findByIdOrThrow(1L)).thenReturn(plan);
        when(businessPlanQuery.save(any(BusinessPlan.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        com.fasterxml.jackson.databind.node.ObjectNode jsonNode = new com.fasterxml.jackson.databind.ObjectMapper()
                .createObjectNode();
        jsonNode.putArray("content");
        when(objectMapper.valueToTree(any())).thenReturn(jsonNode);
        try {
            when(objectMapper.writeValueAsString(eq(jsonNode))).thenReturn("{}");
        } catch (Exception ignored) {
        }

        // when - 마지막 서브섹션 생성
        List<Boolean> checks = List.of(false, false, false, false, false);
        sut.upsertSubSection(1L, jsonNode, checks, SubSectionType.TEAM_MEMBERS, 10L);

        // then - 상태가 WRITTEN_COMPLETED로 변경되어야 함
        verify(plan).updateStatus(starlight.domain.businessplan.enumerate.PlanStatus.WRITTEN_COMPLETED);
    }

    @Test
    @DisplayName("서브섹션 생성: 일부만 생성되면 상태가 변경되지 않는다")
    void upsertSubSection_partialSubSections_noStatusChange() {
        // given
        BusinessPlan plan = spy(buildPlanWithSections(10L));
        doReturn(true).when(plan).isOwnedBy(10L);

        when(businessPlanQuery.findByIdOrThrow(1L)).thenReturn(plan);
        when(businessPlanQuery.save(any(BusinessPlan.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        com.fasterxml.jackson.databind.node.ObjectNode jsonNode = new com.fasterxml.jackson.databind.ObjectMapper()
                .createObjectNode();
        jsonNode.putArray("content");
        when(objectMapper.valueToTree(any())).thenReturn(jsonNode);
        try {
            when(objectMapper.writeValueAsString(eq(jsonNode))).thenReturn("{}");
        } catch (Exception ignored) {
        }

        // when - 첫 번째 서브섹션만 생성
        List<Boolean> checks = List.of(false, false, false, false, false);
        sut.upsertSubSection(1L, jsonNode, checks, SubSectionType.OVERVIEW_BASIC, 10L);

        // then - 상태가 변경되지 않아야 함 (모든 서브섹션이 생성되지 않았으므로)
        verify(plan, never()).updateStatus(any());
    }

    @Test
    @DisplayName("서브섹션 삭제: 삭제 시 상태 변경이 발생하지 않는다")
    void deleteSubSection_noStatusChange() {
        // given
        BusinessPlan plan = spy(buildPlanWithSections(10L));
        doReturn(true).when(plan).isOwnedBy(10L);

        // 모든 서브섹션 생성
        for (SubSectionType type : SubSectionType.values()) {
            SubSection sub = SubSection.create(type, "content", "{}", List.of(false, false, false, false, false));
            getSectionByPlanAndType(plan, type.getSectionType()).putSubSection(sub);
        }

        when(businessPlanQuery.findByIdOrThrow(1L)).thenReturn(plan);
        when(businessPlanQuery.save(any(BusinessPlan.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        // when - 서브섹션 삭제
        sut.deleteSubSection(1L, SubSectionType.OVERVIEW_BASIC, 10L);

        // then - 상태가 변경되지 않아야 함
        verify(plan, never()).updateStatus(any());
    }

    private BaseSection getSectionByPlanAndType(BusinessPlan plan, SectionType type) {
        return switch (type) {
            case OVERVIEW -> plan.getOverview();
            case PROBLEM_RECOGNITION -> plan.getProblemRecognition();
            case FEASIBILITY -> plan.getFeasibility();
            case GROWTH_STRATEGY -> plan.getGrowthTactic();
            case TEAM_COMPETENCE -> plan.getTeamCompetence();
        };
    }
}
