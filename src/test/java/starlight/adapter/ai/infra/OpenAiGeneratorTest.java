package starlight.adapter.ai.infra;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.prompt.Prompt;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class OpenAiGeneratorTest {

    @Test
    @DisplayName("올바른 JSON 배열을 파싱해 반환")
    void generateChecklistArray_parsesJson() {
        ChatClient chatClient = mock(ChatClient.class, RETURNS_DEEP_STUBS);
        ChatClient.Builder builder = mock(ChatClient.Builder.class);
        when(builder.build()).thenReturn(chatClient);

        // RETURNS_DEEP_STUBS를 사용하여 체인이 자동으로 처리되도록 하고,
        // 실제 content() 호출 시 값을 반환하도록 설정
        lenient().when(chatClient.prompt(any(Prompt.class)).call().content())
                .thenReturn("[true,false,true,false,true]");

        PromptProvider promptProvider = mock(PromptProvider.class);
        when(promptProvider.createChecklistGradingPrompt(anyString(), anyList(), isNull(), isNull()))
                .thenReturn(mock(Prompt.class));

        AdvisorProvider advisorProvider = mock(AdvisorProvider.class);

        OpenAiGenerator sut = new OpenAiGenerator(builder, promptProvider, advisorProvider);

        List<Boolean> result = sut.generateChecklistArray("test content", List.of("c1", "c2", "c3", "c4", "c5"), null, null);
        assertThat(result).containsExactly(true, false, true, false, true);
    }

    @Test
    @DisplayName("파싱 실패 시 보수적으로 모두 false 반환")
    void generateChecklistArray_parseFail_returnsAllFalse() {
        ChatClient chatClient = mock(ChatClient.class, RETURNS_DEEP_STUBS);
        ChatClient.Builder builder = mock(ChatClient.Builder.class);
        when(builder.build()).thenReturn(chatClient);

        lenient().when(chatClient.prompt(any(Prompt.class)).call().content())
                .thenReturn("not-json");

        PromptProvider promptProvider = mock(PromptProvider.class);
        when(promptProvider.createChecklistGradingPrompt(anyString(), anyList(), isNull(), isNull()))
                .thenReturn(mock(Prompt.class));

        AdvisorProvider advisorProvider = mock(AdvisorProvider.class);

        OpenAiGenerator sut = new OpenAiGenerator(builder, promptProvider, advisorProvider);

        List<Boolean> result = sut.generateChecklistArray("test content", List.of("c1", "c2", "c3", "c4", "c5"), null, null);
        assertThat(result).containsExactly(false, false, false, false, false);
    }
}
