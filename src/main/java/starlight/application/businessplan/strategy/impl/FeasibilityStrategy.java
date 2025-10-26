package starlight.application.businessplan.strategy.impl;

import com.fasterxml.jackson.databind.JsonNode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import starlight.application.businessplan.strategy.SectionStrategy;
import starlight.application.businessplan.strategy.dto.SectionRequest;
import starlight.application.businessplan.strategy.dto.SectionResponse;
import starlight.application.businessplan.strategy.impl.service.FeasibilityService;
import starlight.domain.businessplan.entity.BusinessPlan;
import starlight.domain.businessplan.entity.Feasibility;
import starlight.domain.businessplan.enumerate.SectionName;
import starlight.domain.businessplan.exception.BusinessPlanErrorType;
import starlight.domain.businessplan.exception.BusinessPlanException;

import java.util.List;

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

        Feasibility entity = feasibilityService.createFrom(rawJson, req.checks());
        plan.attachFeasibility(entity);

        return SectionResponse.Created.create(key(), entity.getId(), "created");
    }

    @Override
    public SectionResponse.Retrieved read(BusinessPlan plan) {
        Feasibility entity = plan.getFeasibility();
        if (entity == null) {
            throw new BusinessPlanException(BusinessPlanErrorType.SECTIONAL_CONTENT_NOT_FOUND);
        }

        return SectionResponse.Retrieved.create("successfully retrieved", entity.getRawJson().asTree());
    }

    @Override
    public SectionResponse.Created update(BusinessPlan plan, JsonNode rawJson, SectionRequest req) {
        Feasibility entity = plan.getFeasibility();
        if (entity == null) {
            throw new BusinessPlanException(BusinessPlanErrorType.SECTIONAL_CONTENT_NOT_FOUND);
        }

        feasibilityService.updateFrom(entity, rawJson, req.checks());

        return SectionResponse.Created.create(key(), entity.getId(), "updated");
    }

    @Override
    public SectionResponse.Deleted delete(BusinessPlan plan) {
        Feasibility entity = plan.getFeasibility();
        if (entity == null) {
            throw new BusinessPlanException(BusinessPlanErrorType.SECTIONAL_CONTENT_NOT_FOUND);
        }

        feasibilityService.delete(entity, plan);

        return SectionResponse.Deleted.create(key(), entity.getId(), "deleted");
    }

    @Override
    public List<Boolean> check(SectionRequest request) {
        return feasibilityService.check(request);
    }
}