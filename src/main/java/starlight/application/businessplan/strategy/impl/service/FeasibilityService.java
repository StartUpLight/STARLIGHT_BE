package starlight.application.businessplan.strategy.impl.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import starlight.application.businessplan.strategy.dto.SectionRequest;
import starlight.application.businessplan.strategy.util.ContentPlainText;
import starlight.application.infrastructure.provided.CheckListGrader;
import starlight.domain.businessplan.entity.BusinessPlan;
import starlight.domain.businessplan.entity.Feasibility;
import starlight.application.businessplan.strategy.util.SectionSupportUtils;

import java.util.List;

@Service
@RequiredArgsConstructor
public class FeasibilityService {

    private final ObjectMapper objectMapper;
    private final CheckListGrader checkListGrader;

    public Feasibility createFrom(JsonNode raw, List<Boolean> checks) {
        SectionSupportUtils.requireSize5(checks);
        String json = SectionSupportUtils.toJson(objectMapper, raw);

        Feasibility entity = Feasibility.create(json);
        entity.updateChecks(checks);

        return entity;
    }

    public void updateFrom(Feasibility entity, JsonNode raw, List<Boolean> checks) {
        SectionSupportUtils.requireSize5(checks);

        String json = SectionSupportUtils.toJson(objectMapper, raw);
        entity.updateRawJson(json);
        entity.updateChecks(checks);
    }

    public void  delete(Feasibility entity, BusinessPlan plan) {
        plan.detachFeasibility();
        // TODO: Feasibility 엔티티 삭제
    }

    public List<Boolean> check(SectionRequest request){
        String text = ContentPlainText.extractPlainText(objectMapper, request);
        //TODO: implement feasibility checks
        return checkListGrader.check(request.sectionName().toString(), text, request.checks().size());
    }
}