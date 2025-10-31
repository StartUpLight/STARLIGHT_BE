package starlight.application.businessplan.dto;

import com.fasterxml.jackson.databind.JsonNode;
import starlight.domain.businessplan.enumerate.SubSectionName;
import java.util.List;

public record SubSectionResponse() {

    public record Created(
            SubSectionName subSectionName,
            Long subSectionId,
            String message) {
        public static SubSectionResponse.Created create(
                SubSectionName subSectionName, Long subSectionId, String message
        ) {
            return new SubSectionResponse.Created(subSectionName, subSectionId, message);
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
            SubSectionName subSectionName,
            Long subSectionId,
            String message
    ) {
        public static SubSectionResponse.Deleted create(
                SubSectionName subSectionName, Long subSectionId, String message) {
            return new SubSectionResponse.Deleted(subSectionName, subSectionId, message);
        }
    }
}