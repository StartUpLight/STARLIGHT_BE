package starlight.application.businessplan.util;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import starlight.domain.businessplan.exception.BusinessPlanErrorType;
import starlight.domain.businessplan.exception.BusinessPlanException;

import java.util.List;

public final class SubSectionSupportUtils {

    private SubSectionSupportUtils() {}

    /**
     * JsonNode를 문자열로 변환하며 안정성 검사
     *
     * @param objectMapper JSON 처리를 위한 ObjectMapper
     * @param node 변환할 JsonNode
     * @return JSON 문자열
     * @throws BusinessPlanException 변환에 실패하거나 node가 null인 경우
     */
    public static String serializeJsonNodeSafely(ObjectMapper objectMapper, JsonNode node) {
        if (node == null) {
            throw new BusinessPlanException(BusinessPlanErrorType.REQUEST_EMPTY_RAW_JSON);
        }
        try {
            return objectMapper.writeValueAsString(node);
        } catch (JsonProcessingException e) {
            throw new BusinessPlanException(BusinessPlanErrorType.RAW_JSON_SERIALIZATION_FAILURE);
        }
    }

    /**
     +     * 리스트가 지정된 크기를 가지는지 검증합니다.
     +     *
     +     * @param checks 검증할 Boolean 리스트
     +     * @param expectedSize 기대되는 리스트 크기
     +     * @throws BusinessPlanException 리스트가 null이거나 크기가 일치하지 않는 경우
     +     */
    public static void requireSize(List<Boolean> checks, int expectedSize) {
        if (checks == null || checks.size() != expectedSize) {
            throw new BusinessPlanException(BusinessPlanErrorType.CHECKS_LIST_SIZE_INVALID);
        }
    }
}
