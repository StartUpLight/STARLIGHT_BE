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
import starlight.domain.businessplan.enumerate.SubSectionType;
import starlight.domain.businessplan.exception.BusinessPlanException;

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
        SubSectionResponse.Created res = sut.createOrUpdateSubSection(1L, jsonNode, SubSectionType.OVERVIEW_BASIC, 10L);

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
        SubSection existing = SubSection.create(SubSectionType.OVERVIEW_BASIC, "old", "{}");
        overview.putSubSection(existing);
        
        when(businessPlanQuery.getOrThrow(1L)).thenReturn(plan);
        when(businessPlanQuery.save(any(BusinessPlan.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        com.fasterxml.jackson.databind.node.ObjectNode jsonNode = new com.fasterxml.jackson.databind.ObjectMapper().createObjectNode();
        jsonNode.putArray("content");
        when(objectMapper.valueToTree(any())).thenReturn(jsonNode);
        try { when(objectMapper.writeValueAsString(eq(jsonNode))).thenReturn("{}"); } catch (Exception ignored) {}

        SubSectionResponse.Created res = sut.createOrUpdateSubSection(1L, jsonNode, SubSectionType.OVERVIEW_BASIC, 10L);
        
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

        org.junit.jupiter.api.Assertions.assertThrows(BusinessPlanException.class,
                () -> sut.createOrUpdateSubSection(1L, jsonNode, SubSectionType.OVERVIEW_BASIC, 10L));
    }

    @Test
    @DisplayName("서브섹션 조회: 본문과 체크리스트를 함께 반환")
    void getSubSection_returnsContentAndChecks() {
        BusinessPlan plan = buildPlanWithSections(10L);
        Overview overview = plan.getOverview();
        
        SubSection sub = SubSection.create(SubSectionType.OVERVIEW_BASIC, "content", "{}");
        overview.putSubSection(sub);
        
        when(businessPlanQuery.getOrThrow(1L)).thenReturn(plan);
        
        // 체크리스트 설정
        sub.updateChecks(List.of(true, false, true, false, true));

        SubSectionResponse.Retrieved res = sut.getSubSection(1L, SubSectionType.OVERVIEW_BASIC, 10L);

        assertThat(res).isNotNull();
        assertThat(res.checks()).containsExactly(true, false, true, false, true);
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
        
        SubSection sub = SubSection.create(SubSectionType.OVERVIEW_BASIC, "content", "{}");
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
        
        SubSection sub = SubSection.create(SubSectionType.OVERVIEW_BASIC, "content", "{}");
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

        SubSectionResponse.Created r1 = sut.createOrUpdateSubSection(1L, jsonNode, SubSectionType.PROBLEM_BACKGROUND, 10L);
        SubSectionResponse.Created r2 = sut.createOrUpdateSubSection(1L, jsonNode, SubSectionType.FEASIBILITY_STRATEGY, 10L);
        SubSectionResponse.Created r3 = sut.createOrUpdateSubSection(1L, jsonNode, SubSectionType.GROWTH_MODEL, 10L);
        SubSectionResponse.Created r4 = sut.createOrUpdateSubSection(1L, jsonNode, SubSectionType.TEAM_FOUNDER, 10L);

        assertThat(r1.message()).isEqualTo("created");
        assertThat(r2.message()).isEqualTo("created");
        assertThat(r3.message()).isEqualTo("created");
        assertThat(r4.message()).isEqualTo("created");
    }
}
