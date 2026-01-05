package starlight.adapter.aireport.reportgrader.provider;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.advisor.SimpleLoggerAdvisor;
import org.springframework.ai.chat.client.advisor.vectorstore.QuestionAnswerAdvisor;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.core.Ordered;
import org.springframework.stereotype.Service;

@Service
@Slf4j
@RequiredArgsConstructor
public class SpringAiAdvisorProvider {

    private final VectorStore vectorStore;

    public QuestionAnswerAdvisor getQuestionAnswerAdvisor(double similarityThreshold, int topK, String filter){
        SearchRequest.Builder builder = SearchRequest.builder()
                .similarityThreshold(similarityThreshold)
                .topK(topK);

        if (filter != null && !filter.trim().isEmpty()) {
            builder.filterExpression(filter);
        }

        SearchRequest searchRequest = builder.build();

        return QuestionAnswerAdvisor
                .builder(vectorStore)
                .searchRequest(searchRequest)
                .build();
    }

    public SimpleLoggerAdvisor getSimpleLoggerAdvisor(){
        return new SimpleLoggerAdvisor(Ordered.LOWEST_PRECEDENCE-1);
    }
}
