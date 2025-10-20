package starlight.application.businessplan.strategy.impl;

import com.fasterxml.jackson.databind.JsonNode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import starlight.adapter.businessplan.webapi.dto.SectionRequest;
import starlight.domain.businessplan.entity.BusinessPlan;
import starlight.domain.businessplan.entity.GrowthTactic;
import starlight.domain.businessplan.enumerate.SectionName;
import starlight.application.businessplan.strategy.SectionStrategy;
import starlight.application.businessplan.strategy.SectionSupportUtils;
import starlight.application.businessplan.strategy.dto.SectionResponse;
import starlight.application.businessplan.strategy.impl.service.GrowthTacticService;
import starlight.domain.businessplan.exception.BusinessPlanErrorType;
import starlight.domain.businessplan.exception.BusinessPlanException;

@Component
@RequiredArgsConstructor
public class GrowthTacticStrategy implements SectionStrategy {

    private final GrowthTacticService growthTacticService;

    @Override
    public SectionName key() {
        return SectionName.GROWTH_STRATEGY;
    }

    @Override
    public SectionResponse.Created create(BusinessPlan plan, JsonNode rawJson, SectionRequest request) {
        if (plan.getGrowthTactic() != null) {
            throw new BusinessPlanException(BusinessPlanErrorType.SECTIONAL_CONTENT_ALREADY_EXISTS);
        }
        GrowthTactic section = growthTacticService.createFrom(rawJson, request.checks());
        plan.attachGrowthStrategy(section);

        return new SectionResponse.Created(SectionName.GROWTH_STRATEGY, section.getId(), "Growth strategy created");
    }

    @Override
    public SectionResponse.Retrieved read(BusinessPlan plan) {
        GrowthTactic entity = plan.getGrowthTactic();
        if (entity == null) {
            throw new BusinessPlanException(BusinessPlanErrorType.SECTIONAL_CONTENT_NOT_FOUND);
        }

        return new SectionResponse.Retrieved("successfully retrieved", entity.getRawJson().asTree());
    }

    @Override
    public SectionResponse.Updated update(BusinessPlan plan, JsonNode rawJson, SectionRequest req) {
        GrowthTactic entity = plan.getGrowthTactic();
        if (entity == null) {
            throw new BusinessPlanException(BusinessPlanErrorType.SECTIONAL_CONTENT_NOT_FOUND);
        }
        SectionSupportUtils.requireSize5(req.checks());
        growthTacticService.updateFrom(entity, rawJson, req.checks());

        return new SectionResponse.Updated(key(), entity.getId(), "updated");
    }

    @Override
    public SectionResponse.Deleted delete(BusinessPlan plan) {
        GrowthTactic entity = plan.getGrowthTactic();
        if (entity == null) {
            throw new BusinessPlanException(BusinessPlanErrorType.SECTIONAL_CONTENT_NOT_FOUND);
        }
        growthTacticService.delete(entity, plan);

        return new SectionResponse.Deleted(key(), entity.getId(), "deleted");
    }
}