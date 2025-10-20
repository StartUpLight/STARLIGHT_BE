package starlight.domain.businessplan.strategy.impl;

import com.fasterxml.jackson.databind.JsonNode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import starlight.adapter.bussinessplan.webapi.dto.SectionRequest;
import starlight.domain.businessplan.entity.BusinessPlan;
import starlight.domain.businessplan.entity.Overview;
import starlight.domain.businessplan.enumerate.SectionName;
import starlight.domain.businessplan.strategy.SectionStrategy;
import starlight.domain.businessplan.strategy.SectionSupportUtils;
import starlight.domain.businessplan.strategy.dto.SectionResponse;
import starlight.domain.businessplan.strategy.service.OverviewService;

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
            throw new IllegalStateException("Overview already exists for planId=" + plan.getId());
        }
        Overview overview = overviewService.createFrom(rawJson, request.checks());
        plan.attachOverview(overview);

        return new SectionResponse.Created(SectionName.OVERVIEW, overview.getId(), "Overview created");
    }

    @Override
    public SectionResponse.Retrieved read(BusinessPlan plan) {
        Overview entity = plan.getOverview();
        if (entity == null) {
            throw new IllegalStateException("Overview not found for plan " + plan.getId());
        }

        return new SectionResponse.Retrieved("successfully retrieved", entity.getRawJson().asTree());
    }

    @Override
    public SectionResponse.Updated update(BusinessPlan plan, JsonNode rawJson, SectionRequest req) {
        Overview entity = plan.getOverview();
        if (entity == null) {
            throw new IllegalStateException("Overview not found for plan " + plan.getId());
        }
        SectionSupportUtils.requireSize5(req.checks());
        overviewService.updateFrom(entity, rawJson, req.checks());

        return new SectionResponse.Updated(key(), entity.getId(), "updated");
    }

    @Override
    public SectionResponse.Deleted delete(BusinessPlan plan) {
        Overview entity = plan.getOverview();
        if (entity == null) {
            throw new IllegalStateException("Overview not found for plan " + plan.getId());
        }
        overviewService.delete(entity, plan);

        return new SectionResponse.Deleted(key(), entity.getId(), "deleted");
    }
}