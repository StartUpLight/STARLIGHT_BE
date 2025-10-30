package starlight.application.businessplan;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import starlight.application.businessplan.dto.SubSectionResponse;
import starlight.application.businessplan.required.BusinessPlanQuery;
import starlight.application.businessplan.required.ChecklistGrader;
import starlight.domain.businessplan.entity.BusinessPlan;
import starlight.domain.businessplan.entity.Feasibility;
import starlight.domain.businessplan.entity.Overview;
import starlight.domain.businessplan.entity.SubSection;
import starlight.domain.businessplan.enumerate.SectionName;
import starlight.domain.businessplan.enumerate.SubSectionName;
import starlight.domain.businessplan.exception.BusinessPlanException;

import java.util.List;
import java.util.Optional;

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
        // ensure section ids are non-null when mocked repository assigns; in unit test,
        // we simulate ids
        // Using reflection is unnecessary; we will stub get...().getId() access in
        // tests via spies/mocks if needed
        return plan;
    }

    @BeforeEach
    void setup() {
        // default: objectMapper.valueToTree(any()) returns empty object
        when(objectMapper.valueToTree(any())).thenReturn(new com.fasterxml.jackson.databind.ObjectMapper().createObjectNode());
        // default: writeValueAsString(any()) returns minimal json
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
    @DisplayName("사업계획서 삭제 시 관련 SubSection을 선삭제한다")
    void deleteBusinessPlan_deletesSubSectionsFirst() {
        // given
        BusinessPlan plan = mock(BusinessPlan.class);
        Overview overview = mock(Overview.class);
        Feasibility feasibility = mock(Feasibility.class);
        when(plan.isOwnedBy(10L)).thenReturn(true);
        when(plan.getOverview()).thenReturn(overview);
        when(plan.getFeasibility()).thenReturn(feasibility);
        when(overview.getId()).thenReturn(111L);
        when(feasibility.getId()).thenReturn(333L);
        when(businessPlanQuery.getOrThrow(100L)).thenReturn(plan);

        // when
        assertDoesNotThrow(() -> sut.deleteBusinessPlan(100L, 10L));

        // then: called delete for each present parent section id
        verify(businessPlanQuery).deleteSubSectionsByParentSectionId(111L);
        verify(businessPlanQuery).deleteSubSectionsByParentSectionId(333L);
        verify(businessPlanQuery).delete(plan);
    }

    @Test
    @DisplayName("서브섹션 생성: 없으면 신규 생성 후 부모 섹션에 연결하여 저장")
    void createOrUpdateSection_creates_whenNotExists() {
        // given
        BusinessPlan plan = mock(BusinessPlan.class);
        Overview overview = mock(Overview.class);
        when(plan.getOverview()).thenReturn(overview);
        when(overview.getId()).thenReturn(777L);
        when(businessPlanQuery.getOrThrow(1L)).thenReturn(plan);

        com.fasterxml.jackson.databind.node.ObjectNode jsonNode = new com.fasterxml.jackson.databind.ObjectMapper().createObjectNode();
        jsonNode.putArray("content");
        when(objectMapper.valueToTree(any())).thenReturn(jsonNode);
        try { when(objectMapper.writeValueAsString(eq(jsonNode))).thenReturn("{}"); } catch (Exception ignored) {}

        // find none -> create
        when(businessPlanQuery.findSubSectionByParentSectionIdAndName(777L, SubSectionName.OVERVIEW_BASIC))
                .thenReturn(Optional.empty());

        ArgumentCaptor<SubSection> savedCaptor = ArgumentCaptor.forClass(SubSection.class);
        when(businessPlanQuery.saveSubSection(savedCaptor.capture()))
                .thenAnswer(invocation -> invocation.getArgument(0));

        // when
        SubSectionResponse.Created res = sut.createOrUpdateSection(1L, jsonNode, SubSectionName.OVERVIEW_BASIC);

        // then
        assertThat(res).isNotNull();
        verify(businessPlanQuery).saveSubSection(any(SubSection.class));
        SubSection saved = savedCaptor.getValue();
        assertThat(saved.getSubSectionName()).isEqualTo(SubSectionName.OVERVIEW_BASIC);
        assertThat(saved.getParentSectionName()).isEqualTo(SectionName.OVERVIEW);
        assertThat(saved.getParentSectionId()).isEqualTo(777L);
    }

    @Test
    @DisplayName("서브섹션 생성: 기존 존재하면 업데이트 경로")
    void createOrUpdateSection_updates_whenExists() {
        BusinessPlan plan = mock(BusinessPlan.class);
        Overview overview = mock(Overview.class);
        when(plan.getOverview()).thenReturn(overview);
        when(overview.getId()).thenReturn(777L);
        when(businessPlanQuery.getOrThrow(1L)).thenReturn(plan);

        com.fasterxml.jackson.databind.node.ObjectNode jsonNode = new com.fasterxml.jackson.databind.ObjectMapper().createObjectNode();
        jsonNode.putArray("content");
        try { when(objectMapper.writeValueAsString(eq(jsonNode))).thenReturn("{}"); } catch (Exception ignored) {}
        when(businessPlanQuery.findSubSectionByParentSectionIdAndName(777L, SubSectionName.OVERVIEW_BASIC))
                .thenReturn(Optional.of(SubSection.create(SubSectionName.OVERVIEW_BASIC, "old", "{}")));
        when(businessPlanQuery.saveSubSection(any(SubSection.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        SubSectionResponse.Created res = sut.createOrUpdateSection(1L, jsonNode, SubSectionName.OVERVIEW_BASIC);
        assertThat(res.message()).isEqualTo("updated");
    }

    @Test
    @DisplayName("서브섹션 조회: 본문과 체크리스트를 함께 반환")
    void getSubSection_returnsContentAndChecks() {
        BusinessPlan plan = mock(BusinessPlan.class);
        Overview overview = mock(Overview.class);
        when(plan.getOverview()).thenReturn(overview);
        when(overview.getId()).thenReturn(5L);
        when(businessPlanQuery.getOrThrow(1L)).thenReturn(plan);

        SubSection sub = Mockito.mock(SubSection.class);
        when(sub.getRawJson()).thenReturn(starlight.domain.businessplan.value.RawJson.create("{}"));
        when(sub.isCheckFirst()).thenReturn(true);
        when(sub.isCheckSecond()).thenReturn(false);
        when(sub.isCheckThird()).thenReturn(true);
        when(sub.isCheckFourth()).thenReturn(false);
        when(sub.isCheckFifth()).thenReturn(true);

        when(businessPlanQuery.findSubSectionByParentSectionIdAndName(5L, SubSectionName.OVERVIEW_BASIC))
                .thenReturn(Optional.of(sub));

        SubSectionResponse.Retrieved res = sut.getSubSection(1L, SubSectionName.OVERVIEW_BASIC);

        assertThat(res).isNotNull();
        assertThat(res.checks()).containsExactly(true, false, true, false, true);
    }

    @Test
    @DisplayName("서브섹션 조회: 없으면 예외")
    void getSubSection_notFound_throws() {
        BusinessPlan plan = mock(BusinessPlan.class);
        Overview overview = mock(Overview.class);
        when(plan.getOverview()).thenReturn(overview);
        when(overview.getId()).thenReturn(7L);
        when(businessPlanQuery.getOrThrow(1L)).thenReturn(plan);
        when(businessPlanQuery.findSubSectionByParentSectionIdAndName(7L, SubSectionName.OVERVIEW_BASIC))
                .thenReturn(Optional.empty());

        org.junit.jupiter.api.Assertions.assertThrows(BusinessPlanException.class,
                () -> sut.getSubSection(1L, SubSectionName.OVERVIEW_BASIC));
    }

    @Test
    @DisplayName("서브섹션 삭제: 없으면 예외")
    void deleteSubSection_notFound_throws() {
        BusinessPlan plan = mock(BusinessPlan.class);
        Overview overview = mock(Overview.class);
        when(plan.getOverview()).thenReturn(overview);
        when(overview.getId()).thenReturn(7L);
        when(businessPlanQuery.getOrThrow(1L)).thenReturn(plan);
        when(businessPlanQuery.findSubSectionByParentSectionIdAndName(7L, SubSectionName.OVERVIEW_BASIC))
                .thenReturn(Optional.empty());

        org.junit.jupiter.api.Assertions.assertThrows(BusinessPlanException.class,
                () -> sut.deleteSubSection(1L, SubSectionName.OVERVIEW_BASIC));
    }

    @Test
    @DisplayName("서브섹션 체크: 체크리스트가 저장된다")
    void checkAndUpdateSubSection_savesChecks() {
        BusinessPlan plan = mock(BusinessPlan.class);
        Overview overview = mock(Overview.class);
        when(plan.getOverview()).thenReturn(overview);
        when(overview.getId()).thenReturn(9L);
        when(businessPlanQuery.getOrThrow(1L)).thenReturn(plan);

        SubSection sub = SubSection.create(SubSectionName.OVERVIEW_BASIC, "content", "{}");
        sub.attachToParent(9L, SectionName.OVERVIEW);

        when(businessPlanQuery.findSubSectionByParentSectionIdAndName(9L, SubSectionName.OVERVIEW_BASIC))
                .thenReturn(Optional.of(sub));
        when(checklistGrader.check(eq(SubSectionName.OVERVIEW_BASIC), anyString()))
                .thenReturn(List.of(true, true, true, true, true));

        JsonNode node = mock(JsonNode.class);

        List<Boolean> result = sut.checkAndUpdateSubSection(1L, node, SubSectionName.OVERVIEW_BASIC);

        assertThat(result).containsExactly(true, true, true, true, true);
        verify(businessPlanQuery).saveSubSection(any(SubSection.class));
    }

    @Test
    @DisplayName("서브섹션 체크: 없으면 예외")
    void checkAndUpdateSubSection_notFound_throws() {
        BusinessPlan plan = mock(BusinessPlan.class);
        Overview overview = mock(Overview.class);
        when(plan.getOverview()).thenReturn(overview);
        when(overview.getId()).thenReturn(7L);
        when(businessPlanQuery.getOrThrow(1L)).thenReturn(plan);
        when(businessPlanQuery.findSubSectionByParentSectionIdAndName(7L, SubSectionName.OVERVIEW_BASIC))
                .thenReturn(Optional.empty());

        JsonNode node = mock(JsonNode.class);
        org.junit.jupiter.api.Assertions.assertThrows(BusinessPlanException.class,
                () -> sut.checkAndUpdateSubSection(1L, node, SubSectionName.OVERVIEW_BASIC));
    }

    @Test
    @DisplayName("섹션 매핑: PROBLEM/FEASIBILITY/GROWTH/TEAM 각각 올바른 parentId 선택")
    void attachMapping_forEachSection() {
        BusinessPlan plan = mock(BusinessPlan.class);
        var pr = mock(starlight.domain.businessplan.entity.ProblemRecognition.class);
        var feas = mock(starlight.domain.businessplan.entity.Feasibility.class);
        var growth = mock(starlight.domain.businessplan.entity.GrowthTactic.class);
        var team = mock(starlight.domain.businessplan.entity.TeamCompetence.class);
        when(plan.getProblemRecognition()).thenReturn(pr);
        when(plan.getFeasibility()).thenReturn(feas);
        when(plan.getGrowthTactic()).thenReturn(growth);
        when(plan.getTeamCompetence()).thenReturn(team);
        when(pr.getId()).thenReturn(11L);
        when(feas.getId()).thenReturn(22L);
        when(growth.getId()).thenReturn(33L);
        when(team.getId()).thenReturn(44L);

        when(businessPlanQuery.getOrThrow(1L)).thenReturn(plan);

        com.fasterxml.jackson.databind.node.ObjectNode jsonNode = new com.fasterxml.jackson.databind.ObjectMapper().createObjectNode();
        jsonNode.putArray("content");
        try { when(objectMapper.writeValueAsString(eq(jsonNode))).thenReturn("{}"); } catch (Exception ignored) {}

        when(businessPlanQuery.findSubSectionByParentSectionIdAndName(11L, SubSectionName.PROBLEM_BACKGROUND))
                .thenReturn(Optional.empty());
        when(businessPlanQuery.findSubSectionByParentSectionIdAndName(22L, SubSectionName.FEASIBILITY_STRATEGY))
                .thenReturn(Optional.empty());
        when(businessPlanQuery.findSubSectionByParentSectionIdAndName(33L, SubSectionName.GROWTH_MODEL))
                .thenReturn(Optional.empty());
        when(businessPlanQuery.findSubSectionByParentSectionIdAndName(44L, SubSectionName.TEAM_FOUNDER))
                .thenReturn(Optional.empty());

        when(businessPlanQuery.saveSubSection(any(SubSection.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        SubSectionResponse.Created r1 = sut.createOrUpdateSection(1L, jsonNode, SubSectionName.PROBLEM_BACKGROUND);
        SubSectionResponse.Created r2 = sut.createOrUpdateSection(1L, jsonNode, SubSectionName.FEASIBILITY_STRATEGY);
        SubSectionResponse.Created r3 = sut.createOrUpdateSection(1L, jsonNode, SubSectionName.GROWTH_MODEL);
        SubSectionResponse.Created r4 = sut.createOrUpdateSection(1L, jsonNode, SubSectionName.TEAM_FOUNDER);

        assertThat(r1.message()).isEqualTo("created");
        assertThat(r2.message()).isEqualTo("created");
        assertThat(r3.message()).isEqualTo("created");
        assertThat(r4.message()).isEqualTo("created");
    }
}
