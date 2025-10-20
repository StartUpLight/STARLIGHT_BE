package starlight.domain.businessplan.strategy.impl;

import com.fasterxml.jackson.databind.JsonNode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import starlight.adapter.bussinessplan.webapi.dto.SectionRequest;
import starlight.domain.businessplan.entity.BusinessPlan;
import starlight.domain.businessplan.entity.Feasibility;
import starlight.domain.businessplan.enumerate.SectionName;
import starlight.domain.businessplan.strategy.SectionStrategy;
import starlight.domain.businessplan.strategy.SectionSupportUtils;
import starlight.domain.businessplan.strategy.dto.SectionResponse;
import starlight.domain.businessplan.strategy.service.FeasibilityService;

@Component
@RequiredArgsConstructor
public class FeasibilityStrategy implements SectionStrategy {

    private final FeasibilityService feasibilityService;

    @Override
    public SectionName key() {
        return SectionName.FEASIBILITY;
    }

    @Override
    public SectionResponse.Created create(BusinessPlan plan, JsonNode rawJson, SectionRequest req) {
        if (plan.getFeasibility() != null) {
            throw new IllegalStateException("Feasibility already exists for plan " + plan.getId());
        }
        SectionSupportUtils.requireSize5(req.checks());
        Feasibility entity = feasibilityService.createFrom(rawJson, req.checks());
        plan.attachFeasibility(entity);

        return new SectionResponse.Created(key(), entity.getId(), "created");
    }

    @Override
    public SectionResponse.Retrieved read(BusinessPlan plan) {
        Feasibility entity = plan.getFeasibility();
        if (entity == null) {
            throw new IllegalStateException("Feasibility not found for plan " + plan.getId());
        }

        return new SectionResponse.Retrieved("successfully retrieved", entity.getRawJson().asTree());
    }

    @Override
    public SectionResponse.Updated update(BusinessPlan plan, JsonNode rawJson, SectionRequest req) {
        Feasibility entity = plan.getFeasibility();
        if (entity == null) {
            throw new IllegalStateException("Feasibility not found for plan " + plan.getId());
        }
        SectionSupportUtils.requireSize5(req.checks());
        feasibilityService.updateFrom(entity, rawJson, req.checks());

        return new SectionResponse.Updated(key(), entity.getId(), "updated");
    }

    @Override
    public SectionResponse.Deleted delete(BusinessPlan plan) {
        Feasibility entity = plan.getFeasibility();
        if (entity == null) {
            throw new IllegalStateException("Feasibility not found for plan " + plan.getId());
        }
        feasibilityService.delete(entity, plan);

        return new SectionResponse.Deleted(key(), entity.getId(), "deleted");
    }
}