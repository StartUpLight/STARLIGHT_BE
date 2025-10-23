package starlight.application.businessplan.strategy.impl.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import starlight.application.businessplan.strategy.dto.SectionRequest;
import starlight.application.businessplan.strategy.util.ContentPlainText;
import starlight.domain.businessplan.entity.BusinessPlan;
import starlight.domain.businessplan.entity.GrowthTactic;
import starlight.application.businessplan.strategy.util.SectionSupportUtils;

import java.util.List;

@Service
@RequiredArgsConstructor
public class GrowthTacticService {

    private final ObjectMapper objectMapper;

    public GrowthTactic createFrom(JsonNode rawJson, List<Boolean> checks) {
        SectionSupportUtils.requireSize5(checks);
        String json = SectionSupportUtils.toJson(objectMapper, rawJson);

        GrowthTactic entity = GrowthTactic.create(json);
        entity.updateChecks(checks);

        return entity;
    }

    public void updateFrom(GrowthTactic entity, JsonNode rawJson, List<Boolean> checks) {
        SectionSupportUtils.requireSize5(checks);

        String json = SectionSupportUtils.toJson(objectMapper, rawJson);
        entity.updateRawJson(json);
        entity.updateChecks(checks);
    }

    public void delete(GrowthTactic entity, BusinessPlan plan) {
        plan.detachGrowthTactic();
        // TODO: GrowthTactic 엔티티 삭제
    }

    public List<Boolean> check(SectionRequest request){
        String text = ContentPlainText.extractPlainText(objectMapper, request);
        //TODO: implement feasibility checks

        return List.of(true, true, true, true, true);
    }
}