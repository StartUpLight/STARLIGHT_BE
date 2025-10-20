package starlight.domain.businessplan.strategy;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.List;

public final class SectionSupportUtils {
    private SectionSupportUtils() {}

    public static String toJson(ObjectMapper om, JsonNode node) {
        if (node == null) throw new IllegalArgumentException("rawJson must not be null");
        try {
            return om.writeValueAsString(node);
        } catch (JsonProcessingException e) {
            throw new IllegalArgumentException("Failed to serialize rawJson", e);
        }
    }

    public static void requireSize5(List<Boolean> checks) {
        if (checks == null || checks.size() != 5) {
            throw new IllegalArgumentException("checks 리스트는 길이 5 여야 합니다.");
        }
    }
}
