package starlight.application.businessplan.provided.dto;

import com.fasterxml.jackson.databind.JsonNode;
import starlight.domain.businessplan.entity.SubSection;
import starlight.domain.businessplan.enumerate.SubSectionType;

public record SubSectionResult() {

    public record Result(
            SubSectionType subSectionType,
            Long subSectionId,
            String message
    ) {
        public static Result from(
                SubSection subSection,
                String message
        ) {
            return new Result(
                    subSection.getSubSectionType(),
                    subSection.getId(),
                    message
            );
        }
    }

    public record Detail(
            SubSectionType subSectionType,
            Long subSectionId,
            JsonNode content
    ) {
        public static Detail from(SubSection subSection) {
            return new Detail(
                    subSection.getSubSectionType(),
                    subSection.getId(),
                    subSection.getRawJson().asTree()
            );
        }
    }
}