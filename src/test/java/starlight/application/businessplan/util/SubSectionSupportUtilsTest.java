package starlight.application.businessplan.util;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import starlight.domain.businessplan.exception.BusinessPlanException;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

class SubSectionSupportUtilsTest {

    private final ObjectMapper mapper = new ObjectMapper();

    @Test
    @DisplayName("JsonNode 직렬화 성공")
    void serializeJsonNodeSafely_success() {
        JsonNode node = mapper.createObjectNode().put("a", 1);
        String json = SubSectionSupportUtils.serializeJsonNodeSafely(mapper, node);
        assertThat(json).isEqualTo("{\"a\":1}");
    }

    @Test
    @DisplayName("JsonNode null이면 예외")
    void serializeJsonNodeSafely_null_throws() {
        assertThrows(BusinessPlanException.class, () -> SubSectionSupportUtils.serializeJsonNodeSafely(mapper, null));
    }

    @Test
    @DisplayName("체크리스트 크기 검증 성공")
    void requireSize_ok() {
        SubSectionSupportUtils.requireSize(List.of(true, false, true, false, true), 5);
    }

    @Test
    @DisplayName("체크리스트 크기 불일치 예외")
    void requireSize_invalid_throws() {
        assertThrows(BusinessPlanException.class, () -> SubSectionSupportUtils.requireSize(List.of(true, false), 5));
    }
}

