package starlight.application.businessplan;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import starlight.application.businessplan.provided.SectionCrudService;
import starlight.application.businessplan.required.BusinessPlanQuery;
import starlight.application.businessplan.strategy.dto.SectionRequest;
import starlight.domain.businessplan.entity.BusinessPlan;
import starlight.domain.businessplan.enumerate.SectionName;
import starlight.application.businessplan.strategy.SectionRouter;
import starlight.application.businessplan.strategy.dto.SectionResponse;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class SectionCrudServiceImpl implements SectionCrudService {

    private final ObjectMapper objectMapper;
    private final SectionRouter sectionRouter;
    private final BusinessPlanQuery businessPlanQuery;

    @Transactional
    public SectionResponse.Created createSection(Long planId, @Valid SectionRequest request) {
        BusinessPlan plan = businessPlanQuery.getOrThrow(planId);

        JsonNode rawJson = objectMapper.valueToTree(request);

        return sectionRouter.routeAndCreate(plan, rawJson, request);
    }

    @Transactional(readOnly = true)
    public SectionResponse.Retrieved getSection(Long planId, SectionName sectionName) {
        BusinessPlan plan = businessPlanQuery.getOrThrow(planId);

        return sectionRouter.routeAndGet(plan, sectionName);
    }

    @Transactional
    public SectionResponse.Updated updateSection(Long planId, SectionRequest req) {
        BusinessPlan plan = businessPlanQuery.getOrThrow(planId);

        JsonNode raw = objectMapper.valueToTree(req);

        return sectionRouter.routeAndUpdate(plan, raw, req);
    }

    @Transactional
    public SectionResponse.Deleted deleteSection(Long planId, SectionName sectionName) {
        BusinessPlan plan = businessPlanQuery.getOrThrow(planId);

        return sectionRouter.routeAndDelete(plan, sectionName);
    }

    @Transactional(readOnly = true)
    public List<Boolean> checkSection(SectionRequest request){

        return sectionRouter.routeAndCheck(request);
    }
}
