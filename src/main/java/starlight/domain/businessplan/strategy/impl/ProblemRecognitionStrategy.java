package starlight.domain.businessplan.strategy.impl;

import com.fasterxml.jackson.databind.JsonNode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import starlight.adapter.bussinessplan.webapi.dto.SectionRequest;
import starlight.domain.businessplan.entity.BusinessPlan;
import starlight.domain.businessplan.entity.ProblemRecognition;
import starlight.domain.businessplan.enumerate.SectionName;
import starlight.domain.businessplan.strategy.SectionStrategy;
import starlight.domain.businessplan.strategy.SectionSupportUtils;
import starlight.domain.businessplan.strategy.dto.SectionResponse;
import starlight.domain.businessplan.strategy.service.ProblemRecognitionService;

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
            throw new IllegalStateException("ProblemRecognition already exists for planId=" + plan.getId());
        }
        ProblemRecognition section = problemRecognitionService.createFrom(rawJson, request.checks());
        plan.attachProblemRecognition(section);

        return new SectionResponse.Created(SectionName.PROBLEM_RECOGNITION, section.getId(), "Problem recognition created");
    }

    @Override
    public SectionResponse.Retrieved read(BusinessPlan plan) {
        ProblemRecognition entity = plan.getProblemRecognition();
        if (entity == null) {
            throw new IllegalStateException("ProblemRecognition not found for plan " + plan.getId());
        }

        return new SectionResponse.Retrieved("successfully retrieved", entity.getRawJson().asTree());
    }

    @Override
    public SectionResponse.Updated update(BusinessPlan plan, JsonNode rawJson, SectionRequest req) {
        ProblemRecognition entity = plan.getProblemRecognition();
        if (entity == null) {
            throw new IllegalStateException("ProblemRecognition not found for plan " + plan.getId());
        }
        SectionSupportUtils.requireSize5(req.checks());
        problemRecognitionService.updateFrom(entity, rawJson, req.checks());

        return new SectionResponse.Updated(key(), entity.getId(), "updated");
    }

    @Override
    public SectionResponse.Deleted delete(BusinessPlan plan) {
        ProblemRecognition entity = plan.getProblemRecognition();
        if (entity == null) {
            throw new IllegalStateException("ProblemRecognition not found for plan " + plan.getId());
        }
        problemRecognitionService.delete(entity, plan);

        return new SectionResponse.Deleted(key(), entity.getId(), "deleted");
    }
}