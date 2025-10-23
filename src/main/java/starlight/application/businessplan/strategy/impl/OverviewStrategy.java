package starlight.application.businessplan.strategy.impl;

import com.fasterxml.jackson.databind.JsonNode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import starlight.application.businessplan.strategy.dto.SectionRequest;
import starlight.application.businessplan.strategy.SectionStrategy;
import starlight.application.businessplan.strategy.dto.SectionResponse;
import starlight.application.businessplan.strategy.impl.service.OverviewService;
import starlight.domain.businessplan.entity.BusinessPlan;
import starlight.domain.businessplan.entity.Overview;
import starlight.domain.businessplan.enumerate.SectionName;
import starlight.domain.businessplan.exception.BusinessPlanErrorType;
import starlight.domain.businessplan.exception.BusinessPlanException;

import java.util.List;

@Component
@RequiredArgsConstructor
public class OverviewStrategy implements SectionStrategy {

    private final OverviewService overviewService;

    @Override
    public SectionName key() {
        return SectionName.OVERVIEW;
    }

    @Override
    public SectionResponse.Created create(BusinessPlan plan, JsonNode rawJson, SectionRequest request) {
        if (plan.getOverview() != null) {
            throw new BusinessPlanException(BusinessPlanErrorType.SECTIONAL_CONTENT_ALREADY_EXISTS);
        }

        Overview overview = overviewService.createFrom(rawJson, request.checks());
        plan.attachOverview(overview);

        return SectionResponse.Created.create(SectionName.OVERVIEW, overview.getId(), "Overview created");
    }

    @Override
    public SectionResponse.Retrieved read(BusinessPlan plan) {
        Overview entity = plan.getOverview();
        if (entity == null) {
            throw new BusinessPlanException(BusinessPlanErrorType.SECTIONAL_CONTENT_NOT_FOUND);
        }

        return SectionResponse.Retrieved.create("successfully retrieved", entity.getRawJson().asTree());
    }

    @Override
    public SectionResponse.Updated update(BusinessPlan plan, JsonNode rawJson, SectionRequest req) {
        Overview entity = plan.getOverview();

        if (entity == null) {
            throw new BusinessPlanException(BusinessPlanErrorType.SECTIONAL_CONTENT_NOT_FOUND);
        }

        overviewService.updateFrom(entity, rawJson, req.checks());

        return SectionResponse.Updated.create(key(), entity.getId(), "updated");
    }

    @Override
    public SectionResponse.Deleted delete(BusinessPlan plan) {
        Overview entity = plan.getOverview();

        if (entity == null) {
            throw new BusinessPlanException(BusinessPlanErrorType.SECTIONAL_CONTENT_NOT_FOUND);
        }

        overviewService.delete(entity, plan);

        return SectionResponse.Deleted.create(key(), entity.getId(), "deleted");
    }

    @Override
    public List<Boolean> check(SectionRequest request) {
        return overviewService.check(request);
    }
}