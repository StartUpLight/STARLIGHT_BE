package starlight.application.businessplan.strategy;

import com.fasterxml.jackson.databind.JsonNode;
import starlight.application.businessplan.strategy.dto.SectionRequest;
import starlight.domain.businessplan.entity.BusinessPlan;
import starlight.domain.businessplan.enumerate.SectionName;
import starlight.application.businessplan.strategy.dto.SectionResponse;
import starlight.domain.businessplan.exception.BusinessPlanErrorType;
import starlight.domain.businessplan.exception.BusinessPlanException;

import java.util.List;

public interface SectionStrategy {

    SectionName key();

    default SectionResponse.Created create(BusinessPlan plan, JsonNode rawJson, SectionRequest req) {
        throw new BusinessPlanException(BusinessPlanErrorType.UNSUPPORTED_OPERATION);
    }

    default SectionResponse.Retrieved read(BusinessPlan plan) {
        throw new BusinessPlanException(BusinessPlanErrorType.UNSUPPORTED_OPERATION);
    }

    default SectionResponse.Created update(BusinessPlan plan, JsonNode rawJson, SectionRequest req) {
        throw new BusinessPlanException(BusinessPlanErrorType.UNSUPPORTED_OPERATION);
    }

    default SectionResponse.Deleted delete(BusinessPlan plan) {
        throw new BusinessPlanException(BusinessPlanErrorType.UNSUPPORTED_OPERATION);
    }

    default List<Boolean> check(SectionRequest request) {
        throw new BusinessPlanException(BusinessPlanErrorType.UNSUPPORTED_OPERATION);
    }
}
