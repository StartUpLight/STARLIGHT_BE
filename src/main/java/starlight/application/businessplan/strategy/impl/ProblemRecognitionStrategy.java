package starlight.application.businessplan.strategy.impl;

import com.fasterxml.jackson.databind.JsonNode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import starlight.adapter.businessplan.webapi.dto.SectionRequest;
import starlight.domain.businessplan.entity.BusinessPlan;
import starlight.domain.businessplan.entity.ProblemRecognition;
import starlight.domain.businessplan.enumerate.SectionName;
import starlight.application.businessplan.strategy.SectionStrategy;
import starlight.application.businessplan.strategy.SectionSupportUtils;
import starlight.application.businessplan.strategy.dto.SectionResponse;
import starlight.application.businessplan.strategy.impl.service.ProblemRecognitionService;
import starlight.domain.businessplan.exception.BusinessPlanErrorType;
import starlight.domain.businessplan.exception.BusinessPlanException;

@Component
@RequiredArgsConstructor
public class ProblemRecognitionStrategy implements SectionStrategy {

    private final ProblemRecognitionService problemRecognitionService;

    @Override
    public SectionName key() {
        return SectionName.PROBLEM_RECOGNITION;
    }

    @Override
    public SectionResponse.Created create(BusinessPlan plan, JsonNode rawJson, SectionRequest request) {
        if (plan.getProblemRecognition() != null) {
            throw new BusinessPlanException(BusinessPlanErrorType.SECTIONAL_CONTENT_ALREADY_EXISTS);
        }
        ProblemRecognition section = problemRecognitionService.createFrom(rawJson, request.checks());
        plan.attachProblemRecognition(section);

        return new SectionResponse.Created(SectionName.PROBLEM_RECOGNITION, section.getId(), "Problem recognition created");
    }

    @Override
    public SectionResponse.Retrieved read(BusinessPlan plan) {
        ProblemRecognition entity = plan.getProblemRecognition();
        if (entity == null) {
            throw new BusinessPlanException(BusinessPlanErrorType.SECTIONAL_CONTENT_NOT_FOUND);
        }

        return new SectionResponse.Retrieved("successfully retrieved", entity.getRawJson().asTree());
    }

    @Override
    public SectionResponse.Updated update(BusinessPlan plan, JsonNode rawJson, SectionRequest req) {
        ProblemRecognition entity = plan.getProblemRecognition();
        if (entity == null) {
            throw new BusinessPlanException(BusinessPlanErrorType.SECTIONAL_CONTENT_NOT_FOUND);
        }
        SectionSupportUtils.requireSize5(req.checks());
        problemRecognitionService.updateFrom(entity, rawJson, req.checks());

        return new SectionResponse.Updated(key(), entity.getId(), "updated");
    }

    @Override
    public SectionResponse.Deleted delete(BusinessPlan plan) {
        ProblemRecognition entity = plan.getProblemRecognition();
        if (entity == null) {
            throw new BusinessPlanException(BusinessPlanErrorType.SECTIONAL_CONTENT_NOT_FOUND);
        }
        problemRecognitionService.delete(entity, plan);

        return new SectionResponse.Deleted(key(), entity.getId(), "deleted");
    }
}