package starlight.domain.businessplan.strategy.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import starlight.domain.businessplan.entity.BusinessPlan;
import starlight.domain.businessplan.entity.GrowthStrategy;
import starlight.domain.businessplan.strategy.SectionSupportUtils;

import java.util.List;

@Service
@RequiredArgsConstructor
public class GrowthStrategyService {

    private final ObjectMapper objectMapper;

    public GrowthStrategy createFrom(JsonNode rawJson, List<Boolean> checks) {
        SectionSupportUtils.requireSize5(checks);
        String json = SectionSupportUtils.toJson(objectMapper, rawJson);

        GrowthStrategy entity = GrowthStrategy.create(json);
        entity.updateChecks(checks);

        return entity;
    }

    public void updateFrom(GrowthStrategy entity, JsonNode rawJson, List<Boolean> checks) {
        SectionSupportUtils.requireSize5(checks);

        String json = SectionSupportUtils.toJson(objectMapper, rawJson);
        entity.updateRawJson(json);
        entity.updateChecks(checks);
    }

    public void delete(GrowthStrategy entity, BusinessPlan plan) {
        plan.detachGrowthStrategy();
    }
}