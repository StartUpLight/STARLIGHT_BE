package starlight.adapter.businessplan.ai;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import starlight.adapter.businessplan.ai.infra.ChatClientLlmGenerator;
import starlight.adapter.businessplan.ai.infra.VectorStoreContextRetriever;
import starlight.adapter.businessplan.ai.infra.ChecklistCatalog;
import starlight.domain.businessplan.enumerate.SubSectionName;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class RagChecklistGraderTest {

    @Test
    @DisplayName("criteria별 컨텍스트를 합치고 LLM 결과를 반환")
    void check_returnsFromLlm() {
        VectorStoreContextRetriever retriever = mock(VectorStoreContextRetriever.class);
        when(retriever.retrieveContext(anyString(), anyString(), anyInt())).thenReturn("CTX");

        ChatClientLlmGenerator generator = mock(ChatClientLlmGenerator.class);
        when(generator.generateChecklistArray(anyString()))
                .thenReturn(List.of(true, false, true, false, true));

        ChecklistCatalog catalog = mock(ChecklistCatalog.class);
        when(catalog.getCriteriaByTag(anyString()))
                .thenReturn(List.of("c1", "c2", "c3", "c4", "c5"));

        RagChecklistGrader sut = new RagChecklistGrader(retriever, generator, catalog);

        List<Boolean> result = sut.check(SubSectionName.OVERVIEW_BASIC, "input text");
        assertThat(result).containsExactly(true, false, true, false, true);
        verify(generator).generateChecklistArray(anyString());
        verify(retriever, atLeastOnce()).retrieveContext(eq(SubSectionName.OVERVIEW_BASIC.getTag()), anyString(), eq(3));
    }

    @Test
    @DisplayName("LLM 결과 길이가 5보다 짧으면 false로 패딩")
    void check_normalizesToFive() {
        VectorStoreContextRetriever retriever = mock(VectorStoreContextRetriever.class);
        when(retriever.retrieveContext(anyString(), anyString(), anyInt())).thenReturn("CTX");

        ChatClientLlmGenerator generator = mock(ChatClientLlmGenerator.class);
        when(generator.generateChecklistArray(anyString())).thenReturn(List.of(true));

        ChecklistCatalog catalog = mock(ChecklistCatalog.class);
        when(catalog.getCriteriaByTag(anyString()))
                .thenReturn(List.of("c1", "c2", "c3", "c4", "c5"));

        RagChecklistGrader sut = new RagChecklistGrader(retriever, generator, catalog);
        List<Boolean> result = sut.check(SubSectionName.OVERVIEW_BASIC, "input text");
        assertThat(result).containsExactly(true, false, false, false, false);
    }
}
