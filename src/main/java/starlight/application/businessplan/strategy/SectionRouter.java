package starlight.application.businessplan.strategy;

import com.fasterxml.jackson.databind.JsonNode;
import org.springframework.stereotype.Component;
import starlight.application.businessplan.strategy.dto.SectionRequest;
import starlight.domain.businessplan.entity.BusinessPlan;
import starlight.domain.businessplan.enumerate.SectionName;
import starlight.application.businessplan.strategy.dto.SectionResponse;
import starlight.domain.businessplan.exception.BusinessPlanErrorType;
import starlight.domain.businessplan.exception.BusinessPlanException;

import java.util.EnumMap;
import java.util.List;
import java.util.Map;

@Component
public class SectionRouter {

    private final Map<SectionName, SectionStrategy> strategies = new EnumMap<>(SectionName.class);

    public SectionRouter(List<SectionStrategy> sectionStrategies) {
        for (SectionStrategy strategy : sectionStrategies) {
            SectionStrategy duplicate = strategies.put(strategy.key(), strategy);
            if (duplicate != null) {
                throw new BusinessPlanException(BusinessPlanErrorType.DUPLICATE_SECTION_STRATEGY);
            }
        }
    }

    private SectionStrategy pick(SectionName sectionName) {
        SectionStrategy strategy = strategies.get(sectionName);
        if (strategy == null) {
            throw new BusinessPlanException(BusinessPlanErrorType.UNSUPPORTED_SECTION_STRATEGY);
        }
        return strategy;
    }

    public SectionResponse.Created create(BusinessPlan plan, JsonNode jsonRaw, SectionRequest request) {
        return pick(request.sectionName()).create(plan, jsonRaw, request);
    }

    public SectionResponse.Retrieved get(BusinessPlan plan, SectionName sectionName) {
        return pick(sectionName).read(plan);
    }

    public SectionResponse.Created update(BusinessPlan plan, JsonNode jsonRaw, SectionRequest request) {
        return pick(request.sectionName()).update(plan, jsonRaw, request);
    }

    public SectionResponse.Deleted delete(BusinessPlan plan, SectionName sectionName) {
        return pick(sectionName).delete(plan);
    }

    public List<Boolean> check(SectionRequest request) {
        return pick(request.sectionName()).check(request);
    }
}
