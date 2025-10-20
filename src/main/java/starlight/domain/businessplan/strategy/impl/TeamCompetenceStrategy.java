package starlight.domain.businessplan.strategy.impl;

import com.fasterxml.jackson.databind.JsonNode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import starlight.adapter.bussinessplan.webapi.dto.SectionRequest;
import starlight.domain.businessplan.entity.BusinessPlan;
import starlight.domain.businessplan.entity.TeamCompetence;
import starlight.domain.businessplan.enumerate.SectionName;
import starlight.domain.businessplan.strategy.SectionStrategy;
import starlight.domain.businessplan.strategy.SectionSupportUtils;
import starlight.domain.businessplan.strategy.dto.SectionResponse;
import starlight.domain.businessplan.strategy.service.TeamCompetenceService;

@Component
@RequiredArgsConstructor
public class TeamCompetenceStrategy implements SectionStrategy {

    private final TeamCompetenceService teamCompetenceService;

    @Override
    public SectionName key() {
        return SectionName.TEAM_COMPETENCE;
    }

    @Override
    public SectionResponse.Created create(BusinessPlan plan, JsonNode rawJson, SectionRequest request) {
        if (plan.getTeamCompetence() != null) {
            throw new IllegalStateException("TeamCompetence already exists for planId=" + plan.getId());
        }
        TeamCompetence section = teamCompetenceService.createFrom(rawJson, request.checks());
        plan.attachTeamCompetence(section);

        return new SectionResponse.Created(SectionName.TEAM_COMPETENCE, section.getId(), "Team competence created");
    }

    @Override
    public SectionResponse.Retrieved read(BusinessPlan plan) {
        TeamCompetence entity = plan.getTeamCompetence();
        if (entity == null) {
            throw new IllegalStateException("TeamCompetence not found for plan " + plan.getId());
        }

        return new SectionResponse.Retrieved("successfully retrieved", entity.getRawJson().asTree());
    }

    @Override
    public SectionResponse.Updated update(BusinessPlan plan, JsonNode rawJson, SectionRequest req) {
        TeamCompetence entity = plan.getTeamCompetence();
        if (entity == null) {
            throw new IllegalStateException("TeamCompetence not found for plan " + plan.getId());
        }
        SectionSupportUtils.requireSize5(req.checks());
        teamCompetenceService.updateFrom(entity, rawJson, req.checks());

        return new SectionResponse.Updated(key(), entity.getId(), "updated");
    }

    @Override
    public SectionResponse.Deleted delete(BusinessPlan plan) {
        TeamCompetence entity = plan.getTeamCompetence();
        if (entity == null) {
            throw new IllegalStateException("TeamCompetence not found for plan " + plan.getId());
        }
        teamCompetenceService.delete(entity, plan);

        return new SectionResponse.Deleted(key(), entity.getId(), "deleted");
    }
}

