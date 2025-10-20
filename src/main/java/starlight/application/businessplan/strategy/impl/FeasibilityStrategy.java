package starlight.application.businessplan.strategy.impl;

import com.fasterxml.jackson.databind.JsonNode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import starlight.adapter.businessplan.webapi.dto.SectionRequest;
import starlight.domain.businessplan.entity.BusinessPlan;
import starlight.domain.businessplan.entity.Feasibility;
import starlight.domain.businessplan.enumerate.SectionName;
import starlight.application.businessplan.strategy.SectionStrategy;
import starlight.application.businessplan.strategy.SectionSupportUtils;
import starlight.application.businessplan.strategy.dto.SectionResponse;
import starlight.application.businessplan.strategy.impl.service.FeasibilityService;
import starlight.domain.businessplan.exception.BusinessPlanErrorType;
import starlight.domain.businessplan.exception.BusinessPlanException;

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
            throw new BusinessPlanException(BusinessPlanErrorType.SECTIONAL_CONTENT_ALREADY_EXISTS);
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
            throw new BusinessPlanException(BusinessPlanErrorType.SECTIONAL_CONTENT_NOT_FOUND);
        }

        return new SectionResponse.Retrieved("successfully retrieved", entity.getRawJson().asTree());
    }

    @Override
    public SectionResponse.Updated update(BusinessPlan plan, JsonNode rawJson, SectionRequest req) {
        Feasibility entity = plan.getFeasibility();
        if (entity == null) {
            throw new BusinessPlanException(BusinessPlanErrorType.SECTIONAL_CONTENT_NOT_FOUND);
        }
        SectionSupportUtils.requireSize5(req.checks());
        feasibilityService.updateFrom(entity, rawJson, req.checks());

        return new SectionResponse.Updated(key(), entity.getId(), "updated");
    }

    @Override
    public SectionResponse.Deleted delete(BusinessPlan plan) {
        Feasibility entity = plan.getFeasibility();
        if (entity == null) {
            throw new BusinessPlanException(BusinessPlanErrorType.SECTIONAL_CONTENT_NOT_FOUND);
        }
        feasibilityService.delete(entity, plan);

        return new SectionResponse.Deleted(key(), entity.getId(), "deleted");
    }
}