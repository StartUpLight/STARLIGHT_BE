package starlight.application.businessplan.strategy.util;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import starlight.domain.businessplan.exception.BusinessPlanErrorType;
import starlight.domain.businessplan.exception.BusinessPlanException;

import java.util.List;

public final class SectionSupportUtils {

    private SectionSupportUtils() {}

    public static String toJson(ObjectMapper objectMapper, JsonNode node) {
        if (node == null) {
            throw new BusinessPlanException(BusinessPlanErrorType.REQUEST_EMPTY_RAW_JSON);
        }
        try {
            return objectMapper.writeValueAsString(node);
        } catch (JsonProcessingException e) {
            throw new BusinessPlanException(BusinessPlanErrorType.RAW_JSON_SERIALIZATION_FAILURE);
        }
    }

    public static void requireSize5(List<Boolean> checks) {
        if (checks == null || checks.size() != 5) {
            throw new BusinessPlanException(BusinessPlanErrorType.CHECKS_LIST_SIZE_INVALID);
        }
    }
}
