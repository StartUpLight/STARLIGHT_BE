package starlight.adapter.businessplan.checklist;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import starlight.adapter.businessplan.checklist.agent.SpringAiChecklistAgent;
import starlight.adapter.businessplan.checklist.provider.ChecklistPromptProvider;
import starlight.domain.businessplan.enumerate.SubSectionType;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@DisplayName("SpringAiChecklistGrader 테스트")
class SpringAiChecklistGraderTest {

    @Test
    @DisplayName("criteria별 컨텍스트를 합치고 LLM 결과를 반환")
    void check_returnsFromLlm() {
        SpringAiChecklistAgent generator = mock(SpringAiChecklistAgent.class);
        when(generator.generateChecklistArray(any(SubSectionType.class), anyString(), anyList(), anyList()))
                .thenReturn(List.of(true, false, true, false, true));

        ChecklistPromptProvider catalog = mock(ChecklistPromptProvider.class);
        when(catalog.getCriteriaBySubSectionType(any(SubSectionType.class)))
                .thenReturn(List.of("c1", "c2", "c3", "c4", "c5"));
        when(catalog.getDetailedCriteriaBySubSectionType(any(SubSectionType.class)))
                .thenReturn(List.of("d1", "d2", "d3", "d4", "d5"));

        SpringAiChecklistGrader sut = new SpringAiChecklistGrader(generator, catalog);

        List<Boolean> result = sut.check(SubSectionType.OVERVIEW_BASIC, "input text");
        assertThat(result).containsExactly(true, false, true, false, true);
        verify(generator).generateChecklistArray(
                eq(SubSectionType.OVERVIEW_BASIC),
                eq("input text"),
                eq(List.of("c1", "c2", "c3", "c4", "c5")),
                eq(List.of("d1", "d2", "d3", "d4", "d5"))
        );
        verify(catalog).getCriteriaBySubSectionType(SubSectionType.OVERVIEW_BASIC);
        verify(catalog).getDetailedCriteriaBySubSectionType(SubSectionType.OVERVIEW_BASIC);
    }

    @Test
    @DisplayName("LLM 결과 길이가 5보다 짧으면 false로 패딩")
    void check_normalizesToFive() {
        SpringAiChecklistAgent generator = mock(SpringAiChecklistAgent.class);
        when(generator.generateChecklistArray(any(SubSectionType.class), anyString(), anyList(), anyList()))
                .thenReturn(List.of(true));

        ChecklistPromptProvider catalog = mock(ChecklistPromptProvider.class);
        when(catalog.getCriteriaBySubSectionType(any(SubSectionType.class)))
                .thenReturn(List.of("c1", "c2", "c3", "c4", "c5"));
        when(catalog.getDetailedCriteriaBySubSectionType(any(SubSectionType.class)))
                .thenReturn(List.of("d1", "d2", "d3", "d4", "d5"));

        SpringAiChecklistGrader sut = new SpringAiChecklistGrader(generator, catalog);
        List<Boolean> result = sut.check(SubSectionType.OVERVIEW_BASIC, "input text");
        assertThat(result).containsExactly(true, false, false, false, false);
    }
}
