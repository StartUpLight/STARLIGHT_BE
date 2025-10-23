package starlight.domain.businessplan.value;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import starlight.shared.apiPayload.exception.GlobalErrorType;
import starlight.shared.apiPayload.exception.GlobalException;

@Slf4j
@Getter
@Embeddable
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class RawJson {

    @Column(columnDefinition = "TEXT", nullable = false)
    private String value;

    // 스레드 세이프 : 정적 재사용
    private static final ObjectMapper MAPPER = new ObjectMapper()
            .configure(SerializationFeature.ORDER_MAP_ENTRIES_BY_KEYS, true);

    public static RawJson create(String raw) {
        if (raw == null) {
            throw new GlobalException(GlobalErrorType.JSON_PROCESSING_ERROR);
        }
        RawJson rawJson = new RawJson();
        rawJson.value = canonicalize(raw);
        return rawJson;
    }

    public static RawJson create(JsonNode node) {
        if (node == null) {
            throw new GlobalException(GlobalErrorType.JSON_PROCESSING_ERROR);
        }
        try {
            RawJson rawJson = new RawJson();
            rawJson.value = MAPPER.writeValueAsString(node); // 이미 정규화된 문자열
            return rawJson;
        } catch (Exception e) {
            log.error("failed to serialize JsonNode", e);
            throw new GlobalException(GlobalErrorType.JSON_PROCESSING_ERROR);
        }
    }

    private static String canonicalize(String raw) {
        try {
            JsonNode node = MAPPER.readTree(raw);
            return MAPPER.writeValueAsString(node); // 공백/순서 정규화
        } catch (Exception e) {
            log.error("failed to canonicalize raw_json", e);
            throw new GlobalException(GlobalErrorType.JSON_PROCESSING_ERROR);
        }
    }

    public JsonNode asTree() {
        try {
            return MAPPER.readTree(value);
        } catch (Exception e) {
            log.error("failed to toTree json_raw", e);
            throw new GlobalException(GlobalErrorType.JSON_PROCESSING_ERROR);
        }
    }
}


