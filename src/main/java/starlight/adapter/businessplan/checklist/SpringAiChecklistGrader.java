package starlight.adapter.businessplan.checklist;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import starlight.adapter.businessplan.checklist.agent.SpringAiChecklistAgent;
import starlight.application.businessplan.required.ChecklistGraderPort;
import starlight.domain.businessplan.enumerate.SubSectionType;

import java.util.List;
import java.util.ArrayList;
import starlight.adapter.businessplan.checklist.provider.ChecklistPromptProvider;

@Slf4j
@Service
@RequiredArgsConstructor
public class SpringAiChecklistGrader implements ChecklistGraderPort {

    private final SpringAiChecklistAgent generator;
    private final ChecklistPromptProvider checklistCatalog;

    @Override
    public List<Boolean> check(
            SubSectionType subSectionType,
            String content
    ) {
        // 1) 서브섹션별 체크리스트 기준 5개 확보
        List<String> criteria = checklistCatalog.getCriteriaBySubSectionType(subSectionType);
        List<String> detailedCriteria = checklistCatalog.getDetailedCriteriaBySubSectionType(subSectionType);

        // 2) LLM 호출 → Boolean 배열 파싱
        List<Boolean> result = generator.generateChecklistArray(subSectionType, content, criteria, detailedCriteria);

        // 3) 보정: 항상 길이 5 보장
        return normalizeToFive(result);
    }

    private List<Boolean> normalizeToFive(List<Boolean> in) {
        List<Boolean> out = new ArrayList<>(5);
        for (int i = 0; i < 5; i++) {
            out.add(i < in.size() && in.get(i) != null ? in.get(i) : false);
        }
        return out;
    }
}
