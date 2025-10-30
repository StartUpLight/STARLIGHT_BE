package starlight.adapter.businessplan.ai;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import starlight.adapter.businessplan.ai.infra.ChatClientLlmGenerator;
import starlight.adapter.businessplan.ai.infra.VectorStoreContextRetriever;
import starlight.application.businessplan.required.ChecklistGrader;
import starlight.domain.businessplan.enumerate.SubSectionName;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class RagChecklistGrader implements ChecklistGrader {

    private final VectorStoreContextRetriever retriever;
    private final ChatClientLlmGenerator generator;

    @Override
    public List<Boolean> check(SubSectionName subSectionName, String content) {

        return List.of();
    }

    private String buildRagUserContent(String input, List<String> criteria, String retrievedContext) {
        StringBuilder sb = new StringBuilder();
        sb.append("[CONTEXT]\n").append(retrievedContext).append("\n\n");
        sb.append("[CHECKLIST]\n");
        for (int i = 0; i < criteria.size(); i++) {
            sb.append(i + 1).append(") ").append(criteria.get(i)).append("\n");
        }
        sb.append("\n[INPUT]\n").append(input).append("\n\n");
        sb.append("[REQUEST]\n").append("위의 CONTEXT를 근거로 CHECKLIST 항목 각각에 대해 TRUE/FALSE로만 판단하되, 최종 출력은 길이 ")
                .append(criteria.size()).append("의 JSON 배열(Boolean)로만 반환");
        return sb.toString();
    }
}
