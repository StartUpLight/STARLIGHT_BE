package starlight.domain.businessplan.strategy.impl;

import com.fasterxml.jackson.databind.JsonNode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import starlight.adapter.bussinessplan.webapi.dto.SectionRequest;
import starlight.domain.businessplan.entity.BusinessPlan;
import starlight.domain.businessplan.entity.GrowthStrategy;
import starlight.domain.businessplan.enumerate.SectionName;
import starlight.domain.businessplan.strategy.SectionStrategy;
import starlight.domain.businessplan.strategy.SectionSupportUtils;
import starlight.domain.businessplan.strategy.dto.SectionResponse;
import starlight.domain.businessplan.strategy.service.GrowthStrategyService;

@Component
@RequiredArgsConstructor
public class GrowthStrategyStrategy implements SectionStrategy {

    private final GrowthStrategyService growthStrategyService;

    @Override
    public SectionName key() {
        return SectionName.GROWTH_STRATEGY;
    }

    @Override
    public SectionResponse.Created create(BusinessPlan plan, JsonNode rawJson, SectionRequest request) {
        if (plan.getGrowthStrategy() != null) {
            throw new IllegalStateException("GrowthStrategy already exists for planId=" + plan.getId());
        }
        GrowthStrategy section = growthStrategyService.createFrom(rawJson, request.checks());
        plan.attachGrowthStrategy(section);

        return new SectionResponse.Created(SectionName.GROWTH_STRATEGY, section.getId(), "Growth strategy created");
    }

    @Override
    public SectionResponse.Retrieved read(BusinessPlan plan) {
        GrowthStrategy entity = plan.getGrowthStrategy();
        if (entity == null) {
            throw new IllegalStateException("GrowthStrategy not found for plan " + plan.getId());
        }

        return new SectionResponse.Retrieved("successfully retrieved", entity.getRawJson().asTree());
    }

    @Override
    public SectionResponse.Updated update(BusinessPlan plan, JsonNode rawJson, SectionRequest req) {
        GrowthStrategy entity = plan.getGrowthStrategy();
        if (entity == null) {
            throw new IllegalStateException("GrowthStrategy not found for plan " + plan.getId());
        }
        SectionSupportUtils.requireSize5(req.checks());
        growthStrategyService.updateFrom(entity, rawJson, req.checks());

        return new SectionResponse.Updated(key(), entity.getId(), "updated");
    }

    @Override
    public SectionResponse.Deleted delete(BusinessPlan plan) {
        GrowthStrategy entity = plan.getGrowthStrategy();
        if (entity == null) {
            throw new IllegalStateException("GrowthStrategy not found for plan " + plan.getId());
        }
        growthStrategyService.delete(entity, plan);

        return new SectionResponse.Deleted(key(), entity.getId(), "deleted");
    }
}