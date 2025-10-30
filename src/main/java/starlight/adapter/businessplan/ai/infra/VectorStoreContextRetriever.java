package starlight.adapter.businessplan.ai.infra;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.stereotype.Component;
import starlight.application.infrastructure.provided.ContextRetriever;

import java.util.List;

/**
 * Spring AI를 사용한 RAG 구현체
 * VectorStore와 EmbeddingModel을 사용하여 컨텍스트를 검색합니다.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class VectorStoreContextRetriever implements ContextRetriever {

    private final VectorStore vectorStore;
    private final EmbeddingModel embeddingModel;

    /**
     * 주어진 서브섹션 태그(예: feasibility_strategy)와 사용자 입력의 순수 텍스트를 사용해
     * 벡터 DB에서 topK 문맥을 검색하여 결합 문자열로 반환한다.
     */
    @Override
    public String retrieveContext(String subSectionTag, String content, int topK) {
        // TODO: Spring AI 의존성 해결 후 구현
        log.warn("SpringAiRagRetriever is not yet implemented due to dependency issues");
        return "";
    }

    /**
     * 문서를 VectorStore에 추가합니다.
     * TODO: Spring AI 의존성 해결 후 구현
     */
    public void addDocument(String content, java.util.Map<String, Object> metadata) {
        log.warn("addDocument is not yet implemented due to dependency issues");
    }

    /**
     * 특정 메타데이터 필터를 사용하여 검색합니다.
     * TODO: Spring AI 의존성 해결 후 구현
     */
    public List<Object> searchWithFilter(String query, int topK, String filterExpression) {
        log.warn("searchWithFilter is not yet implemented due to dependency issues");
        return List.of();
    }
}
