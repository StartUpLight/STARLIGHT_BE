package starlight.adapter.businessplan.ai;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import starlight.adapter.businessplan.ai.infra.ChatClientLlmGenerator;
import starlight.adapter.businessplan.ai.infra.VectorStoreContextRetriever;
import starlight.application.businessplan.required.ChecklistGrader;
import starlight.domain.businessplan.enumerate.SubSectionName;

import java.util.List;
import java.util.ArrayList;
import starlight.adapter.businessplan.ai.infra.ChecklistCatalog;

@Slf4j
@Service
@RequiredArgsConstructor
public class RagChecklistGrader implements ChecklistGrader {

    private final VectorStoreContextRetriever retriever;
    private final ChatClientLlmGenerator generator;
    private final ChecklistCatalog checklistCatalog;

    @Override
    public List<Boolean> check(SubSectionName subSectionName, String content) {
        String tag = subSectionName.getTag();

        // 1) 서브섹션별 체크리스트 기준 5개 확보
        List<String> criteria = checklistCatalog.getCriteriaByTag(tag);

        // 2) 각 기준마다 topK=3 컨텍스트 검색 후 합쳐 하나의 컨텍스트 문자열로 구성
        StringBuilder mergedContext = new StringBuilder();
        for (int i = 0; i < criteria.size(); i++) {
            String c = criteria.get(i);
            String ctx = retriever.retrieveContext(tag, content + "\n\n" + c, 3);
            if (!ctx.isBlank()) {
                mergedContext.append("[CRITERION ").append(i + 1).append("]\n");
                mergedContext.append(ctx).append("\n\n");
            }
        }

        // 3) LLM 프롬프트 생성
        String userPrompt = buildRagUserContent(content, criteria, mergedContext.toString());

        // 4) LLM 호출 → Boolean 배열 파싱
        List<Boolean> result = generator.generateChecklistArray(userPrompt);

        // 5) 보정: 항상 길이 5 보장
        return normalizeToFive(result);
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

    private List<Boolean> normalizeToFive(List<Boolean> in) {
        List<Boolean> out = new ArrayList<>(5);
        for (int i = 0; i < 5; i++) {
            out.add(i < in.size() && in.get(i) != null ? in.get(i) : false);
        }
        return out;
    }
}
