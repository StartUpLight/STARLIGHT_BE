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
import starlight.domain.businessplan.exception.BusinessPlanErrorType;
import starlight.domain.businessplan.exception.BusinessPlanException;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class SectionCrudServiceImpl implements SectionCrudService {

    private final ObjectMapper objectMapper;
    private final SectionRouter sectionRouter;
    private final BusinessPlanQuery businessPlanQuery;

    @Transactional(readOnly = true)
    public SectionResponse.Retrieved getSection(Long planId, SectionName sectionName) {
        BusinessPlan plan = businessPlanQuery.getOrThrow(planId);

        return sectionRouter.get(plan, sectionName);
    }


    public SectionResponse.Created createOrUpdateSection(Long planId, SectionRequest req) {
        BusinessPlan plan = businessPlanQuery.getOrThrow(planId);

        JsonNode raw = objectMapper.valueToTree(req);

        try {
            return sectionRouter.update(plan, raw, req);
        } catch (BusinessPlanException e) {
            if (e.getErrorType() == BusinessPlanErrorType.SECTIONAL_CONTENT_NOT_FOUND) {
                return sectionRouter.create(plan, raw, req);
            }
            throw e;
        }
    }

    public SectionResponse.Deleted deleteSection(Long planId, SectionName sectionName) {
        BusinessPlan plan = businessPlanQuery.getOrThrow(planId);

        return sectionRouter.delete(plan, sectionName);
    }

    @Transactional(readOnly = true)
    public List<Boolean> checkSection(SectionRequest request){

        return sectionRouter.check(request);
    }
}
