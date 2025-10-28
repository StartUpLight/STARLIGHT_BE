package starlight.application.businessplan.strategy.impl;

import com.fasterxml.jackson.databind.JsonNode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import starlight.application.businessplan.strategy.dto.SectionRequest;
import starlight.application.businessplan.strategy.SectionStrategy;
import starlight.application.businessplan.strategy.dto.SectionResponse;
import starlight.application.businessplan.strategy.impl.service.TeamCompetenceService;
import starlight.domain.businessplan.entity.BusinessPlan;
import starlight.domain.businessplan.entity.ProblemRecognition;
import starlight.domain.businessplan.entity.TeamCompetence;
import starlight.domain.businessplan.enumerate.SectionName;
import starlight.domain.businessplan.exception.BusinessPlanErrorType;
import starlight.domain.businessplan.exception.BusinessPlanException;

import java.util.List;

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
            throw new BusinessPlanException(BusinessPlanErrorType.SECTIONAL_CONTENT_ALREADY_EXISTS);
        }

        TeamCompetence section = teamCompetenceService.createFrom(rawJson, request.checks());
        plan.attachTeamCompetence(section);

        return SectionResponse.Created.create(SectionName.TEAM_COMPETENCE, section.getId(), "Team competence created");
    }

    @Override
    public SectionResponse.Retrieved read(BusinessPlan plan) {
        TeamCompetence entity = getTeamCompetenceOrThrow(plan);

        return SectionResponse.Retrieved.create("successfully retrieved", entity.getRawJson().asTree());
    }

    @Override
    public SectionResponse.Created update(BusinessPlan plan, JsonNode rawJson, SectionRequest req) {
        TeamCompetence entity = getTeamCompetenceOrThrow(plan);

        teamCompetenceService.updateFrom(entity, rawJson, req.checks());

        return SectionResponse.Created.create(key(), entity.getId(), "updated");
    }

    @Override
    public SectionResponse.Deleted delete(BusinessPlan plan) {
        TeamCompetence entity = getTeamCompetenceOrThrow(plan);

        teamCompetenceService.delete(entity, plan);

        return SectionResponse.Deleted.create(key(), entity.getId(), "deleted");
    }

    @Override
    public List<Boolean> check(SectionRequest request) {
        return teamCompetenceService.check(request);
    }

    private TeamCompetence getTeamCompetenceOrThrow(BusinessPlan plan) {
        TeamCompetence entity = plan.getTeamCompetence();
        if (entity == null) {
            throw new BusinessPlanException(BusinessPlanErrorType.SECTIONAL_CONTENT_NOT_FOUND);
        }

        return entity;
    }
}
