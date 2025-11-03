package starlight.adapter.businessplan.ai.infra;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import starlight.application.infrastructure.provided.LlmGenerator;
import org.springframework.ai.chat.client.ChatClient;
import java.util.List;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.core.type.TypeReference;

@Slf4j
@Component
@RequiredArgsConstructor
public class ChatClientLlmGenerator implements LlmGenerator {

    private final ChatClient.Builder chatClientBuilder;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public List<Boolean> generateChecklistArray(String userContent) {
        ChatClient chatClient = chatClientBuilder.build();

        String output = chatClient
                .prompt()
                .system("당신은 JSON 검증기이자 창업 사업계획서 체크리스트 채점 보조자입니다. 사용자 메시지에는 [CONTEXT], [CHECKLIST], [INPUT], [REQUEST] 섹션이 포함됩니다. 다음을 엄격히 따르세요:\n"
                        +
                        "- 출력은 오직 JSON 배열(Boolean) 하나만 반환합니다. 다른 텍스트, 주석, 키, 객체, 공백, 줄바꿈 금지.\n" +
                        "- true/false 소문자만 사용합니다.\n" +
                        "- 배열 길이는 [REQUEST]에 명시된 길이와 정확히 동일해야 합니다.\n" +
                        "- 판단은 [CONTEXT]의 근거에 한정합니다. [INPUT]은 보조적 참고만 하며, 근거 부재 시 false로 판정합니다.\n" +
                        "- 도메인 가이드: TAM/SAM/SOM, SWOT/PEST(STEEP), KPI, 제품/기능 로드맵, 자금 조달/집행(정부지원금·투자·매출·자부담), 시장 진입/확장 전략, 팀 R&R 등 용어를 정확히 해석하세요.\n"
                        +
                        "- 과잉 일반화, 환각, 추측 금지. 명시 근거가 없으면 false입니다.\n" +
                        "- 체크리스트 순서를 바꾸지 마세요.")
                .user(userContent)
                .call()
                .content();

        try {
            // 기대 포맷: [true,false,true,false,true]
            return objectMapper.readValue(output, new TypeReference<List<Boolean>>() {
            });
        } catch (Exception e) {
            log.warn("Failed to parse LLM output as boolean array. output={}", output, e);
            // 파싱 실패 시 보수적으로 모두 false 반환
            return List.of(false, false, false, false, false);
        }
    }
}
