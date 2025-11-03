package starlight.adapter.businessplan.ai.infra;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.ai.chat.client.ChatClient;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

class ChatClientLlmGeneratorTest {

    @Test
    @DisplayName("올바른 JSON 배열을 파싱해 반환")
    void generateChecklistArray_parsesJson() {
        ChatClient chatClient = mock(ChatClient.class, withSettings().defaultAnswer(org.mockito.Answers.RETURNS_DEEP_STUBS));
        ChatClient.Builder builder = mock(ChatClient.Builder.class);
        when(builder.build()).thenReturn(chatClient);

        when(chatClient.prompt().system(anyString()).user(anyString()).call().content())
                .thenReturn("[true,false,true,false,true]");

        ChatClientLlmGenerator sut = new ChatClientLlmGenerator(builder);

        List<Boolean> result = sut.generateChecklistArray("input");
        assertThat(result).containsExactly(true, false, true, false, true);
    }

    @Test
    @DisplayName("파싱 실패 시 보수적으로 모두 false 반환")
    void generateChecklistArray_parseFail_returnsAllFalse() {
        ChatClient chatClient = mock(ChatClient.class, withSettings().defaultAnswer(org.mockito.Answers.RETURNS_DEEP_STUBS));
        ChatClient.Builder builder = mock(ChatClient.Builder.class);
        when(builder.build()).thenReturn(chatClient);

        when(chatClient.prompt().system(anyString()).user(anyString()).call().content())
                .thenReturn("not-json");

        ChatClientLlmGenerator sut = new ChatClientLlmGenerator(builder);

        List<Boolean> result = sut.generateChecklistArray("input");
        assertThat(result).containsExactly(false, false, false, false, false);
    }
}
