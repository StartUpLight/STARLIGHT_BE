package starlight.adapter.businessplan.ai.infra;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.document.Document;
import org.springframework.stereotype.Component;
import starlight.application.infrastructure.provided.ContextRetriever;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Spring AI를 사용한 RAG 구현체
 * VectorStore와 EmbeddingModel을 사용하여 컨텍스트를 검색합니다.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class VectorStoreContextRetriever implements ContextRetriever {

    private final VectorStore vectorStore;

    /**
     * 주어진 서브섹션 태그(예: feasibility_strategy)와 사용자 입력의 순수 텍스트를 사용해
     * 벡터 DB에서 topK 문맥을 검색하여 결합 문자열로 반환한다.
     */
    @Override
    public String retrieveContext(String subSectionTag, String content, int topK) {
        String query = content;

        // Pinecone 메타데이터 필터: tag == subSectionTag
        String filter = "tag == '" + subSectionTag + "'";

        List<Document> docs = vectorStore.similaritySearch(
                SearchRequest.builder()
                        .query(query)
                        .topK(topK)
                        .filterExpression(filter)
                        .build()
        );

        String joined = docs.stream()
                .map(doc -> doc.getText())
                .collect(Collectors.joining("\n---\n"));

        log.debug("Retrieved {} docs for tag={} topK={}", docs.size(), subSectionTag, topK);
        return joined;
    }
}
