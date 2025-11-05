package starlight.adapter.ai;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import starlight.adapter.ai.infra.OpenAiGenerator;
import starlight.adapter.ai.util.AiReportResponseParser;
import starlight.adapter.ai.util.BusinessPlanContentExtractor;
import starlight.application.aireport.dto.AiReportResponse;
import starlight.application.aireport.required.AiReportGrader;
import starlight.domain.businessplan.entity.BusinessPlan;

/**
 * AI 리포트 채점을 오케스트레이션하는 컴포넌트
 * 각 단계별 책임을 다른 컴포넌트에 위임하여 단일 책임 원칙을 준수
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class OpenAiReportGrader implements AiReportGrader {

    private final OpenAiGenerator chatClientGenerator;
    private final BusinessPlanContentExtractor contentExtractor;
    private final AiReportResponseParser responseParser;

    @Override
    public AiReportResponse grade(BusinessPlan businessPlan) {
        // 1. BusinessPlan에서 컨텐츠 추출
        String businessPlanContent = contentExtractor.extractContent(businessPlan);

        // 2. LLM 호출
        String llmResponse = chatClientGenerator.generateReport(businessPlanContent);

        // 3. 응답 파싱
        return responseParser.parse(llmResponse);
    }
}
