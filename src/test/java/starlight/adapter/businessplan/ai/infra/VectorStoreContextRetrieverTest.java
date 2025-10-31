package starlight.adapter.businessplan.ai.infra;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.ai.document.Document;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.pinecone.PineconeVectorStore;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class VectorStoreContextRetrieverTest {

    @Test
    @DisplayName("topK와 tag 필터로 검색하고 본문을 구분선으로 합침")
    void retrieveContext_joinsDocuments() {
        PineconeVectorStore vectorStore = mock(PineconeVectorStore.class);
        when(vectorStore.similaritySearch(any(SearchRequest.class)))
                .thenReturn(List.of(new Document("A"), new Document("B")));

        VectorStoreContextRetriever sut = new VectorStoreContextRetriever(vectorStore);

        String ctx = sut.retrieveContext("feasibility_strategy", "query text", 2);

        assertThat(ctx).isEqualTo("A\n---\nB");
        verify(vectorStore).similaritySearch(any(SearchRequest.class));
    }
}

