package starlight.application.businessplan.strategy.impl.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import starlight.application.businessplan.strategy.dto.SectionRequest;
import starlight.application.businessplan.strategy.util.ContentPlainText;
import starlight.domain.businessplan.entity.BusinessPlan;
import starlight.domain.businessplan.entity.Overview;
import starlight.application.businessplan.strategy.util.SectionSupportUtils;

import java.util.List;

@Service
@RequiredArgsConstructor
public class OverviewService {

    private final ObjectMapper objectMapper;

    public Overview createFrom(JsonNode rawJson, List<Boolean> checks) {
        SectionSupportUtils.requireSize(checks, 5);
        String json = SectionSupportUtils.toJson(objectMapper, rawJson);

        Overview entity = Overview.create(json);
        entity.updateChecks(checks);

        return entity;
    }

    public void updateFrom(Overview entity, JsonNode rawJson, List<Boolean> checks) {
        SectionSupportUtils.requireSize(checks, 5);

        String json = SectionSupportUtils.toJson(objectMapper, rawJson);
        entity.updateRawJson(json);
        entity.updateChecks(checks);
    }

    public void delete(Overview entity, BusinessPlan plan) {
        plan.detachOverview();
        // TODO: Overview 엔티티 삭제
    }

    public List<Boolean> check(SectionRequest request){
        String text = ContentPlainText.extractPlainText(objectMapper, request);
        //TODO: implement feasibility checks

        return List.of(true, true, true, true, true);
    }
}