package starlight.adapter.ai;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import starlight.adapter.ai.infra.OpenAiGenerator;
import starlight.adapter.ai.util.AiReportResponseParser;
import starlight.application.aireport.provided.dto.AiReportResponse;
import starlight.application.aireport.required.AiReportGrader;

/**
 * AI 리포트 채점을 오케스트레이션하는 컴포넌트
 * 각 단계별 책임을 다른 컴포넌트에 위임하여 단일 책임 원칙을 준수
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class OpenAiReportGrader implements AiReportGrader {

    private final OpenAiGenerator chatClientGenerator;
    private final AiReportResponseParser responseParser;

    @Override
    public AiReportResponse gradeContent(String content){
        String llmResponse = chatClientGenerator.generateReport(content);

        return responseParser.parse(llmResponse);
    }
}
