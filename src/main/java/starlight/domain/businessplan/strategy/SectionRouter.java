package starlight.domain.businessplan.strategy;

import com.fasterxml.jackson.databind.JsonNode;
import org.springframework.stereotype.Component;
import starlight.adapter.bussinessplan.webapi.dto.SectionRequest;
import starlight.domain.businessplan.entity.BusinessPlan;
import starlight.domain.businessplan.enumerate.SectionName;
import starlight.domain.businessplan.strategy.dto.SectionResponse;

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
                throw new IllegalStateException("Duplicate SectionStrategy for " + strategy.key());
            }
        }
    }

    private SectionStrategy pick(SectionName sectionName) {
        SectionStrategy strategy = strategies.get(sectionName);
        if (strategy == null) {
            throw new UnsupportedOperationException("No strategy for " + sectionName);
        }
        return strategy;
    }

    public SectionResponse.Created routeAndCreate(BusinessPlan plan, JsonNode jsonRaw, SectionRequest request) {
        return pick(request.sectionName()).create(plan, jsonRaw, request);
    }

    public SectionResponse.Retrieved routeAndGet(BusinessPlan plan, SectionName sectionName) {
        return pick(sectionName).read(plan);
    }

    public SectionResponse.Updated routeAndUpdate(BusinessPlan plan, JsonNode jsonRaw, SectionRequest request) {
        return pick(request.sectionName()).update(plan, jsonRaw, request);
    }

    public SectionResponse.Deleted routeAndDelete(BusinessPlan plan, SectionName sectionName) {
        return pick(sectionName).delete(plan);
    }
}
