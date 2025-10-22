package starlight.application.businessplan.strategy.impl.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import starlight.domain.businessplan.entity.BusinessPlan;
import starlight.domain.businessplan.entity.TeamCompetence;
import starlight.application.businessplan.strategy.util.SectionSupportUtils;

import java.util.List;

@Service
@RequiredArgsConstructor
public class TeamCompetenceService {

    private final ObjectMapper objectMapper;

    public TeamCompetence createFrom(JsonNode rawJson, List<Boolean> checks) {
        SectionSupportUtils.requireSize5(checks);
        String json = SectionSupportUtils.toJson(objectMapper, rawJson);

        TeamCompetence entity = TeamCompetence.create(json);
        entity.updateChecks(checks);

        return entity;
    }

    public void updateFrom(TeamCompetence entity, JsonNode rawJson, List<Boolean> checks) {
        SectionSupportUtils.requireSize5(checks);

        String json = SectionSupportUtils.toJson(objectMapper, rawJson);
        entity.updateRawJson(json);
        entity.updateChecks(checks);
    }

    public void delete(TeamCompetence entity, BusinessPlan plan) {
        plan.detachTeamCompetence();
        // TODO: TeamCompetence 엔티티 삭제
    }
}
