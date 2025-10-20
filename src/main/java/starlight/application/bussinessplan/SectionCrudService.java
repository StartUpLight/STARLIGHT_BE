package starlight.application.bussinessplan;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import starlight.adapter.bussinessplan.webapi.dto.SectionRequest;
import starlight.application.bussinessplan.provided.BusinessPlanService;
import starlight.domain.businessplan.entity.BusinessPlan;
import starlight.domain.businessplan.enumerate.SectionName;
import starlight.application.bussinessplan.required.BusinessPlanRepository;
import starlight.domain.businessplan.strategy.SectionRouter;
import starlight.domain.businessplan.strategy.dto.SectionResponse;

@Service
@RequiredArgsConstructor
@Transactional
public class SectionCrudService implements BusinessPlanService {

    private final ObjectMapper objectMapper;
    private final SectionRouter sectionRouter;
    private final BusinessPlanRepository businessPlanRepository;

    @Transactional
    public SectionResponse.Created createSection(Long planId, @Valid SectionRequest request) {
        BusinessPlan plan = businessPlanRepository.getOrThrow(planId);

        JsonNode rawJson = objectMapper.valueToTree(request);

        return sectionRouter.routeAndCreate(plan, rawJson, request);
    }

    @Transactional
    public SectionResponse.Retrieved getSection(Long planId, SectionName sectionName) {
        BusinessPlan plan = businessPlanRepository.getOrThrow(planId);

        return sectionRouter.routeAndGet(plan, sectionName);
    }

    @Transactional
    public SectionResponse.Updated updateSection(Long planId, SectionRequest req) {
        BusinessPlan plan = businessPlanRepository.getOrThrow(planId);

        JsonNode raw = objectMapper.valueToTree(req);

        return sectionRouter.routeAndUpdate(plan, raw, req);
    }

    @Transactional
    public SectionResponse.Deleted deleteSection(Long planId, SectionName sectionName) {
        BusinessPlan plan = businessPlanRepository.getOrThrow(planId);

        return sectionRouter.routeAndDelete(plan, sectionName);
    }
}
