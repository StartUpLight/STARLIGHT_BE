package starlight.adapter.ai;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import starlight.adapter.ai.OpenAiChecklistGrader;
import starlight.adapter.ai.infra.OpenAiGenerator;
import starlight.adapter.ai.util.ChecklistCatalog;
import starlight.domain.businessplan.enumerate.SubSectionType;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class AiChecklistGraderTest {

    @Test
    @DisplayName("criteria별 컨텍스트를 합치고 LLM 결과를 반환")
    void check_returnsFromLlm() {
        OpenAiGenerator generator = mock(OpenAiGenerator.class);
        when(generator.generateChecklistArray(anyString(), anyList(), isNull(), isNull()))
                .thenReturn(List.of(true, false, true, false, true));

        ChecklistCatalog catalog = mock(ChecklistCatalog.class);
        when(catalog.getCriteriaByTag(anyString()))
                .thenReturn(List.of("c1", "c2", "c3", "c4", "c5"));

        OpenAiChecklistGrader sut = new OpenAiChecklistGrader(generator, catalog);

        List<Boolean> result = sut.check(SubSectionType.OVERVIEW_BASIC, "input text", null, null);
        assertThat(result).containsExactly(true, false, true, false, true);
        verify(generator).generateChecklistArray(eq("input text"), anyList(), isNull(), isNull());
    }

    @Test
    @DisplayName("LLM 결과 길이가 5보다 짧으면 false로 패딩")
    void check_normalizesToFive() {
        OpenAiGenerator generator = mock(OpenAiGenerator.class);
        when(generator.generateChecklistArray(anyString(), anyList(), isNull(), isNull()))
                .thenReturn(List.of(true));

        ChecklistCatalog catalog = mock(ChecklistCatalog.class);
        when(catalog.getCriteriaByTag(anyString()))
                .thenReturn(List.of("c1", "c2", "c3", "c4", "c5"));

        OpenAiChecklistGrader sut = new OpenAiChecklistGrader(generator, catalog);
        List<Boolean> result = sut.check(SubSectionType.OVERVIEW_BASIC, "input text", null, null);
        assertThat(result).containsExactly(true, false, false, false, false);
    }

    @Test
    @DisplayName("이전 정보가 있으면 이전 정보를 포함하여 체크")
    void check_withPreviousContent() {
        OpenAiGenerator generator = mock(OpenAiGenerator.class);
        when(generator.generateChecklistArray(eq("new content"), anyList(), eq("previous content"), anyList()))
                .thenReturn(List.of(true, true, true, true, true));

        ChecklistCatalog catalog = mock(ChecklistCatalog.class);
        when(catalog.getCriteriaByTag(anyString()))
                .thenReturn(List.of("c1", "c2", "c3", "c4", "c5"));

        OpenAiChecklistGrader sut = new OpenAiChecklistGrader(generator, catalog);

        List<Boolean> previousChecks = List.of(false, false, false, false, false);
        List<Boolean> result = sut.check(SubSectionType.OVERVIEW_BASIC, "new content", "previous content", previousChecks);
        
        assertThat(result).containsExactly(true, true, true, true, true);
        verify(generator).generateChecklistArray(eq("new content"), anyList(), eq("previous content"), eq(previousChecks));
    }
}
