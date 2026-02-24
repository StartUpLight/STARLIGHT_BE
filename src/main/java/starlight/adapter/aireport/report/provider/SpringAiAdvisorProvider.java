package starlight.adapter.aireport.report.provider;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.advisor.SimpleLoggerAdvisor;
import org.springframework.ai.chat.client.advisor.vectorstore.QuestionAnswerAdvisor;
import org.springframework.ai.chat.prompt.PromptTemplate;
import org.springframework.ai.template.st.StTemplateRenderer;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.Ordered;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

@Service
@Slf4j
@RequiredArgsConstructor
public class SpringAiAdvisorProvider {

    private final VectorStore vectorStore;

    @Value("${prompt.report.qa-advisor.template:}")
    private String qaAdvisorTemplate;

    public QuestionAnswerAdvisor getQuestionAnswerAdvisor(double similarityThreshold, int topK, String filter){
        SearchRequest.Builder builder = SearchRequest.builder()
                .similarityThreshold(similarityThreshold)
                .topK(topK);

        if (filter != null && !filter.trim().isEmpty()) {
            builder.filterExpression(filter);
        }

        SearchRequest searchRequest = builder.build();
        QuestionAnswerAdvisor.Builder advisorBuilder = QuestionAnswerAdvisor
                .builder(vectorStore)
                .searchRequest(searchRequest);

        if (StringUtils.hasText(qaAdvisorTemplate)) {
            PromptTemplate promptTemplate = PromptTemplate.builder()
                    .renderer(StTemplateRenderer.builder().startDelimiterToken('{').endDelimiterToken('}').build())
                    .template(qaAdvisorTemplate)
                    .build();
            advisorBuilder.promptTemplate(promptTemplate);
        }

        return advisorBuilder.build();
    }

    public SimpleLoggerAdvisor getSimpleLoggerAdvisor(){
        return new SimpleLoggerAdvisor(Ordered.LOWEST_PRECEDENCE-1);
    }
}
