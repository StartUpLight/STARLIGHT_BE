package starlight.application.businessplan.dto;

import com.fasterxml.jackson.databind.JsonNode;
import starlight.domain.businessplan.enumerate.SubSectionType;
import java.util.List;

public record SubSectionResponse() {

    public record Created(
            SubSectionType subSectionType,
            Long subSectionId,
            String message) {
        public static SubSectionResponse.Created create(
                SubSectionType subSectionType, Long subSectionId, String message
        ) {
            return new SubSectionResponse.Created(subSectionType, subSectionId, message);
        }
    }

    public record Retrieved(
            String message,
            JsonNode content,
            List<Boolean> checks
    ) {
        public static SubSectionResponse.Retrieved create(String message, JsonNode content, List<Boolean> checks) {
            return new SubSectionResponse.Retrieved(message, content, checks);
        }
    }

    public record Deleted(
            SubSectionType subSectionType,
            Long subSectionId,
            String message
    ) {
        public static SubSectionResponse.Deleted create(
                SubSectionType subSectionType, Long subSectionId, String message) {
            return new SubSectionResponse.Deleted(subSectionType, subSectionId, message);
        }
    }
}