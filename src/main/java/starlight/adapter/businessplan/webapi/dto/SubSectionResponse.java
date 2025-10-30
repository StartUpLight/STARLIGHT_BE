package starlight.adapter.businessplan.webapi.dto;

import com.fasterxml.jackson.databind.JsonNode;
import starlight.domain.businessplan.enumerate.SubSectionName;

public record SubSectionResponse() {

    public record Created(
            SubSectionName subSectionName,
            Long subSectionId,
            String message) {
        public static SubSectionResponse.Created create(SubSectionName subSectionName, Long subSectionId, String message) {
            return new SubSectionResponse.Created(subSectionName, subSectionId, message);
        }
    }

    public record Retrieved(
            String message,
            JsonNode content) {
        public static SubSectionResponse.Retrieved create(String message, JsonNode content) {
            return new SubSectionResponse.Retrieved(message, content);
        }
    }

    public record Deleted(
            SubSectionName subSectionName,
            Long subSectionId,
            String message) {
        public static SubSectionResponse.Deleted create(SubSectionName subSectionName, Long subSectionId, String message) {
            return new SubSectionResponse.Deleted(subSectionName, subSectionId, message);
        }
    }
}