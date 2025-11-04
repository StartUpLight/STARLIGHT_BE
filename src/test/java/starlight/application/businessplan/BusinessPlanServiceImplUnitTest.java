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
import starlight.application.businessplan.dto.SubSectionResponse;
import starlight.application.businessplan.required.BusinessPlanQuery;
import starlight.application.businessplan.required.ChecklistGrader;
import starlight.domain.businessplan.entity.BusinessPlan;
import starlight.domain.businessplan.entity.Overview;
import starlight.domain.businessplan.entity.SubSection;
import starlight.domain.businessplan.entity.BaseSection;
import starlight.domain.businessplan.enumerate.SubSectionType;
import starlight.domain.businessplan.exception.BusinessPlanException;
import starlight.shared.domain.enumerate.SectionType;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@org.mockito.junit.jupiter.MockitoSettings(strictness = org.mockito.quality.Strictness.LENIENT)
class BusinessPlanServiceImplUnitTest {

    @Mock
    private BusinessPlanQuery businessPlanQuery;

    @Mock
    private ChecklistGrader checklistGrader;

    @Mock
    private ObjectMapper objectMapper;

    @InjectMocks
    private BusinessPlanServiceImpl sut;

    private BusinessPlan buildPlanWithSections(Long memberId) {
        BusinessPlan plan = BusinessPlan.create(memberId);
        return plan;
    }

    @BeforeEach
    void setup() {
        when(objectMapper.valueToTree(any())).thenReturn(new com.fasterxml.jackson.databind.ObjectMapper().createObjectNode());
        try {
            when(objectMapper.writeValueAsString(any())).thenReturn("{}");
        } catch (Exception ignored) {}
    }

    @Test
    @DisplayName("사업계획서 생성 시 루트가 저장된다")
    void createBusinessPlan_savesRoot() {
        when(businessPlanQuery.save(any(BusinessPlan.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        BusinessPlan created = sut.createBusinessPlan(1L);

        assertThat(created).isNotNull();
        verify(businessPlanQuery).save(any(BusinessPlan.class));
    }

    @Test
    @DisplayName("사업계획서 제목 수정은 소유자 검증 후 저장한다")
    void updateTitle_checksOwnership_thenSaves() {
        BusinessPlan plan = spy(buildPlanWithSections(10L));
        doReturn(true).when(plan).isOwnedBy(10L);
        when(businessPlanQuery.getOrThrow(100L)).thenReturn(plan);
        when(businessPlanQuery.save(any(BusinessPlan.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        BusinessPlan updated = sut.updateBusinessPlanTitle(100L, 10L, "new-title");

        assertThat(updated).isNotNull();
        verify(businessPlanQuery).save(plan);
    }

    @Test
    @DisplayName("제목 수정 - 소유자 아님이면 예외")
    void updateTitle_unauthorized_throws() {
        BusinessPlan plan = spy(buildPlanWithSections(20L));
        doReturn(false).when(plan).isOwnedBy(10L);
        when(businessPlanQuery.getOrThrow(100L)).thenReturn(plan);

        org.junit.jupiter.api.Assertions.assertThrows(BusinessPlanException.class,
                () -> sut.updateBusinessPlanTitle(100L, 10L, "title"));
    }

    @Test
    @DisplayName("사업계획서 삭제 시 cascade로 SubSection이 함께 삭제된다")
    void deleteBusinessPlan_cascadeDeletesSubSections() {
        BusinessPlan plan = mock(BusinessPlan.class);
        when(plan.isOwnedBy(10L)).thenReturn(true);
        when(businessPlanQuery.getOrThrow(100L)).thenReturn(plan);

        assertDoesNotThrow(() -> sut.deleteBusinessPlan(100L, 10L));

        verify(businessPlanQuery).delete(plan);
    }

    @Test
    @DisplayName("서브섹션 생성: 없으면 신규 생성 후 부모 섹션에 연결하여 저장")
    void createOrUpdateSubSection_creates_whenNotExists() {
        // given
        BusinessPlan plan = buildPlanWithSections(10L);
        Overview overview = plan.getOverview();
        
        when(businessPlanQuery.getOrThrow(1L)).thenReturn(plan);
        when(businessPlanQuery.save(any(BusinessPlan.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        com.fasterxml.jackson.databind.node.ObjectNode jsonNode = new com.fasterxml.jackson.databind.ObjectMapper().createObjectNode();
        jsonNode.putArray("content");
        when(objectMapper.valueToTree(any())).thenReturn(jsonNode);
        try { when(objectMapper.writeValueAsString(eq(jsonNode))).thenReturn("{}"); } catch (Exception ignored) {}

        // when
        List<Boolean> checks = List.of(false, false, false, false, false);
        SubSectionResponse.Created res = sut.createOrUpdateSubSection(1L, jsonNode, checks, SubSectionType.OVERVIEW_BASIC, 10L);

        // then
        assertThat(res).isNotNull();
        assertThat(res.message()).isEqualTo("created");
        assertThat(overview.getSubSectionByType(SubSectionType.OVERVIEW_BASIC)).isNotNull();
        assertThat(overview.getSubSectionByType(SubSectionType.OVERVIEW_BASIC).getSubSectionType())
                .isEqualTo(SubSectionType.OVERVIEW_BASIC);
        verify(businessPlanQuery).save(plan);
    }

    @Test
    @DisplayName("서브섹션 생성: 기존 존재하면 업데이트 경로")
    void createOrUpdateSubSection_updates_whenExists() {
        BusinessPlan plan = buildPlanWithSections(10L);
        Overview overview = plan.getOverview();
        
        // 기존 SubSection 생성 및 설정
        SubSection existing = SubSection.create(SubSectionType.OVERVIEW_BASIC, "old", "{}", List.of(false, false, false, false, false));
        overview.putSubSection(existing);
        
        when(businessPlanQuery.getOrThrow(1L)).thenReturn(plan);
        when(businessPlanQuery.save(any(BusinessPlan.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        com.fasterxml.jackson.databind.node.ObjectNode jsonNode = new com.fasterxml.jackson.databind.ObjectMapper().createObjectNode();
        jsonNode.putArray("content");
        when(objectMapper.valueToTree(any())).thenReturn(jsonNode);
        try { when(objectMapper.writeValueAsString(eq(jsonNode))).thenReturn("{}"); } catch (Exception ignored) {}

        List<Boolean> checks = List.of(false, false, false, false, false);
        SubSectionResponse.Created res = sut.createOrUpdateSubSection(1L, jsonNode, checks, SubSectionType.OVERVIEW_BASIC, 10L);
        
        assertThat(res.message()).isEqualTo("updated");
        verify(businessPlanQuery).save(plan);
    }

    @Test
    @DisplayName("서브섹션 생성: 소유자 아님이면 예외")
    void createOrUpdateSubSection_unauthorized_throws() {
        BusinessPlan plan = mock(BusinessPlan.class);
        when(plan.isOwnedBy(10L)).thenReturn(false);
        when(businessPlanQuery.getOrThrow(1L)).thenReturn(plan);

        com.fasterxml.jackson.databind.node.ObjectNode jsonNode = new com.fasterxml.jackson.databind.ObjectMapper().createObjectNode();
        jsonNode.putArray("content");
        List<Boolean> checks = List.of(false, false, false, false, false);

        org.junit.jupiter.api.Assertions.assertThrows(BusinessPlanException.class,
                () -> sut.createOrUpdateSubSection(1L, jsonNode, checks, SubSectionType.OVERVIEW_BASIC, 10L));
    }

    @Test
    @DisplayName("서브섹션 조회: 본문과 체크리스트를 함께 반환")
    void getSubSection_returnsContentAndChecks() {
        BusinessPlan plan = buildPlanWithSections(10L);
        Overview overview = plan.getOverview();
        
        SubSection sub = SubSection.create(SubSectionType.OVERVIEW_BASIC, "content", "{}", List.of(true, false, true, false, true));
        overview.putSubSection(sub);
        
        when(businessPlanQuery.getOrThrow(1L)).thenReturn(plan);

        SubSectionResponse.Retrieved res = sut.getSubSection(1L, SubSectionType.OVERVIEW_BASIC, 10L);

        assertThat(res).isNotNull();
        assertThat(res.message()).isEqualTo("retrieved");
        assertThat(res.content()).isNotNull();
    }

    @Test
    @DisplayName("서브섹션 조회: 없으면 예외")
    void getSubSection_notFound_throws() {
        BusinessPlan plan = buildPlanWithSections(10L);
        when(businessPlanQuery.getOrThrow(1L)).thenReturn(plan);

        org.junit.jupiter.api.Assertions.assertThrows(BusinessPlanException.class,
                () -> sut.getSubSection(1L, SubSectionType.OVERVIEW_BASIC, 10L));
    }

    @Test
    @DisplayName("서브섹션 조회: 소유자 아님이면 예외")
    void getSubSection_unauthorized_throws() {
        BusinessPlan plan = mock(BusinessPlan.class);
        when(plan.isOwnedBy(10L)).thenReturn(false);
        when(businessPlanQuery.getOrThrow(1L)).thenReturn(plan);

        org.junit.jupiter.api.Assertions.assertThrows(BusinessPlanException.class,
                () -> sut.getSubSection(1L, SubSectionType.OVERVIEW_BASIC, 10L));
    }

    @Test
    @DisplayName("서브섹션 삭제: 정상 삭제")
    void deleteSubSection_success() {
        BusinessPlan plan = buildPlanWithSections(10L);
        Overview overview = plan.getOverview();
        
        SubSection sub = SubSection.create(SubSectionType.OVERVIEW_BASIC, "content", "{}", List.of(false, false, false, false, false));
        overview.putSubSection(sub);
        
        when(businessPlanQuery.getOrThrow(1L)).thenReturn(plan);
        when(businessPlanQuery.save(any(BusinessPlan.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        SubSectionResponse.Deleted res = sut.deleteSubSection(1L, SubSectionType.OVERVIEW_BASIC, 10L);

        assertThat(res).isNotNull();
        assertThat(overview.getSubSectionByType(SubSectionType.OVERVIEW_BASIC)).isNull();
        verify(businessPlanQuery).save(plan);
    }

    @Test
    @DisplayName("서브섹션 삭제: 소유자 아님이면 예외")
    void deleteSubSection_unauthorized_throws() {
        BusinessPlan plan = mock(BusinessPlan.class);
        when(plan.isOwnedBy(10L)).thenReturn(false);
        when(businessPlanQuery.getOrThrow(1L)).thenReturn(plan);

        org.junit.jupiter.api.Assertions.assertThrows(BusinessPlanException.class,
                () -> sut.deleteSubSection(1L, SubSectionType.OVERVIEW_BASIC, 10L));
    }

    @Test
    @DisplayName("서브섹션 체크: 체크리스트가 저장된다")
    void checkAndUpdateSubSection_savesChecks() {
        BusinessPlan plan = buildPlanWithSections(10L);
        Overview overview = plan.getOverview();
        
        SubSection sub = SubSection.create(SubSectionType.OVERVIEW_BASIC, "content", "{}", List.of(false, false, false, false, false));
        overview.putSubSection(sub);
        
        when(businessPlanQuery.getOrThrow(1L)).thenReturn(plan);
        when(businessPlanQuery.save(any(BusinessPlan.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));
        when(checklistGrader.check(eq(SubSectionType.OVERVIEW_BASIC), anyString()))
                .thenReturn(List.of(true, true, true, true, true));

        JsonNode node = mock(JsonNode.class);
        when(objectMapper.valueToTree(any())).thenReturn(node);
        try { when(objectMapper.writeValueAsString(eq(node))).thenReturn("{}"); } catch (Exception ignored) {}

        List<Boolean> result = sut.checkAndUpdateSubSection(1L, node, SubSectionType.OVERVIEW_BASIC, 10L);

        assertThat(result).containsExactly(true, true, true, true, true);
        verify(businessPlanQuery).save(plan);
    }

    @Test
    @DisplayName("서브섹션 체크: 없으면 예외")
    void checkAndUpdateSubSection_notFound_throws() {
        BusinessPlan plan = buildPlanWithSections(10L);
        when(businessPlanQuery.getOrThrow(1L)).thenReturn(plan);

        JsonNode node = mock(JsonNode.class);
        org.junit.jupiter.api.Assertions.assertThrows(BusinessPlanException.class,
                () -> sut.checkAndUpdateSubSection(1L, node, SubSectionType.OVERVIEW_BASIC, 10L));
    }

    @Test
    @DisplayName("서브섹션 체크: 소유자 아님이면 예외")
    void checkAndUpdateSubSection_unauthorized_throws() {
        BusinessPlan plan = mock(BusinessPlan.class);
        when(plan.isOwnedBy(10L)).thenReturn(false);
        when(businessPlanQuery.getOrThrow(1L)).thenReturn(plan);

        JsonNode node = mock(JsonNode.class);
        org.junit.jupiter.api.Assertions.assertThrows(BusinessPlanException.class,
                () -> sut.checkAndUpdateSubSection(1L, node, SubSectionType.OVERVIEW_BASIC, 10L));
    }

    @Test
    @DisplayName("섹션 매핑: 각 Section 타입별로 올바르게 SubSection이 저장된다")
    void createSubSection_forEachSectionType() {
        BusinessPlan plan = buildPlanWithSections(10L);
        when(businessPlanQuery.getOrThrow(1L)).thenReturn(plan);
        when(businessPlanQuery.save(any(BusinessPlan.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        com.fasterxml.jackson.databind.node.ObjectNode jsonNode = new com.fasterxml.jackson.databind.ObjectMapper().createObjectNode();
        jsonNode.putArray("content");
        when(objectMapper.valueToTree(any())).thenReturn(jsonNode);
        try { when(objectMapper.writeValueAsString(eq(jsonNode))).thenReturn("{}"); } catch (Exception ignored) {}

        List<Boolean> checks = List.of(false, false, false, false, false);
        SubSectionResponse.Created r1 = sut.createOrUpdateSubSection(1L, jsonNode, checks, SubSectionType.PROBLEM_BACKGROUND, 10L);
        SubSectionResponse.Created r2 = sut.createOrUpdateSubSection(1L, jsonNode, checks, SubSectionType.FEASIBILITY_STRATEGY, 10L);
        SubSectionResponse.Created r3 = sut.createOrUpdateSubSection(1L, jsonNode, checks, SubSectionType.GROWTH_MODEL, 10L);
        SubSectionResponse.Created r4 = sut.createOrUpdateSubSection(1L, jsonNode, checks, SubSectionType.TEAM_FOUNDER, 10L);

        assertThat(r1.message()).isEqualTo("created");
        assertThat(r2.message()).isEqualTo("created");
        assertThat(r3.message()).isEqualTo("created");
        assertThat(r4.message()).isEqualTo("created");
    }

    @Test
    @DisplayName("서브섹션 생성: 모든 서브섹션이 생성되면 상태가 DRAFTED로 변경된다")
    void createOrUpdateSubSection_allSubSectionsCreated_updatesStatusToDrafted() {
        // given
        BusinessPlan plan = spy(buildPlanWithSections(10L));
        doReturn(true).when(plan).isOwnedBy(10L);
        
        // 모든 서브섹션을 생성 (마지막 하나만 남음)
        List<SubSectionType> allTypes = List.of(
            SubSectionType.OVERVIEW_BASIC,
            SubSectionType.PROBLEM_BACKGROUND, SubSectionType.PROBLEM_PURPOSE, SubSectionType.PROBLEM_MARKET,
            SubSectionType.FEASIBILITY_STRATEGY, SubSectionType.FEASIBILITY_MARKET,
            SubSectionType.GROWTH_MODEL, SubSectionType.GROWTH_FUNDING, SubSectionType.GROWTH_ENTRY,
            SubSectionType.TEAM_FOUNDER
        );
        
        for (SubSectionType type : allTypes) {
            SubSection sub = SubSection.create(type, "content", "{}", List.of(false, false, false, false, false));
            getSectionByPlanAndType(plan, type.getSectionType()).putSubSection(sub);
        }
        
        when(businessPlanQuery.getOrThrow(1L)).thenReturn(plan);
        when(businessPlanQuery.save(any(BusinessPlan.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        com.fasterxml.jackson.databind.node.ObjectNode jsonNode = new com.fasterxml.jackson.databind.ObjectMapper().createObjectNode();
        jsonNode.putArray("content");
        when(objectMapper.valueToTree(any())).thenReturn(jsonNode);
        try { when(objectMapper.writeValueAsString(eq(jsonNode))).thenReturn("{}"); } catch (Exception ignored) {}

        // when - 마지막 서브섹션 생성
        List<Boolean> checks = List.of(false, false, false, false, false);
        sut.createOrUpdateSubSection(1L, jsonNode, checks, SubSectionType.TEAM_MEMBERS, 10L);

        // then - 상태가 DRAFTED로 변경되어야 함
        verify(plan).updateStatus(starlight.domain.businessplan.enumerate.PlanStatus.DRAFTED);
    }

    @Test
    @DisplayName("서브섹션 생성: 일부만 생성되면 상태가 변경되지 않는다")
    void createOrUpdateSubSection_partialSubSections_noStatusChange() {
        // given
        BusinessPlan plan = spy(buildPlanWithSections(10L));
        doReturn(true).when(plan).isOwnedBy(10L);
        
        when(businessPlanQuery.getOrThrow(1L)).thenReturn(plan);
        when(businessPlanQuery.save(any(BusinessPlan.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        com.fasterxml.jackson.databind.node.ObjectNode jsonNode = new com.fasterxml.jackson.databind.ObjectMapper().createObjectNode();
        jsonNode.putArray("content");
        when(objectMapper.valueToTree(any())).thenReturn(jsonNode);
        try { when(objectMapper.writeValueAsString(eq(jsonNode))).thenReturn("{}"); } catch (Exception ignored) {}

        // when - 첫 번째 서브섹션만 생성
        List<Boolean> checks = List.of(false, false, false, false, false);
        sut.createOrUpdateSubSection(1L, jsonNode, checks, SubSectionType.OVERVIEW_BASIC, 10L);

        // then - 상태가 변경되지 않아야 함 (모든 서브섹션이 생성되지 않았으므로)
        verify(plan, never()).updateStatus(any());
    }

    @Test
    @DisplayName("서브섹션 삭제: 모든 서브섹션이 생성되지 않으면 상태가 STARTED로 변경된다")
    void deleteSubSection_notAllSubSectionsCreated_updatesStatusToStarted() {
        // given
        BusinessPlan plan = spy(buildPlanWithSections(10L));
        doReturn(true).when(plan).isOwnedBy(10L);
        
        // 모든 서브섹션 생성
        for (SubSectionType type : SubSectionType.values()) {
            SubSection sub = SubSection.create(type, "content", "{}", List.of(false, false, false, false, false));
            getSectionByPlanAndType(plan, type.getSectionType()).putSubSection(sub);
        }
        
        when(businessPlanQuery.getOrThrow(1L)).thenReturn(plan);
        when(businessPlanQuery.save(any(BusinessPlan.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        // when - 서브섹션 삭제
        sut.deleteSubSection(1L, SubSectionType.OVERVIEW_BASIC, 10L);

        // then - 상태가 STARTED로 변경되어야 함
        verify(plan).updateStatus(starlight.domain.businessplan.enumerate.PlanStatus.STARTED);
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
