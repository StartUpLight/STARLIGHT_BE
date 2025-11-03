package starlight.application.infrastructure.provided;

import java.util.List;

public interface LlmGenerator {

    /**
     * 사용자 프롬프트(컨텍스트+체크리스트+입력)를 기반으로
     * LLM으로부터 길이 N의 Boolean JSON 배열을 생성한다.
     */
    List<Boolean> generateChecklistArray(String userContent);
}
