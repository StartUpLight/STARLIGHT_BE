package starlight.domain.businessplan.strategy;

import com.fasterxml.jackson.databind.JsonNode;
import starlight.adapter.bussinessplan.webapi.dto.SectionRequest;
import starlight.domain.businessplan.entity.BusinessPlan;
import starlight.domain.businessplan.enumerate.SectionName;
import starlight.domain.businessplan.strategy.dto.SectionResponse;

public interface SectionStrategy {
    SectionName key();

    default SectionResponse.Created create(BusinessPlan plan, JsonNode rawJson, SectionRequest req) {
        throw new UnsupportedOperationException(key() + " create not supported");
    }

    default SectionResponse.Retrieved read(BusinessPlan plan) {
        throw new UnsupportedOperationException(key() + " read not supported");
    }

    default SectionResponse.Updated update(BusinessPlan plan, JsonNode rawJson, SectionRequest req) {
        throw new UnsupportedOperationException(key() + " update not supported");
    }

    default SectionResponse.Deleted delete(BusinessPlan plan) {
        throw new UnsupportedOperationException(key() + " delete not supported");
    }
}
