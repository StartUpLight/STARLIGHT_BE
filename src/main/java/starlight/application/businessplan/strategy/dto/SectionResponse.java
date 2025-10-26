package starlight.application.businessplan.strategy.dto;

import com.fasterxml.jackson.databind.JsonNode;
import starlight.domain.businessplan.enumerate.SectionName;

public record SectionResponse() {

    public record Created(
            SectionName section,
            Long sectionId,
            String message
    ) {
        public static Created create(SectionName section, Long sectionId, String message) {
            return new Created(section, sectionId, message);
        }
    }

    public record Retrieved(
            String message,
            JsonNode content
    ) {
        public static Retrieved create(String message, JsonNode content) {
            return new Retrieved(message, content);
        }
    }

    public record Updated(
            SectionName section,
            Long sectionId,
            String message
    ) {
        public static Updated create(SectionName section, Long sectionId, String message) {
            return new Updated(section, sectionId, message);
        }
    }

    public record Deleted(
            SectionName section,
            Long sectionId,
            String message
    ) {
        public static Deleted create(SectionName section, Long sectionId, String message) {
            return new Deleted(section, sectionId, message);
        }
    }
}